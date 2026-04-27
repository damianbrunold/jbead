#pragma once

#include "printsettings.h"

#include <QList>
#include <QSizeF>
#include <QString>

#include <memory>

class QPainter;

namespace jbead {

class Model;

/*  One column in the export / print strip. Implementations cap their
    own height to `paintHeight` and emit a fixed `width(paintHeight)`
    column at (x, y). Coordinates are in points (1/72 inch). The
    strip painter (StripLayout) packs all parts left-to-right.   */
class PartPainter
{
public:
    virtual ~PartPainter() = default;

    /*  Width of this column at the given paint height. Most parts
        ignore the height argument; the bead list shrinks/grows
        based on how many pills fit per column.                   */
    virtual qreal width(qreal paintHeight) const = 0;

    /*  Paint the column into the rect (x, y, width, paintHeight)
        of `painter`. The painter is already positioned at the page
        origin; coordinates are absolute in the painter's coord
        system.                                                    */
    virtual void  paint(QPainter& painter, qreal x, qreal y,
                        qreal paintHeight) const = 0;
};

/*  Drives the layout: builds a list of PartPainters, adds them up
    horizontally, and supports two render modes:

      - exportToCanvas(): renders at natural width (no scaling) into
        an arbitrary QPaintDevice. Used for PNG / JPEG / SVG export
        where the canvas is sized to the strip.
      - paintFitted(): scales the strip uniformly to fit a given
        paint rect (page minus margins). Used for PDF, print, and
        print preview so the output always lands on a single page.

    Layout order matches the textile editor's PDF: ReportInfo,
    Draft, Corrected, Simulation, BeadList. Each grid view is a
    SINGLE COLUMN capped at the top of the page.                  */
class StripLayout
{
public:
    StripLayout(const Model& model, const PrintSettings& settings);
    ~StripLayout();

    /*  Native dimensions of the strip given the available paint
        height. Caller picks the height (e.g. landscape A4 minus
        margins for export, or chooses a target like 595pt for
        natural raster sizing).                                    */
    QSizeF naturalSize(qreal paintHeight) const;

    /*  Emit the strip into `painter` at its natural scale, with
        origin at (offsetX, offsetY). Returns the actual width
        consumed.                                                  */
    qreal paintNatural(QPainter& painter, qreal offsetX, qreal offsetY,
                       qreal paintHeight) const;

    /*  Emit the strip into `painter`, fitted into the rect (x, y,
        w, h) by uniform scaling. Centres horizontally if the
        natural width is narrower than `w`.                       */
    void  paintFitted(QPainter& painter, qreal x, qreal y,
                      qreal w, qreal h) const;

    /*  Number of part columns. >= 0 (zero if every section is
        toggled off in PrintSettings).                            */
    int   columnCount() const;

private:
    void buildParts();

    const Model&         m_model;
    PrintSettings        m_settings;
    QList<std::shared_ptr<PartPainter>> m_parts;
};

} // namespace jbead
