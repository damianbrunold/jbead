#pragma once

#include "beadpoint.h"
#include "beadrect.h"

#include <optional>

namespace jbead {

/*  Row-major iterator over a BeadRect. Order: left-to-right within a
    row, bottom-to-top across rows. Constructed against the
    normalized (min, max) corners; an empty rect (negative bounds, as
    used by BeadRect::empty()) yields no points.                  */
class RectIterator
{
public:
    explicit RectIterator(const BeadRect& rect)
        : m_begin(rect.begin()), m_end(rect.end())
    {
        if (m_begin.x() < 0 || m_end.y() < 0) {
            m_next.reset();
        } else {
            m_next = m_begin;
        }
    }

    bool hasNext() const { return m_next.has_value(); }

    BeadPoint next()
    {
        const BeadPoint cur = *m_next;
        if (cur.x() < m_end.x()) {
            m_next = BeadPoint(cur.x() + 1, cur.y());
        } else if (cur.y() < m_end.y()) {
            m_next = BeadPoint(m_begin.x(), cur.y() + 1);
        } else {
            m_next.reset();
        }
        return cur;
    }

private:
    BeadPoint m_begin;
    BeadPoint m_end;
    std::optional<BeadPoint> m_next;
};

} // namespace jbead
