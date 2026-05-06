#include "reportpanel.h"

#include "beadpainter.h"
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
    /*  When "Draw Symbols" is on, pills carry both the count and the
        glyph (e.g. "5 A"), so we need wider beads. The label includes
        the per-run symbol so the upper bound is honest about what
        we'll actually paint.                                         */
    const bool withSymbols = m_window->drawSymbols();
    const bool withColors  = m_window->drawColors();
    auto labelFor = [&](const BeadRun& run) {
        const QString count = QString::number(run.count());
        if (!withSymbols) return count;
        const QString sym = BeadSymbols::glyph(run.color());
        return count + QStringLiteral(" ") + sym;
    };
    int maxLabelWidth = 0;
    for (const BeadRun& run : list.runs()) {
        maxLabelWidth = qMax(maxLabelWidth, fm.horizontalAdvance(labelFor(run)));
    }
    const int padding = withSymbols ? 28 : 24;
    const int minW    = withSymbols ? 44 : 36;
    const int swW = qBound(minW, maxLabelWidth + padding, 120);
    const int swH = qMax(22, fm.height() + 8);
    const int gap = 4;

    /*  Reading-direction arrow drawn alongside the first column of
        beads — same idea as the textile editor: a vertical line
        with a downward arrowhead, anchored at the top of the
        column so the user knows the run-length sequence is read
        top-to-bottom. The arrow's height is sized to the first
        three pills so it scales with the bead size.              */
    constexpr int arrowGap = 6;
    const int arrowX = x0;
    const int arrowTop = y;
    const int arrowBottom = qMin(y + 3 * (swH + gap), height() - dy);
    const int arrowHead = qMax(4, swH / 4);
    p.setPen(QPen(borderColor, 1.2));
    p.drawLine(arrowX, arrowTop, arrowX, arrowBottom);
    p.drawLine(arrowX, arrowBottom, arrowX - arrowHead / 2, arrowBottom - arrowHead);
    p.drawLine(arrowX, arrowBottom, arrowX + arrowHead / 2, arrowBottom - arrowHead);

    int x = x0 + arrowGap + 4;
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
            editor's bead list. With Draw Colors on we fill the pill
            and pick a contrasting label colour; with it off we drop
            the fill and use the palette's WindowText so the panel
            stays legible in dark mode without competing chroma.    */
        p.setBrush(withColors ? QBrush(fill) : QBrush(Qt::NoBrush));
        p.setPen(QPen(borderColor, withColors ? 0.8 : 1.2));
        p.drawRoundedRect(QRectF(x + 0.5, y + 0.5, swW - 1, swH - 1),
                          swH / 3.0, swH / 3.0);

        p.setPen(withColors ? BeadPainter::contrastingColor(fill) : textColor);
        p.drawText(QRectF(x, y, swW, swH),
                   Qt::AlignCenter, labelFor(run));

        y += swH + gap;
    }
}

} // namespace jbead
