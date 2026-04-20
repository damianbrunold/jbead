namespace JBead.Web.FileFormat;

/// Base for custom type converters — mirrors System.Text.Json's JsonConverter shape.
/// Override to map between your type and a YAML-friendly primitive/tree.
public abstract class YamlConverter<T>
{
    /// Convert a decoded YAML node (string/int/double/bool/List/Dictionary) to T.
    public abstract T? Read(object? node);

    /// Convert T to a value the YAML writer knows how to emit (same primitive set).
    public abstract object? Write(T? value);
}
