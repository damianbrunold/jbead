#include "printjob.h"

#include "domain/beadcounts.h"
#include "domain/beadlist.h"
#include "domain/beadsymbols.h"
#include "domain/model.h"
#include "domain/rectiterator.h"

#include <QFileInfo>
#include <QFontMetricsF>
#include <QPageLayout>
#include <QPainter>
#include <QPrinter>

#include <algorithm>
#include <cmath>

namespace jbead {

namespace {

constexpr qreal CELL_MM = 5.0;        // print scale per bead cell
constexpr qreal HEADER_MM = 8.0;       // height reserved for the per-page header
constexpr qreal MARKER_MM = 12.0;      // row-number column on the draft page

QRectF pageRect(const QPrinter* p)
{
    return p->pageLayout().paintRect(QPageLayout::Point);
}

qreal mmToPt(const QPrinter* p, qreal mm)
{
    /*  QPageLayout::paintRect(Point) gives points (1/72 inch).
        Convert mm -> points via the standard 1 mm = 2.83465 pt. */
    Q_UNUSED(p);
    return mm * 72.0 / 25.4;
}

void drawCell(QPainter& p, qreal x, qreal y, qreal cell,
              const QColor& color, const QString& symbol)
{
    /*  Print rendering: fill, contrasting symbol glyph centred
        inside, hairline black border. Mirrors BeadPainter but is
        copied here to keep src/print/ free of UI dependencies.   */
    p.fillRect(QRectF(x, y, cell, cell), color);
    p.setPen(Qt::black);
    p.drawRect(QRectF(x, y, cell, cell));
    if (!symbol.isEmpty()) {
        QFont f = p.font();
        f.setPointSizeF(qMax(4.0, cell * 0.55));
        p.setFont(f);
        const QFontMetricsF fm(f);
        const qreal w = fm.horizontalAdvance(symbol);
        const qreal h = fm.ascent();
        /*  Pick contrasting text colour with the same euclidean-
            distance heuristic the on-screen painter uses.        */
        const auto dist = [&](const QColor& a, const QColor& b) {
            return std::sqrt(std::pow(a.red()-b.red(), 2.0)
                             + std::pow(a.green()-b.green(), 2.0)
                             + std::pow(a.blue()-b.blue(), 2.0));
        };
        p.setPen(dist(color, Qt::white) > dist(color, Qt::black) ? Qt::white : Qt::black);
        p.drawText(QPointF(x + (cell - w) / 2.0, y + (cell + h) / 2.0 - 1), symbol);
    }
}

} // namespace

PrintJob::PrintJob(const Model& model, const PrintSettings& settings)
    : m_model(model), m_settings(settings)
{
}

int PrintJob::cellSize(const QPrinter* printer) const
{
    return int(std::round(mmToPt(printer, CELL_MM)));
}

void PrintJob::layoutPages(QPrinter* printer)
{
    m_pages.clear();
    const QRectF page = pageRect(printer);
    const qreal headerPt = mmToPt(printer, HEADER_MM);
    const qreal bodyHeight = page.height() - headerPt;
    const int   cell = cellSize(printer);
    if (cell <= 0 || bodyHeight <= 0) return;

    const int totalRows = m_settings.fullPattern
        ? m_model.usedHeight()
        : (m_model.repeat() > 0 ? std::max(m_model.repeat() / m_model.width() + 1,
                                            m_model.usedHeight())
                                : m_model.usedHeight());
    const int rowsPerPage = std::max(1, int(bodyHeight / cell));

    auto pushGridSection = [&](Section sec) {
        for (int start = 0; start < totalRows; start += rowsPerPage) {
            const int rows = std::min(rowsPerPage, totalRows - start);
            m_pages.append({sec, start, rows});
        }
        if (totalRows == 0) m_pages.append({sec, 0, 0});
    };

    if (m_settings.printReport)     m_pages.append({Section::ReportInfos, 0, 0});
    if (m_settings.printDraft)      pushGridSection(Section::Draft);
    if (m_settings.printCorrected)  pushGridSection(Section::Corrected);
    if (m_settings.printSimulation) pushGridSection(Section::Simulation);
    if (m_settings.printBeadList)   m_pages.append({Section::BeadList, 0, 0});
}

bool PrintJob::run(QPrinter* printer)
{
    if (m_model.isRepeatDirty()) const_cast<Model&>(m_model).updateRepeat();
    layoutPages(printer);
    if (m_pages.isEmpty()) return false;

    QPainter p(printer);
    if (!p.isActive()) return false;
    for (int i = 0; i < m_pages.size(); ++i) {
        if (i > 0) printer->newPage();
        drawPage(p, printer, i);
    }
    p.end();
    return true;
}

void PrintJob::paint(QPrinter* printer)
{
    if (m_model.isRepeatDirty()) const_cast<Model&>(m_model).updateRepeat();
    layoutPages(printer);
    if (m_pages.isEmpty()) return;

    QPainter p(printer);
    for (int i = 0; i < m_pages.size(); ++i) {
        if (i > 0) printer->newPage();
        drawPage(p, printer, i);
    }
    p.end();
}

void PrintJob::drawPage(QPainter& p, QPrinter* printer, int idx)
{
    const QRectF page = pageRect(printer);
    const qreal headerPt = mmToPt(printer, HEADER_MM);

    p.save();
    p.translate(page.topLeft());
    const QRectF headerRect(0, 0, page.width(), headerPt);
    const QRectF bodyRect(0, headerPt, page.width(), page.height() - headerPt);

    drawHeader(p, headerRect, m_pages[idx], idx);

    p.save();
    p.translate(bodyRect.topLeft());
    const QRectF body(0, 0, bodyRect.width(), bodyRect.height());
    switch (m_pages[idx].section) {
        case Section::ReportInfos: drawReportInfos(p, body); break;
        case Section::BeadList:    drawBeadList(p, body); break;
        case Section::Draft:
            drawDraft(p, body, m_pages[idx].startRow, m_pages[idx].rowCount); break;
        case Section::Corrected:
            drawCorrected(p, body, m_pages[idx].startRow, m_pages[idx].rowCount); break;
        case Section::Simulation:
            drawSimulation(p, body, m_pages[idx].startRow, m_pages[idx].rowCount); break;
    }
    p.restore();
    p.restore();
}

void PrintJob::drawHeader(QPainter& p, const QRectF& body,
                          const PageDescriptor& page, int idx)
{
    QFont f = p.font();
    f.setPointSizeF(10);
    p.setFont(f);
    p.setPen(Qt::black);

    QString sectionName;
    switch (page.section) {
        case Section::ReportInfos: sectionName = QObject::tr("Report");     break;
        case Section::Draft:       sectionName = QObject::tr("Draft");      break;
        case Section::Corrected:   sectionName = QObject::tr("Corrected");  break;
        case Section::Simulation:  sectionName = QObject::tr("Simulation"); break;
        case Section::BeadList:    sectionName = QObject::tr("Bead list");  break;
    }

    QString file = QFileInfo(m_model.filePath()).fileName();
    if (file.isEmpty()) file = QObject::tr("unnamed");

    const QString left   = QStringLiteral("JBead — %1 — %2").arg(file, sectionName);
    const QString right  = QObject::tr("Page %1 of %2").arg(idx + 1).arg(m_pages.size());
    p.drawText(body.adjusted(0, 0, 0, -2), Qt::AlignLeft  | Qt::AlignBottom, left);
    p.drawText(body.adjusted(0, 0, 0, -2), Qt::AlignRight | Qt::AlignBottom, right);
    p.setPen(QPen(Qt::black, 0.5));
    p.drawLine(QPointF(body.left(), body.bottom()), QPointF(body.right(), body.bottom()));
}

// ---- Section painters --------------------------------------------

void PrintJob::drawReportInfos(QPainter& p, const QRectF& body)
{
    QFont f = p.font();
    f.setPointSizeF(11);
    p.setFont(f);
    const QFontMetricsF fm(f);
    qreal y = fm.ascent() + 4;

    auto line = [&](const QString& label, const QString& value) {
        p.drawText(QPointF(0,       y), label);
        p.drawText(QPointF(body.width() * 0.35, y), value);
        y += fm.height() * 1.4;
    };
    line(QObject::tr("File:"),         QFileInfo(m_model.filePath()).fileName());
    line(QObject::tr("Author:"),       m_model.author());
    line(QObject::tr("Organization:"), m_model.organization());
    line(QObject::tr("Circumference:"), QString::number(m_model.width()));
    line(QObject::tr("Rows:"),          QString::number(m_model.usedHeight()));
    line(QObject::tr("Repeat:"),        QString::number(m_model.repeat()));

    y += fm.height();
    p.drawText(QPointF(0, y), QObject::tr("Color palette:"));
    y += fm.height();

    /*  Palette grid: 8 swatches per row, sized 14 mm each, with a
        per-color count. Colour 0 is included so the user can see
        what's being treated as background.                       */
    const qreal sw = 14.0 * 72.0 / 25.4;          // swatch px
    BeadCounts counts(m_model);
    qreal cx = 0, cy = y;
    for (int i = 0; i < m_model.colorCount(); ++i) {
        if (cx + sw > body.width()) { cx = 0; cy += sw + fm.height() + 4; }
        if (cy + sw > body.height()) break;
        const QColor color = m_model.color(i);
        drawCell(p, cx, cy, sw, color, BeadSymbols::glyph(i));
        p.setPen(Qt::black);
        const QString label = QStringLiteral("%1: %2").arg(i).arg(counts.count(i));
        p.drawText(QRectF(cx, cy + sw + 2, sw, fm.height()),
                   Qt::AlignHCenter, label);
        cx += sw + 6;
    }
}

void PrintJob::drawBeadList(QPainter& p, const QRectF& body)
{
    if (m_model.repeat() <= 0) {
        QFont f = p.font(); f.setPointSizeF(12); p.setFont(f);
        p.drawText(body, Qt::AlignCenter, QObject::tr("(No repeat detected.)"));
        return;
    }

    BeadList list(m_model);
    QFont f = p.font(); f.setPointSizeF(10); p.setFont(f);
    const QFontMetricsF fm(f);
    const qreal sw = 12.0 * 72.0 / 25.4;
    const qreal rowH = std::max(sw, fm.height()) + 4;
    const qreal colW = sw + 8 + fm.horizontalAdvance(QStringLiteral("9999 ×")) + 12;

    qreal cx = 0, cy = 0;
    for (const BeadRun& run : list.runs()) {
        if (cy + rowH > body.height()) {
            cx += colW; cy = 0;
            if (cx + colW > body.width()) break;
        }
        drawCell(p, cx, cy, sw, m_model.color(run.color()),
                 BeadSymbols::glyph(run.color()));
        p.setPen(Qt::black);
        p.drawText(QPointF(cx + sw + 6, cy + sw - 2),
                   QStringLiteral("%1 ×").arg(run.count()));
        cy += rowH;
    }
}

void PrintJob::drawDraft(QPainter& p, const QRectF& body, int startRow, int rowCount)
{
    /*  Layout: a marker column on the left (every 10th row gets a
        tick + numeric label) followed by a row-major grid of beads.
        Y axis is inverted so model row 0 (the bottom of the
        pattern) sits at the BOTTOM of the page area.              */
    const qreal cell = std::round(72.0 / 25.4 * CELL_MM);
    const qreal markerW = 72.0 / 25.4 * MARKER_MM;
    const qreal gridLeft = markerW;
    const int W = m_model.width();

    QFont f = p.font(); f.setPointSizeF(7); p.setFont(f);
    const QFontMetricsF fm(f);
    p.setPen(Qt::black);

    for (int j = 0; j < rowCount; ++j) {
        const int row = startRow + j;
        const qreal y = body.height() - (j + 1) * cell;
        for (int i = 0; i < W; ++i) {
            const std::int8_t c = m_model.get(BeadPoint(i, row));
            drawCell(p, gridLeft + i * cell, y, cell,
                     m_model.color(c), BeadSymbols::glyph(c));
        }
        if (row % 10 == 0) {
            const QString label = QString::number(row);
            p.drawLine(QPointF(markerW - 6, y + cell),
                       QPointF(markerW - 1, y + cell));
            p.drawText(QPointF(0, y + cell + fm.ascent() / 2.0), label);
        }
    }
}

void PrintJob::drawCorrected(QPainter& p, const QRectF& body, int startRow, int rowCount)
{
    /*  Hexagonal-stagger preview. The corrected view shifts every
        other row by half a cell horizontally; otherwise the
        pagination and orientation match drawDraft.                */
    const qreal cell = std::round(72.0 / 25.4 * CELL_MM);
    const int W = m_model.width();
    p.setPen(Qt::black);
    for (int j = 0; j < rowCount; ++j) {
        const int row = startRow + j;
        const qreal y = body.height() - (j + 1) * cell;
        const qreal stagger = (row % 2 == 0) ? 0 : cell / 2.0;
        for (int i = 0; i < W; ++i) {
            const std::int8_t c = m_model.get(BeadPoint(i, row));
            drawCell(p, i * cell + stagger, y, cell,
                     m_model.color(c), BeadSymbols::glyph(c));
        }
    }
}

void PrintJob::drawSimulation(QPainter& p, const QRectF& body, int startRow, int rowCount)
{
    /*  Half-circumference tube. Renders model.width()/2 columns at
        full cell size; the seam-edge half-cells from the on-screen
        painter are simplified away here for legibility on print
        output.                                                    */
    const qreal cell = std::round(72.0 / 25.4 * CELL_MM);
    const int W = m_model.width();
    const int visible = W / 2;
    p.setPen(Qt::black);
    for (int j = 0; j < rowCount; ++j) {
        const int row = startRow + j;
        const qreal y = body.height() - (j + 1) * cell;
        const qreal stagger = (row % 2 == 0) ? 0 : cell / 2.0;
        for (int i = 0; i < visible; ++i) {
            const std::int8_t c = m_model.get(BeadPoint(i, row));
            drawCell(p, i * cell + stagger, y, cell,
                     m_model.color(c), BeadSymbols::glyph(c));
        }
    }
}

} // namespace jbead
