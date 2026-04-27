#include "mainwindow.h"

#include "colorscheme.h"
#include "colorstoolbar.h"
#include "correctedpanel.h"
#include "dialogs.h"
#include "domain/model.h"
#include "domain/selection.h"
#include "draftpanel.h"
#include "io/fileformat.h"
#include "mrumanager.h"
#include "print/printjob.h"
#include "print/printsettings.h"
#include "reportpanel.h"
#include "simulationpanel.h"

#include <QApplication>
#include <QCloseEvent>
#include <QFileDialog>
#include <QFileInfo>
#include <QHBoxLayout>
#include <QKeyEvent>
#include <QLabel>
#include <QMenuBar>
#include <QMessageBox>
#include <QPageSetupDialog>
#include <QPrintDialog>
#include <QPrintPreviewDialog>
#include <QPrinter>
#include <QScrollBar>
#include <QSettings>
#include <QStandardPaths>
#include <QStyleHints>
#include <QTimer>
#include <QSplitter>
#include <QStatusBar>
#include <QToolBar>
#include <QVBoxLayout>

namespace jbead {

MainWindow::MainWindow(QWidget* parent)
    : QMainWindow(parent),
      m_model(new Model(this)),
      m_selection(new Selection(this)),
      m_actions(new Actions(this))
{
    m_model->clear();

    buildCenter();
    buildMenuBar();
    buildToolbars();
    buildStatusBar();

    /*  Wire actions to slots after the menubar/toolbar exist so
        the connections fire correctly when the user picks them.   */
    connect(m_actions->action(Actions::Id::FileNew),    &QAction::triggered, this, &MainWindow::doFileNew);
    connect(m_actions->action(Actions::Id::FileOpen),   &QAction::triggered, this, &MainWindow::doFileOpen);
    connect(m_actions->action(Actions::Id::FileSave),   &QAction::triggered, this, &MainWindow::doFileSave);
    connect(m_actions->action(Actions::Id::FileSaveAs), &QAction::triggered, this, &MainWindow::doFileSaveAs);
    connect(m_actions->action(Actions::Id::FilePrint),         &QAction::triggered, this, &MainWindow::doFilePrint);
    connect(m_actions->action(Actions::Id::FilePrintPreview),  &QAction::triggered, this, &MainWindow::doFilePrintPreview);
    connect(m_actions->action(Actions::Id::FilePageSetup),     &QAction::triggered, this, &MainWindow::doFilePageSetup);
    connect(m_actions->action(Actions::Id::FileExportPng),     &QAction::triggered, this, &MainWindow::doFileExportPng);
    connect(m_actions->action(Actions::Id::FileExportJpeg),    &QAction::triggered, this, &MainWindow::doFileExportJpeg);
    connect(m_actions->action(Actions::Id::FileExportSvg),     &QAction::triggered, this, &MainWindow::doFileExportSvg);
    connect(m_actions->action(Actions::Id::FileExportPdf),     &QAction::triggered, this, &MainWindow::doFileExportPdf);
    connect(m_actions->action(Actions::Id::FileExit),   &QAction::triggered, this, &MainWindow::doFileExit);
    connect(m_actions->action(Actions::Id::EditUndo),   &QAction::triggered, this, &MainWindow::doEditUndo);
    connect(m_actions->action(Actions::Id::EditRedo),   &QAction::triggered, this, &MainWindow::doEditRedo);
    connect(m_actions->action(Actions::Id::EditArrange),       &QAction::triggered, this, &MainWindow::doEditArrange);
    connect(m_actions->action(Actions::Id::EditMirrorHorizontal), &QAction::triggered, this, &MainWindow::doEditMirrorH);
    connect(m_actions->action(Actions::Id::EditMirrorVertical),   &QAction::triggered, this, &MainWindow::doEditMirrorV);
    connect(m_actions->action(Actions::Id::EditRotate),         &QAction::triggered, this, &MainWindow::doEditRotate);
    connect(m_actions->action(Actions::Id::EditDelete),         &QAction::triggered, this, &MainWindow::doEditDelete);
    connect(m_actions->action(Actions::Id::EditInsertRow),      &QAction::triggered, this, &MainWindow::doEditInsertRow);
    connect(m_actions->action(Actions::Id::EditDeleteRow),      &QAction::triggered, this, &MainWindow::doEditDeleteRow);

    for (Actions::Id id : { Actions::Id::ViewDraft, Actions::Id::ViewCorrected,
                            Actions::Id::ViewSimulation, Actions::Id::ViewReport }) {
        connect(m_actions->action(id), &QAction::toggled, this, &MainWindow::doViewToggleVisibility);
    }
    for (Actions::Id id : { Actions::Id::ViewDrawColors, Actions::Id::ViewDrawSymbols }) {
        connect(m_actions->action(id), &QAction::toggled, this, &MainWindow::doViewDrawModeChanged);
    }
    connect(m_actions->action(Actions::Id::ViewZoomIn),     &QAction::triggered, this, &MainWindow::doViewZoomIn);
    connect(m_actions->action(Actions::Id::ViewZoomOut),    &QAction::triggered, this, &MainWindow::doViewZoomOut);
    connect(m_actions->action(Actions::Id::ViewZoomNormal), &QAction::triggered, this, &MainWindow::doViewZoomNormal);
    connect(m_actions->action(Actions::Id::PatternWidth),   &QAction::triggered, this, &MainWindow::doPatternWidth);
    connect(m_actions->action(Actions::Id::PatternHeight),  &QAction::triggered, this, &MainWindow::doPatternHeight);
    connect(m_actions->action(Actions::Id::PatternPalette),     &QAction::triggered, this, &MainWindow::doPatternPalette);
    connect(m_actions->action(Actions::Id::PatternPreferences), &QAction::triggered, this, &MainWindow::doPatternPreferences);
    connect(m_actions->action(Actions::Id::InfoTechInfos),  &QAction::triggered, this, &MainWindow::doInfoTechInfos);
    connect(m_actions->action(Actions::Id::InfoAbout),      &QAction::triggered, this, &MainWindow::doInfoAbout);
    connect(m_actions->action(Actions::Id::RotateLeft),     &QAction::triggered, this, &MainWindow::doRotateLeft);
    connect(m_actions->action(Actions::Id::RotateRight),    &QAction::triggered, this, &MainWindow::doRotateRight);

    connect(m_model, &Model::modelChanged,  this, &MainWindow::onModelChanged);
    connect(m_model, &Model::scrollChanged, this, &MainWindow::onScrollChanged);
    connect(m_model, &Model::zoomChanged,   this, &MainWindow::onZoomChanged);
    connect(m_model, &Model::repeatChanged, this, &MainWindow::onRepeatChanged);
    connect(m_model, &Model::shiftChanged,  this, &MainWindow::onShiftChanged);
    /*  Per-cell edits and palette tweaks don't fire modelChanged
        (see Model::setPoint / setColor — they emit pointChanged
        and colorChanged). Title still has to grow / drop the "*"
        marker on every modified state change, so refresh it from
        these signals too.                                         */
    connect(m_model, &Model::pointChanged,  this, [this](BeadPoint) {
        updateTitle();
        updateScrollbar();           // usedHeight may have grown
    });
    connect(m_model, &Model::colorChanged,  this, [this](int)       { updateTitle(); });
    connect(m_selection, &Selection::selectionUpdated, this, &MainWindow::onSelectionUpdated);
    connect(m_selection, &Selection::selectionDeleted, this, &MainWindow::onSelectionUpdated);

    setWindowTitle(QStringLiteral("JBead"));
    updateScrollbar();
    updateTitle();
    updateStatusBar();

    /*  Idle-timer hook for redo. BeadUndo only stores the *pre*-state
        on snapshot(), so a single edit followed by undo cannot be
        redone — there's nothing in the next slot. The legacy app
        runs prepareSnapshot() periodically from a Swing Timer; this
        is the same idea: every 500 ms we copy the current field over
        the next slot in the ring buffer (no advance), so a
        subsequent undo/redo can round-trip back to the post-edit
        state. The call is a no-op when the model isn't modified.  */
    m_idleTimer = new QTimer(this);
    m_idleTimer->setInterval(500);
    connect(m_idleTimer, &QTimer::timeout, this,
            [this]() { m_model->prepareSnapshot(); });
    m_idleTimer->start();

    /*  Restore window geometry + dock layout from the previous run.
        Stored under "MainWindow/" so the keys don't collide with the
        document-level state in the .jbb file. Falls back to a sane
        default size on first run.                                  */
    {
        QSettings s;
        s.beginGroup(QStringLiteral("MainWindow"));
        const QByteArray geom  = s.value(QStringLiteral("geometry")).toByteArray();
        const QByteArray state = s.value(QStringLiteral("state")).toByteArray();
        const QByteArray split = s.value(QStringLiteral("splitter")).toByteArray();
        s.endGroup();
        const bool restored = !geom.isEmpty() && restoreGeometry(geom);
        if (!state.isEmpty()) restoreState(state);
        if (!split.isEmpty()) m_centralSplitter->restoreState(split);
        if (!restored) resize(1280, 800);
    }
}

MainWindow::~MainWindow() = default;

bool MainWindow::drawColors()  const { return m_actions->action(Actions::Id::ViewDrawColors)->isChecked(); }
bool MainWindow::drawSymbols() const { return m_actions->action(Actions::Id::ViewDrawSymbols)->isChecked(); }

void MainWindow::selectColor(int colorIndex)
{
    if (colorIndex < 0 || colorIndex >= m_model->colorCount()) return;
    /*  setSelectedColor emits selectedColorChanged which the
        colours toolbar (and any other listener) repaints from.
        No manual update() needed.                                 */
    m_model->setSelectedColor(static_cast<std::int8_t>(colorIndex));
}

// -----------------------------------------------------------------
// Layout construction
// -----------------------------------------------------------------

void MainWindow::buildCenter()
{
    auto* central = new QWidget(this);
    auto* outer   = new QHBoxLayout(central);
    outer->setContentsMargins(0, 0, 0, 0);

    m_centralSplitter = new QSplitter(Qt::Horizontal, central);
    m_centralSplitter->setChildrenCollapsible(false);

    auto wrapWithLabel = [&](QWidget* canvas, QLabel*& outLabel, const QString& text) {
        auto* col = new QWidget;
        auto* v = new QVBoxLayout(col);
        v->setContentsMargins(0, 0, 0, 0);
        v->addWidget(canvas, 1);
        outLabel = new QLabel(text);
        outLabel->setAlignment(Qt::AlignHCenter);
        v->addWidget(outLabel);
        return col;
    };

    m_draft       = new DraftPanel(m_model, m_selection, this);
    m_corrected   = new CorrectedPanel(m_model, m_selection, this);
    m_simulation  = new SimulationPanel(m_model, m_selection, this);
    m_report      = new ReportPanel(m_model, m_selection, this);

    m_centralSplitter->addWidget(wrapWithLabel(m_draft,       m_draftLabel,      tr("Draft")));
    m_centralSplitter->addWidget(wrapWithLabel(m_corrected,   m_correctedLabel,  tr("Corrected")));
    m_centralSplitter->addWidget(wrapWithLabel(m_simulation,  m_simulationLabel, tr("Simulation")));
    m_centralSplitter->addWidget(wrapWithLabel(m_report,      m_reportLabel,     tr("Report")));
    m_centralSplitter->setStretchFactor(0, 1);
    m_centralSplitter->setStretchFactor(1, 1);
    m_centralSplitter->setStretchFactor(2, 1);
    m_centralSplitter->setStretchFactor(3, 5);

    outer->addWidget(m_centralSplitter, 1);

    m_scrollbar = new QScrollBar(Qt::Vertical, central);
    m_scrollbar->setRange(0, m_model->height() - 1);
    connect(m_scrollbar, &QScrollBar::valueChanged, this, &MainWindow::onScrollbarMoved);
    outer->addWidget(m_scrollbar);

    setCentralWidget(central);
}

void MainWindow::buildMenuBar()
{
    QMenuBar* mb = menuBar();

    auto* fileMenu = mb->addMenu(tr("&File"));
    fileMenu->addAction(m_actions->action(Actions::Id::FileNew));
    fileMenu->addAction(m_actions->action(Actions::Id::FileOpen));
    fileMenu->addAction(m_actions->action(Actions::Id::FileSave));
    fileMenu->addAction(m_actions->action(Actions::Id::FileSaveAs));
    /*  Recent Files lives between Save As and Print, mirroring the
        legacy menu placement. The MruManager owns its actions and
        replaces them on every addPath() call.                     */
    QMenu* recentMenu = fileMenu->addMenu(tr("&Recent Files"));
    m_mru = new MruManager(recentMenu, this);
    connect(m_mru, &MruManager::openRequested, this, [this](const QString& path) {
        if (!maybeSave()) return;
        loadFrom(path);
        updateScrollbar();
    });
    fileMenu->addSeparator();
    fileMenu->addAction(m_actions->action(Actions::Id::FilePrint));
    fileMenu->addAction(m_actions->action(Actions::Id::FilePrintPreview));
    fileMenu->addAction(m_actions->action(Actions::Id::FilePageSetup));
    /*  Single-page exports cluster under one submenu (textile-style)
        so the File menu doesn't sprawl into a column of "Export X"
        entries. PNG / JPEG / SVG / PDF all use the same StripLayout
        renderer that drives Print Preview.                          */
    QMenu* exportMenu = fileMenu->addMenu(tr("&Export"));
    exportMenu->addAction(m_actions->action(Actions::Id::FileExportPng));
    exportMenu->addAction(m_actions->action(Actions::Id::FileExportJpeg));
    exportMenu->addAction(m_actions->action(Actions::Id::FileExportSvg));
    exportMenu->addAction(m_actions->action(Actions::Id::FileExportPdf));
    fileMenu->addSeparator();
    fileMenu->addAction(m_actions->action(Actions::Id::FileExit));

    auto* editMenu = mb->addMenu(tr("&Edit"));
    editMenu->addAction(m_actions->action(Actions::Id::EditUndo));
    editMenu->addAction(m_actions->action(Actions::Id::EditRedo));
    editMenu->addSeparator();
    editMenu->addAction(m_actions->action(Actions::Id::EditArrange));
    editMenu->addAction(m_actions->action(Actions::Id::EditMirrorHorizontal));
    editMenu->addAction(m_actions->action(Actions::Id::EditMirrorVertical));
    editMenu->addAction(m_actions->action(Actions::Id::EditRotate));
    editMenu->addAction(m_actions->action(Actions::Id::EditDelete));
    auto* rowMenu = editMenu->addMenu(tr("Ro&w"));
    rowMenu->addAction(m_actions->action(Actions::Id::EditInsertRow));
    rowMenu->addAction(m_actions->action(Actions::Id::EditDeleteRow));

    auto* viewMenu = mb->addMenu(tr("&View"));
    viewMenu->addAction(m_actions->action(Actions::Id::ViewDraft));
    viewMenu->addAction(m_actions->action(Actions::Id::ViewCorrected));
    viewMenu->addAction(m_actions->action(Actions::Id::ViewSimulation));
    viewMenu->addAction(m_actions->action(Actions::Id::ViewReport));
    viewMenu->addSeparator();
    viewMenu->addAction(m_actions->action(Actions::Id::ViewDrawColors));
    viewMenu->addAction(m_actions->action(Actions::Id::ViewDrawSymbols));
    viewMenu->addSeparator();
    viewMenu->addAction(m_actions->action(Actions::Id::ViewZoomIn));
    viewMenu->addAction(m_actions->action(Actions::Id::ViewZoomOut));
    viewMenu->addAction(m_actions->action(Actions::Id::ViewZoomNormal));

    auto* toolsMenu = mb->addMenu(tr("&Tools"));
    toolsMenu->addAction(m_actions->action(Actions::Id::ToolPencil));
    toolsMenu->addAction(m_actions->action(Actions::Id::ToolSelect));
    toolsMenu->addAction(m_actions->action(Actions::Id::ToolFill));
    toolsMenu->addAction(m_actions->action(Actions::Id::ToolPipette));

    auto* patternMenu = mb->addMenu(tr("&Pattern"));
    patternMenu->addAction(m_actions->action(Actions::Id::PatternWidth));
    patternMenu->addAction(m_actions->action(Actions::Id::PatternHeight));
    patternMenu->addSeparator();
    patternMenu->addAction(m_actions->action(Actions::Id::PatternPalette));
    patternMenu->addSeparator();
    patternMenu->addAction(m_actions->action(Actions::Id::PatternPreferences));

    auto* infoMenu = mb->addMenu(tr("&Info"));
    infoMenu->addAction(m_actions->action(Actions::Id::InfoTechInfos));
    infoMenu->addAction(m_actions->action(Actions::Id::InfoAbout));
}

void MainWindow::buildToolbars()
{
    auto* main = addToolBar(tr("Main"));
    /*  saveState/restoreState identifies dock widgets and toolbars
        by objectName(); without it Qt warns and the layout slot
        round-trips no info for that bar.                          */
    main->setObjectName(QStringLiteral("MainToolBar"));
    main->setMovable(false);
    main->addAction(m_actions->action(Actions::Id::FileNew));
    main->addAction(m_actions->action(Actions::Id::FileOpen));
    main->addAction(m_actions->action(Actions::Id::FileSave));
    main->addAction(m_actions->action(Actions::Id::FilePrint));
    main->addSeparator();
    main->addAction(m_actions->action(Actions::Id::EditUndo));
    main->addAction(m_actions->action(Actions::Id::EditRedo));
    main->addAction(m_actions->action(Actions::Id::EditArrange));
    main->addSeparator();
    main->addAction(m_actions->action(Actions::Id::RotateLeft));
    main->addAction(m_actions->action(Actions::Id::RotateRight));
    main->addSeparator();
    main->addAction(m_actions->action(Actions::Id::ToolPencil));
    main->addAction(m_actions->action(Actions::Id::ToolSelect));
    main->addAction(m_actions->action(Actions::Id::ToolFill));
    main->addAction(m_actions->action(Actions::Id::ToolPipette));
    main->addSeparator();
    main->addAction(m_actions->action(Actions::Id::ViewZoomIn));
    main->addAction(m_actions->action(Actions::Id::ViewZoomOut));

    addToolBarBreak();
    m_colorsToolbar = new ColorsToolbar(m_model, this);
    m_colorsToolbar->setObjectName(QStringLiteral("ColorsToolBar"));
    addToolBar(m_colorsToolbar);
}

void MainWindow::buildStatusBar()
{
    m_statusCursorLabel    = new QLabel(this);
    m_statusSelectionLabel = new QLabel(this);
    m_statusRepeatLabel    = new QLabel(this);
    m_statusToolLabel      = new QLabel(this);
    statusBar()->addPermanentWidget(m_statusCursorLabel);
    statusBar()->addPermanentWidget(m_statusSelectionLabel);
    statusBar()->addPermanentWidget(m_statusRepeatLabel);
    statusBar()->addPermanentWidget(m_statusToolLabel);
}

// -----------------------------------------------------------------
// File operations
// -----------------------------------------------------------------

bool MainWindow::maybeSave()
{
    if (!m_model->isModified()) return true;
    auto rc = QMessageBox::warning(this, tr("JBead"),
        tr("The pattern has unsaved changes. Save before continuing?"),
        QMessageBox::Save | QMessageBox::Discard | QMessageBox::Cancel);
    if (rc == QMessageBox::Save)   return saveTo(m_model->filePath());
    if (rc == QMessageBox::Cancel) return false;
    return true;
}

bool MainWindow::saveTo(const QString& path)
{
    if (path.isEmpty() || !QFileInfo(path).isAbsolute()) {
        const QString picked = QFileDialog::getSaveFileName(this, tr("Save"),
            lastFileDirectory(), FileFormat::jbeadNameFilter());
        if (picked.isEmpty()) return false;
        rememberFileDirectory(picked);
        return saveTo(picked);
    }
    /*  QFileDialog on most platforms doesn't auto-append the
        selected filter's extension when the user types a bare
        filename. Patch that here: anything that doesn't already
        end in .jbb or .dbb (case-insensitive) gets ".jbb"
        appended so the on-disk format always matches what we
        actually wrote.                                            */
    QString finalPath = path;
    if (!finalPath.endsWith(QStringLiteral(".jbb"), Qt::CaseInsensitive)
     && !finalPath.endsWith(QStringLiteral(".dbb"), Qt::CaseInsensitive)) {
        finalPath += QStringLiteral(".jbb");
    }
    try {
        FileFormat::save(finalPath, *m_model);
        setCurrentFile(finalPath);
        rememberFileDirectory(finalPath);
        return true;
    } catch (const std::exception& e) {
        QMessageBox::critical(this, tr("Save failed"), QString::fromStdString(e.what()));
        return false;
    }
}

QString MainWindow::lastFileDirectory() const
{
    QSettings s;
    const QString stored = s.value(QStringLiteral("Files/lastDirectory")).toString();
    if (!stored.isEmpty() && QFileInfo(stored).isDir()) return stored;
    return QStandardPaths::writableLocation(QStandardPaths::DocumentsLocation);
}

void MainWindow::rememberFileDirectory(const QString& path)
{
    const QString dir = QFileInfo(path).absolutePath();
    if (dir.isEmpty()) return;
    QSettings s;
    s.setValue(QStringLiteral("Files/lastDirectory"), dir);
}

bool MainWindow::loadFrom(const QString& path)
{
    try {
        m_model->clear();
        m_selection->clear();
        FileFormat::load(path, *m_model);
        setCurrentFile(path);
        return true;
    } catch (const std::exception& e) {
        QMessageBox::critical(this, tr("Open failed"), QString::fromStdString(e.what()));
        return false;
    }
}

void MainWindow::setCurrentFile(const QString& path)
{
    m_model->setFilePath(path);
    m_model->setSaved();
    m_model->setModified(false);
    if (m_mru) m_mru->addPath(path);
    updateTitle();
}

void MainWindow::doFileNew()
{
    if (!maybeSave()) return;
    m_model->clear();
    m_selection->clear();
    updateScrollbar();
    updateTitle();
}

void MainWindow::doFileOpen()
{
    if (!maybeSave()) return;
    const QString path = QFileDialog::getOpenFileName(this, tr("Open"),
        lastFileDirectory(), FileFormat::combinedNameFilter());
    if (path.isEmpty()) return;
    rememberFileDirectory(path);
    loadFrom(path);
    updateScrollbar();
}

void MainWindow::doFileSave()    { saveTo(m_model->isSaved() ? m_model->filePath() : QString()); }
void MainWindow::doFileSaveAs()  { saveTo(QString()); }
void MainWindow::doFileExit()    { close(); }

// -----------------------------------------------------------------
// Printing
// -----------------------------------------------------------------

PrintSettings MainWindow::currentPrintSettings() const
{
    PrintSettings s;
    s.load();
    /*  View-menu visibility is the contract for "what's part of
        the document": hiding a canvas drops it from prints and
        exports too. Bead list rides along with the report panel
        because that's what holds the per-pattern summary block —
        the user has no separate toggle for it.                   */
    s.printDraft       = m_actions->action(Actions::Id::ViewDraft)->isChecked();
    s.printCorrected   = m_actions->action(Actions::Id::ViewCorrected)->isChecked();
    s.printSimulation  = m_actions->action(Actions::Id::ViewSimulation)->isChecked();
    s.printReport      = m_actions->action(Actions::Id::ViewReport)->isChecked();
    s.printBeadList    = s.printReport;
    /*  Honour the View-menu Draw Colors / Draw Symbols toggles in
        print and export. stripes.jbb has draw-symbols=false so its
        cells should render as solid colour swatches without glyphs;
        the previous code drew symbols unconditionally, which made
        small cells look like cluttered text overlays.              */
    s.drawColors       = m_actions->action(Actions::Id::ViewDrawColors)->isChecked();
    s.drawSymbols      = m_actions->action(Actions::Id::ViewDrawSymbols)->isChecked();
    return s;
}

void MainWindow::doFilePrint()
{
    PrintSettings settings = currentPrintSettings();

    QPrinter printer(QPrinter::HighResolution);
    settings.apply(&printer);

    QPrintDialog dlg(&printer, this);
    dlg.setWindowTitle(tr("Print"));
    if (dlg.exec() != QDialog::Accepted) return;

    /*  Persist whatever the user changed in the dialog so the
        next print run starts from those values.                  */
    settings.readFromPrinter(&printer);
    settings.save();

    PrintJob job(*m_model, settings);
    if (!job.run(&printer)) {
        QMessageBox::warning(this, tr("Print"),
            tr("Print job produced no output."));
    }
}

void MainWindow::doFilePrintPreview()
{
    PrintSettings settings = currentPrintSettings();

    QPrinter printer(QPrinter::HighResolution);
    settings.apply(&printer);

    QPrintPreviewDialog preview(&printer, this);
    preview.setWindowTitle(tr("Print Preview"));
    /*  paintRequested fires once initially and again on every
        zoom / orientation change inside the dialog. The job is
        cheap enough to recompute pagination each time.           */
    connect(&preview, &QPrintPreviewDialog::paintRequested,
            this, [this, &settings](QPrinter* p) {
                PrintJob job(*m_model, settings);
                job.paint(p);
            });
    preview.exec();
}

void MainWindow::doFilePageSetup()
{
    PrintSettings settings;
    settings.load();           // page setup edits the persisted defaults

    QPrinter printer(QPrinter::HighResolution);
    settings.apply(&printer);

    QPageSetupDialog dlg(&printer, this);
    dlg.setWindowTitle(tr("Page Setup"));
    if (dlg.exec() != QDialog::Accepted) return;

    settings.readFromPrinter(&printer);
    settings.save();
}

namespace {

/*  Generic export-dialog runner. Builds a Save-As suggestion under
    the user's last-used directory with the right extension, hands
    the path to `writer`, and reports failures via a single
    QMessageBox. The lambda lets us share dialog plumbing across
    the four format-specific slots without templating PrintJob.   */
struct ExportSpec
{
    QString title;          // dialog title
    QString filter;         // QFileDialog name filter
    QString extension;      // ".png" / ".jpg" / ".svg" / ".pdf"
};

} // namespace

template <typename Writer>
static bool runExport(MainWindow* w, const QString& fileBase,
                      const QString& lastDir, const ExportSpec& spec,
                      Writer&& writer)
{
    QString suggested = lastDir + QLatin1Char('/') + fileBase + spec.extension;
    QString path = QFileDialog::getSaveFileName(w, spec.title, suggested, spec.filter);
    if (path.isEmpty()) return false;
    if (!path.endsWith(spec.extension, Qt::CaseInsensitive)) path += spec.extension;
    if (!writer(path)) {
        QMessageBox::warning(w, spec.title,
            MainWindow::tr("Could not write %1").arg(path));
        return false;
    }
    return true;
}

void MainWindow::doFileExportPng()
{
    QString base = QFileInfo(m_model->filePath()).completeBaseName();
    if (base.isEmpty()) base = QStringLiteral("pattern");
    PrintSettings settings = currentPrintSettings();
    PrintJob job(*m_model, settings);
    runExport(this, base, lastFileDirectory(),
              {tr("Export PNG"), tr("PNG images (*.png)"), QStringLiteral(".png")},
              [&](const QString& p) {
                  rememberFileDirectory(p);
                  return job.exportImage(p, "PNG");
              });
}

void MainWindow::doFileExportJpeg()
{
    QString base = QFileInfo(m_model->filePath()).completeBaseName();
    if (base.isEmpty()) base = QStringLiteral("pattern");
    PrintSettings settings = currentPrintSettings();
    PrintJob job(*m_model, settings);
    runExport(this, base, lastFileDirectory(),
              {tr("Export JPEG"), tr("JPEG images (*.jpg *.jpeg)"), QStringLiteral(".jpg")},
              [&](const QString& p) {
                  rememberFileDirectory(p);
                  return job.exportImage(p, "JPEG");
              });
}

void MainWindow::doFileExportSvg()
{
    QString base = QFileInfo(m_model->filePath()).completeBaseName();
    if (base.isEmpty()) base = QStringLiteral("pattern");
    PrintSettings settings = currentPrintSettings();
    PrintJob job(*m_model, settings);
    runExport(this, base, lastFileDirectory(),
              {tr("Export SVG"), tr("SVG documents (*.svg)"), QStringLiteral(".svg")},
              [&](const QString& p) {
                  rememberFileDirectory(p);
                  return job.exportSvg(p);
              });
}

void MainWindow::doFileExportPdf()
{
    QString base = QFileInfo(m_model->filePath()).completeBaseName();
    if (base.isEmpty()) base = QStringLiteral("pattern");
    PrintSettings settings = currentPrintSettings();
    PrintJob job(*m_model, settings);
    runExport(this, base, lastFileDirectory(),
              {tr("Export PDF"), tr("PDF documents (*.pdf)"), QStringLiteral(".pdf")},
              [&](const QString& p) {
                  rememberFileDirectory(p);
                  return job.exportPdf(p);
              });
}

// -----------------------------------------------------------------
// Edit operations
// -----------------------------------------------------------------

void MainWindow::doEditUndo() { m_model->undo(); }
void MainWindow::doEditRedo() { m_model->redo(); }

void MainWindow::doEditArrange()
{
    if (!m_selection->isActive()) return;
    ArrangeDialog dlg(2, m_model->width(), this);
    if (dlg.exec() != QDialog::Accepted) return;
    m_model->arrangeSelection(*m_selection, dlg.copies(), dlg.offset());
}

void MainWindow::doEditMirrorH()
{
    if (m_selection->isActive()) m_model->mirrorHorizontal(m_selection->rect());
}
void MainWindow::doEditMirrorV()
{
    if (m_selection->isActive()) m_model->mirrorVertical(m_selection->rect());
}
void MainWindow::doEditRotate()
{
    if (m_selection->isActive()) m_model->rotate(m_selection->rect());
}
void MainWindow::doEditDelete()
{
    if (m_selection->isActive()) m_model->deleteRect(m_selection->rect());
    m_selection->clear();
}
void MainWindow::doEditInsertRow() { m_model->insertRow(); }
void MainWindow::doEditDeleteRow() { m_model->deleteRow(); }

// -----------------------------------------------------------------
// View
// -----------------------------------------------------------------

void MainWindow::doViewToggleVisibility()
{
    /*  The four canvas wrappers are direct children of the
        splitter at indices 0..3 in the same order the View menu
        entries appear.                                            */
    static constexpr Actions::Id ids[4] = {
        Actions::Id::ViewDraft, Actions::Id::ViewCorrected,
        Actions::Id::ViewSimulation, Actions::Id::ViewReport };
    for (int i = 0; i < 4; ++i) {
        m_centralSplitter->widget(i)->setVisible(
            m_actions->action(ids[i])->isChecked());
    }
}

void MainWindow::doViewDrawModeChanged()
{
    m_draft->update();
    m_corrected->update();
    m_simulation->update();
    m_report->update();
}

void MainWindow::doViewZoomIn()     { m_model->zoomIn(); }
void MainWindow::doViewZoomOut()    { m_model->zoomOut(); }
void MainWindow::doViewZoomNormal() { m_model->zoomNormal(); }

// -----------------------------------------------------------------
// Pattern
// -----------------------------------------------------------------

void MainWindow::doPatternWidth()
{
    IntPromptDialog dlg(tr("Pattern Width"), tr("&Width:"),
                        m_model->width(), 1, 1000, this);
    if (dlg.exec() == QDialog::Accepted) m_model->setWidth(dlg.value());
}
void MainWindow::doPatternHeight()
{
    IntPromptDialog dlg(tr("Pattern Height"), tr("&Height:"),
                        m_model->height(), 1, 100000, this);
    if (dlg.exec() == QDialog::Accepted) {
        m_model->setHeight(dlg.value());
        updateScrollbar();
    }
}

void MainWindow::doPatternPalette()
{
    PaletteEditorDialog dlg(m_model, this);
    dlg.exec();
}

void MainWindow::doPatternPreferences()
{
    PreferencesDialog dlg(this);
    if (dlg.exec() != QDialog::Accepted) return;

    QSettings s;
    s.setValue(QStringLiteral("Environment/Language"),    dlg.language());
    s.setValue(QStringLiteral("Environment/ColorScheme"), dlg.colorScheme());

    /*  Color scheme applied live via the shared helper (Fusion
        style + explicit palette). styleHints->setColorScheme alone
        is ignored by the Linux platform theme plugin, so we go the
        full dbweave route. Language still requires a restart since
        QTranslator is wired up before the main window is built.   */
    applyColorScheme(dlg.colorScheme());
}

// -----------------------------------------------------------------
// Info
// -----------------------------------------------------------------

void MainWindow::doInfoTechInfos()
{
    TechInfosDialog dlg(*m_model, this);
    dlg.exec();
}

void MainWindow::doInfoAbout()
{
    /*  Pull the version from QApplication::applicationVersion()
        (set in main.cpp from the CMake project version) and the
        Qt version from QT_VERSION_STR so the dialog stays accurate
        across rebuilds and Qt upgrades.                          */
    const QString text = tr(
        "<h3>JBead %1</h3>"
        "<p>Bead-pattern designer (Qt 6 port of the original Java/Swing app).</p>"
        "<p>© 2009–2026 Damian Brunold. Licensed under GPL v3 or later.</p>"
        "<p>Built against Qt %2.</p>"
        "<p><a href=\"http://www.brunoldsoftware.ch\">brunoldsoftware.ch</a></p>"
    ).arg(QApplication::applicationVersion(), QString::fromLatin1(QT_VERSION_STR));
    QMessageBox::about(this, tr("About JBead"), text);
}

// -----------------------------------------------------------------
// Tube rotation
// -----------------------------------------------------------------

void MainWindow::doRotateLeft()  { m_model->shiftLeft(); }
void MainWindow::doRotateRight() { m_model->shiftRight(); }

// -----------------------------------------------------------------
// Model / scrollbar reactions
// -----------------------------------------------------------------

void MainWindow::onModelChanged()
{
    updateScrollbar();
    updateTitle();
    updateStatusBar();
}

void MainWindow::onScrollbarMoved(int value)
{
    /*  Vertical scrollbar value 0 = top of visible range, max =
        bottom (which corresponds to model.scroll == 0). The
        scrollbar's own maximum() is the source of truth — it has
        already been clamped to usedHeight+headroom by
        updateScrollbar(). Don't recompute from model->height()
        here; that's the full 800-row default field and would
        scroll the viewport hundreds of rows past the data.       */
    m_model->setScroll(m_scrollbar->maximum() - value);
}

void MainWindow::onScrollChanged(int /*scroll*/)
{
    QSignalBlocker block(m_scrollbar);
    m_scrollbar->setValue(m_scrollbar->maximum() - m_model->scroll());
    updateStatusBar();
}

void MainWindow::onZoomChanged(int /*gx*/, int /*gy*/)   { updateScrollbar(); updateStatusBar(); }
void MainWindow::onRepeatChanged(int /*r*/)              { updateStatusBar(); }
void MainWindow::onShiftChanged(int /*s*/)               { updateStatusBar(); }
void MainWindow::onSelectionUpdated()                    { updateStatusBar(); }

void MainWindow::updateScrollbar()
{
    /*  Cap the scroll range to the rows the user actually filled in
        (plus a small headroom). The legacy app exposed the full
        15x800 default field through the scrollbar, so users
        scrolled past the highest bead into hundreds of empty rows
        and saw only the colour-0 background — which can be e.g.
        yellow in samples like stripes.jbb and looks like the
        pattern was wiped. Headroom of ~10 rows lets the user
        extend the design without immediately hitting a wall.      */
    const int gridy = qMax(1, m_model->gridy());
    const int visible = qMax(1, m_draft->height() / gridy);
    const int rows = qMin(m_model->height(),
                          qMax(visible, m_model->usedHeight() + 10));
    const int maxScroll = qMax(0, rows - visible);
    QSignalBlocker block(m_scrollbar);
    m_scrollbar->setRange(0, maxScroll);
    m_scrollbar->setPageStep(visible);
    m_scrollbar->setSingleStep(1);
    /*  Pattern grows upward (row 0 at the bottom). The thumb's
        natural "top" position therefore corresponds to the
        highest-row view, while the user's intuitive "I just opened
        the file, show me the first beads" is at the BOTTOM. So the
        scrollbar value is inverted: 0 == top of pattern, max == bottom.
        Initial scroll == 0 (model default) maps to value == max,
        which puts the thumb at the bottom.                          */
    m_scrollbar->setValue(maxScroll - qMin(m_model->scroll(), maxScroll));
}

void MainWindow::updateTitle()
{
    QString title = QFileInfo(m_model->filePath()).fileName();
    if (title.isEmpty()) title = tr("unnamed");
    if (m_model->isModified()) title += QStringLiteral(" *");
    setWindowTitle(title + QStringLiteral(" — JBead"));
}

void MainWindow::updateStatusBar()
{
    if (m_selection->isActive()) {
        const auto r = m_selection->rect();
        m_statusSelectionLabel->setText(tr("Sel: %1 × %2").arg(r.width()).arg(r.height()));
    } else {
        m_statusSelectionLabel->setText(tr("Sel: —"));
    }
    m_statusCursorLabel->setText(tr("Scroll: %1").arg(m_model->scroll()));
    m_statusRepeatLabel->setText(tr("Repeat: %1").arg(m_model->repeat()));
    QString tool;
    switch (m_actions->currentTool()) {
        case Actions::Id::ToolPencil:  tool = tr("Pencil"); break;
        case Actions::Id::ToolSelect:  tool = tr("Select"); break;
        case Actions::Id::ToolFill:    tool = tr("Fill"); break;
        case Actions::Id::ToolPipette: tool = tr("Pipette"); break;
        default: tool = QStringLiteral("?");
    }
    m_statusToolLabel->setText(tr("Tool: %1").arg(tool));
}

// -----------------------------------------------------------------

void MainWindow::closeEvent(QCloseEvent* event)
{
    if (!maybeSave()) { event->ignore(); return; }

    QSettings s;
    s.beginGroup(QStringLiteral("MainWindow"));
    s.setValue(QStringLiteral("geometry"), saveGeometry());
    s.setValue(QStringLiteral("state"),    saveState());
    s.setValue(QStringLiteral("splitter"), m_centralSplitter->saveState());
    s.endGroup();

    event->accept();
}

void MainWindow::keyPressEvent(QKeyEvent* event)
{
    if (event->key() == Qt::Key_Left)  { doRotateLeft();  return; }
    if (event->key() == Qt::Key_Right) { doRotateRight(); return; }
    QMainWindow::keyPressEvent(event);
}

} // namespace jbead
