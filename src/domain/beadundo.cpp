#include "beadundo.h"

namespace jbead {

BeadUndo::BeadUndo() = default;

void BeadUndo::clear()
{
    m_first = m_last = m_current = 0;
}

void BeadUndo::snapshot(const BeadField& data, const QList<QColor>& colors,
                        const QString& symbols, bool modified)
{
    m_data[m_current].copyFrom(data);
    m_colors[m_current]   = colors;
    m_symbols[m_current]  = symbols;
    m_modified[m_current] = modified;
    m_current = (m_current + 1) % MAX_UNDO;
    if (m_current == m_first) m_first = (m_first + 1) % MAX_UNDO;
    m_last = m_current;
}

void BeadUndo::prepareSnapshot(const BeadField& data, const QList<QColor>& colors,
                               const QString& symbols, bool modified)
{
    if (!modified) return;
    m_data[m_current].copyFrom(data);
    m_colors[m_current]   = colors;
    m_symbols[m_current]  = symbols;
    m_modified[m_current] = modified;
}

void BeadUndo::undo(BeadField& data, QList<QColor>& colors, QString& symbols)
{
    if (m_current == m_first) return;
    m_current = (m_current - 1 + MAX_UNDO) % MAX_UNDO;
    data.copyFrom(m_data[m_current]);
    colors  = m_colors[m_current];
    symbols = m_symbols[m_current];
}

void BeadUndo::redo(BeadField& data, QList<QColor>& colors, QString& symbols)
{
    if (m_current == m_last) return;
    m_current = (m_current + 1) % MAX_UNDO;
    data.copyFrom(m_data[m_current]);
    colors  = m_colors[m_current];
    symbols = m_symbols[m_current];
}

} // namespace jbead
