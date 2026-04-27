#pragma once

#include <QHash>

#include <cstdint>

namespace jbead {

class Model;

/*  Histogram of palette indices over Model::usedRect(). Color 0
    (background) is included; getColorCount() returns only the
    colors with non-zero counts (matches legacy semantics).        */
class BeadCounts
{
public:
    explicit BeadCounts(const Model& model);

    int count(std::int8_t color) const { return m_counts.value(color, 0); }

    /*  Number of distinct colors that appear at least once.       */
    int colorCount() const;

private:
    QHash<std::int8_t, int> m_counts;
};

} // namespace jbead
