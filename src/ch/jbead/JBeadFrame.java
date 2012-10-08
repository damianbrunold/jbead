/** jbead - http://www.jbead.ch
    Copyright (C) 2001-2012  Damian Brunold

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ch.jbead;


import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;

import ch.jbead.action.EditArrangeAction;
import ch.jbead.action.EditDeleteAction;
import ch.jbead.action.EditDeleteRowAction;
import ch.jbead.action.EditInsertRowAction;
import ch.jbead.action.EditMirrorHorizontalAction;
import ch.jbead.action.EditMirrorVerticalAction;
import ch.jbead.action.EditRedoAction;
import ch.jbead.action.EditRotateAction;
import ch.jbead.action.EditUndoAction;
import ch.jbead.action.FileExitAction;
import ch.jbead.action.FileMRUAction;
import ch.jbead.action.FileNewAction;
import ch.jbead.action.FileOpenAction;
import ch.jbead.action.FilePageSetupAction;
import ch.jbead.action.FilePrintAction;
import ch.jbead.action.FileSaveAction;
import ch.jbead.action.FileSaveAsAction;
import ch.jbead.action.InfoAboutAction;
import ch.jbead.action.InfoTechInfosAction;
import ch.jbead.action.InfoUpdateCheckAction;
import ch.jbead.action.PatternHeightAction;
import ch.jbead.action.PatternPreferencesAction;
import ch.jbead.action.PatternWidthAction;
import ch.jbead.action.ToolFillAction;
import ch.jbead.action.ToolPencilAction;
import ch.jbead.action.ToolPipetteAction;
import ch.jbead.action.ToolSelectAction;
import ch.jbead.action.ViewCorrectedAction;
import ch.jbead.action.ViewDraftAction;
import ch.jbead.action.ViewDrawColorsAction;
import ch.jbead.action.ViewDrawSymbolsAction;
import ch.jbead.action.ViewReportAction;
import ch.jbead.action.ViewSimulationAction;
import ch.jbead.action.ViewZoomInAction;
import ch.jbead.action.ViewZoomNormalAction;
import ch.jbead.action.ViewZoomOutAction;
import ch.jbead.dialog.ArrangeDialog;
import ch.jbead.fileformat.DbbFileFilter;
import ch.jbead.fileformat.DbbFileFormat;
import ch.jbead.fileformat.FileFormat;
import ch.jbead.fileformat.JBeadFileFilter;
import ch.jbead.fileformat.JBeadFileFormat;
import ch.jbead.fileformat.JbbFileFilter;
import ch.jbead.fileformat.Memento;
import ch.jbead.print.PrintSettings;
import ch.jbead.storage.JBeadFileFormatException;
import ch.jbead.ui.Button;
import ch.jbead.ui.ColorsToolbar;
import ch.jbead.ui.MRUMenuItem;
import ch.jbead.ui.RotateLeftButton;
import ch.jbead.ui.RotateRightButton;
import ch.jbead.ui.ToolButton;
import ch.jbead.ui.ToolMenuItem;
import ch.jbead.ui.ToolsGroup;
import ch.jbead.util.Platform;
import ch.jbead.version.Version;
import ch.jbead.version.VersionChecker;
import ch.jbead.version.VersionListener;
import ch.jbead.view.CorrectedPanel;
import ch.jbead.view.DraftPanel;
import ch.jbead.view.ReportPanel;
import ch.jbead.view.SimulationPanel;

public class JBeadFrame extends JFrame implements Localization, View, ModelListener, VersionListener {

    private static final int ONE_DAY = 86400;

    private static final long serialVersionUID = 1L;

    private static final int SHIFTING_INTERVAL = 150;
    private static final int UPDATE_INTERVAL = 500;

    private ResourceBundle bundle = ResourceBundle.getBundle("jbead");

    private List<ViewListener> listeners = new ArrayList<ViewListener>();

    private Model model = new Model(this);
    private Selection selection = new Selection();
    private FileFormat fileformat = new JBeadFileFormat();

    private boolean dragging;

    private List<File> mru = new ArrayList<File>();

    private ColorsToolbar colors;

    private JScrollBar scrollbar = new JScrollBar(JScrollBar.VERTICAL);
    private boolean updatingScrollbar = false;

    private DraftPanel draft = new DraftPanel(model, selection, this);
    private CorrectedPanel corrected = new CorrectedPanel(model, selection, this);
    private SimulationPanel simulation = new SimulationPanel(model, selection, this);
    private ReportPanel report = new ReportPanel(model, this, selection, this);

    private JLabel laDraft = new JLabel(getString("draft"));
    private JLabel laCorrected = new JLabel(getString("corrected"));
    private JLabel laSimulation = new JLabel(getString("simulation"));
    private JLabel laReport = new JLabel(getString("report"));

    private JMenuItem viewDraft;
    private JMenuItem viewCorrected;
    private JMenuItem viewSimulation;
    private JMenuItem viewReport;

    private JMenuItem viewDrawColors;
    private JMenuItem viewDrawSymbols;

    private ToolsGroup toolsGroup = new ToolsGroup();

    private Settings settings = new Settings();

    private boolean ignoreSizeChanges = false;

    private PrintSettings printSettings = new PrintSettings(settings);

    private JPanel main = new JPanel();

    private Map<String, Action> actions = new HashMap<String, Action>();

    private Timer updateTimer;
    private Timer shiftTimer;

    public JBeadFrame(String[] args) {
        super("jbead");
        createGUI();
        model.addListener(this);
        model.clear();
        selection.clear();
        loadMRU();
        updateMRU();
        initCloseHandler();

        // TODO persist settings?
        viewDraft.setSelected(true);
        viewCorrected.setSelected(true);
        viewSimulation.setSelected(true);
        viewReport.setSelected(true);

        // TODO persist settings?
        viewDrawColors.setSelected(true);
        viewDrawSymbols.setSelected(false);
        fireDrawColorsChanged();
        fireDrawSymbolsChanged();

        setIconImage(ImageFactory.getImage("jbead-16"));

        restoreScreenBounds();

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                persistScreenBounds();
            }
            @Override
            public void componentMoved(ComponentEvent e) {
                persistScreenBounds();
            }
        });
        addWindowStateListener(new WindowAdapter() {
            @Override
            public void windowStateChanged(WindowEvent e) {
                persistScreenBounds();
            }
        });

        toolsGroup.selectTool("pencil");

        selection.addListener(draft);

        initScrollbar();
        scrollbar.addAdjustmentListener(new AdjustmentListener() {
            public void adjustmentValueChanged(AdjustmentEvent e) {
                if (e.getValueIsAdjusting()) return;
                updateScrollPos(e.getValue());
            }
        });

        draft.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateScrollbar();
            }
        });

        setupUpdateTimer();

        handleCommandLineArgs(args);

        checkVersionUpdate();

        if (Platform.isMacOSX()) {
            initMacOSX();
        }
    }

    private void setupUpdateTimer() {
        updateTimer = new Timer("updateTimer", true);
        updateTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                updateHandler();
            }
        }, UPDATE_INTERVAL, UPDATE_INTERVAL);
    }

    private void checkVersionUpdate() {
        settings.setCategory("update");
        if (settings.loadBoolean("check_at_start", true)) {
            Timer timer = new Timer("updatecheck");
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    new VersionChecker(JBeadFrame.this).check();
                }
            }, 2000);
        }
    }

    private void initMacOSX() {
        new MacOSXInitializer().initialize(this);
    }

    public boolean isConfigMaximized() {
        settings.setCategory("screen");
        return settings.loadBoolean("maximized");
    }

    public Rectangle getMaxBounds() {
        GraphicsEnvironment e = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsConfiguration graphicsConfig = e.getDefaultScreenDevice().getDefaultConfiguration();
        Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(graphicsConfig);
        Rectangle bounds = graphicsConfig.getBounds();
        bounds.x += insets.left;
        bounds.y += insets.top;
        bounds.width -= (insets.left + insets.right);
        bounds.height -= (insets.top + insets.bottom);
        return bounds;
    }

    private void restoreScreenBounds() {
        ignoreSizeChanges = true;
        try {
            Rectangle maxsize = getMaxBounds();
            settings.setCategory("screen");
            if (settings.hasSetting("x")) {
                int x = settings.loadInt("x", 0);
                int y = settings.loadInt("y", 0);
                int width = settings.loadInt("width", 1024);
                int height = settings.loadInt("height", 1024);
                if (x >= maxsize.x && y >= maxsize.y && width <= maxsize.width && height <= maxsize.height) {
                    setSize(width, height);
                    setLocation(x, y);
                } else {
                    setSize(maxsize.width, maxsize.height);
                    setLocation(maxsize.x, maxsize.y);
                }
            } else if (maxsize.width > 1024) {
                setSize(1024, 768);
                setLocationRelativeTo(null);
            } else {
                setSize(maxsize.width, maxsize.height);
                setLocationRelativeTo(null);
            }
        } finally {
            ignoreSizeChanges = false;
        }
    }

    private void persistScreenBounds() {
        if (ignoreSizeChanges) return;
        settings.setCategory("screen");
        if (getExtendedState() == Frame.NORMAL) {
            Rectangle bounds = getBounds();
            settings.saveInt("x", bounds.x);
            settings.saveInt("y", bounds.y);
            settings.saveInt("width", bounds.width);
            settings.saveInt("height", bounds.height);
        }
        settings.saveBoolean("maximized", getExtendedState() == Frame.MAXIMIZED_BOTH);
    }

    private void handleCommandLineArgs(String[] args) {
        if (args.length == 0) return;
        if (args[0].equals("/p") || args[0].equals("-p")) {
            if (args.length < 2) return;
            File file = new File(args[1]);
            loadFile(file, false);
            getAction("file.print").actionPerformed(new ActionEvent(this, 0, null));
        } else {
            File file = new File(args[0]);
            if (!file.exists()) return;
            loadFile(file, true);
        }
    }

    private void initCloseHandler() {
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (canTerminateApp()) {
                    System.exit(0);
                }
            }
        });
    }

    public void registerAction(String name, Action action) {
        actions.put(name, action);
    }

    public void addListener(ViewListener listener) {
        listeners.add(listener);
    }

    public void fireDrawColorsChanged() {
        for (ViewListener listener : listeners) {
            listener.drawColorsChanged(viewDrawColors.isSelected());
        }
    }

    public void fireDrawSymbolsChanged() {
        for (ViewListener listener : listeners) {
            listener.drawSymbolsChanged(viewDrawSymbols.isSelected());
        }
    }

    public boolean isDrawColors() {
        return viewDrawColors.isSelected();
    }

    public boolean isDrawSymbols() {
        return viewDrawSymbols.isSelected();
    }

    public void clearSelection() {
        selection.clear();
    }

    public PrintSettings getPrintSettings() {
        return printSettings;
    }

    public ResourceBundle getBundle() {
        return bundle;
    }

    public String getString(String key) {
        return getBundle().getString(key);
    }

    public int getMnemonic(String key) {
        return getBundle().getString(key).charAt(0);
    }

    public KeyStroke getKeyStroke(String key) {
        return KeyStroke.getKeyStroke(getBundle().getString(key));
    }

    public Action getAction(String name) {
        return actions.get(name);
    }

    private void createGUI() {
        createMenu();
        setLayout(new BorderLayout());
        JPanel toolbars = new JPanel();
        toolbars.setLayout(new FlowLayout(FlowLayout.LEADING));
        toolbars.add(createToolbar());
        toolbars.add(colors = createColorbar());
        add(toolbars, BorderLayout.NORTH);
        add(main, BorderLayout.CENTER);
        createMainGUI();
    }

    private void createMenu() {
        JMenuBar menubar = new JMenuBar();
        menubar.add(createFileMenu());
        menubar.add(createEditMenu());
        menubar.add(createViewMenu());
        menubar.add(createToolMenu());
        menubar.add(createPatternMenu());
        menubar.add(createInfoMenu());
        setJMenuBar(menubar);
    }

    private JMenu createFileMenu() {
        JMenu menuFile = createMenu("action.file");
        menuFile.add(new FileNewAction(this));
        menuFile.add(new FileOpenAction(this));
        menuFile.add(new FileSaveAction(this));
        menuFile.add(new FileSaveAsAction(this));
        menuFile.addSeparator();
        menuFile.add(new JMenuItem(new FilePrintAction(this)));
        menuFile.add(new FilePageSetupAction(this));
        menuFile.addSeparator();
        menuFile.add(new MRUMenuItem(new FileMRUAction(this, 0)));
        menuFile.add(new MRUMenuItem(new FileMRUAction(this, 1)));
        menuFile.add(new MRUMenuItem(new FileMRUAction(this, 2)));
        menuFile.add(new MRUMenuItem(new FileMRUAction(this, 3)));
        menuFile.add(new MRUMenuItem(new FileMRUAction(this, 4)));
        menuFile.add(new MRUMenuItem(new FileMRUAction(this, 5)));
        menuFile.addSeparator();
        FileExitAction exitAction = new FileExitAction(this); // this registers the action
        if (!Platform.isMacOSX()) {
            menuFile.add(exitAction);
        }
        return menuFile;
    }

    private JMenu createEditMenu() {
        JMenu menuEdit = createMenu("action.edit");
        menuEdit.add(new EditUndoAction(this));
        menuEdit.add(new EditRedoAction(this));
        menuEdit.add(new EditArrangeAction(this));
        menuEdit.add(new EditMirrorHorizontalAction(this));
        menuEdit.add(new EditMirrorVerticalAction(this));
        menuEdit.add(new EditRotateAction(this));
        menuEdit.add(new EditDeleteAction(this));
        JMenu menuEditRow = createMenu("action.edit.row");
        menuEdit.add(menuEditRow);
        menuEditRow.add(new EditInsertRowAction(this));
        menuEditRow.add(new EditDeleteRowAction(this));
        return menuEdit;
    }

    private JMenu createViewMenu() {
        JMenu menuView = createMenu("action.view");
        menuView.add(viewDraft = new JCheckBoxMenuItem(new ViewDraftAction(this)));
        menuView.add(viewCorrected = new JCheckBoxMenuItem(new ViewCorrectedAction(this)));
        menuView.add(viewSimulation = new JCheckBoxMenuItem(new ViewSimulationAction(this)));
        menuView.add(viewReport = new JCheckBoxMenuItem(new ViewReportAction(this)));
        menuView.addSeparator();
        menuView.add(viewDrawColors = new JCheckBoxMenuItem(new ViewDrawColorsAction(this)));
        menuView.add(viewDrawSymbols = new JCheckBoxMenuItem(new ViewDrawSymbolsAction(this)));
        menuView.addSeparator();
        menuView.add(new ViewZoomInAction(this));
        menuView.add(new ViewZoomNormalAction(this));
        menuView.add(new ViewZoomOutAction(this));
        return menuView;
    }

    private JMenu createToolMenu() {
        JMenu menuTool = createMenu("action.tool");
        menuTool.add(toolsGroup.addTool("pencil", new ToolMenuItem(new ToolPencilAction(this))));
        menuTool.add(toolsGroup.addTool("select", new ToolMenuItem(new ToolSelectAction(this))));
        menuTool.add(toolsGroup.addTool("fill", new ToolMenuItem(new ToolFillAction(this))));
        menuTool.add(toolsGroup.addTool("pipette", new ToolMenuItem(new ToolPipetteAction(this))));
        return menuTool;
    }

    private JMenu createPatternMenu() {
        JMenu menuPattern = createMenu("action.pattern");
        menuPattern.add(new PatternWidthAction(this));
        menuPattern.add(new PatternHeightAction(this));
        PatternPreferencesAction preferencesAction = new PatternPreferencesAction(this);
        if (!Platform.isMacOSX()) {
            menuPattern.add(preferencesAction);
        }
        return menuPattern;
    }

    private JMenu createInfoMenu() {
        JMenu menuInfo = createMenu("action.info");
        menuInfo.add(new InfoTechInfosAction(this));
        menuInfo.add(new InfoUpdateCheckAction(this));
        InfoAboutAction aboutAction = new InfoAboutAction(this); // this registers the action
        if (!Platform.isMacOSX()) {
            menuInfo.add(aboutAction);
        }
        return menuInfo;
    }

    private JMenu createMenu(String key) {
        JMenu menu = new JMenu(getString(key));
        menu.setMnemonic(getMnemonic(key + ".mnemonic"));
        return menu;
    }

    private JToolBar createToolbar() {
        JToolBar toolbar = new JToolBar();
        toolbar.add(new Button(getAction("file.new")));
        toolbar.add(new Button(getAction("file.open")));
        toolbar.add(new Button(getAction("file.save")));
        toolbar.add(new Button(getAction("file.print")));
        toolbar.add(new Button(getAction("edit.undo")));
        toolbar.add(new Button(getAction("edit.redo")));
        toolbar.add(new RotateLeftButton(this));
        toolbar.add(new RotateRightButton(this));
        toolbar.add(new Button(getAction("edit.arrange")));
        toolbar.addSeparator();
        toolbar.add(toolsGroup.addTool("pencil", new ToolButton(getAction("tool.pencil"))));
        toolbar.add(toolsGroup.addTool("select", new ToolButton(getAction("tool.select"))));
        toolbar.add(toolsGroup.addTool("fill", new ToolButton(getAction("tool.fill"))));
        toolbar.add(toolsGroup.addTool("pipette", new ToolButton(getAction("tool.pipette"))));
        return toolbar;
    }

    private ColorsToolbar createColorbar() {
        return new ColorsToolbar(model, this, this);
    }

    private void createMainGUI() {
        main.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        main.add(draft, c);

        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1;
        main.add(laDraft, c);

        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        main.add(corrected, c);

        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 1;
        main.add(laCorrected, c);

        c = new GridBagConstraints();
        c.gridx = 2;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        main.add(simulation, c);

        c = new GridBagConstraints();
        c.gridx = 2;
        c.gridy = 1;
        main.add(laSimulation, c);

        c = new GridBagConstraints();
        c.gridx = 3;
        c.gridy = 0;
        c.weightx = 5;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        main.add(report, c);

        c = new GridBagConstraints();
        c.gridx = 3;
        c.gridy = 1;
        main.add(laReport, c);

        c = new GridBagConstraints();
        c.gridx = 4;
        c.gridy = 0;
        c.fill = GridBagConstraints.VERTICAL;
        main.add(scrollbar, c);
    }

    public boolean isDraftVisible() {
        return viewDraft.isSelected();
    }

    public boolean isCorrectedVisible() {
        return viewCorrected.isSelected();
    }

    public boolean isSimulationVisible() {
        return viewSimulation.isSelected();
    }

    public boolean isReportVisible() {
        return viewReport.isSelected();
    }

    public void setDraftVisible(boolean visible) {
        viewDraft.setSelected(visible);
    }

    public void setCorrectedVisible(boolean visible) {
        viewCorrected.setSelected(visible);
    }

    public void setSimulationVisible(boolean visible) {
        viewSimulation.setSelected(visible);
    }

    public void setReportVisible(boolean visible) {
        viewReport.setSelected(visible);
    }

    public void initDefaultSymbols() {
        BeadSymbols.SYMBOLS = BeadSymbols.SAVED_SYMBOLS;
    }

    public void selectDefaultColor() {
        colors.selectDefaultColor();
    }

    public void loadFile(File file, boolean addtomru) {
        if (model.isModified()) {
            int answer = JOptionPane.showConfirmDialog(this, getString("savechanges"));
            if (answer == JOptionPane.CANCEL_OPTION) return;
            if (answer == JOptionPane.YES_OPTION) {
                fileSaveClick(model.isSaved(), model.getFile());
            }
        }

        try {
            fileformat.load(model, this, file);
            colors.selectColor(model.getSelectedColor());
            updateScrollbar();
            model.setSaved();
            model.setModified(false);
            model.setRepeatDirty();
            model.setFile(file);
            if (addtomru) addToMRU(file);
        } catch (Exception e) {
            e.printStackTrace();
            String msg = e.getMessage();
            if (msg == null) msg = e.toString();
            JOptionPane.showMessageDialog(this, getString("load.failed").replace("{1}", file.getPath()).replace("{2}", msg));
        }
    }

    public boolean fileSaveClick(boolean isSaved, File file) {
        if (isSaved) {
            try {
                File tempfile = File.createTempFile("temp", ".jbb", file.getParentFile());
                try {
                    fileformat.save(model, this, tempfile);
                    if (file.exists()) file.delete();
                    tempfile.renameTo(file);
                } catch (IOException e) {
                    tempfile.delete();
                    throw e;
                } catch (JBeadFileFormatException e) {
                    tempfile.delete();
                    throw e;
                }
                updateTitle();
                return true;
            } catch (JBeadFileFormatException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, getString("save.failed").replace("{1}", file.getPath()).replace("{2}", e.getMessage()));
                return false;
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, getString("save.failed").replace("{1}", file.getPath()).replace("{2}", e.getMessage()));
                return false;
            }
        } else {
            return fileSaveasClick();
        }
    }

    private void addUniqueFilter(JFileChooser dialog, FileFilter filter, Set<String> filters) {
        if (filters.contains(filter.getDescription())) return;
        dialog.addChoosableFileFilter(filter);
        filters.add(filter.getDescription());
    }

    public void setOpenFileFilters(JFileChooser dialog) {
        dialog.setAcceptAllFileFilterUsed(true);
        dialog.addChoosableFileFilter(new JbbFileFilter());
        dialog.addChoosableFileFilter(new DbbFileFilter());
        dialog.setFileFilter(new JBeadFileFilter());
    }

    private void setSaveFileFilters(JFileChooser dialog) {
        dialog.setAcceptAllFileFilterUsed(true);
        Set<String> filters = new HashSet<String>();
        filters.add(fileformat.getFileFilter().getDescription());
        addUniqueFilter(dialog, new JbbFileFilter(), filters);
        addUniqueFilter(dialog, new DbbFileFilter(), filters);
        dialog.setFileFilter(fileformat.getFileFilter());
    }

    public void updateFileFormat(FileFilter filter, File file) {
        if (file.getName().endsWith(JBeadFileFormat.EXTENSION)) {
            fileformat = new JBeadFileFormat();
        } else if (file.getName().endsWith(DbbFileFormat.EXTENSION)) {
            fileformat = new DbbFileFormat();
        } else if (filter != null && filter instanceof DbbFileFilter) {
            fileformat = new DbbFileFormat();
        } else {
            fileformat = new JBeadFileFormat();
        }
    }

    public boolean fileSaveasClick() {
        JFileChooser dialog = new JFileChooser();
        dialog.setCurrentDirectory(model.getCurrentDirectory());
        dialog.setMultiSelectionEnabled(false);
        setSaveFileFilters(dialog);
        if (dialog.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            FileFormat oldFileFormat = fileformat;
            updateFileFormat(dialog.getFileFilter(), dialog.getSelectedFile());
            File file = dialog.getSelectedFile();
            if (!file.getName().endsWith(fileformat.getExtension())) {
                file = new File(file.getParentFile(), file.getName() + fileformat.getExtension());
            }
            if (file.exists()) {
                String msg = getString("fileexists");
                msg = msg.replace("{1}", file.getName());
                if (JOptionPane.showConfirmDialog(this, msg, "Overwrite", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                    return false;
                }
            }
            if (fileSaveClick(true, file)) {
                model.setFile(file);
                model.setSaved();
                addToMRU(model.getFile());
                updateTitle();
                return true;
            } else {
                fileformat = oldFileFormat;
            }
        }
        return false;
    }

    public boolean isDragging() {
        return dragging;
    }

    public void setDragging(boolean dragging) {
        this.dragging = dragging;
    }

    public void updateVisibility() {
        draft.setVisible(viewDraft.isSelected());
        laDraft.setVisible(draft.isVisible());
        corrected.setVisible(viewCorrected.isSelected());
        laCorrected.setVisible(corrected.isVisible());
        simulation.setVisible(viewSimulation.isSelected());
        laSimulation.setVisible(simulation.isVisible());
        report.setVisible(viewReport.isSelected());
        laReport.setVisible(report.isVisible());
    }

    public void formKeyUp(KeyEvent event) {
        int Key = event.getKeyCode();
        if (Key == KeyEvent.VK_F5) {
            repaint();
        } else if (event.getKeyChar() >= '0' && event.getKeyChar() <= '9') {
            model.setSelectedColor((byte) (event.getKeyChar() - '0'));
            colors.selectColor(model.getSelectedColor());
        } else if (Key == KeyEvent.VK_SPACE) {
            getAction("tool.pencil").putValue("SELECT", true);
            // sbToolPoint.setSelected(true);
            // toolPoint.setSelected(true);
        } else if (Key == KeyEvent.VK_ESCAPE && shiftTimer != null) {
            shiftTimer.cancel();
            shiftTimer = null;
        }
    }

    private void rotateLeft() {
        model.shiftLeft();
    }

    private void rotateRight() {
        model.shiftRight();
    }

    private void updateHandler() {
        getAction("edit.arrange").setEnabled(selection.isActive());
        getAction("edit.rotate").setEnabled(selection.isActive() && selection.isSquare());
        getAction("edit.delete").setEnabled(selection.isActive());
        getAction("edit.undo").setEnabled(model.canUndo());
        getAction("edit.redo").setEnabled(model.canRedo());
        getAction("file.print").setEnabled(model.getUsedHeight() > 0);
        if (model.isRepeatDirty()) {
            model.updateRepeat();
        }
        model.prepareSnapshot();
    }

    public void selectTool(String tool) {
        toolsGroup.selectTool(tool);
    }

    public void sbRotaterightMouseDown(MouseEvent event) {
        rotateRight();
        if (shiftTimer != null) shiftTimer.cancel();
        shiftTimer = new Timer("shiftTimer");
        shiftTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                rotateRight();
            }
        }, SHIFTING_INTERVAL, SHIFTING_INTERVAL);
    }

    public void sbRotaterightMouseUp(MouseEvent event) {
        shiftTimer.cancel();
        shiftTimer = null;
    }

    public void sbRotateleftMouseDown(MouseEvent event) {
        rotateLeft();
        if (shiftTimer != null) shiftTimer.cancel();
        shiftTimer = new Timer("shiftTimer");
        shiftTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                rotateLeft();
            }
        }, SHIFTING_INTERVAL, SHIFTING_INTERVAL);
    }

    public void sbRotateleftMouseUp(MouseEvent event) {
        shiftTimer.cancel();
        shiftTimer = null;
    }

    public void formKeyDown(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.VK_RIGHT) {
            rotateRight();
        } else if (event.getKeyCode() == KeyEvent.VK_LEFT) {
            rotateLeft();
        }
    }

    private boolean canTerminateApp() {
        if (model.isModified()) {
            int r = JOptionPane.showConfirmDialog(this, getString("savechanges"));
            if (r == JOptionPane.CANCEL_OPTION) {
                return false;
            }
            if (r == JOptionPane.OK_OPTION) fileSaveClick(model.isSaved(), model.getFile());
        }
        return true;
    }

    public void editArrangeClick() {
        ArrangeDialog copyform = new ArrangeDialog(this, selection, model);
        copyform.setVisible(true);
        if (copyform.isOK()) {
            int copies = copyform.getCopies();
            int offset = copyform.getOffset(model.getWidth());
            model.arrangeSelection(selection, copies, offset);
        }
    }

    public void updateTitle() {
        String c = getString("title");
        if (model.isSaved()) {
            c = c.replace("{1}", model.getFile().getName());
        } else {
            c = c.replace("{1}", getString("unnamed"));
        }
        if (model.isModified()) {
            c += "*";
        }
        setTitle(c);
    }

    private void addToMRU(File file) {
        if (file.getPath() == "") return;
        if (mru.contains(file)) {
            pullToTop(file);
        } else {
            addToTop(file);
        }
        updateMRU();
        saveMRU();
    }

    private void pullToTop(File file) {
        mru.remove(file);
        mru.add(0, file);
    }

    private void addToTop(File file) {
        mru.add(0, file);
        if (mru.size() > 6) {
            mru.remove(mru.size() - 1);
        }
    }

    private void updateMRU() {
        for (int i = 0; i < mru.size(); i++) {
            getAction("file.mru" + i).putValue(Action.NAME, getMRUDisplayName(i));
        }
        // TODO maybe have to set visibility of separator after last mru menu
        // item
    }

    private String getMRUDisplayName(int index) {
        String curdir = model.getCurrentDirectory().getAbsolutePath();
        String path = mru.get(index).getAbsolutePath();
        if (path.startsWith(curdir)) {
            return path.substring(curdir.length() + 1);
        } else {
            return path;
        }
    }

    public File getMRU(int index) {
        return mru.get(index);
    }

    private void saveMRU() {
        settings.setCategory("mru");
        for (int i = 0; i < mru.size(); i++) {
            settings.saveString("mru" + i, mru.get(i).getAbsolutePath());
        }
    }

    private void loadMRU() {
        settings.setCategory("mru");
        mru.clear();
        for (int i = 0; i < 6; i++) {
            addMRU(settings.loadString("mru" + i));
        }
    }

    private void addMRU(String path) {
        if (path.length() == 0) return;
        mru.add(new File(path));
    }

    public void pointChanged(Point pt) {
        updateTitle();
    }

    public void modelChanged() {
        colors.updateAll();
        updateScrollbar();
        updateTitle();
    }

    public void colorChanged(byte colorIndex) {
        colors.updateColorIcon(colorIndex);
        updateTitle();
    }

    public void colorsChanged() {
        colors.updateAll();
        updateTitle();
    }

    public void scrollChanged(int scroll) {
        updateTitle();
    }

    public void shiftChanged(int shift) {
        updateTitle();
    }

    public void zoomChanged(int gridx, int gridy) {
        updateTitle();
    }

    public void repeatChanged(int repeat) {
        updateTitle();
    }

    private void initScrollbar() {
        scrollbar.setMinimum(0);
        scrollbar.setMaximum(model.getHeight() - 1);
        scrollbar.setUnitIncrement(1);
    }

    public void updateScrollbar() {
        updatingScrollbar = true;
        try {
            int max = model.getHeight() - 1;
            int visible = Math.min(max, draft.getHeight() / model.getGridy());
            scrollbar.setMaximum(max);
            scrollbar.setBlockIncrement(visible / 2);
            scrollbar.setVisibleAmount(visible);
            int value = max - visible - model.getScroll();
            if (value < 0) {
                value = 0;
                model.setScroll(max - visible);
            }
            scrollbar.setValue(value);
        } finally {
            updatingScrollbar = false;
        }
    }

    private void updateScrollPos(int scrollpos) {
        if (updatingScrollbar) return;
        int scroll = scrollbar.getMaximum() - scrollbar.getVisibleAmount() - scrollpos;
        model.setScroll(scroll);
    }

    public void saveTo(Memento memento) {
        memento.setDraftVisible(isDraftVisible());
        memento.setCorrectedVisible(isCorrectedVisible());
        memento.setSimulationVisible(isSimulationVisible());
        memento.setReportVisible(isReportVisible());
        memento.setSelectedTool(getSelectedTool());
        memento.setDrawColors(isDrawColors());
        memento.setDrawSymbols(isDrawSymbols());
        memento.setSymbols(BeadSymbols.SYMBOLS);
    }

    public void loadFrom(Memento memento) {
        setDraftVisible(memento.isDraftVisible());
        setCorrectedVisible(memento.isCorrectedVisible());
        setSimulationVisible(memento.isSimulationVisible());
        setReportVisible(memento.isReportVisible());
        updateVisibility();
        setSelectedTool(memento.getSelectedTool());
        BeadSymbols.SYMBOLS = memento.getSymbols();
        viewDrawColors.setSelected(memento.isDrawColors());
        fireDrawColorsChanged();
        viewDrawSymbols.setSelected(memento.isDrawSymbols());
        fireDrawSymbolsChanged();
    }

    public Model getModel() {
        return model;
    }

    public Selection getSelection() {
        return selection;
    }

    public String getSelectedTool() {
        return toolsGroup.getSelectedTool();
    }

    public void setSelectedTool(String tool) {
        toolsGroup.selectTool(tool);
    }

    public void versionAvailable(final Version version) {
        settings.setCategory("update");
        long lastCheck = settings.loadLong("lastcheck");
        if (elapsedSince(lastCheck) < ONE_DAY) return;
        settings.saveLong("lastcheck", System.currentTimeMillis());
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JOptionPane.showMessageDialog(JBeadFrame.this, getString("updatecheck.updateavailable").replace("{1}", version.getVersionString()));
            }
        });
    }

    private long elapsedSince(long time) {
        return System.currentTimeMillis() - time;
    }

    public void versionUpToDate() {
        // ignore
    }

    public void failure(String msg) {
        // ignore
    }

    public boolean drawColors() {
        return viewDrawColors.isSelected();
    }

    public boolean drawSymbols() {
        return viewDrawSymbols.isSelected();
    }

    public void refresh() {
        fireDrawSymbolsChanged();
    }

    public void selectColor(byte colorIndex) {
        colors.selectColor(colorIndex);
    }
}
