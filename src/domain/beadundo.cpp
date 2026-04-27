#include "beadundo.h"

namespace jbead {

BeadUndo::BeadUndo() = default;

void BeadUndo::clear()
{
    m_first = m_last = m_current = 0;
}

void BeadUndo::snapshot(const BeadField& data, bool modified)
{
    m_data[m_current].copyFrom(data);
    m_modified[m_current] = modified;
    m_current = (m_current + 1) % MAX_UNDO;
    if (m_current == m_first) m_first = (m_first + 1) % MAX_UNDO;
    m_last = m_current;
}

void BeadUndo::prepareSnapshot(const BeadField& data, bool modified)
{
    if (!modified) return;
    m_data[m_current].copyFrom(data);
    m_modified[m_current] = modified;
}

void BeadUndo::undo(BeadField& data)
{
    if (m_current == m_first) return;
    m_current = (m_current - 1 + MAX_UNDO) % MAX_UNDO;
    data.copyFrom(m_data[m_current]);
}

void BeadUndo::redo(BeadField& data)
{
    if (m_current == m_last) return;
    m_current = (m_current + 1) % MAX_UNDO;
    data.copyFrom(m_data[m_current]);
}

} // namespace jbead
