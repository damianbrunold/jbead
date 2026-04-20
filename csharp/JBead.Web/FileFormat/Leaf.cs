using System.Globalization;
using System.Text;

namespace JBead.Web.FileFormat;

public class Leaf : Node
{
    private readonly List<object> values = new();

    public Leaf(string name, params object[] vals) : base(name)
    {
        values.AddRange(vals);
    }

    public Leaf AddValue(object v)
    {
        values.Add(v);
        return this;
    }

    public override int Size => values.Count;

    public override string Format(string indent)
    {
        var sb = new StringBuilder();
        sb.Append(indent).Append('(').Append(name);
        foreach (object v in values) {
			sb.Append(' ').Append(FormatValue(v));
		}
		sb.Append(")\n");
        return sb.ToString();
    }

    private static string FormatValue(object v) => v switch
    {
        string s => "\"" + s.Replace("\\", "\\\\").Replace("\"", "\\\"") + "\"",
        bool b => b ? "true" : "false",
        DateTime d => d.ToString("yyyy-MM-dd'T'HH:mm:ss", CultureInfo.InvariantCulture),
        IFormattable f => f.ToString(null, CultureInfo.InvariantCulture),
        _ => v.ToString() ?? "",
    };

    public object GetValue() => GetValue(0);
    public int GetIntValue() => GetIntValue(0);
    public string GetStringValue() => GetStringValue(0);
    public bool GetBoolValue() => GetBoolValue(0);

    public object GetValue(int index) => values[index];

    public int GetIntValue(int index)
    {
        object v = values[index];
        return v switch
        {
            int i => i,
            long l => (int)l,
            _ => throw new JBeadFileFormatException($"Expected integer value but got {v}"),
        };
    }

    public string GetStringValue(int index)
    {
        if (values[index] is string s) {
			return s;
		}
		throw new JBeadFileFormatException($"Expected string value but got {values[index]}");
    }

    public bool GetBoolValue(int index)
    {
        if (values[index] is bool b) {
			return b;
		}
		throw new JBeadFileFormatException($"Expected boolean value but got {values[index]}");
    }

    public IReadOnlyList<object> Values => values;

    public override string ToString()
    {
        var sb = new StringBuilder();
        sb.Append('(').Append(name);
        foreach (object v in values) {
			sb.Append(' ').Append(v);
		}
		sb.Append(')');
        return sb.ToString();
    }
}
