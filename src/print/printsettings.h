#pragma once

#include <QPageLayout>
#include <QPageSize>

class QPrinter;

namespace jbead {

/*  Persisted print preferences. Stored under the QSettings group
    "print/" alongside the rest of the JBead configuration. The
    fields map directly to the QPageLayout / QPrinter properties
    consumed when a job is dispatched, so applying them is a single
    PrintSettings::apply(QPrinter*) call.

    Section flags decide which of the four pattern views land in the
    print job — they default to all-on but can be toggled from a
    future File -> Print Range dialog. fullPattern controls whether
    the whole pattern (true) or only the discovered repeat unit
    (false) is laid out.                                            */
struct PrintSettings
{
    QPageSize::PageSizeId  pageSize    = QPageSize::A4;
    QPageLayout::Orientation orientation = QPageLayout::Portrait;
    /*  Margins in millimetres, applied uniformly via QPageLayout::
        setMargins. dbweave uses tenths-of-mm; mm is sufficient
        granularity for what's exposed to the user.                */
    qreal marginLeftMm   = 10.0;
    qreal marginRightMm  = 10.0;
    qreal marginTopMm    = 10.0;
    qreal marginBottomMm = 10.0;

    bool printDraft       = true;
    bool printCorrected   = true;
    bool printSimulation  = true;
    bool printReport      = true;
    bool printBeadList    = true;

    bool fullPattern      = false;

    void load();
    void save() const;
    void apply(QPrinter* printer) const;
    void readFromPrinter(const QPrinter* printer);
};

} // namespace jbead
