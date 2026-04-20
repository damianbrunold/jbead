using JBead.Web.Core;

namespace JBead.Web.Components;

/// What BeadEditDialog returns to the parent on Save. The parent decides whether
/// the bead becomes a new palette slot, replaces an existing one, or goes into a
/// catalog. Dialog itself stays free of that policy.
public record BeadEditResult(Bead Bead, bool SaveToLibrary);
