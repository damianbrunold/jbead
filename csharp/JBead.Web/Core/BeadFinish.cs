namespace JBead.Web.Core;

/// Surface finishes common across real bead catalogs (Miyuki, Toho, Preciosa etc).
/// Drives how BeadPoint renders the cell — transparency, highlights, sheen, lining.
///
/// Integer values are persisted in .jbb files, so do not reorder or remove entries;
/// append new ones at the end.
public enum BeadFinish
{
    Opaque = 0,
    Transparent = 1,
    Matte = 2,
    Shiny = 3,
    Pearl = 4,
    SilverLined = 5,
    Metallic = 6,
    TransparentGloss = 7,
    TransparentMatte = 8,
    Iridescent = 9,        // AB / aurora borealis
    Galvanized = 10,
}
