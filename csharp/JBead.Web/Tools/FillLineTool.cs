namespace JBead.Web.Tools;

using JBead.Web.Core;

/// Row-scoped bucket fill. At each row touched by the action, the bead at the
/// origin column is used as the seed colour and the fill extends left and
/// right until it hits a bead of a different colour. A plain click acts on
/// the clicked row; a drag-selection broadens the action to every row the
/// rectangle touches (origin column is reused as the seed column for each).
public class FillLineTool : ITool
{
    public string Name => "Fill line";
    public string? NameKey => "action.tool.fillline";

    public void OnPointerRelease(BeadModel model, Selection selection, Point origin)
    {
        int yMin, yMax;
        if (selection.IsActive)
        {
            yMin = selection.Bottom;
            yMax = selection.Top;
        }
        else
        {
            yMin = yMax = origin.Y;
        }
        model.FillLineBounded(origin.X, yMin, yMax);
    }

    public IEnumerable<PreviewShape> GetPreview(BeadModel model, Selection selection)
    {
        if (!selection.IsSet) yield break;
        yield return new PreviewCell(selection.Origin);
    }
}
