#pragma once

#include "printsettings.h"

#include <QList>
#include <QSizeF>
#include <QString>

#include <memory>

class QPainter;

namespace jbead {

class Model;

/*  One part of the print/export strip. A PartPainter can emit
    several columns: BeadList wraps to extra columns when there are
    more bead runs than fit a single column; grid views (Draft /
    Corrected / Simulation) wrap when the pattern is taller than one
    column UNLESS PrintSettings::singleColumnGrids is set (which is
    how the export sketch keeps everything to a single column per
    grid view). Coordinates are in points (1/72 inch).            */
class PartPainter
{
public:
    virtual ~PartPainter() = default;

    /*  Width of every column emitted at the given paint height.
        The list length is the column count; total horizontal
        footprint is the sum.                                      */
    virtual QList<qreal> columnWidths(qreal paintHeight) const = 0;

    /*  Paint column `columnIndex` into the rect (x, y, width,
        paintHeight). Coordinates are absolute in `painter`'s
        space.                                                     */
    virtual void paintColumn(QPainter& painter, qreal x, qreal y,
                             qreal paintHeight, int columnIndex) const = 0;
};

/*  Single-page packer used by Export (PNG / JPEG / SVG / PDF).
    Lays every column from every visible part out in a single
    horizontal strip, no page wrap. Two render modes:

      paintNatural()  emits at native scale; the canvas is sized
                      to fit the resulting total width.
      paintFitted()   uniformly scales the strip to fit a target
                      rect — kept for callers that want a "fit
                      the strip onto this page" rendering, but
                      not used for the standard export anymore
                      (custom-page PDF avoids the down-scaling).
*/
class StripLayout
{
public:
    StripLayout(const Model& model, const PrintSettings& settings);
    ~StripLayout();

    QSizeF naturalSize(qreal paintHeight) const;
    qreal  paintNatural(QPainter& painter, qreal offsetX, qreal offsetY,
                        qreal paintHeight) const;
    void   paintFitted(QPainter& painter, qreal x, qreal y,
                       qreal w, qreal h) const;

    int    columnCount(qreal paintHeight) const;

private:
    void buildParts();

    const Model&         m_model;
    PrintSettings        m_settings;
    QList<std::shared_ptr<PartPainter>> m_parts;
};

/*  Multi-page packer used by Print + Print Preview. Walks the same
    PartPainter list as StripLayout but breaks across pages: each
    page is filled left-to-right with whatever columns fit, then a
    new page starts. Used with singleColumnGrids = false so a tall
    pattern's draft view spans several columns and pages.        */
class MultiPageLayout
{
public:
    MultiPageLayout(const Model& model, const PrintSettings& settings);
    ~MultiPageLayout();

    /*  Lay out all part columns onto pages of the given rect
        (in points). Call before pageCount() / paintPage().      */
    void layout(const QSizeF& pageSize);
    int  pageCount() const;

    /*  Paint page `index` into `painter`, with the page rect
        positioned at (x, y, w, h) in painter coords (typically
        the printer's pageRect).                                  */
    void paintPage(QPainter& painter, int index,
                   qreal x, qreal y, qreal w, qreal h) const;

private:
    struct PageColumn {
        std::shared_ptr<PartPainter> part;
        int   columnIndex;
        qreal width;
    };
    struct Page {
        QList<PageColumn> columns;
        qreal totalWidth = 0;
    };

    void buildParts();

    const Model&         m_model;
    PrintSettings        m_settings;
    QList<std::shared_ptr<PartPainter>> m_parts;
    QList<Page>          m_pages;
    qreal                m_pageH = 0;
};

} // namespace jbead
