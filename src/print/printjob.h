#pragma once

#include "printsettings.h"

#include <QList>
#include <QString>

class QPrinter;
class QPainter;

namespace jbead {

class Model;

/*  Top-level print orchestrator. Mirrors the legacy DesignPrinter
    role plus dbweave's PrPrinter lifecycle but in a single class:

      run(printer)        — used for live printing (own QPainter)
      paint(printer)      — used by QPrintPreviewDialog::paintRequested

    Pagination model: each enabled section ("Report",  "Draft",
    "Corrected", "Simulation", "Bead list") gets its own page run.
    Multi-row sections (Draft / Corrected / Simulation) chunk across
    pages by row count derived from the printable area and a
    fixed mm-per-cell scale. Sections with bounded content (Report,
    Bead list) take a single page each.

    A header line "<filename> — <section> — page X / Y" is drawn on
    every page; the footer is left blank deliberately to match the
    dbweave layout the user signed off on.                         */
class PrintJob
{
public:
    enum class Section { ReportInfos, Draft, Corrected, Simulation, BeadList };

    struct PageDescriptor {
        Section section;
        int     startRow;       // for grid sections only
        int     rowCount;       // for grid sections only
    };

    PrintJob(const Model& model, const PrintSettings& settings);

    /*  Live-print entry. Allocates a QPainter on `printer`, walks
        every page in m_pages, returns true on success. The caller
        is expected to have already invoked QPrintDialog and
        applied the result to `printer`.                           */
    bool run(QPrinter* printer);

    /*  Preview entry. Recomputes pagination against the current
        printer geometry (preview can be resized / re-oriented
        from inside the dialog) and paints every page in turn,
        calling printer->newPage() between them.                   */
    void paint(QPrinter* printer);

    /*  Page count after pagination — useful for status messages. */
    int  pageCount() const { return m_pages.size(); }

private:
    void layoutPages(QPrinter* printer);
    void drawPage(QPainter& p, QPrinter* printer, int pageIndex);
    void drawHeader(QPainter& p, const QRectF& body, const PageDescriptor& page,
                    int pageIndex);

    /*  Per-section drawing. Each function paints into the body
        rect after the header has been carved off; coordinates are
        relative to body.topLeft().                               */
    void drawReportInfos(QPainter& p, const QRectF& body);
    void drawBeadList(QPainter& p, const QRectF& body);
    void drawDraft(QPainter& p, const QRectF& body, int startRow, int rowCount);
    void drawCorrected(QPainter& p, const QRectF& body, int startRow, int rowCount);
    void drawSimulation(QPainter& p, const QRectF& body, int startRow, int rowCount);

    /*  Pixel size of one bead cell on the printer at the current
        resolution. 5 mm by default — translates to the standard
        legacy print scale.                                       */
    int cellSize(const QPrinter* printer) const;

    const Model&         m_model;
    PrintSettings        m_settings;
    QList<PageDescriptor> m_pages;
};

} // namespace jbead
