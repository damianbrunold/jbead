#include "printjob.h"

#include "domain/model.h"
#include "strippainter.h"

#include <QFileInfo>
#include <QImage>
#include <QPageLayout>
#include <QPageSize>
#include <QPainter>
#include <QPrinter>
#include <QtSvg/QSvgGenerator>

#include <algorithm>
#include <cmath>

namespace jbead {

namespace {

/*  Natural strip height for raster / SVG export. ~595 pt is the
    short edge of a landscape A4; using it as the fixed canvas
    height makes the export visually match the PDF / print output
    even though there's no physical page involved.                */
constexpr qreal NATURAL_HEIGHT = 595.0;
constexpr qreal EXPORT_MARGIN_PT = 6.0;

QRectF pagePaintRectPt(const QPrinter* p)
{
    return p->pageLayout().paintRect(QPageLayout::Point);
}

/*  PrintSettings adjusted for the export sketch: clamp grid views
    to a single column. Keeps the rest of the user's preferences
    (paper, margins, section toggles) untouched.                  */
PrintSettings exportSettings(const PrintSettings& base)
{
    PrintSettings s = base;
    s.singleColumnGrids = true;
    return s;
}

} // namespace

PrintJob::PrintJob(const Model& model, const PrintSettings& settings)
    : m_model(model), m_settings(settings)
{
}

// -----------------------------------------------------------------
// Print + Print Preview — multi-page MultiPageLayout.
// -----------------------------------------------------------------

bool PrintJob::run(QPrinter* printer) const
{
    PrintSettings s = m_settings;
    s.singleColumnGrids = false;            // print never clamps

    MultiPageLayout layout(m_model, s);
    const QRectF page = pagePaintRectPt(printer);
    layout.layout(QSizeF(page.width(), page.height()));
    if (layout.pageCount() == 0) return false;

    QPainter p(printer);
    if (!p.isActive()) return false;
    for (int i = 0; i < layout.pageCount(); ++i) {
        if (i > 0) printer->newPage();
        layout.paintPage(p, i, page.left(), page.top(),
                         page.width(), page.height());
    }
    p.end();
    return true;
}

void PrintJob::paint(QPrinter* printer) const
{
    PrintSettings s = m_settings;
    s.singleColumnGrids = false;

    MultiPageLayout layout(m_model, s);
    const QRectF page = pagePaintRectPt(printer);
    layout.layout(QSizeF(page.width(), page.height()));
    if (layout.pageCount() == 0) return;

    QPainter p(printer);
    if (!p.isActive()) return;
    for (int i = 0; i < layout.pageCount(); ++i) {
        if (i > 0) printer->newPage();
        layout.paintPage(p, i, page.left(), page.top(),
                         page.width(), page.height());
    }
    p.end();
}

// -----------------------------------------------------------------
// Export PDF — single-page sketch on a custom-sized PDF page so the
// strip lands at native scale (no down-scaling to a fixed paper).
// -----------------------------------------------------------------

bool PrintJob::exportPdf(const QString& path) const
{
    const PrintSettings s = exportSettings(m_settings);
    StripLayout layout(m_model, s);
    if (layout.columnCount(NATURAL_HEIGHT) == 0) return false;

    const QSizeF natural = layout.naturalSize(NATURAL_HEIGHT);
    /*  PDF page size = natural strip + symmetric margins, in
        points. QPageSize::Point lets us express that directly.  */
    const QSizeF pageSizePt(natural.width()  + 2 * EXPORT_MARGIN_PT,
                            natural.height() + 2 * EXPORT_MARGIN_PT);

    QPrinter printer(QPrinter::HighResolution);
    printer.setOutputFormat(QPrinter::PdfFormat);
    printer.setOutputFileName(path);
    QPageLayout pageLayout(QPageSize(pageSizePt, QPageSize::Point),
                           QPageLayout::Portrait,
                           QMarginsF(0, 0, 0, 0));
    printer.setPageLayout(pageLayout);

    QPainter p(&printer);
    if (!p.isActive()) return false;
    /*  The printer's logicalDpi is its native resolution (e.g.
        1200 dpi); QPainter on a printer expects coords in device
        pixels by default. paintRect(Point) gave us the page size
        in points; map that to the printer's native pixels with a
        single uniform scale so the rest of the strip painter keeps
        working in points.                                        */
    const QRectF pagePx = printer.pageLayout().paintRectPixels(printer.resolution());
    const qreal  scale  = pagePx.width() / pageSizePt.width();
    p.scale(scale, scale);
    layout.paintNatural(p, EXPORT_MARGIN_PT, EXPORT_MARGIN_PT, NATURAL_HEIGHT);
    p.end();
    return QFileInfo(path).exists();
}

// -----------------------------------------------------------------
// Export PNG / JPEG — natural-size raster, 1 px = 1 pt at 72 dpi
// so setPointSize lines up with the points-based coord system and
// fonts don't render double-scaled.
// -----------------------------------------------------------------

bool PrintJob::exportImage(const QString& path, const char* format) const
{
    const PrintSettings s = exportSettings(m_settings);
    StripLayout layout(m_model, s);
    if (layout.columnCount(NATURAL_HEIGHT) == 0) return false;

    const QSizeF natural = layout.naturalSize(NATURAL_HEIGHT);
    const int margin = int(std::round(EXPORT_MARGIN_PT));
    const int w = int(std::ceil(natural.width()))  + 2 * margin;
    const int h = int(std::ceil(natural.height())) + 2 * margin;

    QImage img(std::max(64, w), std::max(64, h),
               QImage::Format_ARGB32_Premultiplied);
    img.fill(Qt::white);
    /*  Pin the image's logical DPI to 72 so QPainter::setPointSize
        renders at 1 pt = 1 px, matching the points-based coords
        the strip painter uses. (The default 96 dpi would otherwise
        oversize every glyph by 4/3.)                              */
    constexpr qreal DPI_72_PER_METRE = 72.0 / 0.0254;
    img.setDotsPerMeterX(int(std::round(DPI_72_PER_METRE)));
    img.setDotsPerMeterY(int(std::round(DPI_72_PER_METRE)));

    QPainter p(&img);
    p.setRenderHint(QPainter::Antialiasing, true);
    p.setRenderHint(QPainter::TextAntialiasing, true);
    layout.paintNatural(p, EXPORT_MARGIN_PT, EXPORT_MARGIN_PT, NATURAL_HEIGHT);
    p.end();

    return img.save(path, format);
}

// -----------------------------------------------------------------
// Export SVG — vector format, no rasterisation, viewBox = natural.
// -----------------------------------------------------------------

bool PrintJob::exportSvg(const QString& path) const
{
    const PrintSettings s = exportSettings(m_settings);
    StripLayout layout(m_model, s);
    if (layout.columnCount(NATURAL_HEIGHT) == 0) return false;

    const QSizeF natural = layout.naturalSize(NATURAL_HEIGHT);
    const qreal  margin  = EXPORT_MARGIN_PT;
    const QSize  sz(int(std::ceil(natural.width()  + 2 * margin)),
                    int(std::ceil(natural.height() + 2 * margin)));

    QSvgGenerator gen;
    gen.setFileName(path);
    gen.setSize(sz);
    gen.setViewBox(QRect(0, 0, sz.width(), sz.height()));
    gen.setTitle(QFileInfo(m_model.filePath()).fileName());
    gen.setDescription(QStringLiteral("JBead pattern export"));

    QPainter p(&gen);
    p.setRenderHint(QPainter::Antialiasing, true);
    layout.paintNatural(p, margin, margin, NATURAL_HEIGHT);
    p.end();
    return QFileInfo(path).exists();
}

} // namespace jbead
