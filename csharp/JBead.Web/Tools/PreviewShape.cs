namespace JBead.Web.Tools;

using JBead.Web.Core;

/// Grid-space shapes a tool wants painted on the preview layer. DraftPreview
/// converts them to SVG coords — tools stay free of rendering concerns.
public abstract record PreviewShape;

/// Straight line between the centers of two cells (e.g. pencil line preview).
public record PreviewLine(Point From, Point To) : PreviewShape;

/// Dashed rectangle spanning between two cells (e.g. fill-bounds hint).
public record PreviewRect(Point From, Point To) : PreviewShape;

/// Solid outline around one cell (e.g. pipette target, single-cell fill target).
public record PreviewCell(Point At) : PreviewShape;
