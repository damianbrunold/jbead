namespace JBead.Web.Core;

public class Selection : Rect
{
    private bool active;
    private bool isSet;

    public event Action<Selection, Selection>? Updated;
    public event Action<Selection>? Deleted;

    public Selection() : base(new Point(0, 0), new Point(0, 0))
    {
        active = false;
    }

    public Selection(Selection source) : base(source.begin, source.end)
    {
        active = source.active;
        isSet = source.isSet;
    }

    public bool IsActive => active;

    /// True between Init() and ClearSelection(). Tools use this to gate preview
    /// rendering so no ghost preview shows at (0,0) on fresh load.
    public bool IsSet => isSet;

    public void ClearSelection()
    {
        if (!isSet && !active) {
			return;
		}
		var snap = Snapshot();
        active = false;
        isSet = false;
        Deleted?.Invoke(snap);
    }

    public void Init(Point origin)
    {
        var before = Snapshot();
        begin = end = origin;
        active = false;
        isSet = true;
        Updated?.Invoke(before, Snapshot());
    }

    public void Update(Point newEnd)
    {
        if (end.Equals(newEnd) && active == !begin.Equals(newEnd)) {
			return;
		}
		var before = Snapshot();
        end = newEnd;
        active = !begin.Equals(newEnd);
        Updated?.Invoke(before, Snapshot());
    }

    public Selection Snapshot() => new(this);

    public Point Origin => begin;
    public Point Destination => end;

    public bool IsNormal => IsActive && begin.X != end.X && begin.Y != end.Y;

    public Point LineDest
    {
        get
        {
            int x = end.X;
            int y = end.Y;
            int ax = Math.Abs(DeltaX);
            int ay = Math.Abs(DeltaY);
            if (ax == 0 || ay == 0) {
				return end;
			}
			if (ax > ay) {
				x = begin.X + ay * Dx;
			} else {
				y = begin.Y + ax * Dy;
			}
			return new Point(x, y);
        }
    }

    public int DeltaX => end.X - begin.X;
    public int DeltaY => end.Y - begin.Y;
    public int Dx => begin.X < end.X ? 1 : -1;
    public int Dy => begin.Y < end.Y ? 1 : -1;
}
