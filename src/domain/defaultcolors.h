#pragma once

#include <QColor>
#include <QList>

namespace jbead {

/*  The 32-color startup palette. Index 0 is white (the field
    background); index 31 is black. Verbatim port of legacy
    DefaultColors.COLORS — the .jbb file format and existing pattern
    files reference these indices directly, so the order is part of
    the file format contract and must not be reshuffled.           */
class DefaultColors
{
public:
    static constexpr int NUMBER_OF_COLORS = 32;

    static QList<QColor> palette();
};

} // namespace jbead
