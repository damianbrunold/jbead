using System.Globalization;
using System.Net.Http;
using System.Text;
using Microsoft.JSInterop;

namespace JBead.Web.Core;

public record LanguageOption(string Code, string NativeName, string FlagUrl);

/// Scoped service that serves localized strings for the UI. Loads Java-style
/// `.properties` files (the ones shipped alongside the Swing app) over HTTP,
/// caches all supported catalogs, and raises LanguageChanged when components
/// should re-render.
///
/// Initial language resolves in this order: explicit user choice in
/// localStorage → navigator.language → "en". Unsupported codes collapse to "en".
public class LocalizationService
{
    private const string StorageKey = "jbead.language";
    private const string DefaultCode = "en";

    // Supported language codes + their hard-coded native names. Everything else
    // (flag URL, page text) comes from the .properties files. This list stays
    // in code because the WASM host can't enumerate wwwroot/.
    private static readonly (string Code, string NativeName)[] SupportedCodes =
    {
        ("en", "English"),
        ("de", "Deutsch"),
        ("fr", "Français"),
        ("pl", "Polski"),
        ("cs", "Čeština"),
        ("ja", "日本語"),
        ("zh", "中文"),
        ("es", "Español"),
        ("ru", "Русский"),
        ("uk", "Українська"),
    };

    public IReadOnlyList<LanguageOption> Supported { get; private set; } = Array.Empty<LanguageOption>();

    public event Action? LanguageChanged;

    public string Language { get; private set; } = DefaultCode;

    // Every supported language's catalog stays cached once loaded; flag URLs and
    // per-language metadata keys (e.g. language.flag) stay accessible even when
    // the current language is something else.
    private readonly Dictionary<string, Dictionary<string, string>> allCatalogs = new();
    private Dictionary<string, string> current = new();
    private Dictionary<string, string> fallback = new();

    private readonly HttpClient http;
    private readonly IJSRuntime js;

    public LocalizationService(HttpClient http, IJSRuntime js)
    {
        this.http = http;
        this.js = js;
    }

    public async Task InitializeAsync()
    {
        // Parallel preload: fetching 7 small files up front keeps the picker
        // snappy (flag + native name available without per-option fetches) and
        // the extra startup latency is ~1 RTT since all requests overlap.
        var tasks = SupportedCodes.Select(async s =>
        {
            var cat = await TryLoadCatalogAsync(s.Code);
            return (s.Code, Catalog: cat ?? new Dictionary<string, string>());
        });
        var results = await Task.WhenAll(tasks);
        foreach ((string code, Dictionary<string, string> cat) in results) {
			allCatalogs[code] = cat;
		}

		fallback = allCatalogs.TryGetValue(DefaultCode, out var fb) ? fb : new Dictionary<string, string>();

        // Build Supported from the catalogs so FlagUrl can come from the
        // properties files (language.flag key) instead of hard-coded C#.
        Supported = SupportedCodes
            .Select(s => new LanguageOption(
                s.Code,
                s.NativeName,
                FlagUrlFor(s.Code)))
            .ToList();

        string? stored = await TryGetStoredLanguageAsync();
        string detected = stored ?? await DetectBrowserLanguageAsync() ?? DefaultCode;
        if (!IsSupported(detected)) {
			detected = DefaultCode;
		}

		Language = detected;
        current = allCatalogs.TryGetValue(detected, out var c) ? c : fallback;

        LanguageChanged?.Invoke();
    }

    public async Task SetLanguageAsync(string code)
    {
        if (code == Language) {
			return;
		}
		if (!IsSupported(code)) {
			return;
		}

		if (!allCatalogs.TryGetValue(code, out var next))
        {
            // Preload should cover every supported code, but stay defensive:
            // lazy-load if something slipped through.
            var loaded = await TryLoadCatalogAsync(code);
            if (loaded is null) {
				return;
			}
			allCatalogs[code] = loaded;
            next = loaded;
        }

        current = next;
        Language = code;
        try { await js.InvokeVoidAsync("jbeadStorage.set", StorageKey, code); }
        catch { /* localStorage unavailable — stay in memory */ }

        LanguageChanged?.Invoke();
    }

    /// Flag URL for a given language, looked up in that language's own catalog
    /// (`language.flag` key). Returns empty string when unknown — the <img>
    /// element renders empty without breaking the picker layout.
    public string FlagUrlFor(string code) =>
        allCatalogs.TryGetValue(code, out var cat) && cat.TryGetValue("language.flag", out string? url)
            ? url
            : string.Empty;

    /// Returns the localized string; falls back to English, then to the raw key
    /// so missing translations are visible in the UI rather than crashing.
    public string this[string key] =>
        current.TryGetValue(key, out string? v) ? v
            : fallback.TryGetValue(key, out string? f) ? f
            : key;

    /// Same lookup, with `{0}`, `{1}`, … placeholder substitution — C# style, so
    /// razor call sites can pass args as a collection expression:
    ///   `@(Loc["id.of.key", ["arg0", "arg1"]])`
    /// New translation keys should use `{0}`-indexed placeholders. The pre-existing
    /// Java-compat keys (e.g. `title = jbead - {1}`) keep their original tokens and
    /// should be accessed via Format/FormatJava when needed.
    public string this[string key, string[] args]
    {
        get
        {
            string s = this[key];
            for (int i = 0; i < args.Length; i++) {
				s = s.Replace("{" + i + "}", args[i]);
			}
			return s;
        }
    }

    public string Get(string key) => this[key];

    private static bool IsSupported(string code) => SupportedCodes.Any(s => s.Code == code);

    private async Task<string?> TryGetStoredLanguageAsync()
    {
        try
        {
            string? val = await js.InvokeAsync<string?>("jbeadStorage.get", StorageKey);
            return string.IsNullOrWhiteSpace(val) ? null : val;
        }
        catch { return null; }
    }

    private async Task<string?> DetectBrowserLanguageAsync()
    {
        try
        {
            string? val = await js.InvokeAsync<string?>("jbeadEnv.browserLanguage");
            return string.IsNullOrWhiteSpace(val) ? null : val;
        }
        catch { return null; }
    }

    private async Task<Dictionary<string, string>?> TryLoadCatalogAsync(string code)
    {
        try { return await LoadCatalogAsync(code); }
        catch { return null; }
    }

    private async Task<Dictionary<string, string>> LoadCatalogAsync(string code)
    {
        string fileName = code == DefaultCode ? "jbead.properties" : $"jbead_{code}.properties";
        string text = await http.GetStringAsync($"i18n/{fileName}");
        return PropertiesParser.Parse(text);
    }
}

/// Minimal Java `.properties` parser. Enough of the format for our files:
/// UTF-8 text, `#` / `!` comments, line continuations with trailing `\`, `=` /
/// `:` / whitespace as the key-value separator, and the standard escape
/// sequences (`\n`, `\r`, `\t`, `\\`, `\uXXXX`).
internal static class PropertiesParser
{
    public static Dictionary<string, string> Parse(string text)
    {
        var result = new Dictionary<string, string>();
        string[] lines = text.Replace("\r\n", "\n").Split('\n');
        int i = 0;
        while (i < lines.Length)
        {
            string line = StripLeadingWhitespace(lines[i]);
            i++;
            if (line.Length == 0 || line[0] == '#' || line[0] == '!') {
				continue;
			}

			var logical = new StringBuilder(line);
            while (EndsWithOddBackslashes(logical) && i < lines.Length)
            {
                logical.Length--;
                logical.Append(StripLeadingWhitespace(lines[i]));
                i++;
            }

            SplitKeyValue(logical.ToString(), out string key, out string value);
            if (!string.IsNullOrEmpty(key)) {
				result[key] = Unescape(value);
			}
		}
        return result;
    }

    private static string StripLeadingWhitespace(string s)
    {
        int idx = 0;
        while (idx < s.Length && (s[idx] == ' ' || s[idx] == '\t' || s[idx] == '\f')) {
			idx++;
		}
		return idx == 0 ? s : s[idx..];
    }

    private static bool EndsWithOddBackslashes(StringBuilder sb)
    {
        int count = 0;
        for (int j = sb.Length - 1; j >= 0 && sb[j] == '\\'; j--) {
			count++;
		}
		return count % 2 == 1;
    }

    private static void SplitKeyValue(string line, out string key, out string value)
    {
        int sepIdx = -1;
        bool hasExplicitDelimiter = false;
        for (int j = 0; j < line.Length; j++)
        {
            char c = line[j];
            if (c == '\\') { j++; continue; }
            if (c == '=' || c == ':') { sepIdx = j; hasExplicitDelimiter = true; break; }
            if (c == ' ' || c == '\t' || c == '\f') { sepIdx = j; break; }
        }
        if (sepIdx < 0) { key = UnescapeKey(line); value = string.Empty; return; }

        key = UnescapeKey(line[..sepIdx]);
        string rest = line[(sepIdx + (hasExplicitDelimiter ? 1 : 0))..];
        int k = 0;
        while (k < rest.Length && (rest[k] == ' ' || rest[k] == '\t' || rest[k] == '\f')) {
			k++;
		}
		if (!hasExplicitDelimiter && k < rest.Length && (rest[k] == '=' || rest[k] == ':'))
        {
            k++;
            while (k < rest.Length && (rest[k] == ' ' || rest[k] == '\t' || rest[k] == '\f')) {
				k++;
			}
		}
        value = rest[k..];
    }

    private static string UnescapeKey(string key) => Unescape(key);

    private static string Unescape(string s)
    {
        if (!s.Contains('\\')) {
			return s;
		}
		var sb = new StringBuilder(s.Length);
        for (int i = 0; i < s.Length; i++)
        {
            char c = s[i];
            if (c != '\\' || i + 1 >= s.Length) { sb.Append(c); continue; }
            char n = s[++i];
            switch (n)
            {
                case 'n': sb.Append('\n'); break;
                case 'r': sb.Append('\r'); break;
                case 't': sb.Append('\t'); break;
                case 'f': sb.Append('\f'); break;
                case 'u':
                    if (i + 4 < s.Length
                        && ushort.TryParse(s.Substring(i + 1, 4), NumberStyles.HexNumber,
                            CultureInfo.InvariantCulture, out ushort code))
                    {
                        sb.Append((char)code);
                        i += 4;
                    }
                    else {
						sb.Append(n);
					}
					break;
                default: sb.Append(n); break;
            }
        }
        return sb.ToString();
    }
}
