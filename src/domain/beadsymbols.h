#pragma once

#include <QString>

namespace jbead {

/*  Per-color symbol glyphs. Indexed by palette position; index 0
    renders as middle-dot. Identical encoding to the legacy
    BeadSymbols.SAVED_SYMBOLS string ("·abcdefg…+-/\\*").
    Persisted in the .jbb view block under "view/symbols".         */
class BeadSymbols
{
public:
    static const QString MIDDLE_DOT;
    static const QString DEFAULT_SYMBOLS;

    /*  Currently active glyph string. The view layer reads this; the
        memento layer writes/reads it through symbols() / setSymbols(). */
    static QString symbols();
    static void    setSymbols(const QString& symbols);
    static void    restoreDefaults();

    /*  Returns the single-character glyph for `index`. Returns " "
        when `index` exceeds the symbol string length (parity with
        legacy BeadSymbols.get).                                   */
    static QString glyph(int index);
};

} // namespace jbead
