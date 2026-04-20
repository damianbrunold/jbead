using System.Drawing;

namespace JBead.Web.Core;

public static class DefaultColors
{
    public static readonly Color[] Palette =
    {
        Color.FromArgb(255, 255, 255), // background
        Color.FromArgb(128, 0, 0),
        Color.FromArgb(254, 15, 15),
        Color.FromArgb(246, 40, 3),
        Color.FromArgb(254, 76, 38),
        Color.FromArgb(251, 139, 11),
        Color.FromArgb(255, 231, 22),
        Color.FromArgb(245, 249, 6),
        Color.FromArgb(254, 254, 108),
        Color.FromArgb(135, 11, 24),
        Color.FromArgb(179, 94, 3),
        Color.FromArgb(42, 18, 156),
        Color.FromArgb(90, 42, 252),
        Color.FromArgb(64, 154, 230),
        Color.FromArgb(104, 188, 251),
        Color.FromArgb(105, 198, 177),
        Color.FromArgb(64, 172, 185),
        Color.FromArgb(153, 206, 176),
        Color.FromArgb(76, 139, 86),
        Color.FromArgb(0, 176, 92),
        Color.FromArgb(63, 223, 29),
        Color.FromArgb(142, 228, 119),
        Color.FromArgb(223, 87, 187),
        Color.FromArgb(255, 96, 212),
        Color.FromArgb(200, 181, 255),
        Color.FromArgb(176, 136, 160),
        Color.FromArgb(226, 237, 239),
        Color.FromArgb(219, 220, 222),
        Color.FromArgb(143, 147, 159),
        Color.FromArgb(58, 70, 86),
        Color.FromArgb(38, 52, 55),
        Color.FromArgb(0, 0, 0),
    };

    public const int NumberOfColors = 32;

    public static List<Color> CreateList() => new(Palette);

    /// Seed palette for a new project: background + one default paint bead so the
    /// initially-selected index (1) actually points at something. Users curate from
    /// there via "+" in the palette.
    public static List<Bead> CreateBeadList() => new()
    {
        new Bead(Palette[0], "", "background"),
        new Bead(Palette[^1], "", ""), // black at end of palette — obvious paint color
    };
}
