#include "correctedpanel.h"

#include "domain/model.h"
#include "domain/rectiterator.h"
#include "domain/selection.h"
#include "mainwindow.h"

#include <QMouseEvent>
#include <QPainter>

namespace jbead {

CorrectedPanel::CorrectedPanel(Model* model, Selection* selection,
                               MainWindow* window, QWidget* parent)
    : BasePanel(model, selection, window, parent)
{
    setFocusPolicy(Qt::ClickFocus);
}

QSize CorrectedPanel::sizeHint() const
{
    return QSize((m_model->width() + 2) * m_gridx, qMax(80, 3 * m_gridy));
}
QSize CorrectedPanel::minimumSizeHint() const { return sizeHint(); }

int CorrectedPanel::panelOffsetX() const
{
    return m_gridx / 2 + (width() - 1 - m_model->width() * m_gridx - m_gridx / 2) / 2;
}
int CorrectedPanel::panelLeft() const
{
    const int o = panelOffsetX();
    return o < 0 ? m_gridx / 2 : o;
}

int CorrectedPanel::xFor(BeadPoint pt) const { return panelLeft() + pt.x() * m_gridx; }
int CorrectedPanel::yFor(BeadPoint pt) const { return height() - 1 - (pt.y() + 1) * m_gridy; }
int CorrectedPanel::offsetXFor(BeadPoint pt) const
{
    return ((pt.y() + m_scroll) % 2 == 0) ? 0 : m_gridx / 2;
}

void CorrectedPanel::paintEvent(QPaintEvent*)
{
    QPainter p(this);
    p.setRenderHint(QPainter::TextAntialiasing, true);

    if (m_scroll > m_model->height() - 1) return;
    BeadPainter bp(*this, *m_model,
                   m_window->drawColors(), m_window->drawSymbols(),
                   symbolFont());
    RectIterator it(m_model->rect(m_scroll, m_model->height() - 1));
    while (it.hasNext()) {
        const BeadPoint raw = it.next();
        const std::int8_t c = m_model->get(raw);
        const BeadPoint pt = m_model->correct(raw.unscrolled(m_scroll));
        if (yFor(pt) < -m_gridy) break;       // below bottom of viewport
        bp.paint(p, pt, c);
    }
}

bool CorrectedPanel::mouseToField(QPoint pixel, BeadPoint* out) const
{
    const int ox = panelOffsetX();
    const int j = (height() - pixel.y()) / m_gridy;
    if (j < 0) return false;
    const int dx = ((j + m_scroll) % 2 == 0) ? 0 : m_gridx / 2;
    if (pixel.x() < ox - dx || pixel.x() > ox + m_model->width() * m_gridx + dx) return false;
    const int i = (pixel.x() - ox + dx) / m_gridx;
    if (i < 0) return false;
    *out = BeadPoint(i, j);
    return true;
}

void CorrectedPanel::mouseReleaseEvent(QMouseEvent* e)
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
