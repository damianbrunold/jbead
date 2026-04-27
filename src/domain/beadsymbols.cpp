#include "beadsymbols.h"

namespace jbead {

const QString BeadSymbols::MIDDLE_DOT      = QString::fromUtf8("·");
const QString BeadSymbols::DEFAULT_SYMBOLS = MIDDLE_DOT
    + QStringLiteral("abcdefghijklmnopqrstuvwxyz+-/\\*");

namespace { QString g_symbols = BeadSymbols::DEFAULT_SYMBOLS; }

QString BeadSymbols::symbols()                          { return g_symbols; }
void    BeadSymbols::setSymbols(const QString& symbols) { g_symbols = symbols; }
void    BeadSymbols::restoreDefaults()                  { g_symbols = DEFAULT_SYMBOLS; }

QString BeadSymbols::glyph(int index)
{
    if (index < 0 || index >= g_symbols.size()) return QStringLiteral(" ");
    return g_symbols.mid(index, 1);
}

} // namespace jbead
