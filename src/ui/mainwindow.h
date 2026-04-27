#pragma once

#include "actions.h"
#include "domain/beadpoint.h"

#include <QMainWindow>
#include <QString>

class QLabel;
class QScrollBar;
class QSplitter;

namespace jbead {

class ColorsToolbar;
class CorrectedPanel;
class DraftPanel;
class Model;
class ReportPanel;
class Selection;
class SimulationPanel;

/*  Top-level application window. Owns the document Model, the
    rectangular Selection, the Actions registry, and the four
    pattern-view widgets. Layout matches legacy JBeadFrame:
    menubar + two toolbars (main + colour palette) on top,
    statusbar on the bottom, and a horizontal row of
    Draft / Corrected / Simulation / Report views plus a vertical
    scroll bar in the centre.                                     */
class MainWindow : public QMainWindow
{
    Q_OBJECT
public:
    explicit MainWindow(QWidget* parent = nullptr);
    ~MainWindow() override;

    Model*     model()     const { return m_model; }
    Selection* selection() const { return m_selection; }
    Actions*   actions()   const { return m_actions; }

    /*  View-toggle state. The four canvases read these to decide
        whether to draw colours / symbols, and the top-level layout
        hides panels whose checkbox is unchecked.                  */
    bool drawColors() const;
    bool drawSymbols() const;

    /*  Convenience shortcut used by the pipette tool.            */
    void selectColor(int colorIndex);

protected:
    void closeEvent(QCloseEvent* event) override;
    void keyPressEvent(QKeyEvent* event) override;

private slots:
    void doFileNew();
    void doFileOpen();
    void doFileSave();
    void doFileSaveAs();
    void doFilePrint();
    void doFilePrintPreview();
    void doFilePageSetup();
    void doFileExportPdf();
    void doFileExit();
    void doEditUndo();
    void doEditRedo();
    void doEditArrange();
    void doEditMirrorH();
    void doEditMirrorV();
    void doEditRotate();
    void doEditDelete();
    void doEditInsertRow();
    void doEditDeleteRow();
    void doViewToggleVisibility();
    void doViewDrawModeChanged();
    void doViewZoomIn();
    void doViewZoomOut();
    void doViewZoomNormal();
    void doPatternWidth();
    void doPatternHeight();
    void doInfoAbout();
    void doRotateLeft();
    void doRotateRight();
    void onModelChanged();
    void onScrollbarMoved(int value);
    void onScrollChanged(int scroll);
    void onZoomChanged(int gridx, int gridy);
    void onRepeatChanged(int repeat);
    void onShiftChanged(int shift);
    void onSelectionUpdated();

private:
    bool maybeSave();
    bool saveTo(const QString& path);
    bool loadFrom(const QString& path);
    void setCurrentFile(const QString& path);
    void buildMenuBar();
    void buildToolbars();
    void buildStatusBar();
    void buildCenter();
    void updateScrollbar();
    void updateTitle();
    void updateStatusBar();

    Model*           m_model        = nullptr;
    Selection*       m_selection    = nullptr;
    Actions*         m_actions      = nullptr;
    DraftPanel*      m_draft        = nullptr;
    CorrectedPanel*  m_corrected    = nullptr;
    SimulationPanel* m_simulation   = nullptr;
    ReportPanel*     m_report       = nullptr;
    ColorsToolbar*   m_colorsToolbar = nullptr;
    QSplitter*       m_centralSplitter = nullptr;
    QScrollBar*      m_scrollbar    = nullptr;
    QLabel*          m_draftLabel       = nullptr;
    QLabel*          m_correctedLabel   = nullptr;
    QLabel*          m_simulationLabel  = nullptr;
    QLabel*          m_reportLabel      = nullptr;
    QLabel*          m_statusCursorLabel    = nullptr;
    QLabel*          m_statusSelectionLabel = nullptr;
    QLabel*          m_statusRepeatLabel    = nullptr;
    QLabel*          m_statusToolLabel      = nullptr;
};

} // namespace jbead
