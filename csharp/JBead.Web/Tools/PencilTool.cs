namespace JBead.Web.Tools;

using JBead.Web.Core;

public class PencilTool : ITool
{
    public string Name => "Pencil";
    public string? NameKey => "action.tool.pencil";

    public void OnPointerRelease(BeadModel model, Selection selection, Point origin)
    {
        if (!selection.IsActive) {
			model.SetPoint(origin);
		} else {
			model.DrawLine(origin, selection.LineDest);
		}
	}

    public IEnumerable<PreviewShape> GetPreview(BeadModel model, Selection selection)
    {
        // While dragging, show the line the pencil would draw on release.
        if (selection.IsActive) {
			yield return new PreviewLine(selection.Origin, selection.LineDest);
		}
	}
}
