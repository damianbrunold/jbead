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

namespace {

/*  8-direction snap matching the textile editor's _constrainTo8Dir
    (jbead.js:1220). When the user holds Ctrl during a pencil drag,
    the line is constrained to one of horizontal, vertical, or one
    of the four 45-degree diagonals. The snap extends along the
    *longer* axis so the resulting line covers the full drag
    distance — the previous Selection::lineDestination clamped to
    the shorter axis instead, producing a strangely-truncated line.
    The bias `2 * |minor|` for picking H/V favours the cardinal
    when one axis dominates, which matches user intuition.        */
BeadPoint snapTo8Dir(BeadPoint start, BeadPoint end)
{
    const int dx = end.x() - start.x();
    const int dy = end.y() - start.y();
    const int adx = std::abs(dx), ady = std::abs(dy);
    if (adx == 0 && ady == 0) return start;
    if (adx > ady * 2) return BeadPoint(start.x() + dx, start.y());      // horizontal
    if (ady > adx * 2) return BeadPoint(start.x(),       start.y() + dy); // vertical
    const int sx = (dx > 0) ? 1 : (dx < 0 ? -1 : 0);
    const int sy = (dy > 0) ? 1 : (dy < 0 ? -1 : 0);
    const int len = std::max(adx, ady);
    return BeadPoint(start.x() + sx * len, start.y() + sy * len);        // 45 degrees
}

bool snapModifierActive(Qt::KeyboardModifiers mods)
{
    /*  Match the textile editor's "Ctrl snaps to 8 directions"
        binding. Accept Shift too because (a) the original feedback
        round mentioned Shift as a candidate and (b) it's an
        unsurprising alternative for users who associate Shift with
        constrained drawing in other tools.                       */
    return mods.testFlag(Qt::ControlModifier)
        || mods.testFlag(Qt::ShiftModifier);
}

} // namespace

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
    const Actions::Id tool = m_window->actions()->currentTool();

    /*  Pencil tool: while the user is dragging, render a
        translucent Bresenham-cell overlay between the press cell
        and the current cursor cell (the textile editor's "preview
        cells" — see jbead.js:_drawPencilPreview). The selection
        rectangle is suppressed for this tool because it would
        clutter the line preview.                                  */
    if (tool == Actions::Id::ToolPencil && m_dragging) {
        const BeadPoint a = m_dragOrigin;
        const BeadPoint b = m_snapHeld ? snapTo8Dir(a, m_dragLast) : m_dragLast;
        QColor preview = m_model->color(m_model->selectedColor());
        preview.setAlpha(140);
        p.setPen(QPen(palette().color(QPalette::Highlight), 0.5));
        p.setBrush(preview);
        SegmentIterator it(a, b);
        while (it.hasNext()) {
            const BeadPoint cell = it.next();
            const int x = xFor(cell.unscrolled(m_scroll));
            const int y = yFor(cell.unscrolled(m_scroll));
            p.drawRect(x, y, m_gridx, m_gridy);
        }
        return;
    }

    /*  Other tools: keep the red selection rectangle. */
    if (!m_selection->isActive()) return;
    const BeadRect r = m_selection->rect();
    const int x = xFor(r.begin().unscrolled(m_scroll));
    const int y = yFor(r.end().unscrolled(m_scroll));
    const int w = r.width()  * m_gridx;
    const int h = r.height() * m_gridy;
    p.setPen(QPen(Qt::red, 1));
    p.setBrush(Qt::NoBrush);
    p.drawRect(x, y, w, h);
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
    m_dragging   = true;
    m_snapHeld   = snapModifierActive(e->modifiers());
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
    m_snapHeld = snapModifierActive(e->modifiers());
    m_dragLast = pt;
    /*  Keep selection in sync for the select tool; pencil ignores
        it since paintSelection draws the line preview directly.   */
    m_selection->update(pt);
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
    m_snapHeld = snapModifierActive(e->modifiers());
    m_dragLast = pt;
    m_selection->update(pt);

    const Actions::Id tool = m_window->actions()->currentTool();
    switch (tool) {
        case Actions::Id::ToolPencil: {
            /*  No drag (release on the same cell as press): toggle
                the cell. With drag: commit the (possibly snapped)
                Bresenham line. Mirrors the web editor's
                _onMouseUp pencil branch.                          */
            if (m_dragLast == m_dragOrigin) {
                m_model->setPoint(m_dragOrigin);
            } else {
                const BeadPoint dest = m_snapHeld
                    ? snapTo8Dir(m_dragOrigin, m_dragLast)
                    : m_dragLast;
                m_model->drawLine(m_dragOrigin, dest);
            }
            /*  Pencil never leaves a selection rectangle behind —
                clear it so subsequent edits don't apply to a stale
                rect from before the drag.                         */
            m_selection->clear();
            break;
        }
        case Actions::Id::ToolFill:
            m_model->fillLine(m_dragOrigin);
            break;
        case Actions::Id::ToolPipette: {
            const std::int8_t c = m_model->get(m_dragOrigin.scrolled(m_scroll));
            m_window->selectColor(c);
            break;
        }
        case Actions::Id::ToolSelect:
            if (!m_selection->isActive()) m_model->setPoint(m_dragOrigin);
            break;
        default: break;
    }
    update();
}

} // namespace jbead
