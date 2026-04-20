namespace JBead.Web.Core;

/// Bridges between the BeadFinish [Flags] enum and the various serialized forms
/// that have shipped with jbead over the years: legacy integer ordinals in
/// pre-v4 .jbb files, legacy compound names in .jbby YAML ("TransparentGloss",
/// "TransparentMatte"), and the current comma-separated flag-set string form.
public static class BeadFinishConverter
{
    /// Translate the pre-v4 single-ordinal encoding. Integer values 7 and 8
    /// used to be the compound finishes TransparentGloss and TransparentMatte;
    /// they now expand to bitmask combinations of the primitive flags.
    public static BeadFinish FromLegacyInt(int v) => v switch
    {
        0 => BeadFinish.Opaque,
        1 => BeadFinish.Transparent,
        2 => BeadFinish.Matte,
        3 => BeadFinish.Shiny,
        4 => BeadFinish.Pearl,
        5 => BeadFinish.SilverLined,
        6 => BeadFinish.Metallic,
        7 => BeadFinish.Transparent | BeadFinish.Shiny,
        8 => BeadFinish.Transparent | BeadFinish.Matte,
        9 => BeadFinish.Iridescent,
        10 => BeadFinish.Galvanized,
        // Anything else is assumed to be a raw bitmask already (forward-compat
        // with files that happen to carry unknown bits).
        _ => (BeadFinish)v,
    };

    /// Parse a finish string. Accepts:
    ///  - Legacy compound names: "TransparentGloss", "TransparentMatte"
    ///  - Current flag-set form: "Opaque", "Transparent", "Transparent, Shiny"
    ///  - Empty / null → Opaque
    /// Returns Opaque on unrecognised input rather than throwing so a corrupt
    /// catalog entry can't crash the loader.
    public static BeadFinish FromString(string? s)
    {
        if (string.IsNullOrWhiteSpace(s)) return BeadFinish.Opaque;
        var trimmed = s.Trim();
        if (trimmed.Equals("TransparentGloss", System.StringComparison.OrdinalIgnoreCase))
            return BeadFinish.Transparent | BeadFinish.Shiny;
        if (trimmed.Equals("TransparentMatte", System.StringComparison.OrdinalIgnoreCase))
            return BeadFinish.Transparent | BeadFinish.Matte;
        return System.Enum.TryParse<BeadFinish>(trimmed, ignoreCase: true, out var result)
            ? result
            : BeadFinish.Opaque;
    }

    /// Canonical string form used when writing v4+ files. [Flags] ToString
    /// already emits comma-separated names for combined bits, and "Opaque" for
    /// the zero value — exactly the shape FromString round-trips.
    public static string ToCanonicalString(BeadFinish f) => f.ToString();

    /// Space-separated CSS class list ("finish-transparent finish-shiny") so
    /// each primitive flag contributes its own visual layer. Opaque returns
    /// an empty string.
    public static string ToCssClass(BeadFinish f)
    {
        if (f == BeadFinish.Opaque) return "";
        var parts = new System.Collections.Generic.List<string>();
        foreach (var v in System.Enum.GetValues<BeadFinish>())
        {
            if (v == BeadFinish.Opaque) continue;
            if (f.HasFlag(v)) parts.Add($"finish-{v.ToString().ToLowerInvariant()}");
        }
        return string.Join(" ", parts);
    }

    /// Enumerate the primitive flags set in this finish, in enum declaration
    /// order. Useful for rendering one overlay per flag (BeadPoint) and for
    /// listing checked boxes in the editor.
    public static System.Collections.Generic.IEnumerable<BeadFinish> Flags(BeadFinish f)
    {
        foreach (var v in System.Enum.GetValues<BeadFinish>())
        {
            if (v == BeadFinish.Opaque) continue;
            if (f.HasFlag(v)) yield return v;
        }
    }
}
