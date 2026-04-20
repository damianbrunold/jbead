using System.Drawing;

namespace JBead.Web.Core;

/// Ported from ch.jbead.Model. Named BeadModel to avoid clash with Blazor's component model.
public class BeadModel
{
    private const int ZoomNormalIndex = 3;
    private static readonly int[] ZoomTable = { 6, 8, 10, 12, 14, 16, 18, 20 };

    private readonly BeadUndo undo = new();
    private readonly BeadField grid = new();
    private List<Bead> beads = DefaultColors.CreateBeadList();
    private byte colorIndex = 1;
    private int gridx;
    private int gridy;
    private int zoomIndex = ZoomNormalIndex;
    private int scroll;
    private int shift;
    private string fileName = "unnamed.jbb";
    private bool repeatDirty;
    private int repeat;
    private bool saved;
    private bool modified;
    private string author = "";
    private string organization = "";
    private string notes = "";
    private Color stringColor = Color.White;

    public event Action<Point>? PointChanged;
    public event Action? ModelChanged;
    public event Action<int>? ColorChanged;          // single bead edited (color or metadata)
    public event Action? ColorsChanged;              // bulk palette change (load / clear)
    public event Action<int>? BeadAdded;             // new bead appended at index
    public event Action<int>? BeadRemoved;           // bead removed from index
    public event Action<byte>? SelectedColorChanged;
    public event Action? StringColorChanged;
    public event Action<int>? ScrollChanged;
    public event Action<int>? ShiftChanged;
    public event Action<int, int>? ZoomChanged;
    public event Action<int>? RepeatChanged;

    public BeadModel()
    {
        gridx = gridy = ZoomTable[zoomIndex];
    }

    private void FireModelChanged() => ModelChanged?.Invoke();
    private void FirePointChanged(Point p) => PointChanged?.Invoke(p);

    // Color shims — kept so all existing callers (BeadPoint, palette, file format) still work.
    public Color GetColor(int index) => beads[index].Color;
    public int ColorCount => beads.Count;
    public IReadOnlyList<Bead> Beads => beads;

    public Bead GetBead(int index) => beads[index];

    public void SetColor(int index, Color color)
    {
        Snapshot();
        beads[index].Color = color;
        SetModified();
        ColorChanged?.Invoke(index);
    }

    public void UpdateBead(int index, string manufacturer, string id, BeadFinish finish, string catalogSource = "")
    {
        if (index < 0 || index >= beads.Count) return;
        var b = beads[index];
        if (b.Manufacturer == manufacturer && b.Id == id && b.Finish == finish && b.CatalogSource == catalogSource) return;
        Snapshot();
        b.Manufacturer = manufacturer;
        b.Id = id;
        b.Finish = finish;
        b.CatalogSource = catalogSource;
        SetModified();
        ColorChanged?.Invoke(index);
    }

    public int AddBead(Bead bead)
    {
        Snapshot();
        beads.Add(bead);
        SetModified();
        var idx = beads.Count - 1;
        BeadAdded?.Invoke(idx);
        return idx;
    }

    /// Remove a bead from the palette. Index 0 (background) can't be removed.
    /// Any grid cells pointing at `index` are reassigned to 0; cells pointing at a
    /// higher index get decremented so existing patterns stay visually consistent.
    public bool RemoveBeadAt(int index)
    {
        if (index <= 0 || index >= beads.Count) return false;
        Snapshot();
        for (var i = 0; i <= grid.LastIndex; i++)
        {
            var c = grid.Get(i);
            if (c == index) grid.Set(i, 0);
            else if (c > index) grid.Set(i, (byte)(c - 1));
        }
        beads.RemoveAt(index);
        if (colorIndex == index) { colorIndex = 0; SelectedColorChanged?.Invoke(0); }
        else if (colorIndex > index) { colorIndex = (byte)(colorIndex - 1); SelectedColorChanged?.Invoke(colorIndex); }
        SetModified();
        BeadRemoved?.Invoke(index);
        FireModelChanged();
        return true;
    }

    public byte SelectedColor
    {
        get => colorIndex;
        set
        {
            if (colorIndex == value) return;
            colorIndex = value;
            SelectedColorChanged?.Invoke(value);
        }
    }

    public int Width => grid.Width;
    public int Height => grid.Height;

    public void SetWidth(int w)
    {
        Snapshot();
        grid.SetWidth(w);
        NormalizeShift();
        SetModified();
        SetRepeatDirty();
        FireModelChanged();
    }

    public void SetHeight(int h)
    {
        // Shrinking truncates the back of the grid, so snapshot first for undo.
        Snapshot();
        grid.SetHeight(h);
        SetModified();
        SetRepeatDirty();
        FireModelChanged();
    }

    public byte Get(Point p) => grid.Get(p);
    public byte Get(int idx) => grid.Get(idx);
    public bool IsValidIndex(int idx) => grid.IsValidIndex(idx);

    public void Set(Point p, byte value)
    {
        grid.Set(p, value);
        FirePointChanged(p);
    }

    public void Set(int idx, byte value)
    {
        grid.Set(idx, value);
        FirePointChanged(grid.GetPoint(idx));
    }

    public void InsertRow()
    {
        Snapshot();
        grid.InsertRow();
        SetRepeatDirty();
        SetModified();
        FireModelChanged();
    }

    public void DeleteRow()
    {
        Snapshot();
        grid.DeleteRow();
        SetRepeatDirty();
        SetModified();
        FireModelChanged();
    }

    // Caller passes absolute field coordinates; DraftView no longer applies a scroll offset
    // because the viewport scrolls natively over the full-pattern SVG.
    public void DrawLine(Point begin, Point end)
    {
        Snapshot();
        SetModified();
        foreach (var p in new Segment(begin, end))
        {
            Set(p, colorIndex);
        }
        SetRepeatDirty();
    }

    public void FillLine(Point pt)
    {
        Snapshot();
        SetModified();
        var color = colorIndex;
        var background = grid.Get(pt);
        var start = grid.GetIndex(pt);
        for (var i = start; i >= 0; i--)
        {
            var p = grid.GetPoint(i);
            if (grid.Get(p) != background) break;
            Set(p, color);
        }
        var last = grid.GetIndex(new Point(Width - 1, GetUsedHeight() - 1));
        for (var i = start + 1; i <= last; i++)
        {
            var p = grid.GetPoint(i);
            if (grid.Get(p) != background) break;
            Set(p, color);
        }
        SetRepeatDirty();
    }

    /// Row-scoped bucket fill: at each y in [yMin..yMax], read the colour
    /// at column `originX`, then scan left and right replacing matching
    /// cells with the selected colour. Scanning stops at the first cell
    /// whose colour differs from that row's seed — so the fill is bounded
    /// by beads of a different colour. Rows where the origin column is
    /// already the selected colour are skipped. One undo snapshot covers
    /// the whole operation.
    public void FillLineBounded(int originX, int yMin, int yMax)
    {
        if (originX < 0 || originX >= Width) return;
        var lo = Math.Max(0, yMin);
        var hi = Math.Min(Height - 1, yMax);
        if (lo > hi) return;
        var color = colorIndex;
        Snapshot();
        SetModified();
        for (var y = lo; y <= hi; y++)
        {
            var seed = grid.Get(new Point(originX, y));
            if (seed == color) continue;
            for (var x = originX; x >= 0; x--)
            {
                if (grid.Get(new Point(x, y)) != seed) break;
                grid.Set(new Point(x, y), color);
            }
            for (var x = originX + 1; x < Width; x++)
            {
                if (grid.Get(new Point(x, y)) != seed) break;
                grid.Set(new Point(x, y), color);
            }
        }
        SetRepeatDirty();
        FireModelChanged();
    }

    /// Replace every cell whose colour matches the one at `seed` with the
    /// current selected colour. If `bounds` is given, replacement is limited
    /// to cells inside that rect; otherwise the whole grid is scanned.
    /// No-op if the seed already has the selected colour.
    public void ReplaceColor(Point seed, Rect? bounds = null)
    {
        if (seed.X < 0 || seed.X >= Width || seed.Y < 0 || seed.Y >= Height) return;
        var target = grid.Get(seed);
        var color = colorIndex;
        if (target == color) return;
        Snapshot();
        SetModified();
        if (bounds is null)
        {
            for (var i = 0; i <= grid.LastIndex; i++)
            {
                if (grid.Get(i) == target) Set(i, color);
            }
        }
        else
        {
            var l = Math.Max(0, bounds.Left);
            var r = Math.Min(Width - 1, bounds.Right);
            var b = Math.Max(0, bounds.Bottom);
            var t = Math.Min(Height - 1, bounds.Top);
            for (var y = b; y <= t; y++)
                for (var x = l; x <= r; x++)
                {
                    var p = new Point(x, y);
                    if (grid.Get(p) == target) Set(p, color);
                }
        }
        SetRepeatDirty();
    }

    /// Fill every cell inside rect with the current colour. Coordinates are
    /// clipped to the grid; an empty rect is a no-op.
    public void FillRect(Rect rect)
    {
        var l = Math.Max(0, rect.Left);
        var r = Math.Min(Width - 1, rect.Right);
        var b = Math.Max(0, rect.Bottom);
        var t = Math.Min(Height - 1, rect.Top);
        if (l > r || b > t) return;
        Snapshot();
        SetModified();
        var color = colorIndex;
        for (var y = b; y <= t; y++)
            for (var x = l; x <= r; x++) Set(new Point(x, y), color);
        SetRepeatDirty();
    }

    /// Snapshot the bytes inside rect into a [width,height] array. Used by Copy.
    public byte[,] CopyRect(Rect rect)
    {
        var l = Math.Max(0, rect.Left);
        var r = Math.Min(Width - 1, rect.Right);
        var b = Math.Max(0, rect.Bottom);
        var t = Math.Min(Height - 1, rect.Top);
        var w = Math.Max(0, r - l + 1);
        var h = Math.Max(0, t - b + 1);
        var data = new byte[w, h];
        for (var y = 0; y < h; y++)
            for (var x = 0; x < w; x++) data[x, y] = grid.Get(new Point(l + x, b + y));
        return data;
    }

    /// Paste source once, anchored at target's bottom-left corner. Empty
    /// cells in the source (colour index 0) are written through so the
    /// clipboard's shape is faithfully reproduced. Cells of the target rect
    /// beyond the source footprint are left untouched. Bulk-writes the grid
    /// directly and fires a single ModelChanged event — individual cell
    /// events would scale O(paste × cells) and lock up large grids.
    public void PasteRect(Rect target, byte[,] source)
    {
        var sw = source.GetLength(0);
        var sh = source.GetLength(1);
        if (sw == 0 || sh == 0) return;
        var l = Math.Max(0, target.Left);
        var b = Math.Max(0, target.Bottom);
        if (l >= Width || b >= Height) return;
        var r = Math.Min(Width - 1, l + sw - 1);
        var t = Math.Min(Height - 1, b + sh - 1);
        Snapshot();
        SetModified();
        for (var y = b; y <= t; y++)
            for (var x = l; x <= r; x++) grid.Set(new Point(x, y), source[x - l, y - b]);
        SetRepeatDirty();
        FireModelChanged();
    }

    public void SetPoint(Point pt)
    {
        Snapshot();
        SetModified();
        Set(pt, grid.Get(pt) == colorIndex ? (byte)0 : colorIndex);
        SetRepeatDirty();
    }

    public Rect FullRect => grid.FullRect;
    public Rect GetRect(int startY, int endY) => grid.GetRect(startY, endY);
    public Rect UsedRect
    {
        get
        {
            var h = GetUsedHeight();
            return h == 0 ? Rect.Empty : grid.GetRect(0, h - 1);
        }
    }

    public string FileName
    {
        get => fileName;
        set
        {
            fileName = value;
            FireModelChanged();
        }
    }

    public int Repeat => repeat;
    public int GridX => gridx;
    public int GridY => gridy;

    public int Scroll
    {
        get => scroll;
        set { scroll = value; ScrollChanged?.Invoke(value); }
    }

    public int Shift => shift;

    public void ShiftRight()
    {
        shift++;
        NormalizeShift();
        ShiftChanged?.Invoke(shift);
    }

    public void ShiftLeft()
    {
        shift--;
        NormalizeShift();
        ShiftChanged?.Invoke(shift);
    }

    public void NormalizeShift()
    {
        while (shift < 0) shift += Width;
        while (shift > Width) shift -= Width;
    }

    public void Clear()
    {
        undo.Clear();
        grid.Clear();
        repeat = 0;
        colorIndex = 1;
        beads = DefaultColors.CreateBeadList();
        // Reset to the default pixel size so grid display matches the stored zoom.
        gridx = gridy = DefaultGridSize;
        zoomIndex = ZoomNormalIndex;
        scroll = 0;
        shift = 0;
        fileName = "unnamed.jbb";
        saved = false;
        modified = false;
        stringColor = Color.White;
        FireModelChanged();
        StringColorChanged?.Invoke();
    }

    public bool IsSaved
    {
        get => saved;
        set => saved = value;
    }

    public bool IsModified
    {
        get => modified;
        set => modified = value;
    }

    public void SetModified() => modified = true;

    public void SetRepeatDirty() => repeatDirty = true;
    public bool IsRepeatDirty => repeatDirty;

    public void Snapshot() => undo.Snapshot(grid, beads, colorIndex, modified);
    public void PrepareSnapshot() => undo.PrepareSnapshot(grid, beads, colorIndex, modified);

    public void Undo()
    {
        var s = undo.Undo(grid, beads, colorIndex, modified);
        if (s is null) return;
        RestoreFrom(s);
    }

    public void Redo()
    {
        var s = undo.Redo(grid, beads, colorIndex, modified);
        if (s is null) return;
        RestoreFrom(s);
    }

    private void RestoreFrom(ModelSnapshot s)
    {
        grid.CopyFrom(s.Field);
        beads = s.Beads.Select(b => b.Clone()).ToList();
        modified = s.Modified;
        SetRepeatDirty();
        // Palette may have changed — notify before the grid so BeadPoints see the new colors.
        ColorsChanged?.Invoke();
        // Restore selection after palette updates so listeners see a valid index.
        if (colorIndex != s.SelectedColor)
        {
            colorIndex = s.SelectedColor;
            SelectedColorChanged?.Invoke(colorIndex);
        }
        FireModelChanged();
    }

    public bool CanUndo => undo.CanUndo;
    public bool CanRedo => undo.CanRedo;

    public int ZoomIndex
    {
        get => zoomIndex;
        internal set => ApplyLoadedZoomIndex(value);
    }

    public bool IsNormalZoom => gridx == DefaultGridSize;

    /// Grid-size bounds. The upper bound is large enough to count as "unlimited" for
    /// practical purposes but small enough to keep the SVG layer from getting absurd.
    public const int MinGridSize = 2;
    public const int MaxGridSize = 200;
    public const int DefaultGridSize = 12;
    private const int ZoomStep = 2;

    /// Primary zoom API — sets the pixels-per-cell directly. Clamped to
    /// [MinGridSize, MaxGridSize]. Use this from the Zoom… dialog.
    public void SetGridSize(int px)
    {
        px = Math.Clamp(px, MinGridSize, MaxGridSize);
        if (gridx == px && gridy == px) return;
        gridx = gridy = px;
        zoomIndex = SyntheticZoomIndex(px);
        ZoomChanged?.Invoke(gridx, gridy);
    }

    public void ZoomIn() => SetGridSize(gridx + ZoomStep);
    public void ZoomOut() => SetGridSize(gridx - ZoomStep);
    public void ZoomNormal() => SetGridSize(DefaultGridSize);

    /// Map a gridx value back to a legacy ZoomTable index so old-format .jbb files
    /// can still carry something useful in the `view/zoom` field.
    private static int SyntheticZoomIndex(int px)
    {
        for (var i = 0; i < ZoomTable.Length; i++)
            if (ZoomTable[i] == px) return i;
        // Not on the legacy table — clamp to nearest entry.
        if (px < ZoomTable[0]) return 0;
        if (px > ZoomTable[^1]) return ZoomTable.Length - 1;
        var best = 0;
        var bestD = int.MaxValue;
        for (var i = 0; i < ZoomTable.Length; i++)
        {
            var d = Math.Abs(ZoomTable[i] - px);
            if (d < bestD) { bestD = d; best = i; }
        }
        return best;
    }

    public void UpdateRepeat()
    {
        var h = GetUsedHeight();
        SetRepeat(h == 0 ? 0 : CalcRepeat(h));
    }

    private void SetRepeat(int value)
    {
        repeatDirty = false;
        repeat = value;
        RepeatChanged?.Invoke(value);
    }

    private int CalcRepeat(int usedHeight)
    {
        for (var i = 1; i < usedHeight * Width; i++)
        {
            if (grid.Get(i) == grid.Get(0))
            {
                var ok = true;
                for (var k = i + 1; k < usedHeight * Width; k++)
                {
                    if (grid.Get((k - i) % i) != grid.Get(k))
                    {
                        ok = false;
                        break;
                    }
                }
                if (ok) return i;
            }
        }
        return usedHeight * Width;
    }

    public int GetUsedHeight()
    {
        var usedHeight = 0;
        foreach (var p in grid.FullRect)
        {
            if (grid.Get(p) > 0) usedHeight = p.Y + 1;
        }
        return usedHeight;
    }

    public string Author
    {
        get => author;
        set { author = value; FireModelChanged(); }
    }

    public string Organization
    {
        get => organization;
        set { organization = value; FireModelChanged(); }
    }

    public string Notes
    {
        get => notes;
        set => notes = value;
    }

    /// Central string running through the rope axis. Shows through transparent
    /// beads in the Simulation. User-configurable; default white.
    public Color StringColor
    {
        get => stringColor;
        set
        {
            if (stringColor.ToArgb() == value.ToArgb()) return;
            stringColor = value;
            SetModified();
            StringColorChanged?.Invoke();
        }
    }

    public BeadField Field => grid;

    /// Returns distinct bead entries used in the pattern, merging palette slots that
    /// share the same (Manufacturer, Id) — since those are "the same real bead" even
    /// if the user defined them as separate slots. Index 0 (background) is skipped.
    /// Intended for the print/report view.
    public List<BeadUsage> GetMergedBeadUsage()
    {
        // Count cells per palette index first.
        var counts = new int[ColorCount];
        for (var i = 0; i <= grid.LastIndex; i++)
        {
            var c = grid.Get(i);
            if (c < counts.Length) counts[c]++;
        }

        var result = new List<BeadUsage>();
        var handled = new bool[ColorCount];
        for (var i = 1; i < ColorCount; i++) // skip background
        {
            if (handled[i] || counts[i] == 0) continue;
            var bead = beads[i];
            var usage = new BeadUsage
            {
                RepresentativeColor = bead.Color,
                Manufacturer = bead.Manufacturer,
                Id = bead.Id,
                Finish = bead.Finish,
                Count = counts[i],
                MergedIndices = new List<byte> { (byte)i },
            };
            handled[i] = true;

            // Merge siblings only if mfr+id are both set (otherwise "same color" is
            // coincidence, not a real identity).
            if (!string.IsNullOrEmpty(bead.Manufacturer) && !string.IsNullOrEmpty(bead.Id))
            {
                for (var j = i + 1; j < ColorCount; j++)
                {
                    if (handled[j] || counts[j] == 0) continue;
                    var other = beads[j];
                    if (other.Manufacturer == bead.Manufacturer && other.Id == bead.Id)
                    {
                        usage.Count += counts[j];
                        usage.MergedIndices.Add((byte)j);
                        handled[j] = true;
                    }
                }
            }
            result.Add(usage);
        }
        return result;
    }

    /// Threading order of the pattern as runs of consecutive same-colour beads.
    /// Mirrors ch.jbead.BeadList: walks from the last used cell down to index 0,
    /// so the first run is the last bead strung (i.e. the one at the top of the
    /// pattern). Returns an empty list if the pattern is empty.
    public List<(byte Color, int Count)> GetThreadingRuns()
    {
        var h = GetUsedHeight();
        if (h == 0) return new List<(byte, int)>();
        var last = h * Width - 1;
        var runs = new List<(byte, int)>();
        var color = grid.Get(last);
        var count = 1;
        for (var i = last - 1; i >= 0; i--)
        {
            var c = grid.Get(i);
            if (c == color)
            {
                count++;
            }
            else
            {
                runs.Add((color, count));
                color = c;
                count = 1;
            }
        }
        runs.Add((color, count));
        return runs;
    }

    internal void ReplaceBeadsInternal(List<Bead> newBeads)
    {
        beads = newBeads;
        if (beads.Count == 0) beads.Add(new Bead(Color.White));
        ColorsChanged?.Invoke();
    }

    internal void ApplyLoadedZoomIndex(int value)
    {
        // Legacy: file stores an index into the old ZoomTable. Map it to a pixel size.
        var idx = Math.Clamp(value, 0, ZoomTable.Length - 1);
        zoomIndex = idx;
        gridx = gridy = ZoomTable[idx];
        ZoomChanged?.Invoke(gridx, gridy);
    }

    /// Load path: file stored the gridx directly (new format). Preferred over
    /// ApplyLoadedZoomIndex when available because it supports arbitrary sizes.
    internal void ApplyLoadedGridSize(int px)
    {
        SetGridSize(px);
    }

    internal void ApplyScrollShift(int scrollValue, int shiftValue)
    {
        scroll = scrollValue;
        shift = shiftValue;
    }
}
