namespace JBead.Web.FileFormat;

using System.Globalization;
using System.Text;

/// Small but general-purpose YAML reader/writer. Designed to be portable across
/// projects — it has no dependencies beyond the BCL and lives entirely in this
/// file. Intentionally covers the commonly-used subset of YAML:
///
///   - Block mappings (key: value, nested to any depth).
///   - Block sequences (- item, nested or mixed with mappings).
///   - Block mapping as sequence item (- key: value) with further keys indented
///     underneath ("compact" form).
///   - Flow mappings { a: b, c: d } and flow sequences [1, 2, 3], nestable.
///   - Quoted strings: "double" with \" \\ \n \t \r escapes, and 'single' (literal).
///   - Scalar types: int, double, bool (true/false), null (null, ~), string.
///   - Line comments starting with # (outside quotes).
///
/// Not covered (out of scope for the jbead use case, can be added later if needed):
/// anchors/aliases, tagged scalars, multi-line literal/folded scalars (|/>), document
/// separators (---), and merge keys (<<:).
public static class Yaml {
	// ----------------------------------------------------------------------
	// Public API
	// ----------------------------------------------------------------------

	/// Parse a YAML document. The root is returned as one of:
	/// Dictionary&lt;string, object?&gt; (mapping), List&lt;object?&gt; (sequence),
	/// or a scalar (string/int/double/bool/null). Returns null for empty input.
	public static object? Parse(string text) {
		string[] lines = (text ?? "").Replace("\r\n", "\n").Replace('\r', '\n').Split('\n');
		int pos = 0;
		SkipBlank(lines, ref pos);
		if (pos >= lines.Length) {
			return null;
		}
		return ParseNode(lines, ref pos, 0);
	}

	/// Convenience: parse a document expected to be a mapping at the root.
	public static Dictionary<string, object?> ParseMapping(string text)
		=> Parse(text) as Dictionary<string, object?> ?? new();

	/// Serialize an arbitrary value tree. Emits block style for mappings and
	/// sequences by default; uses flow style for leaf-ish lists (lists of primitive
	/// scalars) to keep numeric arrays compact — matches the "rows: [ ... ]" style.
	public static string Serialize(object? root) {
		var sb = new StringBuilder();
		WriteNode(sb, root, indent: 0, atLineStart: true);
		if (sb.Length > 0 && sb[^1] != '\n') {
			sb.Append('\n');
		}
		return sb.ToString();
	}

	/// Typed-object Serialize&lt;T&gt; — walks the object with reflection, respecting
	/// [YamlPropertyName], [YamlIgnore] and [YamlConverter], then emits YAML.
	public static string Serialize<T>(T value) => Serialize(YamlMapper.FromObject<T>(value));

	/// Typed-object Deserialize&lt;T&gt; — parses YAML, then reflects the tree onto
	/// a fresh T using the same attribute rules as Serialize&lt;T&gt;.
	public static T? Deserialize<T>(string text) => YamlMapper.ToObject<T>(Parse(text));

	// ----------------------------------------------------------------------
	// Reader
	// ----------------------------------------------------------------------

	private static object? ParseNode(string[] lines, ref int pos, int indent) {
		SkipBlank(lines, ref pos);
		if (pos >= lines.Length) {
			return null;
		}
		string line = StripComment(lines[pos]);
		int myIndent = IndentOf(line);
		if (myIndent < indent) {
			return null;
		}
		string content = line.Substring(myIndent);
		return content.StartsWith("-")
			? ParseBlockSequence(lines, ref pos, myIndent)
			: ParseBlockMapping(lines, ref pos, myIndent);
	}

	private static Dictionary<string, object?> ParseBlockMapping(string[] lines, ref int pos, int indent) {
		var map = new Dictionary<string, object?>();
		while (pos < lines.Length) {
			string line = StripComment(lines[pos]);
			if (string.IsNullOrWhiteSpace(line)) { pos++; continue; }

			int lineIndent = IndentOf(line);
			if (lineIndent < indent) {
				return map;
			}
			if (lineIndent > indent) { pos++; continue; } // stray deeper line; skip

			string content = line.Substring(lineIndent);
			if (content.StartsWith("-")) {
				return map; // sequence begins here — caller handles
			}

			int colon = FindTopLevelColon(content);
			if (colon < 0) { pos++; continue; }

			string key = UnquoteKey(content.Substring(0, colon));
			string rest = content.Substring(colon + 1).TrimStart();
			pos++;

			if (rest.Length > 0) {
				map[key] = ParseScalarOrFlow(rest);
				continue;
			}

			// Block child. Peek the next non-blank to decide mapping vs sequence.
			int look = pos;
			SkipBlank(lines, ref look);
			if (look >= lines.Length) { map[key] = null; continue; }

			string next = StripComment(lines[look]);
			int nextIndent = IndentOf(next);
			if (nextIndent <= indent) { map[key] = null; continue; }
			string nextContent = next.Substring(nextIndent);
			map[key] = nextContent.StartsWith("-")
				? ParseBlockSequence(lines, ref pos, nextIndent)
				: ParseBlockMapping(lines, ref pos, nextIndent);
		}
		return map;
	}

	private static List<object?> ParseBlockSequence(string[] lines, ref int pos, int indent) {
		var list = new List<object?>();
		while (pos < lines.Length) {
			string line = StripComment(lines[pos]);
			if (string.IsNullOrWhiteSpace(line)) { pos++; continue; }

			int lineIndent = IndentOf(line);
			if (lineIndent < indent) {
				return list;
			}
			string content = line.Substring(lineIndent);
			if (!content.StartsWith("-")) {
				return list;
			}

			string after = content.Substring(1);
			if (after.Length > 0 && after[0] == ' ') {
				after = after.Substring(1);
			} else if (after.Length == 0) {
				after = "";
			}

			if (after.Length == 0) {
				// "- " on its own: the value is the block node beginning on next line.
				pos++;
				object? child = ParseNode(lines, ref pos, indent + 2);
				list.Add(child);
				continue;
			}

			if (after.StartsWith("{")) {
				list.Add(ParseFlow(after));
				pos++;
				continue;
			}
			if (after.StartsWith("[")) {
				list.Add(ParseFlow(after));
				pos++;
				continue;
			}

			// "- key: value" — block mapping as sequence item. The item's own
			// indent is the column where `key` begins (lineIndent + 2 for "- ").
			if (FindTopLevelColon(after) >= 0) {
				int itemIndent = lineIndent + 2;
				// Rewrite current line so the first key is at `itemIndent` and keep
				// parsing additional keys at the same indent on subsequent lines.
				lines[pos] = new string(' ', itemIndent) + after;
				list.Add(ParseBlockMapping(lines, ref pos, itemIndent));
				continue;
			}

			// Plain scalar item.
			list.Add(ParseScalar(after));
			pos++;
		}
		return list;
	}

	private static object? ParseScalarOrFlow(string s) {
		s = s.Trim();
		if (s.StartsWith("{") || s.StartsWith("[")) {
			return ParseFlow(s);
		}
		return ParseScalar(s);
	}

	private static object? ParseFlow(string s) {
		int i = 0;
		object? value = ParseFlowValue(s, ref i);
		return value;
	}

	private static object? ParseFlowValue(string s, ref int i) {
		SkipSpaces(s, ref i);
		if (i >= s.Length) {
			return null;
		}
		char ch = s[i];
		return ch switch {
			'{' => ParseFlowMapping(s, ref i),
			'[' => ParseFlowSequence(s, ref i),
			_ => ParseFlowScalar(s, ref i),
		};
	}

	private static Dictionary<string, object?> ParseFlowMapping(string s, ref int i) {
		var map = new Dictionary<string, object?>();
		i++; // consume {
		SkipSpaces(s, ref i);
		while (i < s.Length && s[i] != '}') {
			string key = ReadFlowKey(s, ref i);
			SkipSpaces(s, ref i);
			if (i < s.Length && s[i] == ':') {
				i++;
			}
			SkipSpaces(s, ref i);
			object? value = ParseFlowValue(s, ref i);
			map[key] = value;
			SkipSpaces(s, ref i);
			if (i < s.Length && s[i] == ',') { i++; SkipSpaces(s, ref i); }
		}
		if (i < s.Length && s[i] == '}') {
			i++;
		}
		return map;
	}

	private static List<object?> ParseFlowSequence(string s, ref int i) {
		var list = new List<object?>();
		i++; // consume [
		SkipSpaces(s, ref i);
		while (i < s.Length && s[i] != ']') {
			list.Add(ParseFlowValue(s, ref i));
			SkipSpaces(s, ref i);
			if (i < s.Length && s[i] == ',') { i++; SkipSpaces(s, ref i); }
		}
		if (i < s.Length && s[i] == ']') {
			i++;
		}
		return list;
	}

	private static string ReadFlowKey(string s, ref int i) {
		SkipSpaces(s, ref i);
		if (i < s.Length && (s[i] == '"' || s[i] == '\'')) {
			return (string)ReadQuoted(s, ref i);
		}
		int start = i;
		while (i < s.Length && s[i] != ':' && s[i] != ',' && s[i] != '}' && s[i] != ']') {
			i++;
		}
		return s.Substring(start, i - start).Trim();
	}

	private static object? ParseFlowScalar(string s, ref int i) {
		SkipSpaces(s, ref i);
		if (i >= s.Length) {
			return "";
		}
		if (s[i] == '"' || s[i] == '\'') {
			return ReadQuoted(s, ref i);
		}
		int start = i;
		while (i < s.Length && s[i] != ',' && s[i] != '}' && s[i] != ']') {
			i++;
		}
		return ParseScalar(s.Substring(start, i - start).Trim());
	}

	private static object ReadQuoted(string s, ref int i) {
		char quote = s[i];
		bool literal = quote == '\'';
		i++;
		var sb = new StringBuilder();
		while (i < s.Length) {
			char ch = s[i];
			if (ch == quote) { i++; return sb.ToString(); }
			if (!literal && ch == '\\' && i + 1 < s.Length) {
				char nxt = s[i + 1];
				sb.Append(nxt switch { 'n' => '\n', 't' => '\t', 'r' => '\r', _ => nxt });
				i += 2;
				continue;
			}
			sb.Append(ch);
			i++;
		}
		return sb.ToString();
	}

	private static object? ParseScalar(string s) {
		s = s.Trim();
		if (s.Length == 0) {
			return "";
		}
		if (s.Length >= 2 && s[0] == '"' && s[^1] == '"') {
			int i = 0;
			return ReadQuoted(s, ref i);
		}
		if (s.Length >= 2 && s[0] == '\'' && s[^1] == '\'') {
			return s.Substring(1, s.Length - 2);
		}
		if (s == "null" || s == "~") {
			return null;
		}
		if (s == "true") {
			return true;
		}
		if (s == "false") {
			return false;
		}
		if (long.TryParse(s, NumberStyles.Integer, CultureInfo.InvariantCulture, out long iv)) {
			return iv <= int.MaxValue && iv >= int.MinValue ? (int)iv : iv;
		}
		if (double.TryParse(s, NumberStyles.Float, CultureInfo.InvariantCulture, out double dv)) {
			return dv;
		}
		return s;
	}

	private static string UnquoteKey(string s) {
		s = s.Trim();
		if (s.Length >= 2 && s[0] == '"' && s[^1] == '"') {
			int i = 0;
			return (string)ReadQuoted(s, ref i);
		}
		if (s.Length >= 2 && s[0] == '\'' && s[^1] == '\'') {
			return s.Substring(1, s.Length - 2);
		}
		return s;
	}

	// ----------------------------------------------------------------------
	// Helpers: tokenization / scanning
	// ----------------------------------------------------------------------

	private static void SkipBlank(string[] lines, ref int pos) {
		while (pos < lines.Length && string.IsNullOrWhiteSpace(StripComment(lines[pos]))) {
			pos++;
		}
	}

	private static void SkipSpaces(string s, ref int i) {
		while (i < s.Length && (s[i] == ' ' || s[i] == '\t')) {
			i++;
		}
	}

	private static string StripComment(string line) {
		bool inQuote = false;
		char quote = '"';
		for (int i = 0; i < line.Length; i++) {
			char ch = line[i];
			if (!inQuote && (ch == '"' || ch == '\'')) { inQuote = true; quote = ch; } else if (inQuote && ch == quote && (i == 0 || line[i - 1] != '\\')) {
				inQuote = false;
			} else if (ch == '#' && !inQuote) {
				return line.Substring(0, i);
			}
		}
		return line;
	}

	private static int IndentOf(string line) {
		int n = 0;
		while (n < line.Length && line[n] == ' ') {
			n++;
		}
		return n;
	}

	private static int FindTopLevelColon(string s) {
		bool inQuote = false;
		char quote = '"';
		int depth = 0;
		for (int i = 0; i < s.Length; i++) {
			char ch = s[i];
			if (!inQuote && (ch == '"' || ch == '\'')) { inQuote = true; quote = ch; } else if (inQuote && ch == quote && (i == 0 || s[i - 1] != '\\')) {
				inQuote = false;
			} else if (!inQuote) {
				if (ch == '{' || ch == '[') {
					depth++;
				} else if (ch == '}' || ch == ']') {
					depth--;
				} else if (ch == ':' && depth == 0) {
					if (i + 1 == s.Length) {
						return i;
					}
					char next = s[i + 1];
					if (next == ' ' || next == '\t') {
						return i;
					}
				}
			}
		}
		return -1;
	}

	// ----------------------------------------------------------------------
	// Writer
	// ----------------------------------------------------------------------

	private static void WriteNode(StringBuilder sb, object? node, int indent, bool atLineStart) {
		switch (node) {
		case null:
			if (atLineStart) {
				Indent(sb, indent);
			}
			sb.Append("null");
			break;
		case string s:
			if (atLineStart) {
				Indent(sb, indent);
			}
			sb.Append(FormatScalar(s));
			break;
		case bool b:
			if (atLineStart) {
				Indent(sb, indent);
			}
			sb.Append(b ? "true" : "false");
			break;
		case int i:
			if (atLineStart) {
				Indent(sb, indent);
			}
			sb.Append(i.ToString(CultureInfo.InvariantCulture));
			break;
		case long l:
			if (atLineStart) {
				Indent(sb, indent);
			}
			sb.Append(l.ToString(CultureInfo.InvariantCulture));
			break;
		case double d:
			if (atLineStart) {
				Indent(sb, indent);
			}
			sb.Append(d.ToString("G", CultureInfo.InvariantCulture));
			break;
		case IDictionary<string, object?> map:
			WriteMapping(sb, map, indent, atLineStart);
			break;
		case IEnumerable<object?> seq:
			WriteSequence(sb, seq, indent, atLineStart);
			break;
		default:
			// Fallback — stringify via ToString.
			if (atLineStart) {
				Indent(sb, indent);
			}
			sb.Append(FormatScalar(node.ToString() ?? ""));
			break;
		}
	}

	private static void WriteMapping(StringBuilder sb, IDictionary<string, object?> map, int indent, bool atLineStart) {
		if (map.Count == 0) {
			if (atLineStart) {
				Indent(sb, indent);
			}
			sb.Append("{}");
			return;
		}
		bool first = true;
		foreach (var kv in map) {
			if (!first || !atLineStart) {
				sb.Append('\n');
			}
			first = false;
			Indent(sb, indent);
			sb.Append(FormatKey(kv.Key)).Append(':');
			WriteMapValue(sb, kv.Value, indent);
		}
	}

	/// Emits the value portion of "key: <value>" — handles FormattedNode hints
	/// (forces flow / JSON style) and falls back to the default block / flow logic.
	private static void WriteMapValue(StringBuilder sb, object? value, int indent) {
		if (value is FormattedNode fn) {
			sb.Append(' ');
			WriteFlowNode(sb, fn.Value, fn.Format);
			return;
		}
		if (value is IDictionary<string, object?> childMap && childMap.Count > 0) {
			sb.Append('\n');
			WriteMapping(sb, childMap, indent + 2, atLineStart: true);
		} else if (value is IEnumerable<object?> childSeq && !(value is string)) {
			var list = childSeq as IList<object?> ?? new List<object?>(childSeq);
			if (IsAllPrimitive(list)) {
				sb.Append(' ');
				WriteFlowSequence(sb, list);
			} else if (list.Count == 0) {
				sb.Append(" []");
			} else {
				sb.Append('\n');
				WriteSequence(sb, list, indent + 2, atLineStart: true);
			}
		} else {
			sb.Append(' ');
			WriteNode(sb, value, indent, atLineStart: false);
		}
	}

	private static void WriteSequence(StringBuilder sb, IEnumerable<object?> seq, int indent, bool atLineStart) {
		var list = seq as IList<object?> ?? new List<object?>(seq);
		if (list.Count == 0) {
			if (atLineStart) {
				Indent(sb, indent);
			}
			sb.Append("[]");
			return;
		}
		bool first = true;
		foreach (object? item in list) {
			if (!first || !atLineStart) {
				sb.Append('\n');
			}
			first = false;
			Indent(sb, indent);
			sb.Append("- ");
			if (item is IDictionary<string, object?> map) {
				// Inline-start: first key on the same line as "- ", then further keys
				// indented to match.
				WriteInlineMapping(sb, map, indent + 2);
			} else if (item is IEnumerable<object?> seqItem && !(item is string)) {
				var childList = seqItem as IList<object?> ?? new List<object?>(seqItem);
				if (IsAllPrimitive(childList)) {
					WriteFlowSequence(sb, childList);
				} else {
					sb.Append('\n');
					WriteSequence(sb, childList, indent + 2, atLineStart: true);
				}
			} else {
				WriteNode(sb, item, indent, atLineStart: false);
			}
		}
	}

	private static void WriteInlineMapping(StringBuilder sb, IDictionary<string, object?> map, int continuationIndent) {
		bool first = true;
		foreach (var kv in map) {
			if (!first) { sb.Append('\n'); Indent(sb, continuationIndent); }
			first = false;
			sb.Append(FormatKey(kv.Key)).Append(':');
			WriteMapValue(sb, kv.Value, continuationIndent);
		}
	}

	private static void WriteFlowSequence(StringBuilder sb, IList<object?> list) {
		sb.Append('[');
		for (int i = 0; i < list.Count; i++) {
			if (i > 0) {
				sb.Append(", ");
			} else {
				sb.Append(' ');
			}
			WriteNode(sb, list[i], 0, atLineStart: false);
		}
		sb.Append(list.Count > 0 ? " ]" : "]");
	}

	/// Flow / JSON-style writer used when a property carries [YamlFormat]. Keys
	/// are quoted iff the Json flag is set; padding is added iff the Spaced flag
	/// is set. Combine both for canonical "{ "k": v, ... }" style.
	private static void WriteFlowNode(StringBuilder sb, object? node, YamlFormat fmt) {
		bool json = (fmt & YamlFormat.Json) != 0;
		bool spaced = (fmt & YamlFormat.Spaced) != 0;
		WriteFlowValue(sb, node, json, spaced);
	}

	private static void WriteFlowValue(StringBuilder sb, object? node, bool json, bool spaced) {
		switch (node) {
		case null:
			sb.Append("null");
			return;
		case bool b:
			sb.Append(b ? "true" : "false");
			return;
		case int i:
			sb.Append(i.ToString(CultureInfo.InvariantCulture));
			return;
		case long l:
			sb.Append(l.ToString(CultureInfo.InvariantCulture));
			return;
		case double d:
			sb.Append(d.ToString("G", CultureInfo.InvariantCulture));
			return;
		case string s:
			sb.Append(json ? Quote(s) : FormatScalar(s));
			return;
		case IDictionary<string, object?> map:
			sb.Append('{');
			if (spaced && map.Count > 0) {
				sb.Append(' ');
			}
		{
				bool first = true;
				foreach (var kv in map) {
					if (!first) {
						sb.Append(spaced ? ", " : ",");
					}
					first = false;
					sb.Append(json ? Quote(kv.Key) : FormatKey(kv.Key));
					sb.Append(spaced ? ": " : ":");
					WriteFlowValue(sb, kv.Value is FormattedNode fn ? fn.Value : kv.Value, json, spaced);
				}
			}
			if (spaced && map.Count > 0) {
				sb.Append(' ');
			}
			sb.Append('}');
			return;
		case IEnumerable<object?> seq: {
				var list = seq as IList<object?> ?? new List<object?>(seq);
				sb.Append('[');
				if (spaced && list.Count > 0) {
					sb.Append(' ');
				}
				for (int i = 0; i < list.Count; i++) {
					if (i > 0) {
						sb.Append(spaced ? ", " : ",");
					}
					WriteFlowValue(sb, list[i], json, spaced);
				}
				if (spaced && list.Count > 0) {
					sb.Append(' ');
				}
				sb.Append(']');
			}
			return;
		default:
			sb.Append(json ? Quote(node?.ToString() ?? "") : FormatScalar(node?.ToString() ?? ""));
			return;
		}
	}

	/// Carries a per-property formatting hint produced by YamlMapper when a
	/// member is decorated with [YamlFormat]. The writer unwraps it and emits
	/// the inner value in flow style.
	public sealed class FormattedNode {
		public YamlFormat Format { get; }
		public object? Value { get; }
		public FormattedNode(YamlFormat format, object? value) { Format = format; Value = value; }
	}

	private static bool IsAllPrimitive(IList<object?> list) {
		foreach (object? it in list) {
			if (it is IDictionary<string, object?> or IEnumerable<object?> && it is not string) {
				return false;
			}
		}
		return true;
	}

	private static void Indent(StringBuilder sb, int n) {
		for (int i = 0; i < n; i++) {
			sb.Append(' ');
		}
	}

	private static string FormatKey(string key) {
		if (NeedsQuoting(key)) {
			return Quote(key);
		}
		return key;
	}

	private static string FormatScalar(string s) {
		if (s.Length == 0) {
			return "\"\"";
		}
		if (NeedsQuoting(s)) {
			return Quote(s);
		}
		return s;
	}

	private static bool NeedsQuoting(string s) {
		if (s.Length == 0) {
			return true;
		}
		if (s == "null" || s == "true" || s == "false" || s == "~") {
			return true;
		}
		if (int.TryParse(s, NumberStyles.Integer, CultureInfo.InvariantCulture, out _)) {
			return true;
		}
		if (double.TryParse(s, NumberStyles.Float, CultureInfo.InvariantCulture, out _)) {
			return true;
		}
		foreach (char ch in s) {
			if (ch == ':' || ch == '#' || ch == '{' || ch == '}' || ch == '[' || ch == ']'
				|| ch == ',' || ch == '&' || ch == '*' || ch == '!' || ch == '|' || ch == '>'
				|| ch == '\'' || ch == '"' || ch == '%' || ch == '@' || ch == '`'
				|| ch == '\n' || ch == '\t') {
				return true;
			}
		}
		if (char.IsWhiteSpace(s[0]) || char.IsWhiteSpace(s[^1])) {
			return true;
		}
		if (s[0] == '-' || s[0] == '?' || s[0] == ':') {
			return true;
		}
		return false;
	}

	private static string Quote(string s) {
		var sb = new StringBuilder(s.Length + 2);
		sb.Append('"');
		foreach (char ch in s) {
			switch (ch) {
			case '\\':
				sb.Append("\\\\");
				break;
			case '"':
				sb.Append("\\\"");
				break;
			case '\n':
				sb.Append("\\n");
				break;
			case '\t':
				sb.Append("\\t");
				break;
			case '\r':
				sb.Append("\\r");
				break;
			default:
				sb.Append(ch);
				break;
			}
		}
		sb.Append('"');
		return sb.ToString();
	}
}
