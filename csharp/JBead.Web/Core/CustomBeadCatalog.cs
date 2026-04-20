using System.Drawing;
using System.Text.Json;
using Microsoft.JSInterop;

namespace JBead.Web.Core;

/// User's bead library. Holds multiple named catalogs — one for the current session
/// and one per imported .jbeadcat file — all persisted to browser localStorage.
public class CustomBeadCatalog
{
    private const string StorageKey = "jbeadCatalogs";
    public const string SessionName = "Session";

    private readonly IJSRuntime js;
    private List<BeadCatalog> catalogs = new();
    private bool loaded;

    public CustomBeadCatalog(IJSRuntime js) { this.js = js; }

    public IReadOnlyList<BeadCatalog> Catalogs => catalogs;

    public event Action? Changed;

    public async Task EnsureLoadedAsync()
    {
        if (loaded) return;
        loaded = true;
        try
        {
            var json = await js.InvokeAsync<string?>("jbeadStorage.get", StorageKey);
            if (!string.IsNullOrEmpty(json))
            {
                var dto = JsonSerializer.Deserialize<List<CatalogDto>>(json);
                if (dto is not null)
                {
                    catalogs = dto.Select(FromDto).ToList();
                }
            }
        }
        catch { /* corrupt / missing — rebuild fresh */ }

        // Guarantee a Session catalog always exists at index 0 and is non-removable.
        if (!catalogs.Any(c => c.Name == SessionName))
        {
            catalogs.Insert(0, new BeadCatalog { Name = SessionName, IsRemovable = false });
        }
        else
        {
            var session = catalogs.First(c => c.Name == SessionName);
            session.IsRemovable = false;
            if (catalogs[0] != session)
            {
                catalogs.Remove(session);
                catalogs.Insert(0, session);
            }
        }
        Changed?.Invoke();
    }

    public BeadCatalog Session => catalogs.First(c => c.Name == SessionName);

    public async Task AddBeadAsync(string catalogName, Bead bead)
    {
        var cat = catalogs.FirstOrDefault(c => c.Name == catalogName);
        if (cat is null) return;
        cat.Beads.Add(bead.Clone());
        await SaveAsync();
        Changed?.Invoke();
    }

    public async Task RemoveBeadAsync(string catalogName, int index)
    {
        var cat = catalogs.FirstOrDefault(c => c.Name == catalogName);
        if (cat is null || index < 0 || index >= cat.Beads.Count) return;
        cat.Beads.RemoveAt(index);
        await SaveAsync();
        Changed?.Invoke();
    }

    public async Task ReplaceBeadAsync(string catalogName, int index, Bead bead)
    {
        var cat = catalogs.FirstOrDefault(c => c.Name == catalogName);
        if (cat is null || index < 0 || index >= cat.Beads.Count) return;
        cat.Beads[index] = bead.Clone();
        await SaveAsync();
        Changed?.Invoke();
    }

    public async Task RenameCatalogAsync(string oldName, string newName)
    {
        newName = newName.Trim();
        if (string.IsNullOrEmpty(newName)) return;
        if (catalogs.Any(c => c.Name == newName)) return; // must be unique
        var cat = catalogs.FirstOrDefault(c => c.Name == oldName);
        if (cat is null || !cat.IsRemovable) return; // Session can't be renamed
        cat.Name = newName;
        await SaveAsync();
        Changed?.Invoke();
    }

    /// Update the free-form metadata (display name + author). Does NOT change the
    /// catalog's identifier (Name), so existing references stay valid.
    public async Task UpdateCatalogMetadataAsync(string name, string displayName, string author)
    {
        var cat = catalogs.FirstOrDefault(c => c.Name == name);
        if (cat is null) return;
        var trimmed = (displayName ?? "").Trim();
        var trimmedAuthor = (author ?? "").Trim();
        if (cat.DisplayName == trimmed && cat.Author == trimmedAuthor) return;
        cat.DisplayName = trimmed;
        cat.Author = trimmedAuthor;
        await SaveAsync();
        Changed?.Invoke();
    }

    public async Task RemoveCatalogAsync(string name)
    {
        var cat = catalogs.FirstOrDefault(c => c.Name == name);
        if (cat is null || !cat.IsRemovable) return;
        catalogs.Remove(cat);
        await SaveAsync();
        Changed?.Invoke();
    }

    public string ExportJson(string name)
    {
        var cat = catalogs.FirstOrDefault(c => c.Name == name);
        if (cat is null) return "{}";
        // Export format: metadata + bead list. Legacy bare-array .jbeadcat files
        // (from older versions) are still accepted by ImportJsonAsync.
        var opts = new JsonSerializerOptions { WriteIndented = true };
        return JsonSerializer.Serialize(new ExportedCatalogDto
        {
            DisplayName = cat.DisplayName,
            Author = cat.Author,
            Beads = cat.Beads.Select(ToBeadDto).ToList(),
        }, opts);
    }

    /// Creates a new catalog from an imported JSON file. Name is derived from the
    /// filename but deduplicated so repeat imports don't overwrite each other.
    /// Accepts both the new object shape ({displayName, author, beads: [...]})
    /// and the legacy shape (a bare array of beads).
    public async Task ImportJsonAsync(string json, string suggestedFileName)
    {
        string displayName = "";
        string author = "";
        List<BeadDto>? beadDtos = null;

        // Try new shape first.
        try
        {
            var exp = JsonSerializer.Deserialize<ExportedCatalogDto>(json);
            if (exp is not null && exp.Beads is not null)
            {
                displayName = exp.DisplayName ?? "";
                author = exp.Author ?? "";
                beadDtos = exp.Beads;
            }
        }
        catch (JsonException) { /* fall through to legacy */ }

        // Legacy: bare array of beads.
        if (beadDtos is null)
        {
            try { beadDtos = JsonSerializer.Deserialize<List<BeadDto>>(json); }
            catch (JsonException) { return; }
        }
        if (beadDtos is null) return;

        var baseName = SanitizeName(suggestedFileName);
        if (string.IsNullOrWhiteSpace(displayName)) displayName = baseName;
        var name = UniqueName(baseName);
        catalogs.Add(new BeadCatalog
        {
            Name = name,
            DisplayName = displayName,
            Author = author,
            Beads = beadDtos.Select(FromBeadDto).ToList(),
            IsRemovable = true,
        });
        await SaveAsync();
        Changed?.Invoke();
    }

    private string UniqueName(string baseName)
    {
        if (!catalogs.Any(c => c.Name == baseName)) return baseName;
        for (var i = 2; i < 1000; i++)
        {
            var candidate = $"{baseName} ({i})";
            if (!catalogs.Any(c => c.Name == candidate)) return candidate;
        }
        return baseName + " (new)";
    }

    private static string SanitizeName(string name)
    {
        var dot = name.LastIndexOf('.');
        if (dot > 0) name = name.Substring(0, dot);
        name = name.Trim();
        return string.IsNullOrEmpty(name) ? "Imported" : name;
    }

    private async Task SaveAsync()
    {
        var dto = catalogs.Select(ToDto).ToList();
        var json = JsonSerializer.Serialize(dto);
        await js.InvokeVoidAsync("jbeadStorage.set", StorageKey, json);
    }

    // ---- DTO mapping ----

    private sealed class CatalogDto
    {
        public string Name { get; set; } = "";
        public string DisplayName { get; set; } = "";
        public string Author { get; set; } = "";
        public bool IsRemovable { get; set; } = true;
        public List<BeadDto> Beads { get; set; } = new();
    }

    private sealed class BeadDto
    {
        public string Color { get; set; } = "#cccccc";
        public string Manufacturer { get; set; } = "";
        public string Id { get; set; } = "";
        public string Finish { get; set; } = nameof(BeadFinish.Opaque);
    }

    /// Shape written by Export / expected by Import (with legacy bare-array fallback).
    private sealed class ExportedCatalogDto
    {
        public string DisplayName { get; set; } = "";
        public string Author { get; set; } = "";
        public List<BeadDto>? Beads { get; set; }
    }

    private static CatalogDto ToDto(BeadCatalog c) => new()
    {
        Name = c.Name,
        DisplayName = c.DisplayName,
        Author = c.Author,
        IsRemovable = c.IsRemovable,
        Beads = c.Beads.Select(ToBeadDto).ToList(),
    };

    private static BeadCatalog FromDto(CatalogDto d) => new()
    {
        Name = d.Name,
        DisplayName = d.DisplayName ?? "",
        Author = d.Author ?? "",
        IsRemovable = d.IsRemovable,
        Beads = d.Beads.Select(FromBeadDto).ToList(),
    };

    private static BeadDto ToBeadDto(Bead b) => new()
    {
        Color = $"#{b.Color.R:X2}{b.Color.G:X2}{b.Color.B:X2}",
        Manufacturer = b.Manufacturer,
        Id = b.Id,
        Finish = b.Finish.ToString(),
    };

    private static Bead FromBeadDto(BeadDto d)
    {
        var finish = Enum.TryParse<BeadFinish>(d.Finish, out var f) ? f : BeadFinish.Opaque;
        return new Bead(ParseHex(d.Color), d.Manufacturer ?? "", d.Id ?? "", finish);
    }

    private static Color ParseHex(string hex)
    {
        if (string.IsNullOrEmpty(hex)) return Color.Black;
        if (hex[0] == '#') hex = hex.Substring(1);
        if (hex.Length != 6) return Color.Black;
        var r = Convert.ToByte(hex.Substring(0, 2), 16);
        var g = Convert.ToByte(hex.Substring(2, 2), 16);
        var b = Convert.ToByte(hex.Substring(4, 2), 16);
        return Color.FromArgb(r, g, b);
    }
}
