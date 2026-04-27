#include "printsettings.h"

#include <QPrinter>
#include <QSettings>

namespace jbead {

void PrintSettings::load()
{
    QSettings s;
    s.beginGroup(QStringLiteral("print"));
    pageSize       = static_cast<QPageSize::PageSizeId>(
                         s.value(QStringLiteral("pageSize"), int(QPageSize::A4)).toInt());
    orientation    = static_cast<QPageLayout::Orientation>(
                         s.value(QStringLiteral("orientation"), int(QPageLayout::Portrait)).toInt());
    marginLeftMm   = s.value(QStringLiteral("marginLeftMm"),   marginLeftMm).toDouble();
    marginRightMm  = s.value(QStringLiteral("marginRightMm"),  marginRightMm).toDouble();
    marginTopMm    = s.value(QStringLiteral("marginTopMm"),    marginTopMm).toDouble();
    marginBottomMm = s.value(QStringLiteral("marginBottomMm"), marginBottomMm).toDouble();
    printDraft       = s.value(QStringLiteral("printDraft"),      printDraft).toBool();
    printCorrected   = s.value(QStringLiteral("printCorrected"),  printCorrected).toBool();
    printSimulation  = s.value(QStringLiteral("printSimulation"), printSimulation).toBool();
    printReport      = s.value(QStringLiteral("printReport"),     printReport).toBool();
    printBeadList    = s.value(QStringLiteral("printBeadList"),   printBeadList).toBool();
    fullPattern      = s.value(QStringLiteral("fullPattern"),     fullPattern).toBool();
    s.endGroup();
}

void PrintSettings::save() const
{
    QSettings s;
    s.beginGroup(QStringLiteral("print"));
    s.setValue(QStringLiteral("pageSize"),       int(pageSize));
    s.setValue(QStringLiteral("orientation"),    int(orientation));
    s.setValue(QStringLiteral("marginLeftMm"),   marginLeftMm);
    s.setValue(QStringLiteral("marginRightMm"),  marginRightMm);
    s.setValue(QStringLiteral("marginTopMm"),    marginTopMm);
    s.setValue(QStringLiteral("marginBottomMm"), marginBottomMm);
    s.setValue(QStringLiteral("printDraft"),      printDraft);
    s.setValue(QStringLiteral("printCorrected"),  printCorrected);
    s.setValue(QStringLiteral("printSimulation"), printSimulation);
    s.setValue(QStringLiteral("printReport"),     printReport);
    s.setValue(QStringLiteral("printBeadList"),   printBeadList);
    s.setValue(QStringLiteral("fullPattern"),     fullPattern);
    s.endGroup();
}

void PrintSettings::apply(QPrinter* printer) const
{
    QPageLayout layout = printer->pageLayout();
    layout.setPageSize(QPageSize(pageSize));
    layout.setOrientation(orientation);
    layout.setUnits(QPageLayout::Millimeter);
    layout.setMargins(QMarginsF(marginLeftMm, marginTopMm, marginRightMm, marginBottomMm));
    printer->setPageLayout(layout);
}

void PrintSettings::readFromPrinter(const QPrinter* printer)
{
    const QPageLayout layout = printer->pageLayout();
    pageSize    = layout.pageSize().id();
    orientation = layout.orientation();
    const QMarginsF m = layout.margins(QPageLayout::Millimeter);
    marginLeftMm   = m.left();
    marginRightMm  = m.right();
    marginTopMm    = m.top();
    marginBottomMm = m.bottom();
}

} // namespace jbead
