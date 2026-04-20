namespace JBead.Web.FileFormat;

public sealed class Parser
{
    private readonly IEnumerator<object> iter;

    public Parser(Tokens tokens) { iter = tokens.GetEnumerator(); }

    public Node Parse()
    {
        if (!iter.MoveNext()) {
			throw new JBeadFileFormatException("Empty input");
		}
		return ParseNode(iter.Current);
    }

    private Node ParseNode(object token)
    {
        Match("(", token);
        string name = (string)Next();
        object t = Next();
        if (t.Equals(")")) {
			return new Node(name);
		}
		if (t.Equals("("))
        {
            var node = new Node(name);
            while (!t.Equals(")"))
            {
                node.Add(ParseNode(t));
                t = Next();
            }
            return node;
        }
        var leaf = new Leaf(name);
        while (!t.Equals(")"))
        {
            leaf.AddValue(t);
            t = Next();
        }
        return leaf;
    }

    private object Next()
    {
        if (!iter.MoveNext()) {
			throw new JBeadFileFormatException("Syntax error, unexpected end of file");
		}
		return iter.Current;
    }

    private static void Match(string expected, object actual)
    {
        if (actual?.ToString() != expected) {
			throw new JBeadFileFormatException($"Syntax error, expected {expected} but got {actual}");
		}
	}
}
