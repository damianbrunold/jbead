#pragma once

#include "beadrun.h"

#include <QList>

namespace jbead {

class Model;

/*  Run-length encoding of the current repeat unit, scanned bottom-
    to-top (legacy iterates index repeat-1 down to 0). Empty if
    Model::repeat() is zero. Phase 3 BeadListWidget consumes this
    plus the redesigned counts panel from textile.                */
class BeadList
{
public:
    explicit BeadList(const Model& model);

    int  size() const { return m_runs.size(); }
    BeadRun at(int idx) const { return m_runs.at(idx); }
    const QList<BeadRun>& runs() const { return m_runs; }

private:
    QList<BeadRun> m_runs;
};

} // namespace jbead
