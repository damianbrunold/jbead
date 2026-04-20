namespace JBead.Web.FileFormat;

/// Rename a property in the serialized YAML. Equivalent to
/// System.Text.Json's JsonPropertyName.
[AttributeUsage(AttributeTargets.Property | AttributeTargets.Field, AllowMultiple = false)]
public sealed class YamlPropertyNameAttribute : Attribute
{
    public string Name { get; }
    public YamlPropertyNameAttribute(string name) { Name = name; }
}

/// Skip a property during both serialization and deserialization.
[AttributeUsage(AttributeTargets.Property | AttributeTargets.Field, AllowMultiple = false)]
public sealed class YamlIgnoreAttribute : Attribute { }

/// Skip a property during serialization when its value is "empty":
/// null, empty string, or an empty collection. Reading is unaffected — a
/// missing key just leaves the property at its declared default.
[AttributeUsage(AttributeTargets.Property | AttributeTargets.Field, AllowMultiple = false)]
public sealed class YamlIgnoreEmptyAttribute : Attribute { }

/// Apply a custom converter to a property or declared type. The type referenced
/// must derive from YamlConverter&lt;T&gt; and have a public parameterless constructor.
[AttributeUsage(AttributeTargets.Property | AttributeTargets.Field | AttributeTargets.Class | AttributeTargets.Struct,
    AllowMultiple = false)]
public sealed class YamlConverterAttribute : Attribute
{
    public Type ConverterType { get; }
    public YamlConverterAttribute(Type converterType) { ConverterType = converterType; }
}

/// Output style for a single property. Combine flags to get JSON-with-spaces.
///   Yaml    — default block style ("size:\n  rows: 50\n  columns: 6").
///   Json    — flow style with quoted keys ({"rows":50,"columns":6}).
///   Spaced  — flow style with padding ({ rows: 50, columns: 6 }).
///   Json|Spaced — JSON-flow with padding ({ "rows": 50, "columns": 6 }).
[Flags]
public enum YamlFormat
{
    Yaml = 0,
    Json = 1,
    Spaced = 2,
}

/// Force a single property to be emitted in flow / JSON style instead of
/// the default YAML block style. Affects writing only — the parser already
/// accepts both forms, so reading is unaffected.
[AttributeUsage(AttributeTargets.Property | AttributeTargets.Field, AllowMultiple = false)]
public sealed class YamlFormatAttribute : Attribute
{
    public YamlFormat Format { get; }
    public YamlFormatAttribute(YamlFormat format) { Format = format; }
}
