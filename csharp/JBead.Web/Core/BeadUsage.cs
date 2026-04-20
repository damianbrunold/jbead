using System.Drawing;

namespace JBead.Web.Core;

/// One entry in a bead-usage report — potentially the merge of several palette slots
/// sharing the same (Manufacturer, Id).
public class BeadUsage
{
    public Color RepresentativeColor { get; set; }
    public string Manufacturer { get; set; } = "";
    public string Id { get; set; } = "";
    public BeadFinish Finish { get; set; }
    public int Count { get; set; }
    public List<byte> MergedIndices { get; set; } = new();
}
