#include "simulationpanel.h"

#include "actions.h"
#include "beadpainter.h"
#include "domain/beadsymbols.h"
#include "domain/model.h"
#include "domain/rectiterator.h"
#include "domain/selection.h"
#include "mainwindow.h"

#include <QFontMetricsF>
#include <QMouseEvent>
#include <QPainter>

namespace jbead {

SimulationPanel::SimulationPanel(Model* model, Selection* selection,
                                 MainWindow* window, QWidget* parent)
    : BasePanel(model, selection, window, parent)
{
    setFocusPolicy(Qt::ClickFocus);
}

int SimulationPanel::visibleWidth() const { return m_model->width() / 2; }

QSize SimulationPanel::sizeHint() const
{
    return QSize((visibleWidth() + 1) * m_gridx, qMax(80, 3 * m_gridy));
}
QSize SimulationPanel::minimumSizeHint() const { return sizeHint(); }

int SimulationPanel::panelOffsetX() const
{
    return (width() - 1 - (m_model->width() + 1) * m_gridx / 2 + m_gridx / 2) / 2;
}
int SimulationPanel::panelLeft() const
{
    const int o = panelOffsetX();
    return o < 0 ? m_gridx / 2 : o;
}

int SimulationPanel::xFor(BeadPoint pt) const { return panelLeft() + pt.x() * m_gridx; }
int SimulationPanel::yFor(BeadPoint pt) const { return height() - 1 - (pt.y() + 1) * m_gridy; }
int SimulationPanel::offsetXFor(BeadPoint pt) const
{
    return ((pt.y() + m_scroll) % 2 == 0) ? 0 : m_gridx / 2;
}

void SimulationPanel::paintBead(QPainter& p, int x, int y, int w, int h,
                                const QColor& c, std::int8_t colorIndex) const
{
    /*  Round-bead rendering, mirroring the textile editor's
        simulation: filled ellipse with a dark stroke. The ellipse
        fills the whole grid cell so the seam-edge half-cells (w =
        gridx/2) look like proper bead halves, not broken
        rectangles. In colour-blind mode (Draw Colors off) we drop
        the fill entirely so the user sees only outlines + symbols. */
    p.setRenderHint(QPainter::Antialiasing, true);
    const bool drawColors = m_window->drawColors();
    const QColor outline   = palette().color(QPalette::WindowText);
    p.setBrush(drawColors ? QBrush(c) : QBrush(Qt::NoBrush));
    p.setPen(QPen(outline, drawColors ? 0.6 : 1.0));
    p.drawEllipse(QRectF(x, y, w, h));

    /*  Symbol overlay when "Draw Symbols" is on. Half-cells (w =
        gridx/2) at the seam are skipped — there isn't enough room
        for a glyph and squashing one in would just look bad.       */
    if (m_window->drawSymbols() && w >= m_gridx) {
        const QString sym = BeadSymbols::glyph(colorIndex);
        if (sym.trimmed().isEmpty()) return;
        QFont f = p.font();
        f.setPixelSize(qMax(6, int(h * 0.6)));
        p.setFont(f);
        /*  Without a fill there's no contrast pivot — just track the
            window text colour so we get black-on-light / white-on-dark
            automatically.                                           */
        p.setPen(drawColors ? BeadPainter::contrastingColor(c) : outline);
        p.drawText(QRectF(x, y, w, h), Qt::AlignCenter, sym);
    }
}

void SimulationPanel::paintEvent(QPaintEvent*)
{
    QPainter p(this);

    if (m_scroll > m_model->height() - 1) return;
    const int W = m_model->width();
    const int w = visibleWidth();
    RectIterator it(m_model->rect(m_scroll, m_model->height() - 1));
    while (it.hasNext()) {
        const BeadPoint raw = it.next();
        const std::int8_t c = m_model->get(raw);
        const BeadPoint pt = m_model->correct(
            raw.unscrolled(m_scroll).shifted(m_model->shift(), W));
        const int yPx = height() - 1 - (pt.y() + 1) * m_gridy;
        if (yPx < -m_gridy) return;
        if (pt.x() > w && pt.x() != W) continue;

        const int xLeft = panelLeft() + pt.x() * m_gridx;
        const QColor col = m_model->color(c);

        /*  Four-way dispatch: scroll parity x row parity. Mirrors
            legacy SimulationPanel.paintBeads byte-for-byte so the
            half-cell wrap at the seam draws identically.          */
        if (m_scroll % 2 == 0) {
            if (pt.y() % 2 == 0) {
                if (pt.x() == w) continue;
                paintBead(p, xLeft, yPx, m_gridx, m_gridy, col, c);
            } else {
                if (pt.x() != W && pt.x() != w) {
                    paintBead(p, xLeft - m_gridx / 2, yPx, m_gridx, m_gridy, col, c);
                } else if (pt.x() == W) {
                    paintBead(p, panelLeft() + 0 - m_gridx / 2,
                              height() - 1 - (pt.y() + 2) * m_gridy,
                              m_gridx / 2, m_gridy, col, c);
                } else {
                    paintBead(p, xLeft - m_gridx / 2, yPx, m_gridx / 2, m_gridy, col, c);
                }
            }
        } else {
            if (pt.y() % 2 == 1) {
                if (pt.x() == w) continue;
                paintBead(p, xLeft, yPx, m_gridx, m_gridy, col, c);
            } else {
                if (pt.x() != W && pt.x() != w) {
                    paintBead(p, xLeft - m_gridx / 2, yPx, m_gridx, m_gridy, col, c);
                } else if (pt.x() == W) {
                    paintBead(p, panelLeft() + 0 - m_gridx / 2,
                              height() - 1 - (pt.y() + 2) * m_gridy,
                              m_gridx / 2, m_gridy, col, c);
                } else {
                    paintBead(p, xLeft - m_gridx / 2, yPx, m_gridx / 2, m_gridy, col, c);
                }
            }
        }
    }
}

bool SimulationPanel::mouseToField(QPoint pixel, BeadPoint* out) const
{
    /*  Hit-test must use the same x origin as the painter — that's
        panelLeft() (clamped >=gridx/2), not the unclamped
        panelOffsetX(). The legacy code uses the unclamped value
        which produces an off-by-one when the panel is narrow
        enough that the offset goes negative; we deliberately fix
        that here.                                                 */
    const int W  = m_model->width();
    const int w  = visibleWidth();
    const int ox = panelLeft();
    const int j  = (height() - pixel.y()) / m_gridy;
    if (j < 0) return false;
    if (pixel.x() < ox - m_gridx / 2 || pixel.x() > ox + w * m_gridx) return false;
    const int dx = ((j + m_scroll) % 2 == 0) ? 0 : m_gridx / 2;
    int i = (pixel.x() - ox + dx) / m_gridx - m_model->shift();
    int jj = j;
    if (i >= W)      { i -= W; ++jj; }
    else if (i < 0)  { i += W; --jj; }
    if (jj < 0) return false;
    *out = BeadPoint(i, jj);
    return true;
}

void SimulationPanel::mouseReleaseEvent(QMouseEvent* e)
{
    if (e->button() != Qt::LeftButton) return;
    BeadPoint pt;
    if (!mouseToField(e->pos(), &pt)) return;
    const int idx = m_model->correctedIndex(pt);
    const BeadPoint fieldPt = m_model->pointAt(idx).unscrolled(m_scroll);
    m_selection->clear();
    if (m_window->actions()->currentTool() == Actions::Id::ToolFill) {
        m_model->fillLine(fieldPt);
    } else {
        m_model->setPoint(fieldPt);
    }
}

} // namespace jbead
