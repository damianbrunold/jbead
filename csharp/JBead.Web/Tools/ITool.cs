namespace JBead.Web.Tools;

using JBead.Web.Core;

public interface ITool
{
    /// Display name shown on the toolbar button. Used as a fallback when NameKey
    /// is null or the translation is missing.
    string Name { get; }

    /// Translation key (in the .properties files) for the localized button label.
    /// Return null to keep the hardcoded Name.
    string? NameKey => null;

    /// Invoked from DraftView on pointer release. `origin` is the cell where the drag started.
    void OnPointerRelease(BeadModel model, Selection selection, Point origin);

    /// Grid-space preview shapes rendered on the overlay layer while the user is dragging.
    /// Default: no preview. Override to show line previews, targeting, etc.
    IEnumerable<PreviewShape> GetPreview(BeadModel model, Selection selection)
        => Array.Empty<PreviewShape>();
}
