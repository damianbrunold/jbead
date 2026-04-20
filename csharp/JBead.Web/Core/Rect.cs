using System.Collections;

namespace JBead.Web.Core;

public class Rect : IEnumerable<Point>
{
    public static readonly Rect Empty = new(new Point(0, 0), new Point(-1, -1));

    protected Point begin;
    protected Point end;

    public Rect(Point begin, Point end)
    {
        this.begin = begin;
        this.end = end;
    }

    public IEnumerator<Point> GetEnumerator() => new RectIterator(begin, end);
    IEnumerator IEnumerable.GetEnumerator() => GetEnumerator();

    public override string ToString() => $"{begin}-{end}";

    public int Left => Begin.X;
    public int Right => End.X;
    public int Bottom => Begin.Y;
    public int Top => End.Y;
    public int Width => Right - Left + 1;
    public int Height => Top - Bottom + 1;

    public Point Begin => new(Math.Min(begin.X, end.X), Math.Min(begin.Y, end.Y));
    public Point End => new(Math.Max(begin.X, end.X), Math.Max(begin.Y, end.Y));

    public bool IsSquare => Math.Abs(end.X - begin.X) == Math.Abs(end.Y - begin.Y);
    public bool IsColumn => begin.X == end.X;
    public bool IsRow => begin.Y == end.Y;
    public int Size => Width * Height;

    public Rect Scrolled(int scroll) => new(begin.Scrolled(scroll), end.Scrolled(scroll));
}
