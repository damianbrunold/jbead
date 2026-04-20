namespace JBead.Web.FileFormat;

using System.Collections;
using System.Collections.Concurrent;
using System.Globalization;
using System.Linq;
using System.Reflection;

/// Reflection-based mapper between strongly-typed objects and the generic YAML tree
/// produced by Yaml.Parse / consumed by Yaml.Serialize. Respects
/// [YamlPropertyName], [YamlIgnore] and [YamlConverter] attributes.
///
/// Default naming convention: camelCase (first letter lowered). Override per-property
/// with [YamlPropertyName].
public static class YamlMapper
{
    // ----------------------------------------------------------------------
    // Public API
    // ----------------------------------------------------------------------

    public static T? ToObject<T>(object? tree) => (T?)ToObject(tree, typeof(T));

    public static object? ToObject(object? tree, Type targetType) => ConvertFromTree(tree, targetType);

    public static object? FromObject<T>(T value) => ConvertToTree(value, typeof(T));

    public static object? FromObject(object? value, Type declaredType) => ConvertToTree(value, declaredType);

    // ----------------------------------------------------------------------
    // Tree → typed object
    // ----------------------------------------------------------------------

    private static object? ConvertFromTree(object? node, Type targetType)
    {
        // Unwrap Nullable<>.
        var underlying = Nullable.GetUnderlyingType(targetType);
        if (underlying is not null)
        {
            if (node is null) {
				return null;
			}
			return ConvertFromTree(node, underlying);
        }

        // Custom converter attribute on the *declared* type.
        var typeConverter = GetConverterAttr(targetType);
        if (typeConverter is not null)
        {
            object? inst = Activator.CreateInstance(typeConverter.ConverterType);
            return InvokeConverterRead(inst!, node);
        }

        if (targetType == typeof(object)) {
			return node;
		}
		if (node is null) {
			return GetDefault(targetType);
		}

		// Primitives / strings.
        if (targetType == typeof(string)) {
			return node is string s ? s : node.ToString() ?? "";
		}
		if (targetType == typeof(bool)) {
			return CoerceBool(node);
		}
		if (targetType.IsEnum) {
			return CoerceEnum(node, targetType);
		}
		if (IsNumeric(targetType)) {
			return CoerceNumeric(node, targetType);
		}
		if (targetType == typeof(DateTime)) {
			return CoerceDateTime(node);
		}

		// Collections.
        if (targetType.IsArray) {
			return CoerceArray(node, targetType);
		}
		if (TryGetEnumerableElementType(targetType, out var elemType))
        {
            if (targetType.IsGenericType && targetType.GetGenericTypeDefinition() == typeof(Dictionary<,>))
            {
                var args = targetType.GetGenericArguments();
                return CoerceDictionary(node, args[0], args[1]);
            }
            return CoerceList(node, targetType, elemType!);
        }

        // Complex object — reflect properties.
        return CoerceObject(node, targetType);
    }

    private static object? CoerceObject(object? node, Type t)
    {
        if (node is not IDictionary<string, object?> src) {
			return GetDefault(t);
		}

		object instance = Activator.CreateInstance(t)
            ?? throw new InvalidOperationException($"Cannot instantiate {t}");
        foreach (var member in GetMembers(t))
        {
            if (!src.TryGetValue(member.SerializedName, out object? raw)) {
				continue;
			}
			object? val;
            if (member.Converter is not null)
            {
                val = InvokeConverterRead(member.Converter, raw);
            }
            else
            {
                val = ConvertFromTree(raw, member.MemberType);
            }
            member.Setter(instance, val);
        }
        return instance;
    }

    private static object? CoerceList(object? node, Type listType, Type elemType)
    {
        var list = (IList)Activator.CreateInstance(listType)!;
        if (node is IEnumerable seq && node is not string)
        {
            foreach (object? item in seq) {
				list.Add(ConvertFromTree(item, elemType));
			}
		}
        return list;
    }

    private static object? CoerceArray(object? node, Type arrayType)
    {
        var elemType = arrayType.GetElementType()!;
        if (node is not IEnumerable seq || node is string) {
			return Array.CreateInstance(elemType, 0);
		}
		var tmp = new List<object?>();
        foreach (object? item in seq) {
			tmp.Add(ConvertFromTree(item, elemType));
		}
		var arr = Array.CreateInstance(elemType, tmp.Count);
        for (int i = 0; i < tmp.Count; i++) {
			arr.SetValue(tmp[i], i);
		}
		return arr;
    }

    private static object? CoerceDictionary(object? node, Type keyType, Type valueType)
    {
        var dictType = typeof(Dictionary<,>).MakeGenericType(keyType, valueType);
        var dict = (IDictionary)Activator.CreateInstance(dictType)!;
        if (node is IDictionary<string, object?> src)
        {
            foreach (var kv in src)
            {
                object? k = ConvertFromTree(kv.Key, keyType);
                object? v = ConvertFromTree(kv.Value, valueType);
                dict[k!] = v;
            }
        }
        return dict;
    }

    private static bool CoerceBool(object? node) => node switch
    {
        bool b => b,
        string s when bool.TryParse(s, out bool b) => b,
        int i => i != 0,
        _ => false,
    };

    private static object CoerceEnum(object? node, Type enumType)
    {
        if (node is string s && Enum.TryParse(enumType, s, ignoreCase: true, out object? parsed) && parsed is not null) {
			return parsed;
		}
		if (node is int i) {
			return Enum.ToObject(enumType, i);
		}
		if (node is long l) {
			return Enum.ToObject(enumType, l);
		}
		return Activator.CreateInstance(enumType)!;
    }

    private static object CoerceNumeric(object? node, Type t)
    {
        string s = node?.ToString() ?? "0";
        return Convert.ChangeType(s, t, CultureInfo.InvariantCulture);
    }

    private static object CoerceDateTime(object? node)
    {
        if (node is DateTime dt) {
			return dt;
		}
		if (node is string s && DateTime.TryParse(s, CultureInfo.InvariantCulture, DateTimeStyles.RoundtripKind, out var parsed)) {
			return parsed;
		}
		return default(DateTime);
    }

    // ----------------------------------------------------------------------
    // Typed object → tree
    // ----------------------------------------------------------------------

    private static object? ConvertToTree(object? value, Type declaredType)
    {
        if (value is null) {
			return null;
		}

		var underlying = Nullable.GetUnderlyingType(declaredType);
        if (underlying is not null) {
			declaredType = underlying;
		}

		var typeConverter = GetConverterAttr(declaredType);
        if (typeConverter is not null)
        {
            object? inst = Activator.CreateInstance(typeConverter.ConverterType);
            return InvokeConverterWrite(inst!, value);
        }

        var valueType = value.GetType();

        // Primitives emitted as-is; writer will format them.
        if (valueType.IsPrimitive || valueType == typeof(string) || valueType == typeof(decimal)) {
			return value;
		}
		if (valueType.IsEnum) {
			return value.ToString();
		}
		if (valueType == typeof(DateTime)) {
			return ((DateTime)value).ToString("o", CultureInfo.InvariantCulture);
		}

		// Dictionary<string, T>.
        if (value is IDictionary dict && IsStringKeyedDictionary(valueType, out var dictValueType))
        {
            var map = new Dictionary<string, object?>();
            foreach (DictionaryEntry kv in dict)
            {
                map[kv.Key!.ToString()!] = ConvertToTree(kv.Value, dictValueType!);
            }
            return map;
        }

        // Arrays / lists / enumerables.
        if (value is IEnumerable seq && value is not string)
        {
            var elem = TryGetEnumerableElementType(valueType, out var et) ? et! : typeof(object);
            var list = new List<object?>();
            foreach (object? item in seq) {
				list.Add(ConvertToTree(item, elem));
			}
			return list;
        }

        // Complex object — reflect members.
        var treeMap = new Dictionary<string, object?>();
        foreach (var member in GetMembers(valueType))
        {
            object? v = member.Getter(value);
            object? subTree = member.Converter is not null
                ? InvokeConverterWrite(member.Converter, v)
                : ConvertToTree(v, member.MemberType);
            if (member.IgnoreEmpty && IsEmpty(subTree)) {
				continue;
			}
			if (member.Format.HasValue) {
				subTree = new Yaml.FormattedNode(member.Format.Value, subTree);
			}
			treeMap[member.SerializedName] = subTree;
        }
        return treeMap;
    }

    private static bool IsEmpty(object? node) => node switch
    {
        null => true,
        string s => s.Length == 0,
        IDictionary<string, object?> map => map.Count == 0,
        IEnumerable seq when node is not string => !seq.Cast<object?>().Any(),
        _ => false,
    };

    // ----------------------------------------------------------------------
    // Reflection cache
    // ----------------------------------------------------------------------

    private sealed class MemberBinding
    {
        public string SerializedName = "";
        public Type MemberType = typeof(object);
        public Func<object, object?> Getter = _ => null;
        public Action<object, object?> Setter = (_, _) => { };
        public object? Converter;
        public YamlFormat? Format;
        public bool IgnoreEmpty;
    }

    private static readonly ConcurrentDictionary<Type, MemberBinding[]> MemberCache = new();

    private static MemberBinding[] GetMembers(Type t)
    {
        return MemberCache.GetOrAdd(t, ComputeMembers);
    }

    private static MemberBinding[] ComputeMembers(Type t)
    {
        var list = new List<MemberBinding>();
        foreach (var p in t.GetProperties(BindingFlags.Public | BindingFlags.Instance))
        {
            if (p.GetCustomAttribute<YamlIgnoreAttribute>() is not null) {
				continue;
			}
			if (!p.CanRead || !p.CanWrite) {
				continue;
			}
			if (p.GetIndexParameters().Length > 0) {
				continue;
			}

			var nameAttr = p.GetCustomAttribute<YamlPropertyNameAttribute>();
            var convAttr = p.GetCustomAttribute<YamlConverterAttribute>();
            var fmtAttr = p.GetCustomAttribute<YamlFormatAttribute>();
            var emptyAttr = p.GetCustomAttribute<YamlIgnoreEmptyAttribute>();
            list.Add(new MemberBinding
            {
                SerializedName = nameAttr?.Name ?? ToCamelCase(p.Name),
                MemberType = p.PropertyType,
                Getter = instance => p.GetValue(instance),
                Setter = (instance, v) => p.SetValue(instance, v),
                Converter = convAttr is null ? null : Activator.CreateInstance(convAttr.ConverterType),
                Format = fmtAttr?.Format,
                IgnoreEmpty = emptyAttr is not null,
            });
        }
        foreach (var f in t.GetFields(BindingFlags.Public | BindingFlags.Instance))
        {
            if (f.GetCustomAttribute<YamlIgnoreAttribute>() is not null) {
				continue;
			}
			var nameAttr = f.GetCustomAttribute<YamlPropertyNameAttribute>();
            var convAttr = f.GetCustomAttribute<YamlConverterAttribute>();
            var fmtAttr = f.GetCustomAttribute<YamlFormatAttribute>();
            var emptyAttr = f.GetCustomAttribute<YamlIgnoreEmptyAttribute>();
            list.Add(new MemberBinding
            {
                SerializedName = nameAttr?.Name ?? ToCamelCase(f.Name),
                MemberType = f.FieldType,
                Getter = instance => f.GetValue(instance),
                Setter = (instance, v) => f.SetValue(instance, v),
                Converter = convAttr is null ? null : Activator.CreateInstance(convAttr.ConverterType),
                Format = fmtAttr?.Format,
                IgnoreEmpty = emptyAttr is not null,
            });
        }
        return list.ToArray();
    }

    // ----------------------------------------------------------------------
    // Helpers
    // ----------------------------------------------------------------------

    private static YamlConverterAttribute? GetConverterAttr(Type t)
        => t.GetCustomAttribute<YamlConverterAttribute>();

    private static object? InvokeConverterRead(object converter, object? node)
    {
        var method = converter.GetType().GetMethod("Read")
            ?? throw new InvalidOperationException("Converter missing Read(object) method");
        return method.Invoke(converter, new[] { node });
    }

    private static object? InvokeConverterWrite(object converter, object? value)
    {
        var method = converter.GetType().GetMethod("Write")
            ?? throw new InvalidOperationException("Converter missing Write(T) method");
        return method.Invoke(converter, new[] { value });
    }

    private static bool IsNumeric(Type t) => t == typeof(byte) || t == typeof(sbyte)
        || t == typeof(short) || t == typeof(ushort) || t == typeof(int) || t == typeof(uint)
        || t == typeof(long) || t == typeof(ulong) || t == typeof(float) || t == typeof(double)
        || t == typeof(decimal);

    private static bool TryGetEnumerableElementType(Type t, out Type? elem)
    {
        if (t.IsArray) { elem = t.GetElementType(); return true; }
        if (t.IsGenericType)
        {
            var def = t.GetGenericTypeDefinition();
            if (def == typeof(List<>) || def == typeof(IList<>) || def == typeof(IEnumerable<>) || def == typeof(ICollection<>))
            {
                elem = t.GetGenericArguments()[0];
                return true;
            }
        }
        foreach (var iface in t.GetInterfaces())
        {
            if (iface.IsGenericType && iface.GetGenericTypeDefinition() == typeof(IEnumerable<>))
            {
                elem = iface.GetGenericArguments()[0];
                return true;
            }
        }
        elem = null;
        return false;
    }

    private static bool IsStringKeyedDictionary(Type t, out Type? valueType)
    {
        if (t.IsGenericType)
        {
            var args = t.GetGenericArguments();
            if (args.Length == 2 && args[0] == typeof(string)
                && (t.GetGenericTypeDefinition() == typeof(Dictionary<,>)
                    || t.GetGenericTypeDefinition() == typeof(IDictionary<,>)))
            {
                valueType = args[1];
                return true;
            }
        }
        valueType = null;
        return false;
    }

    private static object? GetDefault(Type t)
        => t.IsValueType ? Activator.CreateInstance(t) : null;

    private static string ToCamelCase(string name)
    {
        if (string.IsNullOrEmpty(name)) {
			return name;
		}
		if (name.Length == 1) {
			return name.ToLowerInvariant();
		}
		return char.ToLowerInvariant(name[0]) + name.Substring(1);
    }
}
