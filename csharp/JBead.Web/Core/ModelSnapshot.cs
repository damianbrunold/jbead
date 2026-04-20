namespace JBead.Web.Core;

/// One undo/redo frame: the bead grid, the palette, selected bead index, and the
/// "modified" flag at that point in time. Beads are deep-cloned so later palette
/// edits don't mutate the snapshot in place.
public sealed class ModelSnapshot
{
    public BeadField Field { get; } = new();
    public List<Bead> Beads { get; private set; } = new();
    public byte SelectedColor { get; private set; }
    public bool Modified { get; private set; }

    public void Store(BeadField field, IReadOnlyList<Bead> beads, byte selectedColor, bool modified)
    {
        Field.CopyFrom(field);
        Beads = beads.Select(b => b.Clone()).ToList();
        SelectedColor = selectedColor;
        Modified = modified;
    }
}
