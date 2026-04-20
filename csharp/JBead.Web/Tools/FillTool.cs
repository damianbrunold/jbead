namespace JBead.Web.Tools;

using JBead.Web.Core;

/// Replaces every bead matching the clicked cell's colour with the current
/// palette colour. A plain click floods the whole grid; dragging a rectangle
/// first scopes the replacement to that rectangle (origin = seed cell).
public class FillTool : ITool
{
    public string Name => "Fill";
    public string? NameKey => "action.tool.fill";

    public void OnPointerRelease(BeadModel model, Selection selection, Point origin)
    {
        Rect? bounds = selection.IsActive ? selection : null;
        model.ReplaceColor(origin, bounds);
    }

    public IEnumerable<PreviewShape> GetPreview(BeadModel model, Selection selection)
    {
        if (!selection.IsSet) yield break;
        yield return new PreviewCell(selection.Origin);
    }
}
