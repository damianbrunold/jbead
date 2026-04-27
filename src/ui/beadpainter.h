#pragma once

#include "domain/beadpoint.h"

#include <QColor>
#include <QFont>

class QPainter;

namespace jbead {

class Model;

/*  Coordinate calculator interface used by BeadPainter. The four
    canvas widgets each provide their own implementation: DraftPanel
    has uniform cells with no per-row offset; CorrectedPanel and
    SimulationPanel return gridx/2 from offsetXFor() on odd rows to
    produce the hexagonal stagger.                                 */
class CoordinateCalculator
{
public:
    virtual ~CoordinateCalculator() = default;
    virtual int gridX() const = 0;
    virtual int gridY() const = 0;
    virtual int xFor(BeadPoint pt) const = 0;
    virtual int yFor(BeadPoint pt) const = 0;
    virtual int offsetXFor(BeadPoint pt) const = 0;
    virtual int cellWidth(BeadPoint pt) const { return gridX(); }
};

/*  Stateless painter for a single grid cell. Mirrors legacy
    BeadPainter.paint exactly: optional colour fill, optional symbol
    glyph rendered in the contrasting colour, optional 1-pixel black
    border. Three view-mode flags map onto the View menu's "Draw
    Colors" / "Draw Symbols" toggles.                              */
class BeadPainter
{
public:
    BeadPainter(const CoordinateCalculator& coord, const Model& model,
                bool drawColors, bool drawSymbols, const QFont& symbolFont);

    void setDrawBorder(bool draw) { m_drawBorder = draw; }
    void setForceColors(bool on)  { m_forceColors = on; }

    void paint(QPainter& painter, BeadPoint pt, std::int8_t color) const;

    /*  Picks white or black depending on which is further from
        `color` in RGB euclidean distance. Memoised across calls.  */
    static QColor contrastingColor(const QColor& color);

private:
    const CoordinateCalculator& m_coord;
    const Model&                m_model;
    bool                        m_drawColors;
    bool                        m_drawSymbols;
    QFont                       m_symbolFont;
    bool                        m_drawBorder = true;
    bool                        m_forceColors = false;
};

} // namespace jbead
