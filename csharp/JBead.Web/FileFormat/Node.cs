using System.Text;

namespace JBead.Web.FileFormat;

public class Node
{
    public const string Indentation = "    ";

    protected readonly string name;
    private readonly List<Node> children = new();

    public Node(string name) { this.name = name; }

    public string Name => name;
    public virtual int Size => children.Count;
    public IReadOnlyList<Node> Children => children;

    public Leaf AsLeaf()
    {
        if (this is Leaf leaf) return leaf;
        throw new JBeadFileFormatException("Expected leaf but got node " + name);
    }

    public Node Add(Node node)
    {
        children.Add(node);
        return node;
    }

    public Node GetOrAdd(string nodeName)
    {
        foreach (var child in children)
        {
            if (child.Name == nodeName) return child;
        }
        var added = new Node(nodeName);
        children.Add(added);
        return added;
    }

    public Node? Get(string nodeName)
    {
        foreach (var child in children)
        {
            if (child.Name == nodeName) return child;
        }
        return null;
    }

    public List<Node> GetAll(string nodeName)
    {
        var result = new List<Node>();
        foreach (var child in children)
        {
            if (child.Name == nodeName) result.Add(child);
        }
        return result;
    }

    public virtual string Format(string indent)
    {
        var sb = new StringBuilder();
        sb.Append(indent).Append('(').Append(name).Append('\n');
        foreach (var child in children) sb.Append(child.Format(indent + Indentation));
        if (sb.Length > 0 && sb[^1] == '\n') sb.Length--;
        sb.Append(")\n");
        return sb.ToString();
    }

    public override string ToString()
    {
        var sb = new StringBuilder();
        sb.Append('(').Append(name);
        foreach (var child in children) sb.Append(' ').Append(child);
        sb.Append(')');
        return sb.ToString();
    }
}
