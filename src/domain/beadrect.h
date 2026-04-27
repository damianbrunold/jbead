#pragma once

#include "beadpoint.h"

#include <algorithm>

namespace jbead {

/*  Immutable rectangular region. Stores the raw (begin, end) pair as
    given; the begin()/end() accessors normalize to (min, max). The
    sentinel BeadRect::empty() carries begin=(0,0), end=(-1,-1) — the
    invalid bounds short-circuit RectIterator into yielding nothing. */
class BeadRect
{
public:
    constexpr BeadRect() : m_begin(), m_end() {}
    constexpr BeadRect(BeadPoint begin, BeadPoint end) : m_begin(begin), m_end(end) {}

    static constexpr BeadRect empty() { return BeadRect(BeadPoint(0, 0), BeadPoint(-1, -1)); }

    BeadPoint begin() const
    {
        return BeadPoint(std::min(m_begin.x(), m_end.x()),
                         std::min(m_begin.y(), m_end.y()));
    }
    BeadPoint end() const
    {
        return BeadPoint(std::max(m_begin.x(), m_end.x()),
                         std::max(m_begin.y(), m_end.y()));
    }

    BeadPoint rawBegin() const { return m_begin; }
    BeadPoint rawEnd()   const { return m_end; }

    int left()   const { return begin().x(); }
    int right()  const { return end().x(); }
    int bottom() const { return begin().y(); }
    int top()    const { return end().y(); }

    int width()  const { return right() - left() + 1; }
    int height() const { return top() - bottom() + 1; }
    int size()   const { return width() * height(); }

    bool isSquare() const
    {
        return std::abs(m_end.x() - m_begin.x()) == std::abs(m_end.y() - m_begin.y());
    }
    bool isColumn() const { return m_begin.x() == m_end.x(); }
    bool isRow()    const { return m_begin.y() == m_end.y(); }

    BeadRect scrolled(int scroll) const
    {
        return BeadRect(m_begin.scrolled(scroll), m_end.scrolled(scroll));
    }

private:
    BeadPoint m_begin;
    BeadPoint m_end;
};

} // namespace jbead
