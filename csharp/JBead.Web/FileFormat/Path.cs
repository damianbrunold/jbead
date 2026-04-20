using System.Collections;

namespace JBead.Web.FileFormat;

internal sealed class PathRef : IEnumerable<string>
{
    private readonly List<string> parts;
    private readonly string leaf;

    public PathRef(string path)
    {
        parts = new List<string>(path.Split('/'));
        leaf = parts[^1];
    }

    public IReadOnlyList<string> Nodes => parts.GetRange(0, parts.Count - 1);
    public string Leaf => leaf;

    public IEnumerator<string> GetEnumerator() => parts.GetEnumerator();
    IEnumerator IEnumerable.GetEnumerator() => GetEnumerator();
}
