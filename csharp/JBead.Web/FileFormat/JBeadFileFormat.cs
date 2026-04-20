using System.Text;
using JBead.Web.Core;
using System.Drawing;

namespace JBead.Web.FileFormat;

/// Ports ch.jbead.fileformat.JBeadMemento: reads and writes the .jbb S-expression format.
public static class JBeadFileFormat
{
    public const string Extension = ".jbb";
    // v4 switches the bead Finish field from a single enum ordinal to a raw
    // BeadFinish bitmask. Older files are detected via the version field and
    // migrated on load through BeadFinishConverter.FromLegacyInt.
    public const int Version = 4;

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
        byte[] raw = model.Field.RawData;
        int w = model.Width;
        int h = model.Height;
        int size = w * h;
        for (int i = 0; i < size; i++) {
			used.Add(raw[i]);
		}

		// Map old palette indices → new compacted indices.
        byte[] map = new byte[256];
        byte newIdx = 0;
        foreach (byte old in used)
        {
            if (old < model.ColorCount) { map[old] = newIdx; newIdx++; }
        }

        // Write only the kept beads. colors/rgb always goes out (older readers only
        // know that form); beads/bead is appended only when at least one bead
        // carries authored metadata, so plain-colour patterns don't gain an empty
        // definitions block on every save.
        bool anyAuthored = false;
        foreach (byte old in used)
        {
            if (old >= model.ColorCount) {
				continue;
			}
			var b = model.GetBead(old);
            if (!string.IsNullOrEmpty(b.Manufacturer) ||
                !string.IsNullOrEmpty(b.Id) ||
                b.Finish != BeadFinish.Opaque ||
                !string.IsNullOrEmpty(b.CatalogSource))
            {
                anyAuthored = true;
                break;
            }
        }

        foreach (byte old in used)
        {
            if (old >= model.ColorCount) {
				continue;
			}
			var b = model.GetBead(old);
            var c = b.Color;
            om.Add("colors/rgb", (int)c.R, (int)c.G, (int)c.B, (int)c.A);
            if (anyAuthored)
            {
                om.Add("beads/bead",
                    (int)c.R, (int)c.G, (int)c.B, (int)c.A,
                    b.Manufacturer, b.Id, (int)b.Finish,
                    b.CatalogSource);
            }
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
        for (int j = 0; j < h; j++)
        {
            object[] row = new object[w];
            for (int i = 0; i < w; i++) {
				row[i] = (int)map[raw[j * w + i]];
			}
			om.Add("model/row", row);
        }

        return Encoding.UTF8.GetBytes(om.ToString());
    }

    public static void Load(BeadModel model, byte[] data)
    {
        string text = Encoding.UTF8.GetString(data);
        var om = ObjectModel.FromData(text);

        model.Clear();

        model.Author = om.GetStringValue("author", "");
        model.Organization = om.GetStringValue("organization", "");
        model.Notes = om.GetStringValue("notes", "");

        // Version-gated finish decoding: < 4 stored single-ordinal values and
        // needs BeadFinishConverter.FromLegacyInt; >= 4 stores a raw bitmask.
        int fileVersion = om.GetIntValue("version", 1);

        var beads = new List<Bead>();
        var beadNodes = TryGetAll(om, "beads/bead");
        if (beadNodes.Count > 0)
        {
            foreach (var n in beadNodes)
            {
                var leaf = n.AsLeaf();
                byte r = (byte)leaf.GetIntValue(0);
                byte g = (byte)leaf.GetIntValue(1);
                byte b = (byte)leaf.GetIntValue(2);
                byte a = (byte)leaf.GetIntValue(3);
                if (a == 0) {
					a = 255;
				}
				string mfr = leaf.Size > 4 ? leaf.GetStringValue(4) : "";
                string id = leaf.Size > 5 ? leaf.GetStringValue(5) : "";
                BeadFinish finish;
                if (leaf.Size > 6)
                {
                    int rawFinish = leaf.GetIntValue(6);
                    finish = fileVersion < 4
                        ? BeadFinishConverter.FromLegacyInt(rawFinish)
                        : (BeadFinish)rawFinish;
                }
                else finish = BeadFinish.Opaque;
                // New optional trailing field: catalog source. Absence means "no catalog link".
                string catalogSource = leaf.Size > 7 ? leaf.GetStringValue(7) : "";
                beads.Add(new Bead(Color.FromArgb(a, r, g, b), mfr, id, finish, catalogSource));
            }
        }
        else
        {
            // Legacy format (bare colors, no metadata) — keep beads plain. Guessing
            // a manufacturer/id from a nearest-colour catalog match fabricates info
            // the author never chose and pollutes the print legend.
            foreach (var c in TryGetAll(om, "colors/rgb"))
            {
                var leaf = c.AsLeaf();
                byte r = (byte)leaf.GetIntValue(0);
                byte g = (byte)leaf.GetIntValue(1);
                byte b = (byte)leaf.GetIntValue(2);
                byte a = leaf.Size == 4 ? (byte)leaf.GetIntValue(3) : (byte)255;
                if (a == 0) {
					a = 255;
				}
				beads.Add(new Bead(Color.FromArgb(a, r, g, b)));
            }
        }
        if (beads.Count > 0) {
			model.ReplaceBeadsInternal(beads);
		}

		model.SelectedColor = (byte)om.GetIntValue("view/selected-color", 1);
        // Prefer the new free-form grid-size; fall back to the legacy zoom index.
        int gridSize = om.GetIntValue("view/grid-size", 0);
        if (gridSize > 0) {
			model.ApplyLoadedGridSize(gridSize);
		} else {
			model.ApplyLoadedZoomIndex(om.GetIntValue("view/zoom", 2));
		}
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
        if (rows.Count == 0) {
			return;
		}
		int height = rows.Count;
        int width = rows[0].Size;
        byte[] rawData = new byte[width * height];
        int idx = 0;
        foreach (var row in rows)
        {
            var leaf = row.AsLeaf();
            for (int i = 0; i < width; i++)
            {
                rawData[idx++] = (byte)leaf.GetIntValue(i);
            }
        }
        model.Field.LoadRawData(width, height, rawData);
        int usedHeight = model.GetUsedHeight();
        int target = Math.Max(usedHeight + 20, 50);
        if (target < height) {
			model.SetHeight(target);
		}
		model.IsModified = false;
        model.IsSaved = true;
    }

    private static List<Node> TryGetAll(ObjectModel om, string path)
    {
        try { return om.GetAll(path); }
        catch (JBeadFileFormatException) { return new List<Node>(); }
    }
}
