using System.Drawing;

namespace JBead.Web.Core;

/// A "bead type" in the project palette. The grid stores a byte index pointing at an
/// entry in BeadModel.Beads — editing the bead (color, metadata, finish) propagates
/// automatically to every cell that references it.
public class Bead
{
    public Color Color { get; set; }
    public string Manufacturer { get; set; } = "";
    public string Id { get; set; } = "";
    public BeadFinish Finish { get; set; } = BeadFinish.Opaque;

    /// Name of the catalog this bead was originally picked from (Session, or an
    /// imported catalog's name). Empty when the bead is a one-off custom entry.
    /// Persisted with pattern files so the pattern can re-link to the source
    /// catalog if that catalog is still available on load; inline props are the
    /// fallback when the catalog isn't present.
    public string CatalogSource { get; set; } = "";

    public Bead() : this(Color.White) { }

    public Bead(Color color, string manufacturer = "", string id = "", BeadFinish finish = BeadFinish.Opaque, string catalogSource = "")
    {
        Color = color;
        Manufacturer = manufacturer;
        Id = id;
        Finish = finish;
        CatalogSource = catalogSource;
    }

    public Bead Clone() => new(Color, Manufacturer, Id, Finish, CatalogSource);
}
