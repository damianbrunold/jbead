#include "beadpainter.h"

#include "domain/beadsymbols.h"
#include "domain/model.h"

#include <QFontMetrics>
#include <QGuiApplication>
#include <QHash>
#include <QPainter>
#include <QPalette>

#include <cmath>

namespace jbead {

BeadPainter::BeadPainter(const CoordinateCalculator& coord, const Model& model,
                         bool drawColors, bool drawSymbols, const QFont& symbolFont)
    : m_coord(coord), m_model(model),
      m_drawColors(drawColors), m_drawSymbols(drawSymbols),
      m_symbolFont(symbolFont)
{
}

void BeadPainter::paint(QPainter& p, BeadPoint pt, std::int8_t c) const
{
    const QColor color = m_model.color(c);
    const int x  = m_coord.xFor(pt);
    const int y  = m_coord.yFor(pt);
    const int gx = m_coord.gridX();
    const int gy = m_coord.gridY();
    const int dx = m_coord.offsetXFor(pt);

    const bool filled = m_drawColors || m_forceColors;

    /*  In colour-blind mode (Draw Colors off) we drop the fill and
        let cells delineate themselves with the cell border. Symbol
        and border colour follow the active palette so the panel stays
        legible under both light and dark themes — hard-coding black
        produced an unreadable dark-on-dark grid in dark mode.       */
    const QColor outline = filled
        ? QColor(Qt::black)
        : QGuiApplication::palette().color(QPalette::WindowText);

    if (filled) {
        p.fillRect(x - dx, y, gx, gy, color);
    }

    if (m_drawSymbols) {
        const QString sym = BeadSymbols::glyph(c);
        const QColor textColor = filled ? contrastingColor(color) : outline;
        p.setPen(textColor);
        p.setFont(m_symbolFont);
        const QFontMetrics fm(m_symbolFont);
        const int textW = fm.horizontalAdvance(sym);
        p.drawText(x + (gx - textW) / 2 - dx, y + m_symbolFont.pixelSize(), sym);
    }

    if (m_drawBorder) {
        p.setPen(outline);
        p.drawRect(x - dx, y, gx, gy);
    }
}

QColor BeadPainter::contrastingColor(const QColor& color)
{
    static QHash<QRgb, QRgb> cache;
    const QRgb key = color.rgb();
    if (auto it = cache.constFind(key); it != cache.constEnd()) return QColor::fromRgb(*it);

    const auto distance = [&](const QColor& a, const QColor& b) {
        const int dr = a.red()   - b.red();
        const int dg = a.green() - b.green();
        const int db = a.blue()  - b.blue();
        return std::sqrt(double(dr * dr + dg * dg + db * db));
    };

    const QColor pick = distance(color, Qt::white) > distance(color, Qt::black)
                        ? QColor(Qt::white) : QColor(Qt::black);
    cache.insert(key, pick.rgb());
    return pick;
}

} // namespace jbead
