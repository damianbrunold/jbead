using System.Text;
using JBead.Web.Core;
using System.Drawing;

namespace JBead.Web.FileFormat;

/// Ports ch.jbead.fileformat.JBeadMemento: reads and writes the .jbb S-expression format.
public static class JBeadFileFormat
{
    public const string Extension = ".jbb";
    public const int Version = 3;

    public static byte[] Save(BeadModel model)
    {
        var om = new ObjectModel("jbb");
        om.Add("version", Version);
        om.Add("author", model.Author);
        om.Add("organization", model.Organization);
        om.Add("notes", model.Notes);

        // Collect the set of palette indices referenced by the pattern — we only
        // persist those (plus 0 / the currently selected slot). Saves space and
        // matches the "store only used beads" intent.
        var used = new SortedSet<byte> { 0, model.SelectedColor };
        var raw = model.Field.RawData;
        var w = model.Width;
        var h = model.Height;
        var size = w * h;
        for (var i = 0; i < size; i++) used.Add(raw[i]);

        // Map old palette indices → new compacted indices.
        var map = new byte[256];
        byte newIdx = 0;
        foreach (var old in used)
        {
            if (old < model.ColorCount) { map[old] = newIdx; newIdx++; }
        }

        // Write only the kept beads (under both entry names for back-compat).
        foreach (var old in used)
        {
            if (old >= model.ColorCount) continue;
            var b = model.GetBead(old);
            var c = b.Color;
            om.Add("colors/rgb", (int)c.R, (int)c.G, (int)c.B, (int)c.A);
            om.Add("beads/bead",
                (int)c.R, (int)c.G, (int)c.B, (int)c.A,
                b.Manufacturer, b.Id, (int)b.Finish,
                b.CatalogSource);
        }

        om.Add("view/draft-visible", true);
        om.Add("view/corrected-visible", true);
        om.Add("view/simulation-visible", true);
        om.Add("view/report-visible", true);
        om.Add("view/selected-tool", "pencil");
        om.Add("view/selected-color", (int)map[model.SelectedColor]);
        // New: grid size in pixels (any integer). view/zoom kept for legacy readers.
        om.Add("view/grid-size", model.GridX);
        om.Add("view/zoom", model.ZoomIndex);
        om.Add("view/string-color", (int)model.StringColor.R, (int)model.StringColor.G, (int)model.StringColor.B);
        om.Add("view/scroll", model.Scroll);
        om.Add("view/shift", model.Shift);
        om.Add("view/draw-colors", true);
        om.Add("view/draw-symbols", false);

        // Remapped cell data so surviving indices point at the compacted palette.
        for (var j = 0; j < h; j++)
        {
            var row = new object[w];
            for (var i = 0; i < w; i++) row[i] = (int)map[raw[j * w + i]];
            om.Add("model/row", row);
        }

        return Encoding.UTF8.GetBytes(om.ToString());
    }

    public static void Load(BeadModel model, byte[] data)
    {
        var text = Encoding.UTF8.GetString(data);
        var om = ObjectModel.FromData(text);

        model.Clear();

        model.Author = om.GetStringValue("author", "");
        model.Organization = om.GetStringValue("organization", "");
        model.Notes = om.GetStringValue("notes", "");

        var beads = new List<Bead>();
        var beadNodes = TryGetAll(om, "beads/bead");
        if (beadNodes.Count > 0)
        {
            foreach (var n in beadNodes)
            {
                var leaf = n.AsLeaf();
                var r = (byte)leaf.GetIntValue(0);
                var g = (byte)leaf.GetIntValue(1);
                var b = (byte)leaf.GetIntValue(2);
                var a = (byte)leaf.GetIntValue(3);
                if (a == 0) a = 255;
                var mfr = leaf.Size > 4 ? leaf.GetStringValue(4) : "";
                var id = leaf.Size > 5 ? leaf.GetStringValue(5) : "";
                var finish = leaf.Size > 6 ? (BeadFinish)leaf.GetIntValue(6) : BeadFinish.Opaque;
                // New optional trailing field: catalog source. Absence means "no catalog link".
                var catalogSource = leaf.Size > 7 ? leaf.GetStringValue(7) : "";
                beads.Add(new Bead(Color.FromArgb(a, r, g, b), mfr, id, finish, catalogSource));
            }
        }
        else
        {
            // Legacy format (bare colors, no metadata) — try to match each color
            // against the reference catalog so manufacturer/id get filled in automatically.
            foreach (var c in TryGetAll(om, "colors/rgb"))
            {
                var leaf = c.AsLeaf();
                var r = (byte)leaf.GetIntValue(0);
                var g = (byte)leaf.GetIntValue(1);
                var b = (byte)leaf.GetIntValue(2);
                var a = leaf.Size == 4 ? (byte)leaf.GetIntValue(3) : (byte)255;
                if (a == 0) a = 255;
                var color = Color.FromArgb(a, r, g, b);
                var match = BeadReferenceCatalog.FindClosest(color);
                beads.Add(match is null
                    ? new Bead(color)
                    : new Bead(color, match.Manufacturer, match.Id));
            }
        }
        if (beads.Count > 0) model.ReplaceBeadsInternal(beads);

        model.SelectedColor = (byte)om.GetIntValue("view/selected-color", 1);
        // Prefer the new free-form grid-size; fall back to the legacy zoom index.
        var gridSize = om.GetIntValue("view/grid-size", 0);
        if (gridSize > 0) model.ApplyLoadedGridSize(gridSize);
        else model.ApplyLoadedZoomIndex(om.GetIntValue("view/zoom", 2));
        model.ApplyScrollShift(om.GetIntValue("view/scroll", 0), om.GetIntValue("view/shift", 0));
        // Optional: string color for the Simulation beam. Defaults to white when absent.
        try
        {
            var beam = om.Get("view/string-color").AsLeaf();
            model.StringColor = Color.FromArgb(
                (byte)beam.GetIntValue(0), (byte)beam.GetIntValue(1), (byte)beam.GetIntValue(2));
        }
        catch (JBeadFileFormatException) { /* optional field */ }

        var rows = om.GetAll("model/row");
        if (rows.Count == 0) return;
        var height = rows.Count;
        var width = rows[0].Size;
        var rawData = new byte[width * height];
        var idx = 0;
        foreach (var row in rows)
        {
            var leaf = row.AsLeaf();
            for (var i = 0; i < width; i++)
            {
                rawData[idx++] = (byte)leaf.GetIntValue(i);
            }
        }
        model.Field.LoadRawData(width, height, rawData);
        var usedHeight = model.GetUsedHeight();
        var target = Math.Max(usedHeight + 20, 50);
        if (target < height) model.SetHeight(target);
        model.IsModified = false;
        model.IsSaved = true;
    }

    private static List<Node> TryGetAll(ObjectModel om, string path)
    {
        try { return om.GetAll(path); }
        catch (JBeadFileFormatException) { return new List<Node>(); }
    }
}
