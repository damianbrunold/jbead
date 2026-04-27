#include "draftpanel.h"

#include "domain/model.h"
#include "domain/segmentiterator.h"
#include "domain/selection.h"
#include "mainwindow.h"

#include <QFontMetrics>
#include <QMouseEvent>
#include <QPainter>

#include <algorithm>

namespace jbead {

DraftPanel::DraftPanel(Model* model, Selection* selection, MainWindow* window, QWidget* parent)
    : BasePanel(model, selection, window, parent)
{
    setMouseTracking(true);
    setFocusPolicy(Qt::StrongFocus);
    connect(m_selection, &Selection::selectionUpdated,
            this, [this]() { update(); });
    connect(m_selection, &Selection::selectionDeleted,
            this, [this]() { update(); });
}

QSize DraftPanel::sizeHint() const
{
    /*  Left-edge layout: 3 px padding + MARKER_WIDTH (row labels) +
        GAP, then W cells of m_gridx each, then a 1-px right border.
        sizeHint must cover the whole strip, otherwise the splitter
        will allocate just enough for the cells minus the marker
        padding and the rightmost column gets clipped to a sliver. */
    const int leftStrip = 3 + MARKER_WIDTH + GAP;
    return QSize(leftStrip + m_model->width() * m_gridx + 1,
                 qMax(80, 3 * m_gridy));
}

QSize DraftPanel::minimumSizeHint() const { return sizeHint(); }

int DraftPanel::offsetX() const
{
    return std::max(3 + MARKER_WIDTH + GAP,
                    (width() - m_model->width() * m_gridx - 1) / 2);
}

int DraftPanel::maxJ() const
{
    return std::min(m_model->height() - m_scroll, height() / m_gridy + 1);
}

int DraftPanel::xFor(BeadPoint pt) const { return offsetX() + pt.x() * m_gridx; }
int DraftPanel::yFor(BeadPoint pt) const { return height() - 1 - (pt.y() + 1) * m_gridy; }

void DraftPanel::paintEvent(QPaintEvent*)
{
    QPainter p(this);
    p.setRenderHint(QPainter::TextAntialiasing, true);
    paintBeads(p);
    paintMarkers(p);
    paintSelection(p);
}

void DraftPanel::paintBeads(QPainter& p)
{
    BeadPainter bp(*this, *m_model,
                   m_window->drawColors(), m_window->drawSymbols(),
                   symbolFont());
    const int rows = maxJ();
    for (int j = 0; j < rows; ++j) {
        for (int i = 0; i < m_model->width(); ++i) {
            const std::int8_t c = m_model->get(BeadPoint(i, j).scrolled(m_scroll));
            bp.paint(p, BeadPoint(i, j), c);
        }
    }
}

void DraftPanel::paintMarkers(QPainter& p)
{
    /*  Row-number markers along the left edge: a tick + numeric
        label every 10 rows. Pen colour comes from the active
        palette so it stays legible under dark mode. The label for
        row 0 sits at the very bottom of the panel where the legacy
        layout placed it BELOW the canvas (off-screen) — shift it
        up by one row's worth so it actually shows.                */
    p.setPen(palette().color(QPalette::WindowText));
    const QFontMetrics fm(font());
    const int ox = offsetX();
    const int rows = maxJ();
    for (int j = 0; j < rows; ++j) {
        if ((j + m_scroll) % 10 != 0) continue;
        const int tickY = yFor(BeadPoint(0, j)) + m_gridy;
        p.drawLine(ox - GAP - MARKER_WIDTH, tickY, ox - GAP - 1, tickY);
        const QString label = QString::number(j + m_scroll);
        const int lw = fm.horizontalAdvance(label);
        const int textY = qMin(tickY + fm.ascent() + 1,
                               height() - fm.descent() - 1);
        p.drawText(ox - GAP - MARKER_WIDTH + (MARKER_WIDTH - lw) / 2,
                   textY, label);
    }
}

void DraftPanel::paintSelection(QPainter& p)
{
    /*  Phase-3 simplification vs. legacy: legacy uses XOR-mode
        Graphics.getGraphics() to draw selection rectangles outside
        of paintComponent(). Qt 6 doesn't support QPainter XOR cleanly,
        so we do everything in paintEvent — selection updates trigger
        update() and the rectangle is repainted from scratch.
        Slightly less responsive but visually identical.            */
    if (!m_selection->isActive()) return;

    const BeadRect r = m_selection->rect();
    const int x = xFor(r.begin().unscrolled(m_scroll));
    const int y = yFor(r.end().unscrolled(m_scroll));
    const int w = r.width()  * m_gridx;
    const int h = r.height() * m_gridy;
    p.setPen(QPen(Qt::red, 1));
    p.setBrush(Qt::NoBrush);
    p.drawRect(x, y, w, h);

    /*  Pencil-tool line preview: draw the Bresenham endpoint
        candidate so the user sees what will be committed. With
        Shift held we render the snapped (lineDestination) endpoint
        — straight-line drawing from the textile editor.           */
    if (m_window->actions()->currentTool() == Actions::Id::ToolPencil) {
        const BeadPoint a = m_selection->origin();
        const BeadPoint b = m_shiftHeld ? m_selection->lineDestination()
                                        : m_selection->destination();
        const int x0 = xFor(a.unscrolled(m_scroll)) + m_gridx / 2;
        const int y0 = yFor(a.unscrolled(m_scroll)) + m_gridy / 2;
        const int x1 = xFor(b.unscrolled(m_scroll)) + m_gridx / 2;
        const int y1 = yFor(b.unscrolled(m_scroll)) + m_gridy / 2;
        p.setPen(QPen(QColor(255, 255, 255, 200), 1));
        p.drawLine(x0, y0, x1, y1);
    }
}

bool DraftPanel::mouseToField(QPoint pixel, BeadPoint* out) const
{
    const int ox = offsetX();
    if (pixel.x() < ox || pixel.x() > ox + m_model->width() * m_gridx) return false;
    const int i = (pixel.x() - ox) / m_gridx;
    if (i < 0 || i >= m_model->width()) return false;
    const int j = (height() - pixel.y()) / m_gridy;
    if (j < 0) return false;
    *out = BeadPoint(i, j);
    return true;
}

void DraftPanel::mousePressEvent(QMouseEvent* e)
{
    if (e->button() != Qt::LeftButton) return;
    BeadPoint pt;
    if (!mouseToField(e->pos(), &pt)) return;
    m_dragging  = true;
    m_shiftHeld = e->modifiers().testFlag(Qt::ShiftModifier);
    m_dragOrigin = pt;
    m_dragLast   = pt;
    m_selection->init(pt);
    update();
}

void DraftPanel::mouseMoveEvent(QMouseEvent* e)
{
    if (!m_dragging) return;
    BeadPoint pt;
    if (!mouseToField(e->pos(), &pt)) return;
    m_shiftHeld = e->modifiers().testFlag(Qt::ShiftModifier);
    m_selection->update(pt);
    m_dragLast = pt;
    update();
}

void DraftPanel::mouseReleaseEvent(QMouseEvent* e)
{
    if (!m_dragging) return;
    m_dragging = false;
    BeadPoint pt;
    if (!mouseToField(e->pos(), &pt)) {
        update();
        return;
    }
    m_shiftHeld = e->modifiers().testFlag(Qt::ShiftModifier);
    m_selection->update(pt);

    const Actions::Id tool = m_window->actions()->currentTool();
    switch (tool) {
        case Actions::Id::ToolPencil:
            if (!m_selection->isActive()) {
                m_model->setPoint(m_selection->origin());
            } else {
                const BeadPoint dest = m_shiftHeld
                    ? m_selection->lineDestination()
                    : m_selection->destination();
                m_model->drawLine(m_selection->origin(), dest);
            }
            break;
        case Actions::Id::ToolFill:
            m_model->fillLine(m_selection->origin());
            break;
        case Actions::Id::ToolPipette: {
            const std::int8_t c = m_model->get(m_selection->origin().scrolled(m_scroll));
            m_window->selectColor(c);
            break;
        }
        case Actions::Id::ToolSelect:
            if (!m_selection->isActive()) m_model->setPoint(m_selection->origin());
            break;
        default: break;
    }
    update();
}

} // namespace jbead
