#include "defaultcolors.h"

namespace jbead {

QList<QColor> DefaultColors::palette()
{
    return {
        QColor(255, 255, 255),  // 0 — background
        QColor(128,   0,   0),
        QColor(254,  15,  15),
        QColor(246,  40,   3),
        QColor(254,  76,  38),
        QColor(251, 139,  11),
        QColor(255, 231,  22),
        QColor(245, 249,   6),
        QColor(254, 254, 108),
        QColor(135,  11,  24),
        QColor(179,  94,   3),
        QColor( 42,  18, 156),
        QColor( 90,  42, 252),
        QColor( 64, 154, 230),
        QColor(104, 188, 251),
        QColor(105, 198, 177),
        QColor( 64, 172, 185),
        QColor(153, 206, 176),
        QColor( 76, 139,  86),
        QColor(  0, 176,  92),
        QColor( 63, 223,  29),
        QColor(142, 228, 119),
        QColor(223,  87, 187),
        QColor(255,  96, 212),
        QColor(200, 181, 255),
        QColor(176, 136, 160),
        QColor(226, 237, 239),
        QColor(219, 220, 222),
        QColor(143, 147, 159),
        QColor( 58,  70,  86),
        QColor( 38,  52,  55),
        QColor(  0,   0,   0),
    };
}

} // namespace jbead
