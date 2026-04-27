#pragma once

#include <cstdint>

namespace jbead {

/*  Run-length tuple emitted by BeadList: a contiguous stretch of
    `count` cells of palette index `color` within the repeat unit. */
class BeadRun
{
public:
    constexpr BeadRun(std::int8_t color, int count) : m_color(color), m_count(count) {}

    constexpr std::int8_t color() const { return m_color; }
    constexpr int count() const { return m_count; }

    constexpr bool operator==(const BeadRun& o) const { return m_color == o.m_color && m_count == o.m_count; }

private:
    std::int8_t m_color;
    int m_count;
};

} // namespace jbead
