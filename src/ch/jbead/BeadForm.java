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
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.KeyEvent;
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
import java.util.Locale;

import javax.imageio.ImageIO;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
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
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollBar;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;

/**
 * 
 */
public class BeadForm extends JFrame {

    private static final long serialVersionUID = 1L;

    private Model model = new Model();
    
    private int begin_i;
    private int begin_j;
    private int end_i;
    private int end_j;
    private int sel_i1, sel_i2, sel_j1, sel_j2;
    private boolean selection;
    private BeadField sel_buff = new BeadField();
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

    private DraftPanel draft = new DraftPanel(model);
    private NormalPanel normal = new NormalPanel(model);
    private SimulationPanel simulation = new SimulationPanel(model);
    private ReportPanel report = new ReportPanel(model);

    private JLabel laDraft = new JLabel("draft");
    private JLabel laNormal = new JLabel("normal");
    private JLabel laSimulation = new JLabel("simulation");
    private JLabel laReport = new JLabel("report");

    private JMenu menuFile = new JMenu("file");
    private JMenuItem fileNew = new JMenuItem("new");
    private JMenuItem fileOpen = new JMenuItem("open");
    private JMenuItem fileSave = new JMenuItem("save");
    private JMenuItem fileSaveas = new JMenuItem("save as");
    private JMenuItem filePrint = new JMenuItem("print");
    private JMenuItem filePrintersetup = new JMenuItem("printer setup");
    private JMenuItem fileExit = new JMenuItem("exit");

    private JMenuItem fileMRU1 = new JMenuItem();
    private JMenuItem fileMRU2 = new JMenuItem();
    private JMenuItem fileMRU3 = new JMenuItem();
    private JMenuItem fileMRU4 = new JMenuItem();
    private JMenuItem fileMRU5 = new JMenuItem();
    private JMenuItem fileMRU6 = new JMenuItem();

    private JPopupMenu.Separator fileMRUSeparator = new JPopupMenu.Separator();

    private JMenu menuEdit = new JMenu("edit");
    private JMenuItem editUndo = new JMenuItem("undo");
    private JMenuItem editRedo = new JMenuItem("redo");
    private JMenuItem editCopy = new JMenuItem("arrange");
    private JMenu editLine = new JMenu("empty line");
    private JMenuItem editInsertline = new JMenuItem("insert");
    private JMenuItem editDeleteline = new JMenuItem("delete");

    private JMenu menuView = new JMenu("view");
    private JMenuItem viewZoomin = new JMenuItem("zoom in");
    private JMenuItem viewZoomout = new JMenuItem("zoom out");
    private JMenuItem viewZoomnormal = new JMenuItem("normal");
    private JMenu viewLanguage = new JMenu("language");

    private JCheckBoxMenuItem viewDraft = new JCheckBoxMenuItem("draft");
    private JCheckBoxMenuItem viewNormal = new JCheckBoxMenuItem("normal");
    private JCheckBoxMenuItem viewSimulation = new JCheckBoxMenuItem("simulation");
    private JCheckBoxMenuItem viewReport = new JCheckBoxMenuItem("report");

    private ButtonGroup languageGroup = new ButtonGroup();
    private JRadioButtonMenuItem languageEnglish = new JRadioButtonMenuItem("English");
    private JRadioButtonMenuItem languageGerman = new JRadioButtonMenuItem("German");

    private JMenu menuTool = new JMenu("tool");
    private JMenuItem toolPoint = new JMenuItem("pencil");
    private JMenuItem toolSelect = new JMenuItem("select");
    private JMenuItem toolFill = new JMenuItem("fill");
    private JMenuItem toolSniff = new JMenuItem("pipette");

    private JMenu menuPattern = new JMenu("pattern");
    private JMenuItem patternWidth = new JMenuItem("width");

    private JMenu menuInfo = new JMenu("?");
    private JMenuItem infoAbout = new JMenuItem("about jbead");

    private JButton sbNew = createButton("sb_new");
    private JButton sbOpen = createButton("sb_open");
    private JButton sbSave = createButton("sb_save");
    private JButton sbPrint = createButton("sb_print");
    private JButton sbUndo = createButton("sb_undo");
    private JButton sbRedo = createButton("sb_redo");
    private JButton sbRotateleft = createButton("sb_prev");
    private JButton sbRotateright = createButton("sb_next");
    private JButton sbCopy = createButton("sb_copy");
    
    private ButtonGroup toolsGroup = new ButtonGroup();
    private JToggleButton sbToolSelect = createToggleButton("sb_toolselect");
    private JToggleButton sbToolPoint = createToggleButton("sb_toolpoint");
    private JToggleButton sbToolFill = createToggleButton("sb_toolfill");
    private JToggleButton sbToolSniff = createToggleButton("sb_toolsniff");

    private PageFormat pageFormat;

    private JPanel main = new JPanel();
    private JLabel statusbar = new JLabel("X");
    
    public BeadForm() {
        super("jbead");
        createGUI();
        saved = false;
        modified = false;
        selection = false;
        updateTitle();
        setColorIcons();
        loadMRU();
        updateMRU();
        updateScrollbar();
        initLanguage();
        initCloseHandler();
        
        setIconImage(ImageFactory.getImage("jbead-16"));
        
        // TODO persist the pageFormat in Settings?
        pageFormat = PrinterJob.getPrinterJob().defaultPage();
        pageFormat.setOrientation(PageFormat.LANDSCAPE);
    }

    private JButton createButton(String imageName) {
        return new JButton(ImageFactory.getIcon(imageName));
    }
    
    private JToggleButton createToggleButton(String imageName) {
        return new JToggleButton(ImageFactory.getIcon(imageName));
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

    private void initLanguage() {
        Settings settings = new Settings();
        settings.SetCategory("Environment");
        Language language;
        int lang = settings.LoadInt("Language", -1);
        if (lang == -1) { // Windows-Spracheinstellung abfragen
            Locale locale = Locale.getDefault();
            if (locale.getLanguage().equals("de")) {
                lang = 1;
            } else {
                lang = 0;
            }
        }
        language = lang == 0 ? Language.EN : Language.GE;
        Texts.forceLanguage(language,  this);
        if (Texts.active_language == Language.EN) {
            languageEnglish.setSelected(true);
        } else {
            languageGerman.setSelected(true);
        }

        // TODO persist location and size in settings
        setSize(1024, 700);
        setLocation(100, 35);
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
        menuFile.add(fileNew);
        menuFile.add(fileOpen);
        fileOpen.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fileOpenClick();
            }
        });
        menuFile.add(fileSave);
        fileSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fileSaveClick();
            }
        });
        menuFile.add(fileSaveas);
        menuFile.addSeparator();
        menuFile.add(filePrint);
        menuFile.add(filePrintersetup);
        menuFile.addSeparator();
        menuFile.add(fileMRU1);
        menuFile.add(fileMRU2);
        menuFile.add(fileMRU3);
        menuFile.add(fileMRU4);
        menuFile.add(fileMRU5);
        menuFile.add(fileMRU6);
        menuFile.add(fileMRUSeparator);
        menuFile.add(fileExit);
        fileExit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fileExitClick();
            }
        });
        return menuFile;
    }
    
    private JMenu createEditMenu() {
        menuEdit.add(editUndo);
        menuEdit.add(editRedo);
        menuEdit.add(editCopy);
        menuEdit.add(editLine);
        editLine.add(editInsertline);
        editLine.add(editDeleteline);
        return menuEdit;
    }

    private JMenu createViewMenu() {
        menuView.add(viewDraft);
        menuView.add(viewNormal);
        menuView.add(viewSimulation);
        menuView.add(viewReport);
        menuView.addSeparator();
        menuView.add(viewLanguage);
        viewLanguage.add(languageEnglish);
        viewLanguage.add(languageGerman);
        menuView.addSeparator();
        menuView.add(viewZoomin);
        menuView.add(viewZoomnormal);
        menuView.add(viewZoomout);
        return menuView;
    }

    private JMenu createToolMenu() {
        menuTool.add(toolPoint);
        menuTool.add(toolSelect);
        menuTool.add(toolFill);
        menuTool.add(toolSniff);
        return menuTool;
    }

    private JMenu createPatternMenu() {
        menuPattern.add(patternWidth);
        patternWidth.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new PatternWidthForm().formShow();
            }
        });
        return menuPattern;
    }

    private JMenu createInfoMenu() {
        menuInfo.add(infoAbout);
        infoAbout.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new AboutBox().formShow();
            }
        });
        return menuInfo;
    }

    private void createToolbar() {
        toolbar.add(sbNew);
        toolbar.add(sbOpen);
        toolbar.add(sbSave);
        toolbar.add(sbPrint);
        toolbar.add(sbUndo);
        toolbar.add(sbRedo);
        toolbar.add(sbRotateleft);
        toolbar.add(sbRotateright);
        toolbar.add(sbCopy);
        
        toolbar.addSeparator();

        sbToolPoint.setSelected(true);
        toolbar.add(sbToolPoint);
        toolbar.add(sbToolSelect);
        toolbar.add(sbToolFill);
        toolbar.add(sbToolSniff);
        
        toolsGroup.add(sbToolPoint);
        toolsGroup.add(sbToolSelect);
        toolsGroup.add(sbToolFill);
        toolsGroup.add(sbToolSniff);

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
        c.weightx = 2;
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
        
        // init button group
        languageGroup.add(languageEnglish);
        languageGroup.add(languageGerman);
    }

    private BeadField getField() {
        return model.getField();
    }
    
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

    private void formResize() {
        int cheight = getContentPane().getHeight() - toolbar.getHeight();
        int cwidth = getContentPane().getWidth() - scrollbar.getWidth();
        int top = toolbar.getHeight() + 6;

        int nr = 0;
        if (viewDraft.isSelected()) nr++;
        if (viewNormal.isSelected()) nr++;
        if (viewSimulation.isSelected()) nr++;
        if (viewReport.isSelected()) nr += 2;
        if (nr == 0) {
            viewDraft.setSelected(true);
            draft.setVisible(true);
            laDraft.setVisible(true);
            nr = 1;
        }

        int m = 6;
        int grid = model.getGrid();

        if (viewDraft.isSelected()) {
            draft.setBounds(m, top, getField().getWidth() * grid + 35, cheight - 6 - laDraft.getHeight() - 3);
            laDraft.setLocation(m + (draft.getWidth() - laDraft.getWidth()) / 2, draft.getY() + draft.getHeight() + 2);
            m += draft.getWidth() + 12;
        }

        if (viewNormal.isSelected()) {
            normal.setBounds(m, top, (getField().getWidth() + 1) * grid + 10, cheight - 6 - laNormal.getHeight() - 3);
            laNormal.setLocation(m + (normal.getWidth() - laNormal.getWidth()) / 2, normal.getY() + normal.getHeight() + 2);
            m += normal.getWidth() + 12;
        }

        if (viewSimulation.isSelected()) {
            simulation.setBounds(m, top, (getField().getWidth() + 2) * grid / 2 + 10, cheight - 6 - laSimulation.getHeight() - 3);
            laSimulation.setLocation(m + (simulation.getWidth() - laSimulation.getWidth()) / 2, simulation.getY() + simulation.getHeight() + 2);
            m += simulation.getWidth() + 12;
        }

        if (viewReport.isSelected()) {
            report.setBounds(m, top, cwidth - m - 6, cheight - 6 - laReport.getHeight() - 3);
            laReport.setLocation(m + 5, report.getY() + report.getHeight() + 2);
        }

        scrollbar.setBounds(getContentPane().getWidth() - scrollbar.getWidth(), top, scrollbar.getWidth(), cheight - 6 - laDraft.getHeight() - 3);

        updateScrollbar();
    }

    private void updateScrollbar() {
        int h = draft.getHeight() / model.getGrid();
        assert (h < getField().getHeight());
        scrollbar.setMinimum(0);
        scrollbar.setMaximum(getField().getHeight() - h);
        if (scrollbar.getMaximum() < 0) scrollbar.setMaximum(0);
        scrollbar.setUnitIncrement(h);
        scrollbar.setBlockIncrement(h);
        scrollbar.setValue(scrollbar.getMaximum() - scrollbar.getBlockIncrement() - model.getScroll());
    }

    private int correctCoordinatesX(int i, int j) {
        int idx = i + (j + model.getScroll()) * getField().getWidth();
        int m1 = getField().getWidth();
        int m2 = getField().getWidth() + 1;
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
        int idx = i + (j + model.getScroll()) * getField().getWidth();
        int m1 = getField().getWidth();
        int m2 = getField().getWidth() + 1;
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

    private void updateBead(int i, int j) {
        // use observer pattern to remove this explicit dependency
        draft.redraw(i, j);
        normal.updateBead(i, j);
        simulation.updateBead(i, j);
    }

    private void fileNewClick() {
        // ask whether to save modified document
        if (modified) {
            int answer = JOptionPane.showConfirmDialog(this,
                    Texts.text("Do you want to save your changes?", "Sollen die Änderungen gespeichert werden?"));
            if (answer == JOptionPane.CANCEL_OPTION) return;
            if (answer == JOptionPane.YES_OPTION) {
                fileSaveClick();
            }
        }

        // delete all
        model.clear();
        invalidate();
        sbColor1.setSelected(true);
        setColorIcons();
        updateScrollbar();
        selection = false;
        sbToolPoint.setSelected(true);
        toolPoint.setSelected(true);
        saved = false;
        modified = false;
        updateTitle();
    }

    private void loadFile(File file, boolean addtomru) {
        // ask whether to save modified document
        if (modified) {
            int answer = JOptionPane.showConfirmDialog(this,
                    Texts.text("Do you want to save your changes?", "Sollen die Änderungen gespeichert werden?"));
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
                    JOptionPane.showMessageDialog(this, Texts.text("The file is not a jbead pattern file. It cannot be loaded.",
                            "Die Datei ist keine jbead Musterdatei. Sie kann nicht geladen werden."));
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
        formResize();
        invalidate();
        if (addtomru) addToMRU(file);
    }

    private void fileOpenClick() {
        JFileChooser dialog = new JFileChooser();
        dialog.setCurrentDirectory(model.getFile().getParentFile());
        if (dialog.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            loadFile(dialog.getSelectedFile(), true);
        }
    }

    private void fileSaveClick() {
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

    private void fileSaveasClick() {
        JFileChooser dialog = new JFileChooser();
        if (dialog.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            if (dialog.getSelectedFile().exists()) {
                String msg = Texts.text("The file ", "Die Datei ") + dialog.getSelectedFile().getName()
                        + Texts.text(" already exists. Do you want to overwrite it?", " existiert bereits. Soll sie überschrieben werden?");
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

    private void filePrintClick(ActionEvent event) {
        try {
            Object Sender = event.getSource();
            if (Sender != sbPrint) {
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

    private void filePrintersetupClick() {
        PrinterJob pj = PrinterJob.getPrinterJob();
        pageFormat = pj.pageDialog(pj.defaultPage());
    }

    private void fileExitClick() {
        if (modified) {
            int r = JOptionPane.showConfirmDialog(this,
                    Texts.text("Do you want to save your changes?", "Sollen die Änderungen gespeichert werden?"));
            if (r == JOptionPane.CANCEL_OPTION) return;
            if (r == JOptionPane.OK_OPTION) fileSaveClick();
        }
        // TODO maybe need to save settings?
        System.exit(0);
    }

    private void patternWidthClick() {
        int old = getField().getWidth();
        PatternWidthForm form = new PatternWidthForm();
        form.setWidth(getField().getWidth());
        form.formShow();
        if (form.isOK()) {
            model.snapshot(modified);
            getField().setWidth(form.getWidth());
            formResize();
            invalidate();
            if (!modified) {
                modified = (old != getField().getWidth());
            }
            updateTitle();
            model.setRepeatDirty();
        }
    }

    private int calcLineCoordX(int _i1, int _j1, int _i2, int _j2) {
        int dx = Math.abs(_i2 - _i1);
        int dy = Math.abs(_j2 - _j1);
        if (2 * dy < dx) {
            _j2 = _j1;
        } else if (2 * dx < dy) {
            _i2 = _i1;
        } else {
            int d = Math.min(dx, dy);
            if (_i2 - _i1 > d)
                _i2 = _i1 + d;
            else if (_i1 - _i2 > d) _i2 = _i1 - d;
            if (_j2 - _j1 > d)
                _j2 = _j1 + d;
            else if (_j1 - _j2 > d) _j2 = _j1 - d;
        }
        return _i2;
    }

    private int calcLineCoordY(int _i1, int _j1, int _i2, int _j2) {
        int dx = Math.abs(_i2 - _i1);
        int dy = Math.abs(_j2 - _j1);
        if (2 * dy < dx) {
            _j2 = _j1;
        } else if (2 * dx < dy) {
            _i2 = _i1;
        } else {
            int d = Math.min(dx, dy);
            if (_i2 - _i1 > d)
                _i2 = _i1 + d;
            else if (_i1 - _i2 > d) _i2 = _i1 - d;
            if (_j2 - _j1 > d)
                _j2 = _j1 + d;
            else if (_j1 - _j2 > d) _j2 = _j1 - d;
        }
        return _j2;
    }

    private void draftLinePreview() {
        if (!sbToolPoint.isSelected()) return;
        if (begin_i == end_i && begin_j == end_j) return;

        int ei = end_i;
        int ej = end_j;
        ei = calcLineCoordX(begin_i, begin_j, ei, ej);
        ej = calcLineCoordY(begin_i, begin_j, ei, ej);

        draft.linePreview(new Point(begin_i, begin_j), new Point(ei, ej));
    }

    private void draftSelectPreview(boolean _draw, boolean _doit) {
        if (!sbToolSelect.isSelected() && !_doit) return;
        if (begin_i == end_i && begin_j == end_j) return;

        int i1 = Math.min(begin_i, end_i);
        int i2 = Math.max(begin_i, end_i);
        int j1 = Math.min(begin_j, end_j);
        int j2 = Math.max(begin_j, end_j);

        draft.selectPreview(_draw, new Point(i1, j1), new Point(i2, j2));
    }

    private void draftSelectDraw() {
        if (!selection) return;
        begin_i = sel_i1;
        begin_j = sel_j1;
        end_i = sel_i2;
        end_j = sel_j2;
        draftSelectPreview(true, true);
    }

    private void draftSelectClear() {
        if (!selection) return;
        begin_i = sel_i1;
        begin_j = sel_j1;
        end_i = sel_i2;
        end_j = sel_j2;
        draftSelectPreview(false, true);
        selection = false;
    }

    private void draftMouseDown(MouseEvent event, int X, int Y) {
        if (dragging) return;
        Point pt = new Point(event.getX(), event.getY());
        if (event.getButton() == MouseEvent.BUTTON1 && draft.mouseToField(pt)) {
            draftSelectClear();
            dragging = true;
            begin_i = pt.getX();
            begin_j = pt.getY();
            end_i = pt.getX();
            end_j = pt.getY();
            // Prepress
            if (sbToolPoint.isSelected()) {
                draft.drawPrepress(new Point(begin_i, begin_j));
            }
            draftLinePreview();
            draftSelectPreview(true, false);
        }
    }

    private void draftMouseMove(MouseEvent event) {
        Point pt = new Point(event.getX(), event.getY());
        if (dragging && draft.mouseToField(pt)) {
            draftSelectPreview(false, false);
            draftLinePreview();
            end_i = pt.getX();
            end_j = pt.getY();
            draftLinePreview();
            draftSelectPreview(true, false);
        }
    }

    private void draftMouseUp(MouseEvent event) {
        Point pt = new Point(event.getX(), event.getY());
        int scroll = model.getScroll();
        byte colorIndex = model.getColorIndex();
        if (dragging && draft.mouseToField(pt)) {
            draftLinePreview();
            end_i = pt.getX();
            end_j = pt.getY();
            dragging = false;

            if (sbToolPoint.isSelected()) {
                if (begin_i == end_i && begin_j == end_j) {
                    setPoint(begin_i, begin_j);
                } else {
                    end_i = calcLineCoordX(begin_i, begin_j, end_i, end_j);
                    end_j = calcLineCoordY(begin_i, begin_j, end_i, end_j);
                    if (Math.abs(end_i - begin_i) == Math.abs(end_j - begin_j)) {
                        // 45 grad Linie
                        model.snapshot(modified);
                        int jj;
                        if (begin_i > end_i) {
                            int tmp = begin_i;
                            begin_i = end_i;
                            end_i = tmp;
                            tmp = begin_j;
                            begin_j = end_j;
                            end_j = tmp;
                        }
                        for (int i = begin_i; i <= end_i; i++) {
                            if (begin_j < end_j)
                                jj = begin_j + (i - begin_i);
                            else
                                jj = begin_j - (i - begin_i);
                            getField().set(i, jj + scroll, colorIndex);
                            updateBead(i, jj);
                        }
                        model.setRepeatDirty();
                        modified = true;
                        updateTitle();
                    } else if (end_i == begin_i) {
                        // Senkrechte Linie
                        model.snapshot(modified);
                        int j1 = Math.min(end_j, begin_j);
                        int j2 = Math.max(end_j, begin_j);
                        for (int jj = j1; jj <= j2; jj++) {
                            getField().set(begin_i, jj + scroll, colorIndex);
                            updateBead(begin_i, jj);
                        }
                        modified = true;
                        model.setRepeatDirty();
                        updateTitle();
                    } else if (end_j == begin_j) {
                        // Waagrechte Linie ziehen
                        model.snapshot(modified);
                        int i1 = Math.min(end_i, begin_i);
                        int i2 = Math.max(end_i, begin_i);
                        for (int i = i1; i <= i2; i++) {
                            getField().set(i, begin_j + scroll, colorIndex);
                            updateBead(i, begin_j);
                        }
                        modified = true;
                        model.setRepeatDirty();
                        updateTitle();
                    }
                }
            } else if (sbToolFill.isSelected()) {
                model.snapshot(modified);
                fillLine(end_i, end_j);
                modified = true;
                updateTitle();
                model.setRepeatDirty();
                report.invalidate();
            } else if (sbToolSniff.isSelected()) {
                colorIndex = getField().get(begin_i, begin_j + scroll);
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
            } else if (sbToolSelect.isSelected()) {
                draftSelectPreview(false, false);
                if (begin_i != end_i || begin_j != end_j) {
                    selection = true;
                    sel_i1 = begin_i;
                    sel_j1 = begin_j;
                    sel_i2 = end_i;
                    sel_j2 = end_j;
                    draftSelectDraw();
                }
            }
        }
    }

    private void fillLine(int _i, int _j) {
        int scroll = model.getScroll();
        byte colorIndex = model.getColorIndex();
        // xxx experimentell nach links und rechts
        byte bk = getField().get(_i, _j + scroll);
        int i = _i;
        while (i >= 0 && getField().get(i, _j + scroll) == bk) {
            getField().set(i, _j + scroll, colorIndex);
            // TODO make draft an observer of field!
            updateBead(i, _j);
            i--;
        }
        i = begin_i + 1;
        while (i < getField().getWidth() && getField().get(i, _j + scroll) == bk) {
            getField().set(i, _j + scroll, colorIndex);
            // TODO make draft an observer of field!
            updateBead(i, _j);
            i++;
        }
    }

    private void setPoint(int _i, int _j) {
        int scroll = model.getScroll();
        byte colorIndex = model.getColorIndex();
        model.snapshot(modified);
        byte s = getField().get(_i, _j + scroll);
        if (s == colorIndex) {
            getField().set(_i, _j + scroll, (byte) 0);
        } else {
            getField().set(_i, _j + scroll, colorIndex);
        }
        updateBead(_i, _j);
        modified = true;
        model.setRepeatDirty();
        updateTitle();
    }

    private void editUndoClick() {
        modified = model.undo();
        updateTitle();
        invalidate();
        model.setRepeatDirty();
    }

    private void editRedoClick() {
        modified = model.redo();
        updateTitle();
        invalidate();
        model.setRepeatDirty();
    }

    private void viewZoominClick() {
        model.zoomIn();
        formResize();
        invalidate();
        updateScrollbar();
    }

    private void viewZoomnormalClick() {
        if (model.isNormalZoom()) return;
        model.zoomNormal();
        formResize();
        invalidate();
        updateScrollbar();
    }

    private void viewZoomoutClick() {
        model.zoomOut();
        formResize();
        invalidate();
        updateScrollbar();
    }

    private void viewDraftClick() {
        viewDraft.setSelected(!viewDraft.isSelected());
        draft.setVisible(viewDraft.isSelected());
        laDraft.setVisible(draft.isVisible());
        formResize();
    }

    private void viewNormalClick() {
        viewNormal.setSelected(!viewNormal.isSelected());
        normal.setVisible(viewNormal.isSelected());
        laNormal.setVisible(normal.isVisible());
        formResize();
    }

    private void viewSimulationClick() {
        viewSimulation.setSelected(!viewSimulation.isSelected());
        simulation.setVisible(viewSimulation.isSelected());
        laSimulation.setVisible(simulation.isVisible());
        formResize();
    }

    private void viewReportClick() {
        viewReport.setSelected(!viewReport.isSelected());
        report.setVisible(viewReport.isSelected());
        laReport.setVisible(report.isVisible());
        formResize();
    }

    private void formKeyUp(KeyEvent event) {
        int Key = event.getKeyCode();
        if (Key == KeyEvent.VK_F5)
            invalidate();
        else if (Key == KeyEvent.VK_1 && event.isControlDown() && !event.isAltDown()) {
            sbToolPoint.setSelected(true);
            toolPoint.setSelected(true);
        } else if (Key == KeyEvent.VK_2 && event.isControlDown() && !event.isAltDown()) {
            sbToolSelect.setSelected(true);
            toolSelect.setSelected(true);
        } else if (Key == KeyEvent.VK_3 && event.isControlDown() && !event.isAltDown()) {
            sbToolFill.setSelected(true);
            toolFill.setSelected(true);
        } else if (Key == KeyEvent.VK_4 && event.isControlDown() && !event.isAltDown()) {
            sbToolSniff.setSelected(true);
            toolSniff.setSelected(true);
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
            sbToolPoint.setSelected(true);
            toolPoint.setSelected(true);
        } else if (Key == KeyEvent.VK_ESCAPE) {
            // righttimer.Enabled = false;
            // lefttimer.Enabled = false;
        }
    }

    private void rotateLeft() {
        int shift = model.getShift();
        shift = (shift - 1 + getField().getWidth()) % getField().getWidth();
        modified = true;
        updateTitle();
        simulation.invalidate();
    }

    private void rotateRight() {
        int shift = model.getShift();
        shift = (shift + 1) % getField().getWidth();
        modified = true;
        updateTitle();
        simulation.invalidate();
    }

    // TODO split this for every color toolbar button
    private void colorClick(ActionEvent event) {
        Object Sender = event.getSource();
        if (Sender == sbColor0)
            model.setColorIndex((byte) 0);
        else if (Sender == sbColor1)
            model.setColorIndex((byte) 1);
        else if (Sender == sbColor2)
            model.setColorIndex((byte) 2);
        else if (Sender == sbColor3)
            model.setColorIndex((byte) 3);
        else if (Sender == sbColor4)
            model.setColorIndex((byte) 4);
        else if (Sender == sbColor5)
            model.setColorIndex((byte) 5);
        else if (Sender == sbColor6)
            model.setColorIndex((byte) 6);
        else if (Sender == sbColor7)
            model.setColorIndex((byte) 7);
        else if (Sender == sbColor8)
            model.setColorIndex((byte) 8);
        else if (Sender == sbColor9) model.setColorIndex((byte) 9);
    }

    // TODO split this for every color toolbar button
    private void colorDblClick(ActionEvent event) {
        Object Sender = event.getSource();
        int c = 0;
        if (Sender == sbColor0)
            c = 0;
        else if (Sender == sbColor1)
            c = 1;
        else if (Sender == sbColor2)
            c = 2;
        else if (Sender == sbColor3)
            c = 3;
        else if (Sender == sbColor4)
            c = 4;
        else if (Sender == sbColor5)
            c = 5;
        else if (Sender == sbColor6)
            c = 6;
        else if (Sender == sbColor7)
            c = 7;
        else if (Sender == sbColor8)
            c = 8;
        else if (Sender == sbColor9) c = 9;
        if (c == 0) return;
        Color color = JColorChooser.showDialog(this, "choose color", model.getColor(c));
        if (color == null) return;
        model.snapshot(modified);
        model.setColor(c, color);
        // TODO propagate change to all dependants (or better use observer
        // pattern)
        modified = true;
        updateTitle();
        invalidate();
        setColorIcons();
    }

    // TODO handle out parameter
    private void scrollbarScroll(AdjustmentEvent event) {
        int oldscroll = model.getScroll();
        // if (ScrollPos > scrollbar.Max - scrollbar.PageSize) ScrollPos =
        // scrollbar.Max - scrollbar.PageSize;
        model.setScroll(scrollbar.getMaximum() - scrollbar.getBlockIncrement() - scrollbar.getValue());
        if (oldscroll != model.getScroll()) invalidate();
    }

    private void idleHandler() {
        // Menü- und Toolbar enablen/disablen
        editCopy.setEnabled(selection);
        sbCopy.setEnabled(selection);
        editUndo.setEnabled(model.canUndo());
        editRedo.setEnabled(model.canRedo());
        sbUndo.setEnabled(model.canUndo());
        sbRedo.setEnabled(model.canRedo());

        // FIXME is this whole rapport stuff needed? all drawing code was
        // commented out and thus removed...

        // Rapport berechnen und zeichnen
        if (model.isRepeatDirty()) {
            model.updateRepeat();
            report.invalidate();
        }

        // Vorsorgliches Undo
        model.prepareSnapshot(modified);
    }

    private void toolPointClick() {
        toolPoint.setSelected(true);
        sbToolPoint.setSelected(true);
        draftSelectClear();
    }

    private void toolSelectClick() {
        toolSelect.setSelected(true);
        sbToolSelect.setSelected(true);
    }

    private void toolFillClick() {
        toolFill.setSelected(true);
        sbToolFill.setSelected(true);
        draftSelectClear();
    }

    private void toolSniffClick() {
        toolSniff.setSelected(true);
        sbToolSniff.setSelected(true);
        draftSelectClear();
    }

    private void sbToolPointClick() {
        toolPoint.setSelected(true);
        draftSelectClear();
    }

    private void sbToolFillClick() {
        toolFill.setSelected(true);
        draftSelectClear();
    }

    private void sbToolSniffClick() {
        toolSniff.setSelected(true);
        draftSelectClear();
    }

    private void sbToolSelectClick() {
        toolSelect.setSelected(true);
    }

    private void normalMouseUp(MouseEvent event) {
        int scroll = model.getScroll();
        // TODO move this to the NormalPanel
        Point pt = new Point(event.getX(), event.getY());
        if (event.getButton() == MouseEvent.BUTTON1 && normal.mouseToField(pt)) {
            // Lineare Koordinaten berechnen
            int idx = 0;
            int m1 = getField().getWidth();
            int m2 = m1 + 1;
            for (int j = 0; j < pt.getY() + scroll; j++) {
                if (j % 2 == 0)
                    idx += m1;
                else
                    idx += m2;
            }
            idx += pt.getX();

            // Feld setzen und Darstellung nachf�hren
            int j = idx / getField().getWidth();
            int i = idx % getField().getWidth();
            setPoint(i, j - scroll);
        }
    }

    private void infoAboutClick() {
        new AboutBox().setVisible(true);
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

    private void formKeyDown(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.VK_RIGHT) {
            rotateRight();
        } else if (event.getKeyCode() == KeyEvent.VK_LEFT) {
            rotateLeft();
        }
    }

    private boolean canTerminateApp() {
        if (modified) {
            int r = JOptionPane.showConfirmDialog(this,
                    Texts.text("Do you want to save your changes?", "Sollen die �nderungen gespeichert werden?"));
            if (r == JOptionPane.CANCEL_OPTION) {
                return false;
            }
            if (r == JOptionPane.OK_OPTION) fileSaveClick();
        }
        return true;
    }

    private void editCopyClick() {
        CopyForm copyform = new CopyForm();
        copyform.setVisible(true);
        if (copyform.isOK()) {
            model.snapshot(modified);
            // Aktuelle Daten in Buffer kopieren
            sel_buff.copyFrom(model.getField());
            // Daten vervielf�ltigen
            if (sel_i1 > sel_i2) {
                int temp = sel_i1;
                sel_i1 = sel_i2;
                sel_i2 = temp;
            }
            if (sel_j1 > sel_j2) {
                int temp = sel_j1;
                sel_j1 = sel_j2;
                sel_j2 = temp;
            }
            for (int i = sel_i1; i <= sel_i2; i++) {
                for (int j = sel_j1; j <= sel_j2; j++) {
                    byte c = sel_buff.get(i, j);
                    if (c == 0) continue;
                    int idx = getIndex(i, j);
                    // Diesen Punkt x-mal vervielf�ltigen
                    for (int k = 0; k < copyform.getCopies(); k++) {
                        idx += getCopyOffset(copyform);
                        if (getField().isValidIndex(idx)) getField().set(idx, c);
                    }
                }
            }
            model.setRepeatDirty();
            modified = true;
            updateTitle();
            invalidate();
        }
    }

    private int getCopyOffset(CopyForm form) {
        return form.getVertOffset() * getField().getWidth() + form.getHorzOffset();
    }

    private int getIndex(int i, int j) {
        return j * getField().getWidth() + i;
    }

    private void editInsertlineClick() {
        model.snapshot(modified);
        getField().insertLine();
        model.setRepeatDirty();
        modified = true;
        updateTitle();
        invalidate();
    }

    private void editDeletelineClick() {
        model.snapshot(modified);
        getField().deleteLine();
        model.setRepeatDirty();
        modified = true;
        updateTitle();
        invalidate();
    }

    void setAppTitle() {
        updateTitle();
    }

    private void updateTitle() {
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

    private void languageEnglishClick() {
        Texts.setLanguage(Language.EN, this);
        languageEnglish.setSelected(true);
        Settings settings = new Settings();
        settings.SetCategory("Environment");
        settings.SaveInt("Language", 0);
    }

    private void languageGermanClick() {
        Texts.setLanguage(Language.GE, this);
        languageGerman.setSelected(true);
        Settings settings = new Settings();
        settings.SetCategory("Environment");
        settings.SaveInt("Language", 1);
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
            m += mm2px(13, sx) + getField().getWidth() * gx + mm2px(7, sx);
        }

        if (normal.isVisible()) {
            normalleft = m;
            m += mm2px(7, sx) + (getField().getWidth() + 1) * gx;
        }

        if (simulation.isVisible()) {
            simulationleft = m;
            m += mm2px(7, sx) + (getField().getWidth() / 2 + 1) * gx;
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
        int maxj = Math.min(getField().getHeight(), (h - mm2py(10, sy)) / gy);
        for (int i = 0; i < getField().getWidth() + 1; i++) {
            g.drawLine(left + i * gx, h - (maxj) * gy, left + i * gx, h - 1);
        }
        for (int j = 0; j <= maxj; j++) {
            g.drawLine(left, h - 1 - j * gy, left + getField().getWidth() * gx, h - 1 - j * gy);
        }

        // Daten
        for (int i = 0; i < getField().getWidth(); i++) {
            for (int j = 0; j < maxj; j++) {
                byte c = getField().get(i, j);
                assert (c >= 0 && c <= 9);
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
        maxj = Math.min(getField().getHeight(), (h - mm2py(10, sy)) / gy);
        for (int i = 0; i < getField().getWidth() + 1; i++) {
            for (int jj = 0; jj < maxj; jj += 2) {
                g.drawLine(left + i * gx, h - (jj + 1) * gy, left + i * gx, h - jj * gy);
            }
        }
        for (int i = 0; i <= getField().getWidth() + 1; i++) {
            for (int jj = 1; jj < maxj; jj += 2) {
                g.drawLine(left + i * gx - gx / 2, h - (jj + 1) * gy, left + i * gx - gx / 2, h - jj * gy);
            }
        }
        g.drawLine(left, h - 1, left + getField().getWidth() * gx + 1, h - 1);
        for (int jj = 1; jj <= maxj; jj++) {
            g.drawLine(left - gx / 2, h - 1 - jj * gy, left + getField().getWidth() * gx + gx / 2 + 1, h - 1 - jj * gy);
        }

        // Daten
        int scroll = model.getScroll();
        for (int i = 0; i < getField().getWidth(); i++) {
            for (int jj = 0; jj < maxj; jj++) {
                byte c = getField().get(i, jj + scroll);
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
        maxj = Math.min(getField().getHeight(), (h - mm2py(10, sy)) / gy);
        int w = getField().getWidth() / 2;
        for (int j = 0; j < maxj; j += 2) {
            for (int i = 0; i < w + 1; i++) {
                g.drawLine(left + i * gx, h - (j + 1) * gy, left + i * gx, h - j * gy);
            }
            if (j > 0 || scroll > 0) {
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
        for (int i = 0; i < getField().getWidth(); i++) {
            for (int j = 0; j < maxj; j++) {
                byte c = getField().get(i, j + scroll);
                if (c == 0) continue;
                g.setColor(model.getColor(c));
                int ii = i;
                int jj = j;
                ii = correctCoordinatesX(ii, jj);
                jj = correctCoordinatesY(ii, jj);
                if (ii > w && ii != getField().getWidth()) continue;
                if (jj % 2 == 0) {
                    if (ii == w) continue;
                    g.fillRect(left + ii * gx + 1, h - (jj + 1) * gy, gx, gy);
                } else {
                    if (ii != getField().getWidth() && ii != w) {
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
        g.drawString(Texts.text("Pattern:", "Muster:"), x1, y);
        g.drawString(model.getFile().getName(), x2, y);
        y += dy;
        // Umfang
        g.drawString(Texts.text("Circumference:", "Umfang:"), x1, y);
        g.drawString(Integer.toString(getField().getWidth()), x2, y);
        y += dy;
        // Farbrapport
        g.drawString(Texts.text("Repeat of colors:", "Farbrapport:"), x1, y);
        g.drawString(Integer.toString(model.getColorRepeat()) + Texts.text(" beads", " Perlen"), x2, y);
        y += dy;
        int colorRepeat = model.getColorRepeat();
        // Faedelliste...
        if (colorRepeat > 0) {
            int page = 1;
            int column = 0;
            g.drawString(Texts.text("List of beads", "Fädelliste"), x1, y);
            y += dy;
            int ystart = y;
            byte col = getField().get(colorRepeat - 1);
            int count = 1;
            for (int i = colorRepeat - 2; i >= 0; i--) {
                if (getField().get(i) == col) {
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
                    col = getField().get(i);
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

    void reloadLanguage() {
        // Menüs
        // Menu Datei
        Texts.update(menuFile, Language.EN, "&File");
        Texts.update(menuFile, Language.GE, "&Datei");
        Texts.update(fileNew, Language.EN, "&New", "Creates a new pattern");
        Texts.update(fileNew, Language.GE, "&Neu", "Erstellt ein neues Muster");
        Texts.update(fileOpen, Language.EN, "&Open...", "Opens a pattern");
        Texts.update(fileOpen, Language.GE, "�&ffnen...", "�ffnet ein Muster");
        Texts.update(fileSave, Language.EN, "&Save", "Saves the pattern");
        Texts.update(fileSave, Language.GE, "&Speichern", "Speichert das Muster");
        Texts.update(fileSaveas, Language.EN, "Save &as...", "Saves the pattern to a new file");
        Texts.update(fileSaveas, Language.GE, "Speichern &unter...", "Speichert das Muster unter einem neuen Namen");
        Texts.update(filePrint, Language.EN, "&Print...", "Prints the pattern");
        Texts.update(filePrint, Language.GE, "&Drucken...", "Druckt das Muster");
        Texts.update(filePrintersetup, Language.EN, "Printer set&up...", "Configures the printer");
        Texts.update(filePrintersetup, Language.GE, "D&ruckereinstellung...", "Konfiguriert den Drucker");
        Texts.update(fileExit, Language.EN, "E&xit", "Exits the program");
        Texts.update(fileExit, Language.GE, "&Beenden", "Beendet das Programm");

        // Menu Bearbeiten
        Texts.update(menuEdit, Language.EN, "&Edit", "");
        Texts.update(menuEdit, Language.GE, "&Bearbeiten", "");
        Texts.update(editUndo, Language.EN, "&Undo", "Undoes the last action");
        Texts.update(editUndo, Language.GE, "&R�ckg�ngig", "Macht die letzte �nderung r�ckg�ngig");
        Texts.update(editRedo, Language.EN, "&Redo", "Redoes the last undone action");
        Texts.update(editRedo, Language.GE, "&Wiederholen", "F�hrt die letzte r�ckg�ngig gemachte �nderung durch");
        Texts.update(editCopy, Language.EN, "&Arrange", "");
        Texts.update(editCopy, Language.GE, "&Anordnen", "");
        Texts.update(editLine, Language.EN, "&Empty Line", "");
        Texts.update(editLine, Language.GE, "&Leerzeile", "");
        Texts.update(editInsertline, Language.EN, "&Insert", "");
        Texts.update(editInsertline, Language.GE, "&Einf�gen", "");
        Texts.update(editDeleteline, Language.EN, "&Delete", "");
        Texts.update(editDeleteline, Language.GE, "E&ntfernen", "");

        // Menu Werkzeug
        Texts.update(menuTool, Language.EN, "&Tool", "");
        Texts.update(menuTool, Language.GE, "&Werkzeug", "");
        Texts.update(toolPoint, Language.EN, "&Pencil", "");
        Texts.update(toolPoint, Language.GE, "&Eingabe", "");
        Texts.update(toolSelect, Language.EN, "&Select", "");
        Texts.update(toolSelect, Language.GE, "&Auswahl", "");
        Texts.update(toolFill, Language.EN, "&Fill", "");
        Texts.update(toolFill, Language.GE, "&F�llen", "");
        Texts.update(toolSniff, Language.EN, "P&ipette", "");
        Texts.update(toolSniff, Language.GE, "&Pipette", "");

        // Menu Ansicht
        Texts.update(menuView, Language.EN, "&View", "");
        Texts.update(menuView, Language.GE, "&Ansicht", "");
        Texts.update(viewDraft, Language.EN, "&Design", "");
        Texts.update(viewDraft, Language.GE, "&Entwurf", "");
        Texts.update(viewNormal, Language.EN, "&Corrected", "");
        Texts.update(viewNormal, Language.GE, "&Korrigiert", "");
        Texts.update(viewSimulation, Language.EN, "&Simulation", "");
        Texts.update(viewSimulation, Language.GE, "&Simulation", "");
        Texts.update(viewReport, Language.EN, "&Report", "");
        Texts.update(viewReport, Language.GE, "&Auswertung", "");
        Texts.update(viewZoomin, Language.EN, "&Zoom in", "Zoom in");
        Texts.update(viewZoomin, Language.GE, "&Vergr�ssern", "Vergr�ssert die Ansicht");
        Texts.update(viewZoomnormal, Language.EN, "&Normal", "Sets magnification to default value");
        Texts.update(viewZoomnormal, Language.GE, "&Normal", "Stellt die Standardgr�sse ein");
        Texts.update(viewZoomout, Language.EN, "Zoo&m out", "Zoom out");
        Texts.update(viewZoomout, Language.GE, "Ver&kleinern", "Verkleinert die Ansicht");
        Texts.update(viewLanguage, Language.EN, "&Language", "");
        Texts.update(viewLanguage, Language.GE, "&Sprache", "");
        Texts.update(languageEnglish, Language.EN, "&English", "");
        Texts.update(languageEnglish, Language.GE, "&Englisch", "");
        Texts.update(languageGerman, Language.EN, "&German", "");
        Texts.update(languageGerman, Language.GE, "&Deutsch", "");

        // Menu Muster
        Texts.update(menuPattern, Language.EN, "&Pattern", "");
        Texts.update(menuPattern, Language.GE, "&Muster", "");
        Texts.update(patternWidth, Language.EN, "&Width...", "");
        Texts.update(patternWidth, Language.GE, "&Breite...", "");

        // Menu ?
        Texts.update(menuInfo, Language.EN, "&?", "");
        Texts.update(menuInfo, Language.GE, "&?", "");
        Texts.update(infoAbout, Language.EN, "About &DB-BEAD...", "Displays information about DB-BEAD");
        Texts.update(infoAbout, Language.GE, "�ber &DB-BEAD...", "Zeigt Informationen �ber DB-BEAD an");

        // Toolbar
        Texts.update(sbNew, Language.EN, "", "New|Creates a new pattern");
        Texts.update(sbNew, Language.GE, "", "Neu|Erstellt ein neues Muster");
        Texts.update(sbOpen, Language.EN, "", "Open|Opens a pattern");
        Texts.update(sbOpen, Language.GE, "", "�ffnen|�ffnet ein Muster");
        Texts.update(sbSave, Language.EN, "", "Save|Saves the pattern");
        Texts.update(sbSave, Language.GE, "", "Speichern|Speichert das Muster");
        Texts.update(sbPrint, Language.EN, "", "Print|Prints the pattern");
        Texts.update(sbPrint, Language.GE, "", "Drucken|Druckt das Muster");
        Texts.update(sbUndo, Language.EN, "", "Undo|Undoes the last change");
        Texts.update(sbUndo, Language.GE, "", "R�ckg�ngig|Macht die letzte �nderung r�ckg�ngig");
        Texts.update(sbRedo, Language.EN, "", "Redo|Redoes the last undone change");
        Texts.update(sbRedo, Language.GE, "", "Wiederholen|Macht die letzte r�ckg�ngig gemachte �nderung");
        Texts.update(sbRotateleft, Language.EN, "", "Left|Rotates the pattern left");
        Texts.update(sbRotateleft, Language.GE, "", "Links|Rotiert das Muster nach links");
        Texts.update(sbRotateright, Language.EN, "", "Right|Rotates the pattern right");
        Texts.update(sbRotateright, Language.GE, "", "Rechts|Rotiert das Muster nach rechts");
        Texts.update(sbCopy, Language.EN, "", "Arrange");
        Texts.update(sbCopy, Language.GE, "", "Anordnen");
        Texts.update(sbColor0, Language.EN, "", "Color 0");
        Texts.update(sbColor0, Language.GE, "", "Farbe 0");
        Texts.update(sbColor1, Language.EN, "", "Color 1");
        Texts.update(sbColor1, Language.GE, "", "Farbe 1");
        Texts.update(sbColor2, Language.EN, "", "Color 2");
        Texts.update(sbColor2, Language.GE, "", "Farbe 2");
        Texts.update(sbColor3, Language.EN, "", "Color 3");
        Texts.update(sbColor3, Language.GE, "", "Farbe 3");
        Texts.update(sbColor4, Language.EN, "", "Color 4");
        Texts.update(sbColor4, Language.GE, "", "Farbe 4");
        Texts.update(sbColor5, Language.EN, "", "Color 5");
        Texts.update(sbColor5, Language.GE, "", "Farbe 5");
        Texts.update(sbColor6, Language.EN, "", "Color 6");
        Texts.update(sbColor6, Language.GE, "", "Farbe 6");
        Texts.update(sbColor7, Language.EN, "", "Color 7");
        Texts.update(sbColor7, Language.GE, "", "Farbe 7");
        Texts.update(sbColor8, Language.EN, "", "Color 8");
        Texts.update(sbColor8, Language.GE, "", "Farbe 8");
        Texts.update(sbColor9, Language.EN, "", "Color 9");
        Texts.update(sbColor9, Language.GE, "", "Farbe 9");
        Texts.update(sbToolSelect, Language.EN, "", "Select");
        Texts.update(sbToolSelect, Language.GE, "", "Auswahl");
        Texts.update(sbToolPoint, Language.EN, "", "Pencil");
        Texts.update(sbToolPoint, Language.GE, "", "Eingabe");
        Texts.update(sbToolFill, Language.EN, "", "Fill");
        Texts.update(sbToolFill, Language.GE, "", "F�llen");
        Texts.update(sbToolSniff, Language.EN, "", "Pipette");
        Texts.update(sbToolSniff, Language.GE, "", "Pipette");

        Texts.update(laDraft, Language.EN, "Draft");
        Texts.update(laDraft, Language.GE, "Entwurf");
        Texts.update(laNormal, Language.EN, "Corrected");
        Texts.update(laNormal, Language.GE, "Korrigiert");
        Texts.update(laSimulation, Language.EN, "Simulation");
        Texts.update(laSimulation, Language.GE, "Simulation");
        Texts.update(laReport, Language.EN, "Report");
        Texts.update(laReport, Language.GE, "Auswertung");

        invalidate();
    }

    private void addToMRU(File file) {
        if (file.getPath() == "") return;

        // Wenn Datei schon in MRU: Eintrag nach oben schieben
        for (int i = 0; i < 6; i++) {
            if (mru[i] == file.getPath()) {
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
        updateMRUMenu(1, fileMRU1, mru[0]);
        updateMRUMenu(2, fileMRU2, mru[1]);
        updateMRUMenu(3, fileMRU3, mru[2]);
        updateMRUMenu(4, fileMRU4, mru[3]);
        updateMRUMenu(5, fileMRU5, mru[4]);
        updateMRUMenu(6, fileMRU6, mru[5]);
        fileMRUSeparator.setVisible(fileMRU1.isVisible() || fileMRU2.isVisible() || fileMRU3.isVisible() || fileMRU4.isVisible()
                || fileMRU5.isVisible() || fileMRU6.isVisible());
    }

    private void updateMRUMenu(int index, JMenuItem menuitem, String filename) {
        menuitem.setVisible(filename != "");
        // xxx Eigene Dateien oder so?!
        // Bestimmen ob Datei im Daten-Verzeichnis ist, falls
        // nicht, ganzen Pfad anzeigen!
        String path = filename;
        String datapath = System.getProperty("user.dir");
        if (path == datapath) {
            menuitem.setText(new File(filename).getName());
        } else {
            menuitem.setText(filename);
        }
        menuitem.setAccelerator(KeyStroke.getKeyStroke(Integer.toString(index)));
    }

    private void fileMRU1Click() {
        loadFile(new File(mru[0]), true);
    }

    private void fileMRU2Click() {
        loadFile(new File(mru[1]), true);
    }

    private void fileMRU3Click() {
        loadFile(new File(mru[2]), true);
    }

    private void fileMRU4Click() {
        loadFile(new File(mru[3]), true);
    }

    private void fileMRU5Click() {
        loadFile(new File(mru[4]), true);
    }

    private void fileMRU6Click() {
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
