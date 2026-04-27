#include "reportpanel.h"

#include "domain/beadcounts.h"
#include "domain/beadlist.h"
#include "domain/beadsymbols.h"
#include "domain/model.h"
#include "mainwindow.h"

#include <QFontMetrics>
#include <QPainter>

namespace jbead {

ReportPanel::ReportPanel(Model* model, Selection* selection,
                         MainWindow* window, QWidget* parent)
    : BasePanel(model, selection, window, parent)
{
}

QSize ReportPanel::sizeHint() const        { return QSize(220, 400); }
QSize ReportPanel::minimumSizeHint() const { return QSize(160, 200); }

void ReportPanel::paintEvent(QPaintEvent*)
{
    QPainter p(this);
    p.setRenderHint(QPainter::TextAntialiasing, true);
    p.setRenderHint(QPainter::Antialiasing, true);
    if (m_model->isRepeatDirty()) m_model->updateRepeat();

    /*  Use the active palette throughout — labels track the current
        color scheme so the panel reads correctly under both light
        and dark themes.                                            */
    const QColor textColor   = palette().color(QPalette::WindowText);
    const QColor borderColor = palette().color(QPalette::WindowText);

    const QFontMetrics fm(font());
    const int dy = fm.height() + 2;
    const int x0 = 12;
    int y = dy;

    // ---- header info ---------------------------------------------
    auto line = [&](const QString& label, const QString& value) {
        p.setPen(textColor);
        p.drawText(x0, y, label);
        p.drawText(x0 + 110, y, value);
        y += dy;
    };
    line(tr("Circumference:"), QString::number(m_model->width()));
    line(tr("Rows:"),          QString::number(m_model->usedHeight()));
    line(tr("Repeat:"),        QString::number(m_model->repeat()));

    // ---- bead-list run sequence (textile redesign) --------------
    if (m_model->repeat() <= 0) {
        y += dy;
        p.setPen(textColor);
        p.drawText(x0, y, tr("(No repeat detected.)"));
        return;
    }

    y += dy;
    p.setPen(textColor);
    p.drawText(x0, y, tr("Bead list:"));
    y += dy / 2 + 4;

    const BeadList list(*m_model);

    /*  Auto-size the bead so the longest count fits comfortably
        inside it. The longest count comes from BeadList::runs;
        width is sized to label width + 12 px padding on each side,
        clamped between 36 and 96 px so individual short runs don't
        produce tiny pills and a giant 9999-count pattern doesn't
        push beads off the panel.                                  */
    int maxLabelWidth = 0;
    for (const BeadRun& run : list.runs()) {
        const QString label = QString::number(run.count());
        maxLabelWidth = qMax(maxLabelWidth, fm.horizontalAdvance(label));
    }
    const int swW = qBound(36, maxLabelWidth + 24, 96);
    const int swH = qMax(22, fm.height() + 8);
    const int gap = 4;

    int x = x0;
    const int colTop = y;
    for (const BeadRun& run : list.runs()) {
        if (y + swH > height() - dy) {
            // Wrap into next column.
            x += swW + 8;
            y = colTop;
            if (x + swW > width() - x0) break;
        }
        const QColor fill = m_model->color(run.color());

        /*  Round/oval bead rendering — same look as the textile
            editor's bead list. Filled ellipse with a contrasting
            stroke and the count drawn dead-centre in the
            contrasting colour for legibility against any palette
            colour.                                                 */
        p.setBrush(fill);
        p.setPen(QPen(borderColor, 0.8));
        p.drawRoundedRect(QRectF(x + 0.5, y + 0.5, swW - 1, swH - 1),
                          swH / 3.0, swH / 3.0);

        const QString label = QString::number(run.count());
        p.setPen(BeadPainter::contrastingColor(fill));
        p.drawText(QRectF(x, y, swW, swH),
                   Qt::AlignCenter, label);

        y += swH + gap;
    }
}

} // namespace jbead
