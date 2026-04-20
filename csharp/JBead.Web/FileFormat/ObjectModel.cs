namespace JBead.Web.FileFormat;

public class ObjectModel
{
    private readonly Node root;

    public ObjectModel(string rootName) { root = new Node(rootName); }
    private ObjectModel(Node root) { this.root = root; }

    public void Add(string pathStr, params object[] values)
    {
        var path = new PathRef(pathStr);
        var current = root;
        foreach (var node in path.Nodes) current = current.GetOrAdd(node);
        current.Add(new Leaf(path.Leaf, values));
    }

    public Node Get(string pathStr)
    {
        var path = new PathRef(pathStr);
        var current = root;
        foreach (var node in path)
        {
            current = current.Get(node) ?? throw new JBeadFileFormatException($"Path {pathStr} cannot be resolved, node {node} not found");
        }
        return current;
    }

    public List<Node> GetAll(string pathStr)
    {
        var path = new PathRef(pathStr);
        var current = root;
        foreach (var node in path.Nodes)
        {
            current = current.Get(node) ?? throw new JBeadFileFormatException($"Path {pathStr} cannot be resolved, node {node} not found");
        }
        return current.GetAll(path.Leaf);
    }

    public int GetIntValue(string path, int fallback)
    {
        try { return Get(path).AsLeaf().GetIntValue(); }
        catch (JBeadFileFormatException) { return fallback; }
    }

    public string GetStringValue(string path, string fallback)
    {
        try { return Get(path).AsLeaf().GetStringValue(); }
        catch (JBeadFileFormatException) { return fallback; }
    }

    public bool GetBoolValue(string path, bool fallback)
    {
        try { return Get(path).AsLeaf().GetBoolValue(); }
        catch (JBeadFileFormatException) { return fallback; }
    }

    public override string ToString() => root.Format("");

    public static ObjectModel FromData(string data)
    {
        var node = new Parser(new Tokens(data)).Parse();
        return new ObjectModel(node);
    }
}
