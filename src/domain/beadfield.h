#pragma once

#include "beadpoint.h"
#include "beadrect.h"

#include <QByteArray>

#include <cstdint>

namespace jbead {

/*  2D byte grid that backs a JBead pattern. Cell value 0 = empty;
    1..N = palette index. Storage is row-major: index = x + width*y.
    Default 15 x 800 mirrors legacy DEFAULT_WIDTH / DEFAULT_HEIGHT.
    Resize semantics preserve the top-left corner (legacy
    System.arraycopy stride copy in setWidth/setHeight).         */
class BeadField
{
public:
    static constexpr int DEFAULT_WIDTH  = 15;
    static constexpr int DEFAULT_HEIGHT = 800;

    BeadField();

    int width()  const { return m_width; }
    int height() const { return m_height; }

    BeadRect fullRect() const { return BeadRect(BeadPoint(0, 0), BeadPoint(m_width - 1, m_height - 1)); }
    BeadRect rect(int starty, int endy) const { return BeadRect(BeadPoint(0, starty), BeadPoint(m_width - 1, endy)); }

    int  lastIndex()        const { return m_width * m_height - 1; }
    bool isValidIndex(int i) const { return i >= 0 && i <= lastIndex(); }

    int  indexOf(BeadPoint p) const { return p.x() + m_width * p.y(); }
    BeadPoint pointAt(int i)  const { return BeadPoint(i % m_width, i / m_width); }

    std::int8_t get(BeadPoint p) const { return get(indexOf(p)); }
    std::int8_t get(int i)       const { return static_cast<std::int8_t>(m_field.at(i)); }
    void set(BeadPoint p, std::int8_t v) { set(indexOf(p), v); }
    void set(int i, std::int8_t v)       { m_field[i] = static_cast<char>(v); }

    void clear();
    void setWidth(int width);
    void setHeight(int height);
    void copyFrom(const BeadField& source);

    void swap(BeadPoint a, BeadPoint b);
    void mirrorHorizontal(const BeadRect& rect);
    void mirrorVertical(const BeadRect& rect);
    void rotate(const BeadRect& rect);
    void deleteRect(const BeadRect& rect);
    void insertRow();
    void deleteRow();
    void replaceColor(std::int8_t oldColor, std::int8_t newColor);

    QByteArray copyOf(const BeadRect& rect) const;

    /*  Direct buffer access — used by the file format layer. The
        QByteArray length is always width*height bytes.            */
    const QByteArray& data() const { return m_field; }
    void setData(const QByteArray& data, int width, int height);

private:
    QByteArray m_field;
    int m_width;
    int m_height;
};

} // namespace jbead
