#pragma once

#include <QHash>
#include <QtCore/QtGlobal>

namespace jbead {

/*  Immutable grid coordinate. Mirrors legacy ch.jbead.Point.

    Convention: y == 0 is the *bottom* row of the field; y grows
    upward. The renderer flips this on screen (see
    SimpleCoordinateCalculator). Storage in BeadField is row-major
    using the same convention: index = x + width * y.            */
class BeadPoint
{
public:
    constexpr BeadPoint() : m_x(0), m_y(0) {}
    constexpr BeadPoint(int x, int y) : m_x(x), m_y(y) {}

    constexpr int x() const { return m_x; }
    constexpr int y() const { return m_y; }

    constexpr bool operator==(const BeadPoint& o) const { return m_x == o.m_x && m_y == o.m_y; }
    constexpr bool operator!=(const BeadPoint& o) const { return !(*this == o); }

    constexpr BeadPoint scrolled(int scroll) const   { return BeadPoint(m_x, m_y + scroll); }
    constexpr BeadPoint unscrolled(int scroll) const { return BeadPoint(m_x, m_y - scroll); }
    constexpr BeadPoint nextLeft() const             { return BeadPoint(m_x - 1, m_y); }
    constexpr BeadPoint nextRight() const            { return BeadPoint(m_x + 1, m_y); }
    constexpr BeadPoint nextBelow() const            { return BeadPoint(m_x, m_y - 1); }
    constexpr BeadPoint nextAbove() const            { return BeadPoint(m_x, m_y + 1); }
    constexpr BeadPoint lastLeft() const             { return BeadPoint(0, m_y); }
    constexpr BeadPoint lastRight(int width) const   { return BeadPoint(width - 1, m_y); }

    /*  Shift wraps horizontally with vertical carry — used to apply
        Model.shift to view-side coordinates. Mirrors Point.shifted. */
    constexpr BeadPoint shifted(int shift, int width) const
    {
        const int s = m_x + shift;
        return BeadPoint(s % width, m_y + s / width);
    }

private:
    int m_x;
    int m_y;
};

} // namespace jbead

inline size_t qHash(const jbead::BeadPoint& p, size_t seed = 0) noexcept
{
    return qHash(p.x() ^ p.y(), seed);
}
