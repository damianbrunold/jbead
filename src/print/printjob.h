#pragma once

#include "printsettings.h"

#include <QString>

class QPrinter;

namespace jbead {

class Model;

/*  Front door for every print and export route. Two distinct
    pipelines live behind the same class:

    Export  (PNG / JPEG / SVG / PDF): a single-page sketch via
            StripLayout. Grid views (Draft / Corrected / Simulation)
            are clamped to one column each (PrintSettings::
            singleColumnGrids = true is forced by the export
            entry points); the bead list still wraps to multiple
            columns when the run count overflows. Output canvas
            is sized to fit the natural strip — no down-scaling,
            no clipping.

    Print + Print Preview: a multi-page render via MultiPageLayout.
            Every visible part is rendered fully — Draft can take
            several columns / pages for a tall pattern. Page size
            comes from PrintSettings (paper + orientation), packed
            left-to-right.

    Both routes consume the same PrintSettings — flipping a
    printDraft / printCorrected / etc. toggle drops that section
    from both pipelines.                                          */
class PrintJob
{
public:
    PrintJob(const Model& model, const PrintSettings& settings);

    bool run(QPrinter* printer) const;
    void paint(QPrinter* printer) const;

    bool exportPdf(const QString& path) const;
    bool exportImage(const QString& path, const char* format = nullptr) const;
    bool exportSvg(const QString& path) const;

private:
    const Model&  m_model;
    PrintSettings m_settings;
};

} // namespace jbead
