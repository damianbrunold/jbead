#pragma once

#include <QAction>
#include <QHash>
#include <QObject>
#include <QString>

namespace jbead {

class MainWindow;

/*  Central registry of every QAction in the app. Mirrors the
    36-action surface from legacy ch.jbead.action — the menu /
    toolbar layout in mainwindow.cpp pulls from this map by id.
    Action ids carry the same dotted form as the legacy
    .properties keys ("file.new", "edit.undo", "view.zoomin", …);
    that gives a 1:1 mapping into the .ts translation catalog
    populated in Phase 4.                                          */
class Actions : public QObject
{
    Q_OBJECT
public:
    enum class Id {
        // File
        FileNew, FileOpen, FileSave, FileSaveAs,
        FilePrint, FilePrintPreview, FilePageSetup, FileExportPdf, FileExit,
        // Edit
        EditUndo, EditRedo, EditArrange,
        EditMirrorHorizontal, EditMirrorVertical, EditRotate, EditDelete,
        EditInsertRow, EditDeleteRow,
        // View
        ViewDraft, ViewCorrected, ViewSimulation, ViewReport,
        ViewDrawColors, ViewDrawSymbols,
        ViewZoomIn, ViewZoomOut, ViewZoomNormal,
        // Tools
        ToolPencil, ToolSelect, ToolFill, ToolPipette,
        // Pattern
        PatternWidth, PatternHeight, PatternPreferences,
        // Info
        InfoTechInfos, InfoAbout,
        // Toolbar-only rotation
        RotateLeft, RotateRight,
    };

    explicit Actions(MainWindow* window);

    QAction* action(Id id) const { return m_actions.value(id); }

    /*  Tools form an exclusive group — toggling one un-toggles the
        rest. Returns the currently selected tool, or ToolPencil if
        nothing is selected (the default at startup).              */
    Id      currentTool() const;
    void    setCurrentTool(Id id);

private:
    void buildActions();
    QAction* make(Id id,
                  const QString& text,
                  const QString& iconName = QString(),
                  const QKeySequence& shortcut = QKeySequence(),
                  const QString& description = QString(),
                  bool checkable = false);

    MainWindow*           m_window;
    QHash<Id, QAction*>   m_actions;
};

} // namespace jbead
