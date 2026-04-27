#include "beadpainter.h"

#include "domain/beadsymbols.h"
#include "domain/model.h"

#include <QFontMetrics>
#include <QHash>
#include <QPainter>

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

    if (m_drawColors || m_forceColors) {
        p.fillRect(x - dx, y, gx, gy, color);
    }

    if (m_drawSymbols) {
        const QString sym = BeadSymbols::glyph(c);
        const QColor textColor = (m_drawColors || m_forceColors)
            ? contrastingColor(color) : QColor(Qt::black);
        p.setPen(textColor);
        p.setFont(m_symbolFont);
        const QFontMetrics fm(m_symbolFont);
        const int textW = fm.horizontalAdvance(sym);
        p.drawText(x + (gx - textW) / 2 - dx, y + m_symbolFont.pixelSize(), sym);
    }

    if (m_drawBorder) {
        p.setPen(Qt::black);
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
