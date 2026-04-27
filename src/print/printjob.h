#pragma once

#include "printsettings.h"

#include <QString>

class QPainter;
class QPrinter;

namespace jbead {

class Model;

/*  Orchestrator wrapping StripLayout for the four entry points the
    File menu offers:

      run(printer)         — live print: page-fitted single page
      paint(printer)       — print preview's paintRequested signal
      exportPdf(path)      — single-page PDF, page-fitted
      exportImage(path)    — PNG / JPEG raster at natural strip size
      exportSvg(path)      — SVG at natural strip size

    All routes use the same layout (see StripLayout for the spec —
    Report, Draft, Corrected, Simulation, Bead list, each as a
    single column) so what the user sees in print preview matches
    what the export files look like.                              */
class PrintJob
{
public:
    PrintJob(const Model& model, const PrintSettings& settings);

    bool run(QPrinter* printer);
    void paint(QPrinter* printer);

    /*  Export entry points. Return true on success and write to
        `path`. The image writers pick PNG vs JPEG from the file
        extension; pass an explicit "PNG" / "JPEG" / "SVG" / "PDF"
        format string when the extension is ambiguous.            */
    bool exportPdf(const QString& path) const;
    bool exportImage(const QString& path, const char* format = nullptr) const;
    bool exportSvg(const QString& path) const;

private:
    void paintPage(QPainter& p, QPrinter* printer) const;

    const Model&  m_model;
    PrintSettings m_settings;
};

} // namespace jbead
