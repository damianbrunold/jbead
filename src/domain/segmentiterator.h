#pragma once

#include "beadpoint.h"

#include <cstdlib>
#include <optional>

namespace jbead {

/*  Iterates the discrete points along a line from begin to end
    inclusive. Faithful port of legacy ch.jbead.SegmentIterator —
    parametric form, not classical Bresenham. Java's truncate-toward-
    zero integer division is mirrored by C++11's integer division
    semantics, so behaviour is identical, including for the diagonal
    sign-mixing cases the legacy code does not specifically handle. */
class SegmentIterator
{
public:
    SegmentIterator(BeadPoint begin, BeadPoint end)
        : m_begin(begin), m_end(end), m_next(begin),
          m_dx(end.x() - begin.x()),
          m_dy(end.y() - begin.y()),
          m_sx(m_dx > 0 ? 1 : -1),
          m_sy(m_dy > 0 ? 1 : -1)
    {
    }

    bool hasNext() const { return m_next.has_value(); }

    BeadPoint next()
    {
        const BeadPoint result = *m_next;
        if (result == m_end) {
            m_next.reset();
        } else if (m_dx == 0) {
            m_next = BeadPoint(result.x(), result.y() + m_sy);
        } else if (m_dy == 0) {
            m_next = BeadPoint(result.x() + m_sx, result.y());
        } else if (std::abs(m_dx) > std::abs(m_dy)) {
            const int nx = result.x() + m_sx;
            m_next = BeadPoint(nx, m_begin.y() + std::abs(nx - m_begin.x()) * m_dy / m_dx);
        } else if (std::abs(m_dx) < std::abs(m_dy)) {
            const int ny = result.y() + m_sy;
            m_next = BeadPoint(m_begin.x() + std::abs(ny - m_begin.y()) * m_dx / m_dy, ny);
        } else {
            m_next = BeadPoint(result.x() + m_sx, result.y() + m_sy);
        }
        return result;
    }

private:
    BeadPoint m_begin;
    BeadPoint m_end;
    std::optional<BeadPoint> m_next;
    int m_dx, m_dy, m_sx, m_sy;
};

} // namespace jbead
