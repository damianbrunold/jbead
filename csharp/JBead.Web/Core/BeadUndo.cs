namespace JBead.Web.Core;

public class BeadUndo
{
    private const int MaxUndo = 100;

    private readonly ModelSnapshot[] snaps = new ModelSnapshot[MaxUndo];
    private int first;
    private int last;
    private int current;

    public BeadUndo()
    {
        for (int i = 0; i < MaxUndo; i++) {
			snaps[i] = new ModelSnapshot();
		}
	}

    public bool CanUndo => current != first;
    public bool CanRedo => current != last;

    public void Clear()
    {
        first = 0;
        last = 0;
        current = 0;
    }

    public void Snapshot(BeadField field, IReadOnlyList<Bead> beads, byte selectedColor, bool modified)
    {
        snaps[current].Store(field, beads, selectedColor, modified);
        current = (current + 1) % MaxUndo;
        if (current == first) {
			first = (first + 1) % MaxUndo;
		}
		last = current;
    }

    public void PrepareSnapshot(BeadField field, IReadOnlyList<Bead> beads, byte selectedColor, bool modified)
    {
        if (!modified) {
			return;
		}
		snaps[current].Store(field, beads, selectedColor, modified);
    }

    public ModelSnapshot? Undo(BeadField liveField, IReadOnlyList<Bead> liveBeads, byte liveSelected, bool liveModified)
    {
        if (current == first) {
			return null;
		}
		snaps[current].Store(liveField, liveBeads, liveSelected, liveModified);
        current = (current - 1 + MaxUndo) % MaxUndo;
        return snaps[current];
    }

    public ModelSnapshot? Redo(BeadField liveField, IReadOnlyList<Bead> liveBeads, byte liveSelected, bool liveModified)
    {
        if (current == last) {
			return null;
		}
		current = (current + 1) % MaxUndo;
        return snaps[current];
    }
}
