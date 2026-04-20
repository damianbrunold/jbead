namespace JBead.Web.Tools;

using JBead.Web.Core;

public class SelectionTool : ITool
{
    public string Name => "Select";
    public string? NameKey => "action.tool.select";

    // Matches the Java behavior: a bare click (no drag) toggles the cell; a drag leaves
    // the selection active for follow-up ops (rotate/mirror/arrange — not yet ported).
    public void OnPointerRelease(BeadModel model, Selection selection, Point origin)
    {
        if (!selection.IsActive) {
			model.SetPoint(origin);
		}
	}

    // The dashed selection rectangle is drawn universally by DraftPreview when
    // Selection.IsNormal, so no extra shape needed here.
}
