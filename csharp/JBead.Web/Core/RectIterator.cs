using System.Collections;

namespace JBead.Web.Core;

public sealed class RectIterator : IEnumerator<Point>
{
    private readonly Point begin;
    private readonly Point end;
    private Point? next;
    private Point current;

    public RectIterator(Point begin, Point end)
    {
        this.begin = new Point(Math.Min(begin.X, end.X), Math.Min(begin.Y, end.Y));
        this.end = new Point(Math.Max(begin.X, end.X), Math.Max(begin.Y, end.Y));
        next = this.begin.X < 0 || this.end.Y < 0 ? null : this.begin;
    }

    public Point Current => current;
    object IEnumerator.Current => current;

    public bool MoveNext()
    {
        if (next is null) {
			return false;
		}
		current = next.Value;
        var advanced = current.NextRight();
        if (advanced.X > end.X) {
			advanced = new Point(begin.X, advanced.Y + 1);
		}
		next = advanced.Y > end.Y ? null : advanced;
        return true;
    }

    public void Reset()
    {
        next = begin.X < 0 || end.Y < 0 ? null : begin;
    }

    public void Dispose() { }
}
