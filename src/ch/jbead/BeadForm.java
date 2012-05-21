/** jbead - http://www.brunoldsoftware.ch
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
import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JColorChooser;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollBar;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import ch.jbead.action.EditArrangeAction;
import ch.jbead.action.EditDeleteLineAction;
import ch.jbead.action.EditInsertLineAction;
import ch.jbead.action.EditRedoAction;
import ch.jbead.action.EditUndoAction;
import ch.jbead.action.FileExitAction;
import ch.jbead.action.FileMRU1Action;
import ch.jbead.action.FileMRU2Action;
import ch.jbead.action.FileMRU3Action;
import ch.jbead.action.FileMRU4Action;
import ch.jbead.action.FileMRU5Action;
import ch.jbead.action.FileMRU6Action;
import ch.jbead.action.FileNewAction;
import ch.jbead.action.FileOpenAction;
import ch.jbead.action.FilePrintAction;
import ch.jbead.action.FilePrintSetupAction;
import ch.jbead.action.FileSaveAction;
import ch.jbead.action.FileSaveAsAction;
import ch.jbead.action.ToolFillAction;
import ch.jbead.action.ToolPencilAction;
import ch.jbead.action.ToolPipetteAction;
import ch.jbead.action.ToolSelectAction;
import ch.jbead.action.ViewDraftAction;
import ch.jbead.action.ViewNormalAction;
import ch.jbead.action.ViewReportAction;
import ch.jbead.action.ViewSimulationAction;
import ch.jbead.action.ViewZoomInAction;
import ch.jbead.action.ViewZoomNormalAction;
import ch.jbead.action.ViewZoomOutAction;
import ch.jbead.dialog.AboutBox;
import ch.jbead.dialog.CopyForm;
import ch.jbead.dialog.PatternWidthForm;

/**
 * 
 */
public class BeadForm extends JFrame implements Localization {

    private static final long serialVersionUID = 1L;

    private ResourceBundle bundle = ResourceBundle.getBundle("jbead");
    
    private Model model = new Model(this);
    private Selection selection = new Selection();

    private boolean dragging;
    private boolean saved;
    private boolean modified;
    private String mru[] = new String[6];

    private JToolBar toolbar = new JToolBar();

    private ButtonGroup colorsGroup = new ButtonGroup();
    private JToggleButton sbColor0 = new JToggleButton();
    private JToggleButton sbColor1 = new JToggleButton();
    private JToggleButton sbColor2 = new JToggleButton();
    private JToggleButton sbColor3 = new JToggleButton();
    private JToggleButton sbColor4 = new JToggleButton();
    private JToggleButton sbColor5 = new JToggleButton();
    private JToggleButton sbColor6 = new JToggleButton();
    private JToggleButton sbColor7 = new JToggleButton();
    private JToggleButton sbColor8 = new JToggleButton();
    private JToggleButton sbColor9 = new JToggleButton();

    private JScrollBar scrollbar = new JScrollBar(JScrollBar.VERTICAL);

    private DraftPanel draft = new DraftPanel(model, selection, this);
    private NormalPanel normal = new NormalPanel(model, this);
    private SimulationPanel simulation = new SimulationPanel(model);
    private ReportPanel report = new ReportPanel(model, this);

    private JLabel laDraft = new JLabel("draft");
    private JLabel laNormal = new JLabel("normal");
    private JLabel laSimulation = new JLabel("simulation");
    private JLabel laReport = new JLabel("report");

    private JMenuItem viewDraft;
    private JMenuItem viewNormal;
    private JMenuItem viewSimulation;
    private JMenuItem viewReport;

    private JMenu menuPattern = new JMenu("pattern");
    private JMenuItem patternWidth = new JMenuItem("width");

    private JMenu menuInfo = new JMenu("?");
    private JMenuItem infoAbout = new JMenuItem("about jbead");

    private JButton sbRotateleft;
    private JButton sbRotateright;
    
    private ButtonGroup toolsGroup = new ButtonGroup();
    private JRadioButtonMenuItem toolPencil;
    private JRadioButtonMenuItem toolSelect;
    private JRadioButtonMenuItem toolFill;
    private JRadioButtonMenuItem toolPipette;

    private ButtonGroup sbToolsGroup = new ButtonGroup();
    private JToggleButton sbToolPencil;
    private JToggleButton sbToolSelect;
    private JToggleButton sbToolFill;
    private JToggleButton sbToolPipette;

    private PageFormat pageFormat;

    private JPanel main = new JPanel();
    private JLabel statusbar = new JLabel("X");
    
    private Map<String, Action> actions = new HashMap<String, Action>();
    
    private Timer timer;
    
    public BeadForm() {
        super("jbead");
        createGUI();
        saved = false;
        modified = false;
        selection.clear();
        updateTitle();
        setColorIcons();
        loadMRU();
        updateMRU();
        updateScrollbar();
        initCloseHandler();

        // persist settings?
        viewDraft.setSelected(true);
        viewNormal.setSelected(true);
        viewSimulation.setSelected(true);
        viewReport.setSelected(true);
        
        ActionListener colorActionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                colorClick(e);
            }
        };
        MouseAdapter colorMouseAdapter = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (e.getClickCount() == 2) {
                    colorDblClick(e.getSource());
                }
            }
            
        };
        sbColor0.addActionListener(colorActionListener);
        sbColor1.addActionListener(colorActionListener);
        sbColor2.addActionListener(colorActionListener);
        sbColor3.addActionListener(colorActionListener);
        sbColor4.addActionListener(colorActionListener);
        sbColor5.addActionListener(colorActionListener);
        sbColor6.addActionListener(colorActionListener);
        sbColor7.addActionListener(colorActionListener);
        sbColor8.addActionListener(colorActionListener);
        sbColor9.addActionListener(colorActionListener);
        
        sbColor1.addMouseListener(colorMouseAdapter);
        sbColor2.addMouseListener(colorMouseAdapter);
        sbColor3.addMouseListener(colorMouseAdapter);
        sbColor4.addMouseListener(colorMouseAdapter);
        sbColor5.addMouseListener(colorMouseAdapter);
        sbColor6.addMouseListener(colorMouseAdapter);
        sbColor7.addMouseListener(colorMouseAdapter);
        sbColor8.addMouseListener(colorMouseAdapter);
        sbColor9.addMouseListener(colorMouseAdapter);
        
        setIconImage(ImageFactory.getImage("jbead-16"));
        
        // TODO persist location and size in settings
        setSize(1024, 700);
        setLocation(100, 35);

        // TODO persist the pageFormat in Settings?
        pageFormat = PrinterJob.getPrinterJob().defaultPage();
        pageFormat.setOrientation(PageFormat.LANDSCAPE);
        
        timer = new Timer("updateTimer", true);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                updateHandler();
            }
        }, 500, 500);
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
    
    @Override
    public ResourceBundle getBundle() {
        return bundle;
    }
    
    @Override
    public String getString(String key) {
        return getBundle().getString(key);
    }
    
    public Action getAction(String name) {
        return actions.get(name);
    }

    private void createGUI() {
        createMenu();
        setLayout(new BorderLayout());
        add(toolbar, BorderLayout.NORTH);
        add(main, BorderLayout.CENTER);
        add(statusbar, BorderLayout.SOUTH);
        createToolbar();
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
        JMenu menuFile = new JMenu(bundle.getString("action.file"));
        menuFile.add(new FileNewAction(this));
        menuFile.add(new FileOpenAction(this));
        menuFile.add(new FileSaveAction(this));
        menuFile.add(new FileSaveAsAction(this));
        menuFile.addSeparator();
        menuFile.add(new FilePrintAction(this));
        menuFile.add(new FilePrintSetupAction(this));
        menuFile.addSeparator();
        menuFile.add(new MRUMenuItem(new FileMRU1Action(this)));
        menuFile.add(new MRUMenuItem(new FileMRU2Action(this)));
        menuFile.add(new MRUMenuItem(new FileMRU3Action(this)));
        menuFile.add(new MRUMenuItem(new FileMRU4Action(this)));
        menuFile.add(new MRUMenuItem(new FileMRU5Action(this)));
        menuFile.add(new MRUMenuItem(new FileMRU6Action(this)));
        menuFile.addSeparator(); // TODO what if no mru files are there?
        menuFile.add(new FileExitAction(this));
        return menuFile;
    }
    
    private JMenu createEditMenu() {
        JMenu menuEdit = new JMenu(bundle.getString("action.edit"));
        menuEdit.add(new EditUndoAction(this));
        menuEdit.add(new EditRedoAction(this));
        menuEdit.add(new EditArrangeAction(this));
        JMenu menuEditLine = new JMenu(bundle.getString("action.edit.line"));
        menuEdit.add(menuEditLine);
        menuEditLine.add(new EditInsertLineAction(this));
        menuEditLine.add(new EditDeleteLineAction(this));
        return menuEdit;
    }

    private JMenu createViewMenu() {
        JMenu menuView = new JMenu(bundle.getString("action.view"));
        menuView.add(viewDraft = new JCheckBoxMenuItem(new ViewDraftAction(this)));
        menuView.add(viewNormal = new JCheckBoxMenuItem(new ViewNormalAction(this)));
        menuView.add(viewSimulation = new JCheckBoxMenuItem(new ViewSimulationAction(this)));
        menuView.add(viewReport = new JCheckBoxMenuItem(new ViewReportAction(this)));
        menuView.addSeparator();
        menuView.add(new ViewZoomInAction(this));
        menuView.add(new ViewZoomNormalAction(this));
        menuView.add(new ViewZoomOutAction(this));
        return menuView;
    }

    private JMenu createToolMenu() {
        JMenu menuTool = new JMenu(bundle.getString("action.tool"));
        menuTool.add(toolPencil = new JRadioButtonMenuItem(new ToolPencilAction(this)));
        menuTool.add(toolSelect = new JRadioButtonMenuItem(new ToolSelectAction(this)));
        menuTool.add(toolFill = new JRadioButtonMenuItem(new ToolFillAction(this)));
        menuTool.add(toolPipette = new JRadioButtonMenuItem(new ToolPipetteAction(this)));
        toolsGroup.add(toolPencil);
        toolsGroup.add(toolSelect);
        toolsGroup.add(toolFill);
        toolsGroup.add(toolPipette);
        return menuTool;
    }

    private JMenu createPatternMenu() {
        menuPattern.add(patternWidth);
        patternWidth.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                patternWidthClick();
            }
        });
        return menuPattern;
    }

    private JMenu createInfoMenu() {
        menuInfo.add(infoAbout);
        infoAbout.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new AboutBox(BeadForm.this).setVisible(true);
            }
        });
        return menuInfo;
    }

    private void createToolbar() {
        toolbar.add(getAction("file.new"));
        toolbar.add(getAction("file.open"));
        toolbar.add(getAction("file.save"));
        toolbar.add(getAction("file.print"));
        toolbar.add(getAction("edit.undo"));
        toolbar.add(getAction("edit.redo"));
        toolbar.add(sbRotateleft = new JButton(ImageFactory.getIcon("sb_prev")));
        toolbar.add(sbRotateright = new JButton(ImageFactory.getIcon("sb_next")));
        toolbar.add(getAction("edit.arrange"));
        
        toolbar.addSeparator();

        toolbar.add(sbToolPencil = new JToggleButton(getAction("tool.pencil")));
        toolbar.add(sbToolSelect = new JToggleButton(getAction("tool.select")));
        toolbar.add(sbToolFill = new JToggleButton(getAction("tool.fill")));
        toolbar.add(sbToolPipette = new JToggleButton(getAction("tool.pipette")));
        
        sbToolPencil.setSelected(true);
        
        sbToolsGroup.add(sbToolPencil);
        sbToolsGroup.add(sbToolSelect);
        sbToolsGroup.add(sbToolFill);
        sbToolsGroup.add(sbToolPipette);

        toolbar.addSeparator();

        sbColor1.setSelected(true);
        toolbar.add(sbColor0);
        toolbar.add(sbColor1);
        toolbar.add(sbColor2);
        toolbar.add(sbColor3);
        toolbar.add(sbColor4);
        toolbar.add(sbColor5);
        toolbar.add(sbColor6);
        toolbar.add(sbColor7);
        toolbar.add(sbColor8);
        toolbar.add(sbColor9);

        colorsGroup.add(sbColor0);
        colorsGroup.add(sbColor1);
        colorsGroup.add(sbColor2);
        colorsGroup.add(sbColor3);
        colorsGroup.add(sbColor4);
        colorsGroup.add(sbColor5);
        colorsGroup.add(sbColor6);
        colorsGroup.add(sbColor7);
        colorsGroup.add(sbColor8);
        colorsGroup.add(sbColor9);
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
        main.add(normal, c);
        
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 1;
        main.add(laNormal, c);
        
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

//    private BeadField getField() {
//        return model.getField();
//    }
    
    private void setColorIcons() {
        sbColor0.setIcon(new ColorIcon(model, 0));
        sbColor1.setIcon(new ColorIcon(model, 1));
        sbColor2.setIcon(new ColorIcon(model, 2));
        sbColor3.setIcon(new ColorIcon(model, 3));
        sbColor4.setIcon(new ColorIcon(model, 4));
        sbColor5.setIcon(new ColorIcon(model, 5));
        sbColor6.setIcon(new ColorIcon(model, 6));
        sbColor7.setIcon(new ColorIcon(model, 7));
        sbColor8.setIcon(new ColorIcon(model, 8));
        sbColor9.setIcon(new ColorIcon(model, 9));
    }

    private void updateScrollbar() {
        int h = draft.getHeight() / model.getGrid();
        assert (h < model.getHeight());
        scrollbar.setMinimum(0);
        scrollbar.setMaximum(model.getHeight() - h);
        if (scrollbar.getMaximum() < 0) scrollbar.setMaximum(0);
        scrollbar.setUnitIncrement(h);
        scrollbar.setBlockIncrement(h);
        scrollbar.setValue(scrollbar.getMaximum() - scrollbar.getBlockIncrement() - model.getScroll());
    }

    private int correctCoordinatesX(int i, int j) {
        int idx = i + (j + model.getScroll()) * model.getWidth();
        int m1 = model.getWidth();
        int m2 = model.getWidth() + 1;
        int k = 0;
        int m = (k % 2 == 0) ? m1 : m2;
        while (idx >= m) {
            idx -= m;
            k++;
            m = (k % 2 == 0) ? m1 : m2;
        }
        i = idx;
        j = k - model.getScroll();
        return i;
    }

    private int correctCoordinatesY(int i, int j) {
        int idx = i + (j + model.getScroll()) * model.getWidth();
        int m1 = model.getWidth();
        int m2 = model.getWidth() + 1;
        int k = 0;
        int m = (k % 2 == 0) ? m1 : m2;
        while (idx >= m) {
            idx -= m;
            k++;
            m = (k % 2 == 0) ? m1 : m2;
        }
        i = idx;
        j = k - model.getScroll();
        return j;
    }

    public void fileNewClick() {
        // ask whether to save modified document
        if (modified) {
            int answer = JOptionPane.showConfirmDialog(this, getString("savechanges"));
            if (answer == JOptionPane.CANCEL_OPTION) return;
            if (answer == JOptionPane.YES_OPTION) {
                fileSaveClick();
            }
        }

        // delete all
        selection.clear();
        model.clear();
        sbColor1.setSelected(true);
        setColorIcons();
        updateScrollbar();
        saved = false;
        modified = false;
        updateTitle();
    }

    private void loadFile(File file, boolean addtomru) {
        // ask whether to save modified document
        if (modified) {
            int answer = JOptionPane.showConfirmDialog(this, getString("savechanges"));
            if (answer == JOptionPane.CANCEL_OPTION) return;
            if (answer == JOptionPane.YES_OPTION) {
                fileSaveClick();
            }
        }

        // Datei laden
        try {
            JBeadInputStream in = new JBeadInputStream(new FileInputStream(file));
            try {
                String strid = in.read(13);
                if (!strid.equals("DB-BEAD/01:\r\n")) {
                    JOptionPane.showMessageDialog(this, getString("invalidformat"));
                    return;
                }
                model.clear();
                model.load(in);
                viewDraft.setSelected(in.readBool());
                viewNormal.setSelected(in.readBool());
                viewSimulation.setSelected(in.readBool());
                switch (model.getColorIndex()) {
                case 0:
                    sbColor0.setSelected(true);
                    break;
                case 1:
                    sbColor1.setSelected(true);
                    break;
                case 2:
                    sbColor2.setSelected(true);
                    break;
                case 3:
                    sbColor3.setSelected(true);
                    break;
                case 4:
                    sbColor4.setSelected(true);
                    break;
                case 5:
                    sbColor5.setSelected(true);
                    break;
                case 6:
                    sbColor6.setSelected(true);
                    break;
                case 7:
                    sbColor7.setSelected(true);
                    break;
                case 8:
                    sbColor8.setSelected(true);
                    break;
                case 9:
                    sbColor9.setSelected(true);
                    break;
                default:
                    assert (false);
                    break;
                }
                //setColorIcons(); TODO not needed anymore, I think, but maybe a refresh/invalidation
                updateScrollbar();
            } finally {
                in.close();
            }
        } catch (IOException e) {
            // xxx
            model.clear();
        }
        saved = true;
        modified = false;
        model.setRepeatDirty();
        model.setFile(file);
        updateTitle();
        if (addtomru) addToMRU(file);
    }

    public void fileOpenClick() {
        JFileChooser dialog = new JFileChooser();
        dialog.setCurrentDirectory(model.getFile().getParentFile());
        if (dialog.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            loadFile(dialog.getSelectedFile(), true);
        }
    }

    public void fileSaveClick() {
        if (saved) {
            // Einfach abspeichern...
            try {
                JBeadOutputStream out = new JBeadOutputStream(new FileOutputStream(model.getFile()));
                try {
                    out.write("DB-BEAD/01:\r\n");
                    model.save(out);
                    out.writeBool(viewDraft.isSelected());
                    out.writeBool(viewNormal.isSelected());
                    out.writeBool(viewSimulation.isSelected());
                    // report flag is not saved?!
                    modified = false;
                    updateTitle();
                } finally {
                    out.close();
                }
            } catch (IOException e) {
                // xxx
            }
        } else {
            fileSaveasClick();
        }
    }

    public void fileSaveasClick() {
        JFileChooser dialog = new JFileChooser();
        if (dialog.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            if (dialog.getSelectedFile().exists()) {
                String msg = getString("fileexists");
                msg = msg.replace("{1}", dialog.getSelectedFile().getName());
                if (JOptionPane.showConfirmDialog(this, msg, "Overwrite", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                    return;
                }
            }
            model.setFile(dialog.getSelectedFile());
            saved = true;
            fileSaveClick();
            addToMRU(model.getFile());
        }
    }

    public void filePrintClick(boolean showDialog) {
        try {
            if (showDialog) {
                PrinterJob pj = PrinterJob.getPrinterJob();
                if (pj.printDialog()) {
                    pj.setPrintable(new Printable() {
                        @Override
                        public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
                            if (pageIndex == 0) {
                                printAll(graphics, pageFormat, pageIndex);
                                return PAGE_EXISTS;
                            } else {
                                return NO_SUCH_PAGE;
                            }
                        }
                    }, pageFormat);
                    pj.print();
                }
            } else {
                PrinterJob pj = PrinterJob.getPrinterJob();
                pj.setPrintable(new Printable() {
                    @Override
                    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
                        if (pageIndex == 0) {
                            printAll(graphics, pageFormat, pageIndex);
                            return PAGE_EXISTS;
                        } else {
                            return NO_SUCH_PAGE;
                        }
                    }
                }, pageFormat);
                pj.print();
            }
        } catch (PrinterException e) {
            // TODO show error dialog
        }
    }

    public void filePrintersetupClick() {
        PrinterJob pj = PrinterJob.getPrinterJob();
        pageFormat = pj.pageDialog(pj.defaultPage());
    }

    public void fileExitClick() {
        if (modified) {
            int r = JOptionPane.showConfirmDialog(this, getString("savechanges"));
            if (r == JOptionPane.CANCEL_OPTION) return;
            if (r == JOptionPane.OK_OPTION) fileSaveClick();
        }
        // TODO maybe need to save settings?
        System.exit(0);
    }

    public void patternWidthClick() {
        int old = model.getWidth();
        PatternWidthForm form = new PatternWidthForm(this);
        form.setPatternWidth(model.getWidth());
        form.setVisible(true);
        if (form.isOK()) {
            model.snapshot(modified);
            model.setWidth(form.getPatternWidth());
            if (!modified) {
                modified = (old != model.getWidth());
            }
            updateTitle();
            model.setRepeatDirty();
        }
    }

    private void draftLinePreview() {
        if (!sbToolPencil.isSelected()) return;
        if (!selection.isActive()) return;
        draft.linePreview(selection.getOrigin(), selection.getLineDest());
    }

    private void drawSelection() {
        if (!sbToolSelect.isSelected()) return;
        if (!selection.isActive()) return;
        draft.drawSelection();
    }

    private void clearSelection() {
        if (!selection.isActive()) return;
        draft.clearSelection();
        selection.clear();
    }

    private void drawPrepress() {
        if (sbToolPencil.isSelected()) {
            draft.drawPrepress(selection.getOrigin());
        }
    }

    public void draftMouseDown(MouseEvent event) {
        if (dragging) return;
        Point pt = new Point(event.getX(), event.getY());
        if (event.getButton() == MouseEvent.BUTTON1 && draft.mouseToField(pt)) {
            clearSelection();
            dragging = true;
            selection.init(pt);
            drawPrepress();
            draftLinePreview();
            drawSelection();
        }
    }

    public void draftMouseMove(MouseEvent event) {
        Point pt = new Point(event.getX(), event.getY());
        if (dragging && draft.mouseToField(pt)) {
            clearSelection();
            draftLinePreview();
            selection.update(pt);
            draftLinePreview();
            drawSelection();
        }
    }

    public void draftMouseUp(MouseEvent event) {
        Point pt = new Point(event.getX(), event.getY());
        if (dragging && draft.mouseToField(pt)) {
            draftLinePreview();
            clearSelection();
            selection.update(pt);
            dragging = false;
            if (sbToolPencil.isSelected()) {
                if (!selection.isActive()) {
                    setPoint(selection.getOrigin());
                } else {
                    drawLine(selection.getOrigin(), selection.getLineDest());
                }
            } else if (sbToolFill.isSelected()) {
                fillLine(selection.getOrigin());
            } else if (sbToolPipette.isSelected()) {
                selectColorFrom(selection.getOrigin());
            } else if (sbToolSelect.isSelected()) {
                if (selection.isActive()) {
                    drawSelection();
                } else {
                    setPoint(selection.getOrigin());
                }
            }
        }
    }

    private void selectColorFrom(Point pt) {
        byte colorIndex;
        colorIndex = model.get(pt.scrolled(model.getScroll()));
        assert (colorIndex >= 0 && colorIndex < 10);
        switch (colorIndex) {
        case 0:
            sbColor0.setSelected(true);
            break;
        case 1:
            sbColor1.setSelected(true);
            break;
        case 2:
            sbColor2.setSelected(true);
            break;
        case 3:
            sbColor3.setSelected(true);
            break;
        case 4:
            sbColor4.setSelected(true);
            break;
        case 5:
            sbColor5.setSelected(true);
            break;
        case 6:
            sbColor6.setSelected(true);
            break;
        case 7:
            sbColor7.setSelected(true);
            break;
        case 8:
            sbColor8.setSelected(true);
            break;
        case 9:
            sbColor9.setSelected(true);
            break;
        default:
            assert (false);
            break;
        }
    }

    private void drawLine(Point begin, Point end) {
        model.snapshot(modified);
        model.drawLine(begin, end);
        modified = true;
        updateTitle();
    }

    private void fillLine(Point pt) {
        model.snapshot(modified);
        model.fillLine(pt);
        modified = true;
        updateTitle();
        report.repaint();
    }

    private void setPoint(Point pt) {
        model.snapshot(modified);
        model.setPoint(pt);
        modified = true;
        updateTitle();
        report.repaint();
    }

    public void editUndoClick() {
        modified = model.undo();
        updateTitle();
        model.setRepeatDirty();
    }

    public void editRedoClick() {
        modified = model.redo();
        updateTitle();
        model.setRepeatDirty();
    }

    public void viewZoomInClick() {
        model.zoomIn();
        updateScrollbar();
    }

    public void viewZoomNormalClick() {
        if (model.isNormalZoom()) return;
        model.zoomNormal();
        updateScrollbar();
    }

    public void viewZoomOutClick() {
        model.zoomOut();
        updateScrollbar();
    }

    public void viewDraftClick() {
        draft.setVisible(viewDraft.isSelected());
        laDraft.setVisible(draft.isVisible());
    }

    public void viewNormalClick() {
        normal.setVisible(viewNormal.isSelected());
        laNormal.setVisible(normal.isVisible());
    }

    public void viewSimulationClick() {
        simulation.setVisible(viewSimulation.isSelected());
        laSimulation.setVisible(simulation.isVisible());
    }

    public void viewReportClick() {
        report.setVisible(viewReport.isSelected());
        laReport.setVisible(report.isVisible());
    }

    public void formKeyUp(KeyEvent event) {
        int Key = event.getKeyCode();
        if (Key == KeyEvent.VK_F5) {
            repaint();
        } else if (event.getKeyChar() >= '0' && event.getKeyChar() <= '9') {
            model.setColorIndex((byte) (event.getKeyChar() - '0'));
            switch (model.getColorIndex()) {
            case 0:
                sbColor0.setSelected(true);
                break;
            case 1:
                sbColor1.setSelected(true);
                break;
            case 2:
                sbColor2.setSelected(true);
                break;
            case 3:
                sbColor3.setSelected(true);
                break;
            case 4:
                sbColor4.setSelected(true);
                break;
            case 5:
                sbColor5.setSelected(true);
                break;
            case 6:
                sbColor6.setSelected(true);
                break;
            case 7:
                sbColor7.setSelected(true);
                break;
            case 8:
                sbColor8.setSelected(true);
                break;
            case 9:
                sbColor9.setSelected(true);
                break;
            default:
                assert (false);
                break;
            }
        } else if (Key == KeyEvent.VK_SPACE) {
            getAction("tool.pencil").putValue("SELECT", true);
//            sbToolPoint.setSelected(true);
//            toolPoint.setSelected(true);
        } else if (Key == KeyEvent.VK_ESCAPE) {
            // righttimer.Enabled = false;
            // lefttimer.Enabled = false;
        }
    }

    private void rotateLeft() {
        int shift = model.getShift();
        shift = (shift - 1 + model.getWidth()) % model.getWidth();
        modified = true;
        updateTitle();
        simulation.repaint();
    }

    private void rotateRight() {
        int shift = model.getShift();
        shift = (shift + 1) % model.getWidth();
        modified = true;
        updateTitle();
        simulation.repaint();
    }

    // TODO split this for every color toolbar button
    public void colorClick(ActionEvent event) {
        Object sender = event.getSource();
        if (sender == sbColor0)
            model.setColorIndex((byte) 0);
        else if (sender == sbColor1)
            model.setColorIndex((byte) 1);
        else if (sender == sbColor2)
            model.setColorIndex((byte) 2);
        else if (sender == sbColor3)
            model.setColorIndex((byte) 3);
        else if (sender == sbColor4)
            model.setColorIndex((byte) 4);
        else if (sender == sbColor5)
            model.setColorIndex((byte) 5);
        else if (sender == sbColor6)
            model.setColorIndex((byte) 6);
        else if (sender == sbColor7)
            model.setColorIndex((byte) 7);
        else if (sender == sbColor8)
            model.setColorIndex((byte) 8);
        else if (sender == sbColor9) model.setColorIndex((byte) 9);
    }

    // TODO split this for every color toolbar button
    public void colorDblClick(Object sender) {
        int c = 0;
        if (sender == sbColor0)
            c = 0;
        else if (sender == sbColor1)
            c = 1;
        else if (sender == sbColor2)
            c = 2;
        else if (sender == sbColor3)
            c = 3;
        else if (sender == sbColor4)
            c = 4;
        else if (sender == sbColor5)
            c = 5;
        else if (sender == sbColor6)
            c = 6;
        else if (sender == sbColor7)
            c = 7;
        else if (sender == sbColor8)
            c = 8;
        else if (sender == sbColor9) c = 9;
        if (c == 0) return;
        Color color = JColorChooser.showDialog(this, "choose color", model.getColor(c));
        if (color == null) return;
        model.snapshot(modified);
        model.setColor(c, color);
        modified = true;
        updateTitle();
        setColorIcons();
    }

    // TODO handle out parameter
    private void scrollbarScroll(AdjustmentEvent event) {
        int oldscroll = model.getScroll();
        // if (ScrollPos > scrollbar.Max - scrollbar.PageSize) ScrollPos =
        // scrollbar.Max - scrollbar.PageSize;
        model.setScroll(scrollbar.getMaximum() - scrollbar.getBlockIncrement() - scrollbar.getValue());
    }

    private void updateHandler() {
        // Menü- und Toolbar enablen/disablen
        getAction("edit.arrange").setEnabled(selection.isActive());
        getAction("edit.undo").setEnabled(model.canUndo());
        getAction("edit.redo").setEnabled(model.canRedo());

        // Rapport berechnen und zeichnen
        if (model.isRepeatDirty()) {
            model.updateRepeat();
        }

        // Vorsorgliches Undo
        model.prepareSnapshot(modified);
    }

    public void toolPencilClick() {
        clearSelection();
    }

    public void toolSelectClick() {
        clearSelection();
    }

    public void toolFillClick() {
        clearSelection();
    }

    public void toolPipetteClick() {
        clearSelection();
    }

    public void normalMouseUp(MouseEvent event) {
        int scroll = model.getScroll();
        // TODO move this to the NormalPanel
        Point pt = new Point(event.getX(), event.getY());
        if (event.getButton() == MouseEvent.BUTTON1 && normal.mouseToField(pt)) {
            // Lineare Koordinaten berechnen
            int idx = 0;
            int m1 = model.getWidth();
            int m2 = m1 + 1;
            for (int j = 0; j < pt.getY() + scroll; j++) {
                if (j % 2 == 0)
                    idx += m1;
                else
                    idx += m2;
            }
            idx += pt.getX();

            // Feld setzen und Darstellung nachf�hren
            int j = idx / model.getWidth();
            int i = idx % model.getWidth();
            setPoint(new Point(i, j - scroll));
        }
    }

    public void infoAboutClick() {
        new AboutBox(this).setVisible(true);
    }

    private void lefttimerTimer() {
        rotateLeft();
        // Application.ProcessMessages(); // FIXME maybe just remove it?
    }

    private void righttimerTimer() {
        rotateRight();
        // Application.ProcessMessages(); // FIXME maybe just remove it?
    }

    private void sbRotaterightMouseDown(MouseEvent event) {
        rotateRight();
        // Application.ProcessMessages();
        // righttimer.Enabled = true;
    }

    private void sbRotaterightMouseUp(MouseEvent event) {
        // righttimer.Enabled = false;
    }

    private void sbRotateleftMouseDown(MouseEvent event) {
        rotateLeft();
        // Application.ProcessMessages();
        // lefttimer.Enabled = true;
    }

    private void sbRotateleftMouseUp(MouseEvent event) {
        // lefttimer.Enabled = false;
    }

    public void formKeyDown(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.VK_RIGHT) {
            rotateRight();
        } else if (event.getKeyCode() == KeyEvent.VK_LEFT) {
            rotateLeft();
        }
    }

    private boolean canTerminateApp() {
        if (modified) {
            int r = JOptionPane.showConfirmDialog(this, getString("savechanges"));
            if (r == JOptionPane.CANCEL_OPTION) {
                return false;
            }
            if (r == JOptionPane.OK_OPTION) fileSaveClick();
        }
        return true;
    }

    public void editArrangeClick() {
        CopyForm copyform = new CopyForm(this);
        copyform.setVisible(true);
        if (copyform.isOK()) {
            // TODO move this to model!
            model.snapshot(modified);
            BeadField buffer = model.getCopy();
            for (int i = selection.left(); i <= selection.right(); i++) {
                for (int j = selection.bottom(); j <= selection.top(); j++) {
                    byte c = buffer.get(new Point(i, j));
                    if (c == 0) continue;
                    int idx = getIndex(i, j);
                    for (int k = 0; k < copyform.getCopies(); k++) {
                        idx += getCopyOffset(copyform);
                        if (model.isValidIndex(idx)) model.set(idx, c);
                    }
                }
            }
            model.setRepeatDirty();
            modified = true;
            updateTitle();
            report.repaint();
        }
    }

    private int getCopyOffset(CopyForm form) {
        return form.getVertOffset() * model.getWidth() + form.getHorzOffset();
    }

    private int getIndex(int i, int j) {
        return j * model.getWidth() + i;
    }

    public void editInsertLineClick() {
        model.snapshot(modified);
        model.insertLine();
        model.setRepeatDirty();
        modified = true;
        updateTitle();
    }

    public void editDeleteLineClick() {
        model.snapshot(modified);
        model.deleteLine();
        model.setRepeatDirty();
        modified = true;
        updateTitle();
    }

    public void setAppTitle() {
        updateTitle();
    }

    public void updateTitle() {
        String c = "jbead"; // APP_TITLE;
        c += " - ";
        if (saved) {
            c += model.getFile().getName();
        } else {
            c += "unnamed"; // DATEI_UNBENANNT;
        }
        if (modified) {
            c += "*";
        }
        setTitle(c);
    }

    private int mm2px(int x, int sx) {
        return x * sx / 254;
    }

    private int mm2py(int y, int sy) {
        return y * sy / 254;
    }

    private void printAll(Graphics g, PageFormat pageFormat, int pageIndex) {
//        String title = "jbead"; // APP_TITLE;
//        title += " - " + savedialog.getSelectedFile().getName();
        // TODO print headers and footers?

        int sx = 72; // 72 dpi
        int sy = 72; // 72 dpi

        int zoomIndex = model.getZoomIndex();
        int gx = (15 + zoomIndex * 5) * sx / 254;
        int gy = (15 + zoomIndex * 5) * sy / 254;

        int draftleft = 0;
        int normalleft = 0;
        int simulationleft = 0;
        int reportleft = 0;
        int reportcols = 0;

        int m = mm2px(10, sx);
        if (draft.isVisible()) {
            draftleft = m;
            m += mm2px(13, sx) + model.getWidth() * gx + mm2px(7, sx);
        }

        if (normal.isVisible()) {
            normalleft = m;
            m += mm2px(7, sx) + (model.getWidth() + 1) * gx;
        }

        if (simulation.isVisible()) {
            simulationleft = m;
            m += mm2px(7, sx) + (model.getWidth() / 2 + 1) * gx;
        }

        if (report.isVisible()) {
            reportleft = m;
            reportcols = ((int) pageFormat.getWidth() - m - 10) / (mm2px(5, sx) + mm2px(8, sx));
        }

        int h = (int) pageFormat.getHeight() - mm2py(10, sy);

        // //////////////////////////////////////
        //
        // Draft
        //
        // //////////////////////////////////////

        // Grid
        g.setColor(Color.BLACK);
        int left = draftleft + mm2px(13, sx);
        if (left < 0) left = 0;
        int maxj = Math.min(model.getHeight(), (h - mm2py(10, sy)) / gy);
        for (int i = 0; i < model.getWidth() + 1; i++) {
            g.drawLine(left + i * gx, h - (maxj) * gy, left + i * gx, h - 1);
        }
        for (int j = 0; j <= maxj; j++) {
            g.drawLine(left, h - 1 - j * gy, left + model.getWidth() * gx, h - 1 - j * gy);
        }

        // Daten
        for (int i = 0; i < model.getWidth(); i++) {
            for (int j = 0; j < maxj; j++) {
                byte c = model.get(new Point(i, j));
                if (c > 0) {
                    g.setColor(model.getColor(c));
                    g.fillRect(left + i * gx + 1, h - (j + 1) * gy, gx, gy);
                }
            }
        }

        // Zehnermarkierungen
        g.setColor(Color.BLACK);
        for (int j = 0; j < maxj; j++) {
            if ((j % 10) == 0) {
                g.drawLine(draftleft, h - j * gy - 1, left - mm2px(3, sx), h - j * gy - 1);
                g.drawString(Integer.toString(j), draftleft, h - j * gy + mm2py(1, sy));
            }
        }

        // //////////////////////////////////////
        //
        // Korrigiert (normal)
        //
        // //////////////////////////////////////

        // Grid
        g.setColor(Color.BLACK);
        left = normalleft + gx / 2;
        if (left < 0) left = gx / 2;
        maxj = Math.min(model.getHeight(), (h - mm2py(10, sy)) / gy);
        for (int i = 0; i < model.getWidth() + 1; i++) {
            for (int jj = 0; jj < maxj; jj += 2) {
                g.drawLine(left + i * gx, h - (jj + 1) * gy, left + i * gx, h - jj * gy);
            }
        }
        for (int i = 0; i <= model.getWidth() + 1; i++) {
            for (int jj = 1; jj < maxj; jj += 2) {
                g.drawLine(left + i * gx - gx / 2, h - (jj + 1) * gy, left + i * gx - gx / 2, h - jj * gy);
            }
        }
        g.drawLine(left, h - 1, left + model.getWidth() * gx + 1, h - 1);
        for (int jj = 1; jj <= maxj; jj++) {
            g.drawLine(left - gx / 2, h - 1 - jj * gy, left + model.getWidth() * gx + gx / 2 + 1, h - 1 - jj * gy);
        }

        // Daten
        for (int i = 0; i < model.getWidth(); i++) {
            for (int jj = 0; jj < maxj; jj++) {
                byte c = model.get(new Point(i, jj));
                if (c == 0) continue;
                g.setColor(model.getColor(c));
                int ii = i;
                int j1 = jj;
                ii = correctCoordinatesX(ii, j1);
                j1 = correctCoordinatesY(ii, j1);
                if (j1 % 2 == 0) {
                    g.fillRect(left + ii * gx + 1, h - (j1 + 1) * gy, gx, gy);
                } else {
                    g.fillRect(left - gx / 2 + ii * gx + 1, h - (j1 + 1) * gy, gx, gy);
                }
            }
        }

        // //////////////////////////////////////
        //
        // Simulation
        //
        // //////////////////////////////////////

        // Grid
        g.setColor(Color.BLACK);
        left = simulationleft + gx / 2;
        if (left < 0) left = gx / 2;
        maxj = Math.min(model.getHeight(), (h - mm2py(10, sy)) / gy);
        int w = model.getWidth() / 2;
        for (int j = 0; j < maxj; j += 2) {
            for (int i = 0; i < w + 1; i++) {
                g.drawLine(left + i * gx, h - (j + 1) * gy, left + i * gx, h - j * gy);
            }
            if (j > 0) {
                g.drawLine(left - gx / 2, h - (j + 1) * gy, left - gx / 2, h - j * gy);
            }
        }
        for (int j = 1; j < maxj; j += 2) {
            for (int i = 0; i < w + 1; i++) {
                g.drawLine(left + i * gx - gx / 2, h - (j + 1) * gy, left + i * gx - gx / 2, h - j * gy);
            }
            g.drawLine(left + w * gx, h - (j + 1) * gy, left + w * gx, h - j * gy);
        }
        g.drawLine(left, h - 1, left + w * gx + 1, h - 1);
        for (int j = 1; j <= maxj; j++) {
            g.drawLine(left - gx / 2, h - 1 - j * gy, left + w * gx + 1, h - 1 - j * gy);
        }

        // Daten
        for (int i = 0; i < model.getWidth(); i++) {
            for (int j = 0; j < maxj; j++) {
                byte c = model.get(new Point(i, j));
                if (c == 0) continue;
                g.setColor(model.getColor(c));
                int ii = i;
                int jj = j;
                ii = correctCoordinatesX(ii, jj);
                jj = correctCoordinatesY(ii, jj);
                if (ii > w && ii != model.getWidth()) continue;
                if (jj % 2 == 0) {
                    if (ii == w) continue;
                    g.fillRect(left + ii * gx + 1, h - (jj + 1) * gy, gx, gy);
                } else {
                    if (ii != model.getWidth() && ii != w) {
                        g.fillRect(left - gx / 2 + ii * gx + 1, h - (jj + 1) * gy, gx, gy);
                    } else if (ii == w) {
                        g.fillRect(left - gx / 2 + ii * gx + 1, h - (jj + 1) * gy, gx / 2, gy);
                    } else {
                        g.fillRect(left - gx / 2 + 1, h - (jj + 2) * gy, gx / 2, gy);
                    }
                }
            }
        }

        // //////////////////////////////////////
        //
        // Auswertung
        //
        // //////////////////////////////////////

        int x1 = reportleft;
        int x2 = reportleft + mm2px(30, sx);
        int y = mm2py(10, sy);
        int dy = mm2py(5, sy);
        int dx = mm2px(5, sx);

        // Mustername
        g.setColor(Color.BLACK);
        g.drawString(getString("report.pattern"), x1, y);
        g.drawString(model.getFile().getName(), x2, y);
        y += dy;
        // Umfang
        g.drawString(getString("report.circumference"), x1, y);
        g.drawString(Integer.toString(model.getWidth()), x2, y);
        y += dy;
        // Farbrapport
        g.drawString(getString("report.colorrepeat"), x1, y);
        g.drawString(Integer.toString(model.getColorRepeat()) + " " + getString("report.beads"), x2, y);
        y += dy;
        int colorRepeat = model.getColorRepeat();
        // Faedelliste...
        if (colorRepeat > 0) {
            int page = 1;
            int column = 0;
            g.drawString(getString("report.listofbeads"), x1, y);
            y += dy;
            int ystart = y;
            byte col = model.get(colorRepeat - 1);
            int count = 1;
            for (int i = colorRepeat - 2; i >= 0; i--) {
                if (model.get(i) == col) {
                    count++;
                } else {
                    if (col != 0) {
                        g.setColor(model.getColor(col));
                        g.fillRect(x1, y, dx - mm2px(1, sx), dy - mm2py(1, sy));
                        g.setColor(Color.WHITE);
                        g.drawRect(x1, y, dx - mm2px(1, sx), dy - mm2py(1, sy));
                    } else {
                        g.setColor(Color.WHITE);
                        g.fillRect(x1, y, dx - mm2px(1, sx), dy - mm2py(1, sy));
                        g.setColor(Color.BLACK);
                        g.drawRect(x1, y, dx - mm2px(1, sx), dy - mm2py(1, sy));
                    }
                    g.setColor(Color.BLACK);
                    g.drawString(Integer.toString(count), x1 + dx + 3, y);
                    y += dy;
                    col = model.get(i);
                    count = 1;
                }
                if (y >= (int) pageFormat.getHeight() - mm2py(10, sy)) {
                    x1 += dx + mm2px(8, sx);
                    y = ystart;
                    column++;
                    if (column >= reportcols) { // neue Seite und weiter...
                        // TODO handle multipage output, sigh...
                        break;
                        // Printer().NewPage();
                        // x1 = draftleft;
                        // x2 = draftleft + MM2PRx(30, sx);
                        // y = MM2PRy(10, sy);
                        // reportcols = (Printer().PageWidth - draftleft - 10) /
                        // (MM2PRx(5, sx) + MM2PRx(8, sx));
                        // column = 0;
                        // page++;
                        // canvas.Pen.Color = clBlack;
                        // canvas.TextOut (x1, y,
                        // String(Language.STR("Pattern ",
                        // "Muster "))+savedialog.getSelectedFile().getName() +
                        // " - " + Language.STR("page ", "Seite ") +
                        // IntToStr(page));
                        // y += dy;
                        // ystart = y;
                    }
                }
            }
            if (y < (int) pageFormat.getHeight() - mm2py(10, sy)) {
                if (col != 0) {
                    g.setColor(model.getColor(col));
                    g.fillRect(x1, y, dx - mm2px(1, sx), dy - mm2py(1, sy));
                    g.setColor(Color.WHITE);
                    g.drawRect(x1, y, dx - mm2px(1, sx), dy - mm2py(1, sy));
                } else {
                    g.setColor(Color.WHITE);
                    g.fillRect(x1, y, dx - mm2px(1, sx), dy - mm2py(1, sy));
                    g.setColor(Color.BLACK);
                    g.drawRect(x1, y, dx - mm2px(1, sx), dy - mm2py(1, sy));
                }
                g.setColor(Color.BLACK);
                g.drawString(Integer.toString(count), x1 + dx + 3, y);
            }
        }
    }

    private void addToMRU(File file) {
        if (file.getPath() == "") return;

        // Wenn Datei schon in MRU: Eintrag nach oben schieben
        for (int i = 0; i < 6; i++) {
            if (mru[i].equals(file.getPath())) {
                if (i > 0) {
                    String temp = mru[i];
                    for (int j = i; j > 0; j--)
                        mru[j] = mru[j - 1];
                    mru[0] = temp;
                }
                updateMRU();
                saveMRU();
                return;
            }
        }

        // Ansonsten wird alles um einen Platz nach unten
        // geschoben und der Dateiname im ersten Eintrag
        // vermerkt.
        for (int i = 5; i > 0; i--)
            mru[i] = mru[i - 1];
        mru[0] = file.getPath();

        updateMRU();
        saveMRU();
    }

    private void updateMRU() {
        // TODO maybe need to tweak the mru text so that local directory is taken into account
        getAction("file.mru1").putValue(Action.NAME, mru[0]);
        getAction("file.mru2").putValue(Action.NAME, mru[1]);
        getAction("file.mru3").putValue(Action.NAME, mru[2]);
        getAction("file.mru4").putValue(Action.NAME, mru[3]);
        getAction("file.mru5").putValue(Action.NAME, mru[4]);
        getAction("file.mru6").putValue(Action.NAME, mru[5]);
        // TODO maybe have to set visibility of separator after last mru menu item
    }
    
    public void fileMRU1Click() {
        loadFile(new File(mru[0]), true);
    }

    public void fileMRU2Click() {
        loadFile(new File(mru[1]), true);
    }

    public void fileMRU3Click() {
        loadFile(new File(mru[2]), true);
    }

    public void fileMRU4Click() {
        loadFile(new File(mru[3]), true);
    }

    public void fileMRU5Click() {
        loadFile(new File(mru[4]), true);
    }

    public void fileMRU6Click() {
        loadFile(new File(mru[5]), true);
    }

    private void saveMRU() {
        Settings settings = new Settings();
        settings.SetCategory("mru");
        settings.SaveString("mru0", mru[0]);
        settings.SaveString("mru1", mru[1]);
        settings.SaveString("mru2", mru[2]);
        settings.SaveString("mru3", mru[3]);
        settings.SaveString("mru4", mru[4]);
        settings.SaveString("mru5", mru[5]);
    }

    private void loadMRU() {
        Settings settings = new Settings();
        settings.SetCategory("mru");
        mru[0] = settings.LoadString("mru0");
        mru[1] = settings.LoadString("mru1");
        mru[2] = settings.LoadString("mru2");
        mru[3] = settings.LoadString("mru3");
        mru[4] = settings.LoadString("mru4");
        mru[5] = settings.LoadString("mru5");
    }

    public static void main(String[] args) {
        new BeadForm().setVisible(true);
    }

}
