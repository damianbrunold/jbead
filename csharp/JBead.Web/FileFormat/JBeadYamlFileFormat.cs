using System.Drawing;
using System.Text;
using JBead.Web.Core;

namespace JBead.Web.FileFormat;

/// YAML-flavoured JBead pattern file. Uses the same "only used beads" compaction
/// as the S-expression format. Round-trips to .jbb → .yaml → .jbb without loss.
public static class JBeadYamlFileFormat
{
    public const string Extension = ".jbby";
    public const int Version = 3;

    public static byte[] Save(BeadModel model)
    {
        var w = model.Width;
        var h = model.Height;
        var raw = model.Field.RawData;

        // Collect used indices and build the remap (same logic as JBeadFileFormat).
        var used = new SortedSet<byte> { 0, model.SelectedColor };
        for (var i = 0; i < w * h; i++) used.Add(raw[i]);
        var map = new byte[256];
        byte next = 0;
        foreach (var old in used)
        {
            if (old < model.ColorCount) { map[old] = next; next++; }
        }

        var dto = new PatternDto
        {
            Version = Version,
            Author = model.Author,
            Organization = model.Organization,
            Notes = model.Notes,
            Beads = used
                .Where(old => old < model.ColorCount)
                .Select(old =>
                {
                    var b = model.GetBead(old);
                    return new BeadDto
                    {
                        Color = b.Color,
                        Manufacturer = b.Manufacturer,
                        Id = b.Id,
                        Finish = b.Finish,
                        CatalogSource = b.CatalogSource,
                    };
                })
                .ToList(),
            GridSize = model.GridX,
            StringColor = model.StringColor,
            Size = new SizeDto { Rows = h, Columns = w },
            Rows = BuildRows(raw, w, h, map),
        };

        var yaml = Yaml.Serialize(dto);
        return Encoding.UTF8.GetBytes(yaml);
    }

    public static void Load(BeadModel model, byte[] data)
    {
        var text = Encoding.UTF8.GetString(data);
        var dto = Yaml.Deserialize<PatternDto>(text);
        if (dto is null) return;

        model.Clear();
        model.Author = dto.Author;
        model.Organization = dto.Organization;
        model.Notes = dto.Notes;

        var beads = dto.Beads.Select(b =>
            new Bead(b.Color, b.Manufacturer, b.Id, b.Finish, b.CatalogSource)).ToList();
        if (beads.Count > 0) model.ReplaceBeadsInternal(beads);

        if (dto.GridSize > 0) model.ApplyLoadedGridSize(dto.GridSize);
        if (!dto.StringColor.IsEmpty) model.StringColor = dto.StringColor;

        // Prefer the explicit `size: { rows, columns }` header when present;
        // otherwise infer dimensions from the row data shape.
        int width, height;
        if (dto.Size is { Rows: > 0, Columns: > 0 })
        {
            height = dto.Size.Rows;
            width = dto.Size.Columns;
        }
        else if (dto.Rows.Count > 0)
        {
            height = dto.Rows.Count;
            width = dto.Rows[0].Count;
        }
        else
        {
            model.IsModified = false; model.IsSaved = true; return;
        }

        var raw = new byte[width * height];
        for (var j = 0; j < dto.Rows.Count && j < height; j++)
        {
            var row = dto.Rows[j];
            for (var i = 0; i < width && i < row.Count; i++) raw[j * width + i] = (byte)row[i];
        }
        model.Field.LoadRawData(width, height, raw);
        var usedH = model.GetUsedHeight();
        var target = Math.Max(usedH + 20, 50);
        if (target < height) model.SetHeight(target);
        model.IsModified = false;
        model.IsSaved = true;
    }

    private static List<List<int>> BuildRows(byte[] raw, int w, int h, byte[] map)
    {
        var rows = new List<List<int>>(h);
        for (var j = 0; j < h; j++)
        {
            var row = new List<int>(w);
            for (var i = 0; i < w; i++) row.Add(map[raw[j * w + i]]);
            rows.Add(row);
        }
        return rows;
    }

    // ---- DTO + converters ----

    public sealed class PatternDto
    {
        public int Version { get; set; } = 3;
        [YamlIgnoreEmpty]
        public string Author { get; set; } = "";
        [YamlIgnoreEmpty]
        public string Organization { get; set; } = "";
        [YamlIgnoreEmpty]
        public string Notes { get; set; } = "";
        [YamlFormat(YamlFormat.Json | YamlFormat.Spaced)]
        public SizeDto Size { get; set; } = new();
        public int GridSize { get; set; }
        [YamlConverter(typeof(ColorHexConverter))]
        public Color StringColor { get; set; } = Color.White;
        public List<BeadDto> Beads { get; set; } = new();
        public List<List<int>> Rows { get; set; } = new();
    }

    public sealed class SizeDto
    {
        public int Rows { get; set; }
        public int Columns { get; set; }
    }

    public sealed class BeadDto
    {
        [YamlConverter(typeof(ColorHexConverter))]
        public Color Color { get; set; } = Color.White;
        public string Manufacturer { get; set; } = "";
        public string Id { get; set; } = "";
        public BeadFinish Finish { get; set; } = BeadFinish.Opaque;
        public string CatalogSource { get; set; } = "";
    }

    /// Emits #RRGGBB and reads the same. Keeps files human-readable.
    public sealed class ColorHexConverter : YamlConverter<Color>
    {
        public override Color Read(object? node)
        {
            if (node is not string s || s.Length == 0) return Color.Black;
            if (s[0] == '#') s = s.Substring(1);
            if (s.Length != 6 && s.Length != 8) return Color.Black;
            var r = Convert.ToByte(s.Substring(0, 2), 16);
            var g = Convert.ToByte(s.Substring(2, 2), 16);
            var b = Convert.ToByte(s.Substring(4, 2), 16);
            var a = s.Length == 8 ? Convert.ToByte(s.Substring(6, 2), 16) : (byte)255;
            return Color.FromArgb(a, r, g, b);
        }

        public override object? Write(Color value)
        {
            return value.A == 255
                ? $"#{value.R:X2}{value.G:X2}{value.B:X2}"
                : $"#{value.R:X2}{value.G:X2}{value.B:X2}{value.A:X2}";
        }
    }
}
