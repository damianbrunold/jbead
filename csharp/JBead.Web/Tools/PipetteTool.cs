namespace JBead.Web.Tools;

using JBead.Web.Core;

public class PipetteTool : ITool
{
    public string Name => "Pipette";
    public string? NameKey => "action.tool.pipette";

    public void OnPointerRelease(BeadModel model, Selection selection, Point origin)
    {
        model.SelectedColor = model.Get(origin);
    }

    public IEnumerable<PreviewShape> GetPreview(BeadModel model, Selection selection)
    {
        if (!selection.IsSet) yield break;
        yield return new PreviewCell(selection.Destination);
    }
}
