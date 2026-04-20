using JBead.Web.Tools;

namespace JBead.Web.Core;

public class AppState
{
    public BeadModel Model { get; } = new();
    public Selection Selection { get; } = new();
    public bool IsDragging { get; set; }

    /// The palette index currently under the pointer in the draft view.
    /// null when nothing is hovered; fires HoveredChanged on transitions only.
    private int? hoveredColorIndex;
    public event Action<int?>? HoveredChanged;

    public int? HoveredColorIndex
    {
        get => hoveredColorIndex;
        set
        {
            if (hoveredColorIndex == value) return;
            hoveredColorIndex = value;
            HoveredChanged?.Invoke(value);
        }
    }

    private ViewMode viewMode = ViewMode.Draft;
    public event Action<ViewMode>? ViewModeChanged;
    public ViewMode ViewMode
    {
        get => viewMode;
        set
        {
            if (viewMode == value) return;
            viewMode = value;
            ViewModeChanged?.Invoke(value);
        }
    }


    /// All tools registered in DI, in registration order.
    public IReadOnlyList<ITool> Tools { get; }

    private ITool selectedTool;
    public event Action<ITool>? SelectedToolChanged;

    public ITool SelectedTool
    {
        get => selectedTool;
        set
        {
            if (ReferenceEquals(selectedTool, value)) return;
            selectedTool = value;
            SelectedToolChanged?.Invoke(value);
        }
    }

    public AppState(IEnumerable<ITool> tools)
    {
        Tools = tools.ToList();
        if (Tools.Count == 0) throw new InvalidOperationException("No ITool registered in DI.");
        selectedTool = Tools[0];
    }

    // ---- Clipboard / selection actions ----

    private byte[,]? clipboard;
    public event Action? ClipboardChanged;
    public event Action? SelectionActionsChanged;

    public bool HasClipboard => clipboard is not null;
    public bool HasUsableSelection => Selection.IsSet && Selection.IsActive;
    /// Paste is allowed as soon as the user has picked a target cell —
    /// either a dragged rect or a single click. PasteRect anchors at the
    /// bottom-left and uses the clipboard's own size, so a one-cell
    /// selection is a valid anchor.
    public bool CanPaste => Selection.IsSet && clipboard is not null;

    public void CopySelection()
    {
        if (!HasUsableSelection) return;
        clipboard = Model.CopyRect(Selection);
        ClipboardChanged?.Invoke();
    }

    public void PasteSelection()
    {
        if (!CanPaste) return;
        Model.PasteRect(Selection, clipboard!);
    }

    public void FillSelection()
    {
        if (!HasUsableSelection) return;
        Model.FillRect(Selection);
    }

    /// Re-evaluate which selection-action buttons should be enabled. Components
    /// listening to this re-render — used when the selection rectangle changes.
    public void NotifySelectionActionsChanged() => SelectionActionsChanged?.Invoke();

    // ---- Simulation zoom ----
    // Independent of the Draft grid. 20 px = 100 %; clamp to 5–40 px (25 %–200 %).
    public const int SimulationBaseGridPx = 20;
    public const int SimulationMinGridPx = 5;
    public const int SimulationMaxGridPx = 40;

    private int simulationGridPx = SimulationBaseGridPx;
    public event Action<int>? SimulationGridPxChanged;

    public int SimulationGridPx
    {
        get => simulationGridPx;
        set
        {
            var clamped = Math.Clamp(value, SimulationMinGridPx, SimulationMaxGridPx);
            if (simulationGridPx == clamped) return;
            simulationGridPx = clamped;
            SimulationGridPxChanged?.Invoke(clamped);
        }
    }

    public int SimulationZoomPercent => (int)Math.Round(100.0 * simulationGridPx / SimulationBaseGridPx);
}
