namespace JBead.Web.Core;

/// Surface finishes common across real bead catalogs (Miyuki, Toho, Preciosa etc).
/// Drives how BeadPoint renders the cell — transparency, highlights, sheen, lining.
///
/// [Flags] so real catalog entries that combine multiple treatments (e.g. a
/// transparent bead with a matte sheen) can be expressed as a bitmask rather
/// than enumerating every combination up front. Opaque = 0 means "no extra
/// treatment", i.e. a plain solid bead.
///
/// Integer values are persisted in .jbb files. When the file-format version is
/// &lt; 4 the int is a single legacy enum ordinal and must be translated through
/// BeadFinishConverter.FromLegacyInt. Version 4 and later stores the raw
/// bitmask directly.
[System.Flags]
public enum BeadFinish
{
    Opaque       = 0,
    Transparent  = 1 << 0, //  1
    Matte        = 1 << 1, //  2
    Shiny        = 1 << 2, //  4
    Pearl        = 1 << 3, //  8
    SilverLined  = 1 << 4, // 16
    Metallic     = 1 << 5, // 32
    Iridescent   = 1 << 6, // 64 (AB / aurora borealis)
    Galvanized   = 1 << 7, // 128
}
