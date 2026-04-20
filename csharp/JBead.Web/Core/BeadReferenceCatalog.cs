using System.Drawing;

namespace JBead.Web.Core;

/// Static reference catalog of known beads, used during .jbb load to auto-fill
/// Manufacturer/Id when a file is bare-colors (legacy / Java-originated).
/// Extend Entries with real chart data (Miyuki Delica, Toho, Preciosa, etc).
public static class BeadReferenceCatalog
{
    public static readonly Bead[] Entries =
    {
        new(Color.FromArgb(255, 255, 255), "Miyuki", "11-0402"),
        new(Color.FromArgb(0, 0, 0),       "Miyuki", "11-0401"),
        new(Color.FromArgb(200, 40, 40),   "Miyuki", "11-0408"),
        new(Color.FromArgb(34, 110, 58),   "Miyuki", "11-0411"),
        new(Color.FromArgb(30, 60, 180),   "Miyuki", "11-0414"),
        new(Color.FromArgb(250, 220, 40),  "Miyuki", "11-0404"),
        new(Color.FromArgb(230, 140, 40),  "Miyuki", "11-0406"),
        new(Color.FromArgb(240, 170, 190), "Miyuki", "11-0415"),
        new(Color.FromArgb(120, 80, 160),  "Miyuki", "11-0417"),
        new(Color.FromArgb(160, 120, 80),  "Miyuki", "11-0409"),
        new(Color.FromArgb(150, 150, 150), "Miyuki", "11-0451"),
    };

    public static Bead? FindClosest(Color color, int tolerance = 2500)
    {
        Bead? best = null;
        var bestDist = int.MaxValue;
        foreach (var entry in Entries)
        {
            var dr = entry.Color.R - color.R;
            var dg = entry.Color.G - color.G;
            var db = entry.Color.B - color.B;
            var d = dr * dr + dg * dg + db * db;
            if (d < bestDist)
            {
                bestDist = d;
                best = entry;
            }
        }
        return bestDist <= tolerance ? best : null;
    }
}
