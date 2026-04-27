#include "actions.h"

#include "imagefactory.h"
#include "mainwindow.h"

#include <QActionGroup>

namespace jbead {

Actions::Actions(MainWindow* window)
    : QObject(window), m_window(window)
{
    buildActions();
}

QAction* Actions::make(Id id,
                       const QString& text,
                       const QString& iconName,
                       const QKeySequence& shortcut,
                       const QString& description,
                       bool checkable)
{
    auto* a = new QAction(text, this);
    if (!iconName.isEmpty()) a->setIcon(ImageFactory::icon(iconName));
    if (!shortcut.isEmpty()) a->setShortcut(shortcut);
    if (!description.isEmpty()) {
        a->setStatusTip(description);
        a->setToolTip(description);
    }
    a->setCheckable(checkable);
    m_actions.insert(id, a);
    return a;
}

void Actions::buildActions()
{
    /*  Labels and descriptions reproduce legacy
        legacy/src/jbead.properties verbatim. They are wrapped in
        tr() so lupdate adds them to i18n/jbead_*.ts in Phase 4.   */

    // ---- File menu ------------------------------------------------
    make(Id::FileNew,    tr("&New"),       QStringLiteral("file.new"),
         QKeySequence::New,  tr("Creates a new pattern"));
    make(Id::FileOpen,   tr("&Open..."),   QStringLiteral("file.open"),
         QKeySequence::Open, tr("Opens a pattern"));
    make(Id::FileSave,   tr("&Save"),      QStringLiteral("file.save"),
         QKeySequence::Save, tr("Saves the pattern"));
    make(Id::FileSaveAs, tr("Save &As..."), QString(),
         QKeySequence::SaveAs, tr("Saves a pattern to a new file"));
    make(Id::FilePrint,  tr("&Print..."),  QStringLiteral("file.print"),
         QKeySequence::Print, tr("Prints the pattern"));
    make(Id::FilePrintPreview, tr("Print Pre&view..."), QString(),
         QKeySequence(), tr("Previews the print output"));
    make(Id::FilePageSetup, tr("Page Set&up..."), QString(),
         QKeySequence(), tr("Configures the page format"));
    /*  Single-page exports — all four wrap the same StripLayout
        that drives Print and Print Preview, so the on-screen
        preview matches every export format.                       */
    make(Id::FileExportPng,  tr("&PNG image..."),  QString(),
         QKeySequence(), tr("Exports the pattern as a PNG image"));
    make(Id::FileExportJpeg, tr("&JPEG image..."), QString(),
         QKeySequence(), tr("Exports the pattern as a JPEG image"));
    make(Id::FileExportSvg,  tr("&SVG vector..."), QString(),
         QKeySequence(), tr("Exports the pattern as an SVG document"));
    make(Id::FileExportPdf,  tr("PD&F document..."), QString(),
         QKeySequence(), tr("Exports the pattern as a PDF document"));
    make(Id::FileExit,   tr("E&xit"), QString(),
         QKeySequence::Quit, tr("Exits the program"));

    // ---- Edit menu ------------------------------------------------
    make(Id::EditUndo,   tr("&Undo"), QStringLiteral("edit.undo"),
         QKeySequence::Undo, tr("Undoes the last change"));
    make(Id::EditRedo,   tr("&Redo"), QStringLiteral("edit.redo"),
         QKeySequence::Redo, tr("Redoes the last undone change"));
    make(Id::EditArrange, tr("&Arrange..."), QStringLiteral("edit.arrange"),
         QKeySequence(Qt::Key_F8), tr("Arranges copies of the selected part"));
    make(Id::EditInsertRow, tr("&Insert Row"), QString(),
         QKeySequence(), tr("Inserts an empty row at the bottom"));
    make(Id::EditDeleteRow, tr("&Delete Row"), QString(),
         QKeySequence(), tr("Deletes the row at the bottom"));
    make(Id::EditMirrorHorizontal, tr("Mirror &Horizontal"), QString(),
         QKeySequence(), tr("Mirrors the selection horizontally"));
    make(Id::EditMirrorVertical,   tr("Mirror &Vertical"),   QString(),
         QKeySequence(), tr("Mirrors the selection vertically"));
    make(Id::EditRotate, tr("Rotate &90°"), QString(),
         QKeySequence(), tr("Rotates the selection 90 degrees clockwise"));
    make(Id::EditDelete, tr("Delete Selection"), QString(),
         QKeySequence(Qt::Key_Delete), tr("Deletes the selected region"));

    // ---- View menu ------------------------------------------------
    make(Id::ViewDraft,      tr("&Draft"),      QString(), QKeySequence(),
         tr("Show the draft view"), true);
    make(Id::ViewCorrected,  tr("&Corrected"),  QString(), QKeySequence(),
         tr("Show the corrected view"), true);
    make(Id::ViewSimulation, tr("&Simulation"), QString(), QKeySequence(),
         tr("Show the simulation view"), true);
    make(Id::ViewReport,     tr("&Report"),     QString(), QKeySequence(),
         tr("Show the report view"), true);
    make(Id::ViewDrawColors,  tr("Draw Colo&rs"),  QString(), QKeySequence(),
         tr("Render bead colors"), true);
    make(Id::ViewDrawSymbols, tr("Draw S&ymbols"), QString(), QKeySequence(),
         tr("Render bead symbols"), true);
    make(Id::ViewZoomIn,     tr("Zoom &In"),  QStringLiteral("view.zoomin"),
         QKeySequence(QStringLiteral("Ctrl+I")), tr("Zooms in"));
    make(Id::ViewZoomOut,    tr("Zoom &Out"), QStringLiteral("view.zoomout"),
         QKeySequence(QStringLiteral("Ctrl+U")), tr("Zooms out"));
    make(Id::ViewZoomNormal, tr("&Normal Zoom"), QString(),
         QKeySequence(QStringLiteral("Ctrl+0")), tr("Resets the zoom"));

    /*  Default toggles match the .jbb view block defaults.        */
    action(Id::ViewDraft)->setChecked(true);
    action(Id::ViewCorrected)->setChecked(true);
    action(Id::ViewSimulation)->setChecked(true);
    action(Id::ViewReport)->setChecked(true);
    action(Id::ViewDrawColors)->setChecked(true);
    action(Id::ViewDrawSymbols)->setChecked(false);

    // ---- Tools (exclusive group) ---------------------------------
    auto* tools = new QActionGroup(this);
    tools->setExclusive(true);
    auto addTool = [&](Id id, const QString& text, const QString& icon,
                       Qt::Key key, const QString& tip) {
        QAction* a = make(id, text, icon, QKeySequence(key), tip, true);
        tools->addAction(a);
    };
    addTool(Id::ToolPencil,  tr("&Pencil"),   QStringLiteral("tool.pencil"),
            Qt::Key_P, tr("Draws beads"));
    addTool(Id::ToolSelect,  tr("&Select"),   QStringLiteral("tool.select"),
            Qt::Key_S, tr("Selects a region"));
    addTool(Id::ToolFill,    tr("&Fill"),     QStringLiteral("tool.fill"),
            Qt::Key_F, tr("Fills a contiguous region"));
    addTool(Id::ToolPipette, tr("Pip&ette"),  QStringLiteral("tool.pipette"),
            Qt::Key_E, tr("Picks a color from the pattern"));
    action(Id::ToolPencil)->setChecked(true);

    // ---- Pattern menu --------------------------------------------
    make(Id::PatternWidth,  tr("&Width..."),  QString(),
         QKeySequence(), tr("Sets the pattern width"));
    make(Id::PatternHeight, tr("&Height..."), QString(),
         QKeySequence(), tr("Sets the pattern height"));
    make(Id::PatternPreferences, tr("&Preferences..."), QString(),
         QKeySequence(), tr("Opens the preferences dialog"));

    // ---- Info menu -----------------------------------------------
    make(Id::InfoTechInfos, tr("Technical &Infos..."), QString(),
         QKeySequence(), tr("Shows technical information about the pattern"));
    make(Id::InfoAbout,     tr("&About JBead..."), QString(),
         QKeySequence(), tr("Shows the about dialog"));

    // ---- Rotation (toolbar) --------------------------------------
    make(Id::RotateLeft,  tr("Rotate &Left"),  QStringLiteral("view.rotateleft"),
         QKeySequence(Qt::Key_Left),  tr("Rotates the simulation tube to the left"));
    make(Id::RotateRight, tr("Rotate &Right"), QStringLiteral("view.rotateright"),
         QKeySequence(Qt::Key_Right), tr("Rotates the simulation tube to the right"));
}

Actions::Id Actions::currentTool() const
{
    if (action(Id::ToolPencil )->isChecked()) return Id::ToolPencil;
    if (action(Id::ToolSelect )->isChecked()) return Id::ToolSelect;
    if (action(Id::ToolFill   )->isChecked()) return Id::ToolFill;
    if (action(Id::ToolPipette)->isChecked()) return Id::ToolPipette;
    return Id::ToolPencil;
}

void Actions::setCurrentTool(Id id)
{
    QAction* a = action(id);
    if (a && a->isCheckable()) a->setChecked(true);
}

} // namespace jbead
