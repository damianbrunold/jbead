#pragma once

#include "beadpoint.h"

#include <cstdlib>
#include <optional>

namespace jbead {

/*  Iterates the discrete cells along a line from begin to end
    inclusive using the standard Bresenham line algorithm.

    Earlier this class was a literal port of legacy ch.jbead.
    SegmentIterator — a parametric formula that only converges for
    axis-aligned or pure 45-degree inputs. The legacy app always
    snapped to one of those before calling drawLine (via
    Selection::lineDestination), so the bug never surfaced in
    Java. The Qt port now passes the *raw cursor position* when
    no snap modifier is held; with a non-axis-aligned arbitrary
    direction the parametric formula diverged and the iterator
    looped forever, producing the user-reported app freeze.

    Plain Bresenham handles every direction correctly, including
    negative-dx-with-negative-dy, so we use that here.            */
class SegmentIterator
{
public:
    SegmentIterator(BeadPoint begin, BeadPoint end)
        : m_x(begin.x()), m_y(begin.y()),
          m_endX(end.x()), m_endY(end.y()),
          m_dx(std::abs(end.x() - begin.x())),
          m_dy(std::abs(end.y() - begin.y())),
          m_sx(begin.x() < end.x() ? 1 : -1),
          m_sy(begin.y() < end.y() ? 1 : -1),
          m_err(m_dx - m_dy),
          m_done(false)
    {
    }

    bool hasNext() const { return !m_done; }

    BeadPoint next()
    {
        const BeadPoint cur(m_x, m_y);
        if (m_x == m_endX && m_y == m_endY) {
            m_done = true;
            return cur;
        }
        const int e2 = 2 * m_err;
        if (e2 > -m_dy) { m_err -= m_dy; m_x += m_sx; }
        if (e2 <  m_dx) { m_err += m_dx; m_y += m_sy; }
        return cur;
    }

private:
    int m_x, m_y;
    int m_endX, m_endY;
    int m_dx, m_dy;
    int m_sx, m_sy;
    int m_err;
    bool m_done;
};

} // namespace jbead
