namespace JBead.Web.Core;

/// Which editor view(s) are visible in the canvas area. On wide screens Both is
/// convenient (edit on the left, live 3D preview on the right); narrower viewports
/// usually want just one at a time.
public enum ViewMode
{
    Draft = 0,
    Simulation = 1,
    Both = 2,
}
