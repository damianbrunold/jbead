namespace JBead.Web.Core;

/// A named group of beads — one tab in the bead selector dialog. Could be the
/// user's own "Session" catalog, or an imported .jbeadcat file's catalog.
public class BeadCatalog
{
    /// Internal identifier — unique across the user's catalog list, used for
    /// lookup/rename/dedupe. Stable across session reloads.
    public string Name { get; set; } = "";

    /// Human-friendly label shown on the tab and in export filenames. Free-form;
    /// falls back to Name when empty.
    public string DisplayName { get; set; } = "";

    /// Optional attribution — who compiled the catalog. Displayed in a tooltip
    /// and preserved across export/import.
    public string Author { get; set; } = "";

    public List<Bead> Beads { get; set; } = new();

    /// Imported catalogs can be removed; the Session catalog is intrinsic and stays.
    public bool IsRemovable { get; set; } = true;

    /// What the UI should render on the tab.
    public string Label => string.IsNullOrWhiteSpace(DisplayName) ? Name : DisplayName;
}
