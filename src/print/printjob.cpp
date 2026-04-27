#include "printjob.h"

#include "domain/model.h"
#include "strippainter.h"

#include <QFileInfo>
#include <QImage>
#include <QPageLayout>
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

/*  Resolution used when rasterising the strip into a PNG / JPEG.
    150 dpi gives a decent print-ready pixel size for the natural
    point dimensions without producing absurdly large files for
    the small bead patterns this tool actually deals with.        */
constexpr int RASTER_DPI = 150;

QRectF pageRect(const QPrinter* p)
{
    return p->pageLayout().paintRect(QPageLayout::Point);
}

} // namespace

PrintJob::PrintJob(const Model& model, const PrintSettings& settings)
    : m_model(model), m_settings(settings)
{
}

void PrintJob::paintPage(QPainter& p, QPrinter* printer) const
{
    const QRectF page = pageRect(printer);
    StripLayout layout(m_model, m_settings);
    /*  Single-page strip, scaled uniformly to fit the page rect.
        Header / footer omitted intentionally: the report-info part
        already carries the filename in the body, and the textile
        editor PDF doesn't add page chrome either.                */
    layout.paintFitted(p, page.left(), page.top(),
                       page.width(), page.height());
}

bool PrintJob::run(QPrinter* printer)
{
    QPainter p(printer);
    if (!p.isActive()) return false;
    paintPage(p, printer);
    p.end();
    return true;
}

void PrintJob::paint(QPrinter* printer)
{
    QPainter p(printer);
    if (!p.isActive()) return;
    paintPage(p, printer);
    p.end();
}

bool PrintJob::exportPdf(const QString& path) const
{
    QPrinter printer(QPrinter::HighResolution);
    m_settings.apply(&printer);
    printer.setOutputFormat(QPrinter::PdfFormat);
    printer.setOutputFileName(path);

    QPainter p(&printer);
    if (!p.isActive()) return false;
    paintPage(p, &printer);
    p.end();
    return QFileInfo(path).exists();
}

bool PrintJob::exportImage(const QString& path, const char* format) const
{
    StripLayout layout(m_model, m_settings);
    if (layout.columnCount() == 0) return false;

    /*  Convert points -> pixels at RASTER_DPI. The natural strip
        layout produces dimensions in points (1/72"); QImage wants
        pixels.                                                    */
    const QSizeF naturalPt = layout.naturalSize(NATURAL_HEIGHT);
    const qreal  ptToPx    = RASTER_DPI / 72.0;
    const int    margin    = int(std::round(6.0 * ptToPx));
    const int    w = int(std::round(naturalPt.width()  * ptToPx)) + 2 * margin;
    const int    h = int(std::round(naturalPt.height() * ptToPx)) + 2 * margin;

    QImage img(std::max(64, w), std::max(64, h),
               QImage::Format_ARGB32_Premultiplied);
    img.fill(Qt::white);
    img.setDotsPerMeterX(int(std::round(RASTER_DPI / 0.0254)));
    img.setDotsPerMeterY(int(std::round(RASTER_DPI / 0.0254)));

    QPainter p(&img);
    p.setRenderHint(QPainter::Antialiasing, true);
    p.setRenderHint(QPainter::TextAntialiasing, true);
    p.scale(ptToPx, ptToPx);
    layout.paintNatural(p, 6.0, 6.0, NATURAL_HEIGHT);
    p.end();

    return img.save(path, format);
}

bool PrintJob::exportSvg(const QString& path) const
{
    StripLayout layout(m_model, m_settings);
    if (layout.columnCount() == 0) return false;

    const QSizeF natural = layout.naturalSize(NATURAL_HEIGHT);
    const qreal  margin  = 6.0;
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
