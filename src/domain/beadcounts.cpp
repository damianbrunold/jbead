#include "beadcounts.h"

#include "model.h"
#include "rectiterator.h"

namespace jbead {

BeadCounts::BeadCounts(const Model& model)
{
    for (std::int8_t color = 0; color < model.colorCount(); ++color) {
        m_counts.insert(color, 0);
    }
    RectIterator it(model.usedRect());
    while (it.hasNext()) {
        const std::int8_t c = model.get(it.next());
        m_counts[c] = m_counts.value(c, 0) + 1;
    }
}

int BeadCounts::colorCount() const
{
    int result = 0;
    for (auto v : m_counts) if (v > 0) ++result;
    return result;
}

} // namespace jbead
