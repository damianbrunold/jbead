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
    if (m_model->isRepeatDirty()) m_model->updateRepeat();

    const QFontMetrics fm(font());
    const int dy = fm.height() + 2;
    const int x0 = 12;
    int y = dy;

    // ---- header info ---------------------------------------------
    auto line = [&](const QString& label, const QString& value) {
        p.setPen(QColor(50, 50, 50));
        p.drawText(x0, y, label);
        p.drawText(x0 + 110, y, value);
        y += dy;
    };
    line(tr("Circumference:"), QString::number(m_model->width()));
    line(tr("Rows:"),          QString::number(m_model->usedHeight()));
    line(tr("Repeat:"),        QString::number(m_model->repeat()));

    /*  Per-color totals across the used region. Skip the
        background colour (index 0) — it doesn't represent beads.  */
    BeadCounts counts(*m_model);
    y += dy / 2;
    p.drawText(x0, y, tr("Beads by color:"));
    y += dy;
    const int swatch = qMax(12, m_gridy);
    for (int c = 1; c < m_model->colorCount(); ++c) {
        const int n = counts.count(static_cast<std::int8_t>(c));
        if (n <= 0) continue;
        if (y > height() - dy) break;
        const QColor col = m_model->color(c);
        p.fillRect(x0, y - swatch + 2, swatch, swatch, col);
        p.setPen(Qt::black);
        p.drawRect(x0, y - swatch + 2, swatch, swatch);
        p.setPen(BeadPainter::contrastingColor(col));
        const QString sym = BeadSymbols::glyph(c);
        p.drawText(x0 + (swatch - fm.horizontalAdvance(sym)) / 2,
                   y - 1, sym);
        p.setPen(Qt::black);
        p.drawText(x0 + swatch + 6, y - 1, QString::number(n) + tr(" ×"));
        y += swatch + 2;
    }

    // ---- bead-list run sequence (textile redesign) --------------
    if (m_model->repeat() <= 0) return;

    y += dy;
    if (y > height() - dy) return;
    p.drawText(x0, y, tr("Bead list:"));
    y += dy / 2;

    const BeadList list(*m_model);
    const int swW = qMax(40, swatch * 3);
    const int swH = qMax(16, swatch);
    int x = x0;
    int colTop = y;
    for (const BeadRun& run : list.runs()) {
        if (y + swH > height() - dy) {
            // Wrap into next column.
            x += swW + 8;
            y = colTop;
            if (x + swW > width() - x0) break;
        }
        const QColor col = m_model->color(run.color());
        p.fillRect(x, y, swW, swH, col);
        p.setPen(Qt::black);
        p.drawRect(x, y, swW, swH);
        p.setPen(BeadPainter::contrastingColor(col));
        const QString label =
            QString::number(run.count()) + QStringLiteral(" × ") + BeadSymbols::glyph(run.color());
        p.drawText(x + 4, y + (swH + fm.ascent()) / 2 - 2, label);
        y += swH + 4;
    }
}

} // namespace jbead
