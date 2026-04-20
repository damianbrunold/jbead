namespace JBead.Web.Core;

public readonly record struct Point(int X, int Y)
{
    public Point Scrolled(int scroll) => new(X, Y + scroll);
    public Point Unscrolled(int scroll) => new(X, Y - scroll);
    public Point Shifted(int shift, int width) => new((X + shift) % width, Y + (X + shift) / width);
    public Point NextLeft() => new(X - 1, Y);
    public Point NextRight() => new(X + 1, Y);
    public Point NextBelow() => new(X, Y - 1);
    public Point NextAbove() => new(X, Y + 1);
    public Point LastLeft() => new(0, Y);
    public Point LastRight(int width) => new(width - 1, Y);
    public override string ToString() => $"{X},{Y}";
}
