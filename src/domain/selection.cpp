#include "selection.h"

namespace jbead {

Selection::Selection(QObject* parent)
    : QObject(parent), m_begin(0, 0), m_end(0, 0)
{
}

BeadPoint Selection::lineDestination() const
{
    int x = m_end.x();
    int y = m_end.y();
    const int ax = std::abs(deltaX());
    const int ay = std::abs(deltaY());
    if (ax == 0 || ay == 0) return m_end;
    if (ax > ay) {
        x = m_begin.x() + ay * dx();
    } else {
        y = m_begin.y() + ax * dy();
    }
    return BeadPoint(x, y);
}

void Selection::init(BeadPoint origin)
{
    const BeadRect before = rect();
    const bool wasActive = m_active;
    m_begin = m_end = origin;
    m_active = false;
    emit selectionUpdated(before, rect(), wasActive, m_active);
}

void Selection::update(BeadPoint end)
{
    const BeadRect before = rect();
    const bool wasActive = m_active;
    m_end = end;
    m_active = !(m_begin == m_end);
    emit selectionUpdated(before, rect(), wasActive, m_active);
}

void Selection::clear()
{
    if (!m_active) return;
    const BeadRect snap = rect();
    m_active = false;
    emit selectionDeleted(snap);
}

} // namespace jbead
