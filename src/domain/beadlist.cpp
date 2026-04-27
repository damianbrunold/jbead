#include "beadlist.h"

#include "model.h"

namespace jbead {

BeadList::BeadList(const Model& model)
{
    const int rep = model.repeat();
    if (rep <= 0) return;
    std::int8_t color = model.get(rep - 1);
    int count = 1;
    for (int i = rep - 2; i >= 0; --i) {
        if (model.get(i) == color) {
            ++count;
        } else {
            m_runs.append(BeadRun(color, count));
            color = model.get(i);
            count = 1;
        }
    }
    m_runs.append(BeadRun(color, count));
}

} // namespace jbead
