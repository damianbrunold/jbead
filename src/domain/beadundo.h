#pragma once

#include "beadfield.h"

#include <QColor>
#include <QList>
#include <QString>

#include <array>

namespace jbead {

/*  Ring-buffer undo stack of pattern snapshots. Capacity is fixed at
    100 (legacy MAXUNDO); writes past capacity overwrite the oldest
    snapshot. Direct port of legacy BeadUndo — three indices first /
    last / current cycle modulo MAXUNDO.

    Note: this is *not* a QUndoStack. The legacy app stores whole
    BeadField copies per snapshot rather than per-operation
    QUndoCommand entries. Phase 3 may revisit this once the editing
    operations exist as commands; for now we mirror legacy.        */
class BeadUndo
{
public:
    static constexpr int MAX_UNDO = 100;

    BeadUndo();

    bool canUndo() const { return m_current != m_first; }
    bool canRedo() const { return m_current != m_last; }
    bool isModified() const { return m_modified[m_current]; }

    void clear();
    void snapshot(const BeadField& data, const QList<QColor>& colors,
                  const QString& symbols, bool modified);
    void prepareSnapshot(const BeadField& data, const QList<QColor>& colors,
                         const QString& symbols, bool modified);
    void undo(BeadField& data, QList<QColor>& colors, QString& symbols);
    void redo(BeadField& data, QList<QColor>& colors, QString& symbols);

private:
    std::array<BeadField,      MAX_UNDO> m_data;
    std::array<QList<QColor>,  MAX_UNDO> m_colors;
    std::array<QString,        MAX_UNDO> m_symbols;
    std::array<bool,           MAX_UNDO> m_modified{};
    int m_first   = 0;
    int m_last    = 0;
    int m_current = 0;
};

} // namespace jbead
