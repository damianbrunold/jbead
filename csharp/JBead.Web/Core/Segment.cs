using System.Collections;

namespace JBead.Web.Core;

public sealed class Segment : IEnumerable<Point>
{
    private readonly Point begin;
    private readonly Point end;

    public Segment(Point begin, Point end)
    {
        this.begin = begin;
        this.end = end;
    }

    public IEnumerator<Point> GetEnumerator() => new SegmentIterator(begin, end);
    IEnumerator IEnumerable.GetEnumerator() => GetEnumerator();
    public override string ToString() => $"{begin}->{end}";
}

internal sealed class SegmentIterator : IEnumerator<Point>
{
    private readonly Point begin;
    private readonly Point end;
    private readonly int dx;
    private readonly int dy;
    private readonly int sx;
    private readonly int sy;
    private Point? next;
    private Point current;

    public SegmentIterator(Point begin, Point end)
    {
        this.begin = begin;
        this.end = end;
        next = begin;
        dx = end.X - begin.X;
        dy = end.Y - begin.Y;
        sx = dx > 0 ? 1 : -1;
        sy = dy > 0 ? 1 : -1;
    }

    public Point Current => current;
    object IEnumerator.Current => current;

    public bool MoveNext()
    {
        if (next is null) return false;
        current = next.Value;
        if (current.Equals(end))
        {
            next = null;
        }
        else if (dx == 0)
        {
            next = new Point(current.X, current.Y + sy);
        }
        else if (dy == 0)
        {
            next = new Point(current.X + sx, current.Y);
        }
        else if (Math.Abs(dx) > Math.Abs(dy))
        {
            next = new Point(current.X + sx, begin.Y + Math.Abs(current.X + sx - begin.X) * dy / dx);
        }
        else if (Math.Abs(dx) < Math.Abs(dy))
        {
            next = new Point(begin.X + Math.Abs(current.Y + sy - begin.Y) * dx / dy, current.Y + sy);
        }
        else
        {
            next = new Point(current.X + sx, current.Y + sy);
        }
        return true;
    }

    public void Reset() => next = begin;
    public void Dispose() { }
}
