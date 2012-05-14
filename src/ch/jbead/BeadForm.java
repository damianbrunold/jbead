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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.AdjustmentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Locale;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JColorChooser;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
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

    BeadUndo  undo = new BeadUndo();
    BeadField field = new BeadField();
    Color coltable[] = new Color[10];
    byte color;
    int begin_i;
    int begin_j;
    int end_i;
    int end_j;
    int sel_i1, sel_i2, sel_j1, sel_j2;
    boolean selection;
    BeadField sel_buff = new BeadField();
    boolean dragging;
    int draftleft;
    int normalleft;
    int simulationleft;
    int grid;
    int zoomtable[] = new int[5];
    int zoomidx;
    int scroll;
    int shift;
    boolean saved;
    boolean modified;
    boolean rapportdirty;
    int rapport;
    int farbrapp;
    String mru[] = new String[6];
    
    JFileChooser opendialog = new JFileChooser();
    JFileChooser savedialog = new JFileChooser();

    ButtonGroup languageGroup = new ButtonGroup();
    JRadioButtonMenuItem LanguageEnglish = new JRadioButtonMenuItem("English");
    JRadioButtonMenuItem LanguageGerman = new JRadioButtonMenuItem("German");
    
    JToolBar coolbar = new JToolBar();
    JButton sbColor0 = new JButton();
    JButton sbColor1 = new JButton();
    JButton sbColor2 = new JButton();
    JButton sbColor3 = new JButton();
    JButton sbColor4 = new JButton();
    JButton sbColor5 = new JButton();
    JButton sbColor6 = new JButton();
    JButton sbColor7 = new JButton();
    JButton sbColor8 = new JButton();
    JButton sbColor9 = new JButton();
    
    JScrollBar scrollbar = new JScrollBar(JScrollBar.VERTICAL);
    
    DraftPanel draft = new DraftPanel(field, coltable, grid, scroll);
    NormalPanel normal = new NormalPanel(field, coltable, grid, scroll);
    SimulationPanel simulation = new SimulationPanel(field, coltable, grid, scroll, shift);
    ReportPanel report = new ReportPanel(field, coltable, farbrapp, opendialog.getSelectedFile().getPath());
    
    JLabel laDraft = new JLabel("draft");
    JLabel laNormal = new JLabel("normal");
    JLabel laSimulation = new JLabel("simulation");
    JLabel laReport = new JLabel("report");
    
    JMenu MenuFile = new JMenu("file");
    JMenuItem FileNew = new JMenuItem("new");
    JMenuItem FileOpen = new JMenuItem("open");
    JMenuItem FileSave = new JMenuItem("save");
    JMenuItem FileSaveas = new JMenuItem("save as");
    JMenuItem FilePrint = new JMenuItem("print");
    JMenuItem FilePrintersetup = new JMenuItem("printer setup");
    JMenuItem FileExit = new JMenuItem("exit");
      
    JMenu MenuEdit = new JMenu("edit");
    JMenuItem EditUndo = new JMenuItem("undo");
    JMenuItem EditRedo = new JMenuItem("redo");
    JMenuItem EditCopy = new JMenuItem("arrange");
    JMenuItem EditLine = new JMenuItem("empty line");
    JMenuItem EditInsertline = new JMenuItem("insert");
    JMenuItem EditDeleteline = new JMenuItem("delete");
      
    JMenuItem Werkzeug1 = new JMenuItem("tool");
    JMenuItem ToolPoint = new JMenuItem("pencil");
    JMenuItem ToolSelect = new JMenuItem("select");
    JMenuItem ToolFill = new JMenuItem("fill");
    JMenuItem ToolSniff = new JMenuItem("pipette");
    
    JMenu MenuView = new JMenu("view");
    JMenuItem ViewZoomin = new JMenuItem("zoom in");
    JMenuItem ViewZoomout = new JMenuItem("zoom out");
    JMenuItem ViewZoomnormal = new JMenuItem("normal");
    JMenu ViewLanguage = new JMenu("language");
    
    JCheckBoxMenuItem ViewDraft = new JCheckBoxMenuItem("draft");
    JCheckBoxMenuItem ViewNormal = new JCheckBoxMenuItem("normal");
    JCheckBoxMenuItem ViewSimulation = new JCheckBoxMenuItem("simulation");
    JCheckBoxMenuItem ViewReport = new JCheckBoxMenuItem("report");
    
    JMenuItem FileMRU1 = new JMenuItem();
    JMenuItem FileMRU2 = new JMenuItem();
    JMenuItem FileMRU3 = new JMenuItem();
    JMenuItem FileMRU4 = new JMenuItem();
    JMenuItem FileMRU5 = new JMenuItem();
    JMenuItem FileMRU6 = new JMenuItem();

    JPopupMenu.Separator FileMRUSeparator = new JPopupMenu.Separator();

    JMenu MenuPattern = new JMenu("pattern");
    JMenuItem PatternWidth = new JMenuItem("width");
    
    JMenu MenuInfo = new JMenu("?");
    JMenuItem InfoAbout = new JMenuItem("about jbead");

    JButton sbNew = new JButton("new");
    JButton sbOpen = new JButton("open");
    JButton sbSave = new JButton("save");
    JButton sbPrint = new JButton("print");
    JButton sbUndo = new JButton("undo");
    JButton sbRedo = new JButton("redo");
    JButton sbRotateleft = new JButton("left");
    JButton sbRotateright = new JButton("right");
    JButton sbCopy = new JButton("arrange");
    JToggleButton sbToolSelect = new JToggleButton("select"); 
    JToggleButton sbToolPoint = new JToggleButton("pencil"); 
    JToggleButton sbToolFill = new JToggleButton("fill"); 
    JToggleButton sbToolSniff = new JToggleButton("pipette"); 

        public BeadForm() {
            super("jbead");
            // TODO maybe need to set other default for opendialog and savedialog
            opendialog.setCurrentDirectory(new File(System.getProperty("user.home")));
            savedialog.setCurrentDirectory(new File(System.getProperty("user.home")));
            savedialog.setSelectedFile(new File(savedialog.getCurrentDirectory(), Language.STR("unnamed", "unbenannt")));
            saved = false;
            modified = false;
            rapportdirty = false;
            selection = false;
            UpdateTitle();
            field.Clear();
            field.SetWidth(15);
            color = 1;
            DefaultColors();
            SetGlyphColors();
            scroll = 0;
            zoomidx = 2;
            zoomtable[0] = 6;
            zoomtable[1] = 8;
            zoomtable[2] = 10;
            zoomtable[3] = 12;
            zoomtable[4] = 14;
            grid = zoomtable[zoomidx];
            LoadMRU();
            UpdateMRU();
            UpdateScrollbar();
//            Printer().Orientation = poLandscape;

            // init button group
            languageGroup.add(LanguageEnglish);
            languageGroup.add(LanguageGerman);
            
            // init color buttons
            // TODO handle sbColor0 with transparent color and x lines
            sbColor1.setIcon(new ColorIcon(coltable[1]));
            sbColor2.setIcon(new ColorIcon(coltable[2]));
            sbColor3.setIcon(new ColorIcon(coltable[3]));
            sbColor4.setIcon(new ColorIcon(coltable[4]));
            sbColor5.setIcon(new ColorIcon(coltable[5]));
            sbColor6.setIcon(new ColorIcon(coltable[6]));
            sbColor7.setIcon(new ColorIcon(coltable[7]));
            sbColor8.setIcon(new ColorIcon(coltable[8]));
            sbColor9.setIcon(new ColorIcon(coltable[9]));
            
            Settings settings;
            settings.SetCategory ("Environment");
            Language.LANG language;
            int lang = settings.LoadInt ("Language", -1);
            if (lang==-1) { // Windows-Spracheinstellung abfragen
                Locale locale = Locale.getDefault();
                if (locale.getLanguage().equals("de")) {
                    lang = 1;
                } else {
                    lang = 0;
                }
            }
            language = lang==0 ? Language.LANG.EN : Language.LANG.GE;
            if (Language.active_language == language) {
                Language.active_language = language==Language.LANG.EN ? Language.LANG.GE : Language.LANG.EN; // Update erzwingen
            }
            Language.SwitchLanguage (language);
            if (Language.active_language == Language.LANG.EN) {
                LanguageEnglish.setSelected(true);
            } else {
                LanguageGerman.setSelected(true);
            }
        }

        void DefaultColors()
        {
            coltable[0] = Color.LIGHT_GRAY; // was clBtnFace
            coltable[1] = new Color(128, 0, 0); // maroon
            coltable[2] = new Color(0, 0, 128); // navy
            coltable[3] = Color.GREEN;
            coltable[4] = Color.YELLOW;
            coltable[5] = Color.RED;
            coltable[6] = Color.BLUE;
            coltable[7] = new Color(128, 0, 128); // purple
            coltable[8] = Color.BLACK;
            coltable[9] = Color.WHITE;
        }
        
        void SetGlyphColors()
        {
            sbColor1.setIcon(new ColorIcon(coltable[1]));
            sbColor2.setIcon(new ColorIcon(coltable[2]));
            sbColor3.setIcon(new ColorIcon(coltable[3]));
            sbColor4.setIcon(new ColorIcon(coltable[4]));
            sbColor5.setIcon(new ColorIcon(coltable[5]));
            sbColor6.setIcon(new ColorIcon(coltable[6]));
            sbColor7.setIcon(new ColorIcon(coltable[7]));
            sbColor8.setIcon(new ColorIcon(coltable[8]));
            sbColor9.setIcon(new ColorIcon(coltable[9]));
        }

        void FormResize()
        {
            int cheight = getContentPane().getHeight() - coolbar.getHeight();
            int cwidth = getContentPane().getWidth() - scrollbar.getWidth();
            int top = coolbar.getHeight() + 6;

            int nr = 0;
            if (ViewDraft.isSelected()) nr++;
            if (ViewNormal.isSelected()) nr++;
            if (ViewSimulation.isSelected()) nr++;
            if (ViewReport.isSelected()) nr += 2;
            if (nr==0) {
                ViewDraft.setSelected(true);
                draft.setVisible(true);
                laDraft.setVisible(true);
                nr = 1;
            }

            int m = 6;

            if (ViewDraft.isSelected()) {
                draft.setBounds(m, top, field.Width()*grid + 35, cheight - 6 - laDraft.getHeight() - 3);
                laDraft.setLocation(m + (draft.getWidth()-laDraft.getWidth())/2, draft.getY() + draft.getHeight() + 2);
                m += draft.getWidth() + 12;
            }

            if (ViewNormal.isSelected()) {
                normal.setBounds(m, top, (field.Width()+1)*grid + 10, cheight - 6 - laNormal.getHeight() - 3);
                laNormal.setLocation(m + (normal.getWidth()-laNormal.getWidth())/2, normal.getY() + normal.getHeight() + 2);
                m += normal.getWidth() + 12;
            }

            if (ViewSimulation.isSelected()) {
                simulation.setBounds(m, top, (field.Width()+2)*grid/2 + 10, cheight - 6 - laSimulation.getHeight() - 3);
                laSimulation.setLocation(m + (simulation.getWidth()-laSimulation.getWidth())/2, simulation.getY() + simulation.getHeight() + 2);
                m += simulation.getWidth() + 12;
            }

            if (ViewReport.isSelected()) {
                report.setBounds(m, top, cwidth - m - 6, cheight - 6 - laReport.getHeight() - 3);
                laReport.setLocation(m + 5, report.getY() + report.getHeight() + 2);
            }

            scrollbar.setBounds(getContentPane().getWidth() - scrollbar.getWidth(), top, scrollbar.getWidth(), cheight - 6 - laDraft.getHeight() - 3);

            UpdateScrollbar();
        }

        void UpdateScrollbar()
        {
            int h = draft.getHeight()/grid;
            assert(h<field.Height());
            scrollbar.setMinimum(0);
            scrollbar.setMaximum(field.Height()-h);
            if (scrollbar.getMaximum()<0) scrollbar.setMaximum(0);
            scrollbar.setUnitIncrement(h);
            scrollbar.setBlockIncrement(h);
            scrollbar.setValue(scrollbar.getMaximum() - scrollbar.getBlockIncrement() - scroll);
        }

        int CorrectCoordinatesX(int _i, int _j)
        {
            int idx = _i + (_j+scroll)*field.Width();
            int m1 = field.Width();
            int m2 = field.Width()+1;
            int k = 0;
            int m = (k%2==0) ? m1 : m2;
            while (idx>=m) {
                idx -= m;
                k++;
                m = (k%2==0) ? m1 : m2;
            }
            _i = idx;
            _j = k-scroll;
            return _i;
        }

        int CorrectCoordinatesY(int _i, int _j)
        {
            int idx = _i + (_j+scroll)*field.Width();
            int m1 = field.Width();
            int m2 = field.Width()+1;
            int k = 0;
            int m = (k%2==0) ? m1 : m2;
            while (idx>=m) {
                idx -= m;
                k++;
                m = (k%2==0) ? m1 : m2;
            }
            _i = idx;
            _j = k-scroll;
            return _j;
        }

        void UpdateBead (int _i, int _j)
        {
            // use observer pattern to remove this explicit dependency
            draft.updateBead(_i, _j);
            normal.updateBead(_i, _j);
            simulation.updateBead(_i, _j);
        }

        void FileNewClick()
        {
            // ask whether to save modified document
            if (modified) {
                int answer = JOptionPane.showConfirmDialog(this, Language.STR("Do you want to save your changes?", "Sollen die Änderungen gespeichert werden?"));
                if (answer == JOptionPane.CANCEL_OPTION) return;
                if (answer == JOptionPane.YES_OPTION) {
                    FileSaveClick();
                }
            }
            
            // delete all
            undo.Clear();
            field.Clear();
            rapport = 0;
            farbrapp = 0;
            invalidate();
            color = 1;
            sbColor1.setSelected(true);
            DefaultColors();
            SetGlyphColors();
            scroll = 0;
            UpdateScrollbar();
            selection = false;
            sbToolPoint.setSelected(true);
            ToolPoint.setSelected(true);
            // TODO fix this by using explicit filename
            opendialog.setSelectedFile(new File("*.dbb"));
            savedialog.setSelectedFile(new File(Language.STR("unnamed", "unbenannt")));
            saved = false;
            modified = false;
            UpdateTitle();
        }

        void LoadFile(String _filename, boolean _addtomru)
        {
            // ask whether to save modified document
            if (modified) {
                int answer = JOptionPane.showConfirmDialog(this, Language.STR("Do you want to save your changes?", "Sollen die Änderungen gespeichert werden?"));
                if (answer == JOptionPane.CANCEL_OPTION) return;
                if (answer == JOptionPane.YES_OPTION) {
                    FileSaveClick();
                }
            }

            // Datei laden
            try {
                ObjectInputStream in = new ObjectInputStream(new FileInputStream(_filename));
                try {
                    byte[] id = new byte[13];
                    int read = in.read(id);
                    String strid = new String(id, "UTF-8");
                    if (!strid.equals("DB-BEAD/01:\r\n")) {
                        JOptionPane.showMessageDialog(this, Language.STR("The file is not a DB-BEAD pattern file. It cannot be loaded", "Die Datei ist keine DB-BEAD Musterdatei. Sie kann nicht geladen werden."));
                        return;
                    }
                    undo.Clear();
                    field.Clear();
                    rapport = 0;
                    farbrapp = 0;
                    field.Load (in);
                    // TODO handle colors in a backwards compatible way!
                    // f.Read (coltable, sizeof(coltable));
//                    f.Read (&color, sizeof(color));
//                    f.Read (&zoomidx, sizeof(zoomidx));
//                    f.Read (&shift, sizeof(shift));
//                    f.Read (&scroll, sizeof(scroll));
//                    boolean vis; f.Read (&vis, sizeof(vis)); ViewDraft.Checked = vis;
//                    f.Read (&vis, sizeof(vis)); ViewNormal.Checked = vis;
//                    f.Read (&vis, sizeof(vis)); ViewSimulation.Checked = vis;
                    switch (color) {
                        case 0: sbColor0.setSelected(true); break;
                        case 1: sbColor1.setSelected(true); break;
                        case 2: sbColor2.setSelected(true); break;
                        case 3: sbColor3.setSelected(true); break;
                        case 4: sbColor4.setSelected(true); break;
                        case 5: sbColor5.setSelected(true); break;
                        case 6: sbColor6.setSelected(true); break;
                        case 7: sbColor7.setSelected(true); break;
                        case 8: sbColor8.setSelected(true); break;
                        case 9: sbColor9.setSelected(true); break;
                        default: assert(false); break;
                    }
                    SetGlyphColors();
                    UpdateScrollbar();
                } finally {
                    in.close();
                }
            } catch (IOException e) {
                //xxx
                undo.Clear();
                field.Clear();
                rapport = 0;
                farbrapp = 0;
            }
            saved = true;
            modified = false;
            rapportdirty = true;
            savedialog.setSelectedFile(new File(_filename));
            UpdateTitle();
            FormResize();
            invalidate();
            if (_addtomru) AddToMRU (_filename);
        }

        void FileOpenClick()
        {
            if (opendialog.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                LoadFile(opendialog.getSelectedFile().getPath(), true);
            }
        }

        void FileSaveClick()
        {
            if (saved) {
                // Einfach abspeichern...
                try {
                    JBeadOutputStream out = new JBeadOutputStream(new FileOutputStream(savedialog.getSelectedFile()));
                    try {
                        out.write ("DB-BEAD/01:\r\n");
                        field.Save(out);
                        for (Color color : coltable) {
                            out.writeColor(color);
                        }
                        out.writeInt(color);
                        out.writeInt(zoomidx);
                        out.writeInt(shift);
                        out.writeInt(scroll);
                        out.writeBool(ViewDraft.isSelected());
                        out.writeBool(ViewNormal.isSelected());
                        out.writeBool(ViewSimulation.isSelected());
                        modified = false;
                        UpdateTitle();
                    } finally {
                        out.close();
                    }
                } catch (IOException e) {
                    //xxx
                }
            } else {
                FileSaveasClick();
            }
        }

        void FileSaveasClick()
        {
            if (savedialog.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                if (savedialog.getSelectedFile().exists()) {
                    String msg = Language.STR("The file ", "Die Datei ") + savedialog.getSelectedFile().getName() +
                                 Language.STR(" already exists. Do you want to overwrite it?", " existiert bereits. Soll sie überschrieben werden?");
                    if (JOptionPane.showConfirmDialog(this, msg, "Overwrite", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                        return;
                    }
                }
                saved = true;
                FileSaveClick();
                AddToMRU(savedialog.getSelectedFile().getPath());
            }
        }

        // TODO may possibly be removed
        void OnShowPrintDialog ()
        {
//            SetFocus (printdialog.Handle);
        }

        // TODO may possibly be removed
        void OnShowPrintersetupDialog ()
        {
//            SetFocus (printersetupdialog.Handle);
        }

        void FilePrintClick(ActionEvent event)
        {
            Object Sender = event.getSource();
            if (Sender!=sbPrint) {
//                TNotifyEvent old = printdialog.OnShow;
//                printdialog.OnShow = OnShowPrintDialog;
//                if (printdialog.Execute()) {
//                    Cursor = crHourGlass;
//                    PrintItAll();
//                    Cursor = crDefault;
//                }
//                printdialog.OnShow = old;
            } else {
//                Cursor = crHourGlass;
                PrintItAll();
//                Cursor = crDefault;
            }
        }

        void FilePrintersetupClick()
        {
//            TNotifyEvent old = printersetupdialog.OnShow;
//            printersetupdialog.OnShow = OnShowPrintersetupDialog;
//            printersetupdialog.Execute();
//            printersetupdialog.OnShow = old;
        }

        void FileExitClick()
        {
            if (modified) {
                 int r = JOptionPane.showConfirmDialog(this, Language.STR("Do you want to save your changes?", "Sollen die Änderungen gespeichert werden?"));
                 if (r==JOptionPane.CANCEL_OPTION) return;
                 if (r==JOptionPane.OK_OPTION) FileSaveClick();
            }
            // TODO maybe need to save settings?
            System.exit(0);
        }

        void PatternWidthClick()
        {
            int old = field.Width();
            PatternWidthForm form = new PatternWidthForm();
            form.setWidth(field.Width());
            form.FormShow();
            if (form.isOK()) {
                undo.Snapshot (field, modified);
                field.SetWidth(form.getWidth());
                FormResize();
                invalidate();
                if (!modified) {
                    modified = (old!=field.Width());
                }
                UpdateTitle();
                rapportdirty = true;
            }
        }

        int CalcLineCoordX (int _i1, int _j1, int _i2, int _j2)
        {
            int dx = Math.abs(_i2-_i1);
            int dy = Math.abs(_j2-_j1);
            if (2*dy<dx) {
                _j2 = _j1;
            } else if (2*dx<dy) {
                _i2 = _i1;
            } else {
                int d = Math.min(dx, dy);
                if (_i2-_i1 > d) _i2 = _i1 + d;
                else if (_i1-_i2 > d) _i2 = _i1 - d;
                if (_j2-_j1 > d) _j2 = _j1 + d;
                else if (_j1-_j2 > d) _j2 = _j1 - d;
            }
            return _i2;
        }

        int CalcLineCoordY (int _i1, int _j1, int _i2, int _j2)
        {
            int dx = Math.abs(_i2-_i1);
            int dy = Math.abs(_j2-_j1);
            if (2*dy<dx) {
                _j2 = _j1;
            } else if (2*dx<dy) {
                _i2 = _i1;
            } else {
                int d = Math.min(dx, dy);
                if (_i2-_i1 > d) _i2 = _i1 + d;
                else if (_i1-_i2 > d) _i2 = _i1 - d;
                if (_j2-_j1 > d) _j2 = _j1 + d;
                else if (_j1-_j2 > d) _j2 = _j1 - d;
            }
            return _j2;
        }

        void DraftLinePreview()
        {
            if (!sbToolPoint.Down) return;
            if (begin_i==end_i && begin_j==end_j) return;

            int ei = end_i;
            int ej = end_j;
            ei = CalcLineCoordX(begin_i, begin_j, ei, ej);
            ej = CalcLineCoordY(begin_i, begin_j, ei, ej);

            TPenMode oldmode = draft.Canvas.Pen.Mode;
            draft.Canvas.Pen.Mode = pmNot;
            draft.Canvas.MoveTo (draftleft + begin_i*grid+grid/2, draft.ClientHeight - begin_j*grid - grid/2);
            draft.Canvas.LineTo (draftleft + ei*grid+grid/2, draft.ClientHeight - ej*grid - grid/2);
            draft.Canvas.Pen.Mode = oldmode;
        }

        void DraftSelectPreview (boolean _draw, boolean _doit)
        {
            if (!sbToolSelect.isSelected() && !_doit) return;
            if (begin_i==end_i && begin_j==end_j) return;

            int i1 = Math.min(begin_i, end_i);
            int i2 = Math.max(begin_i, end_i);
            int j1 = Math.min(begin_j, end_j);
            int j2 = Math.max(begin_j, end_j);

            draft.selectPreview(_draw, new Point(i1, j1), new Point(i2, j2));
        }

        void DraftSelectDraw()
        {
            if (!selection) return;
            begin_i = sel_i1;
            begin_j = sel_j1;
            end_i = sel_i2;
            end_j = sel_j2;
            DraftSelectPreview (true, true);
        }

        void DraftSelectClear()
        {
            if (!selection) return;
            begin_i = sel_i1;
            begin_j = sel_j1;
            end_i = sel_i2;
            end_j = sel_j2;
            DraftSelectPreview (false, true);
            selection = false;
        }

        void draftMouseDown(MouseEvent event, int X, int Y)
        {
            if (dragging) return;
            Point pt = new Point(event.getX(), event.getY());
            if (event.getButton() == MouseEvent.BUTTON1 && draft.mouseToField(pt)) {
                DraftSelectClear();
                dragging = true;
                begin_i = pt.getX();
                begin_j = pt.getY();
                end_i = pt.getX();
                end_j = pt.getY();
                // Prepress
                if (sbToolPoint.isSelected()) {
                    draft.Canvas.Pen.Color = clBlack;
                    draft.Canvas.MoveTo (draftleft+begin_i*grid+1, draft.ClientHeight-begin_j*grid-2);
                    draft.Canvas.LineTo (draftleft+begin_i*grid+1, draft.ClientHeight-(begin_j+1)*grid);
                    draft.Canvas.LineTo (draftleft+(begin_i+1)*grid-1, draft.ClientHeight-(begin_j+1)*grid);
                    draft.Canvas.Pen.Color = clWhite;
                    draft.Canvas.MoveTo (draftleft+(begin_i+1)*grid-1, draft.ClientHeight-(begin_j+1)*grid+1);
                    draft.Canvas.LineTo (draftleft+(begin_i+1)*grid-1, draft.ClientHeight-begin_j*grid-2);
                    draft.Canvas.LineTo (draftleft+begin_i*grid, draft.ClientHeight-begin_j*grid-2);
                }
                DraftLinePreview();
                DraftSelectPreview(true);
            }
        }

        void draftMouseMove(MouseEvent event)
        {
            Point pt = new Point(event.getX(), event.getY());
            if (dragging && draft.mouseToField(pt)) {
                DraftSelectPreview(false);
                DraftLinePreview();
                end_i = pt.getX();
                end_j = pt.getY();
                DraftLinePreview();
                DraftSelectPreview(true);
            }
        }

        void draftMouseUp(MouseEvent event)
        {
            Point pt = new Point(event.getX(), event.getY());
            if (dragging && draft.mouseToField(pt)) {
                DraftLinePreview();
                end_i = pt.getX();
                end_j = pt.getY();
                dragging = false;

                if (sbToolPoint.isSelected()) {
                    if (begin_i==end_i && begin_j==end_j) {
                        SetPoint (begin_i, begin_j);
                    } else {
                        end_i = CalcLineCoordX(begin_i, begin_j, end_i, end_j);
                        end_j = CalcLineCoordY(begin_i, begin_j, end_i, end_j);
                        if (abs(end_i-begin_i)==abs(end_j-begin_j)) {
                            // 45 grad Linie
                            undo.Snapshot(field, modified);
                            int jj;
                            if (begin_i>end_i) {
                                int tmp = begin_i;
                                begin_i = end_i;
                                end_i = tmp;
                                tmp = begin_j;
                                begin_j = end_j;
                                end_j = tmp;
                            }
                            for (int i=begin_i; i<=end_i; i++) {
                                if (begin_j<end_j) jj = begin_j + (i-begin_i) ;
                                else jj = begin_j - (i-begin_i);
                                field.Set (i, jj+scroll, color);
                                UpdateBead (i, jj);
                            }
                            rapportdirty = true;
                            modified = true;
                            UpdateTitle();
                        }  else if (end_i==begin_i) {
                            // Senkrechte Linie
                            undo.Snapshot(field, modified);
                            int j1 = min(end_j, begin_j);
                            int j2 = max(end_j, begin_j);
                            for (int jj=j1; jj<=j2; jj++) {
                                field.Set (begin_i, jj+scroll, color);
                                UpdateBead (begin_i, jj);
                            }
                            modified = true;
                            rapportdirty = true;
                            UpdateTitle();
                        } else if (end_j==begin_j) {
                            // Waagrechte Linie ziehen
                            undo.Snapshot(field, modified);
                            int i1 = min(end_i, begin_i);
                            int i2 = max(end_i, begin_i);
                            for (int i=i1; i<=i2; i++) {
                                field.Set (i, begin_j+scroll, color);
                                UpdateBead (i, begin_j);
                            }
                            modified = true;
                            rapportdirty = true;
                            UpdateTitle();
                        }
                    }
                } else if (sbToolFill.Down) {
                    undo.Snapshot (field, modified);
                    FillLine (end_i, end_j);
                    modified = true; UpdateTitle();
                    rapportdirty = true;
                    report.Invalidate();
                } else if (sbToolSniff.Down) {
                    color = field.Get(begin_i, begin_j+scroll);
                    assert(color>=0 && color<10);
                    switch (color) {
                        case 0: sbColor0.setSelected(true); break;
                        case 1: sbColor1.setSelected(true); break;
                        case 2: sbColor2.setSelected(true); break;
                        case 3: sbColor3.setSelected(true); break;
                        case 4: sbColor4.setSelected(true); break;
                        case 5: sbColor5.setSelected(true); break;
                        case 6: sbColor6.setSelected(true); break;
                        case 7: sbColor7.setSelected(true); break;
                        case 8: sbColor8.setSelected(true); break;
                        case 9: sbColor9.setSelected(true); break;
                        default: assert(false); break;
                    }
                } else if (sbToolSelect.Down) {
                    DraftSelectPreview(false);
                    if (begin_i!=end_i || begin_j!=end_j) {
                        selection = true;
                        sel_i1 = begin_i;
                        sel_j1 = begin_j;
                        sel_i2 = end_i;
                        sel_j2 = end_j;
                        DraftSelectDraw();
                    }
                }
            }
        }

        void FillLine (int _i, int _j)
        {
            // F�llen
            //xxx experimentell nach links und rechts
            byte bk = field.Get (_i, _j+scroll);
            int i = _i;
            while (i>=0 && field.Get(i, _j+scroll)==bk) {
                field.Set (i, _j+scroll, color);
                // TODO make draft an observer of field!
                UpdateBead (i, _j);
                i--;
            }
            i = begin_i+1;
            while (i<field.Width() && field.Get(i, _j+scroll)==bk) {
                field.Set (i, _j+scroll, color);
                // TODO make draft an observer of field!
                UpdateBead (i, _j);
                i++;
            }
        }

        void SetPoint (int _i, int _j)
        {
            undo.Snapshot (field, modified);
            byte s = field.Get(_i, _j+scroll);
            if (s==color) {
                field.Set (_i, _j+scroll, (byte) 0);
            } else {
                field.Set(_i, _j+scroll, color);
            }
            UpdateBead (_i, _j);
            modified = true;
            rapportdirty = true;
            UpdateTitle();
        }

        void EditUndoClick()
        {
            undo.Undo(field);
            modified = undo.Modified(); 
            UpdateTitle();
            invalidate();
            rapportdirty = true;
        }

        void EditRedoClick()
        {
            undo.Redo(field);
            modified = undo.Modified(); 
            UpdateTitle();
            invalidate();
            rapportdirty = true;
        }

        void ViewZoominClick()
        {
            if (zoomidx<4) zoomidx++;
            grid = zoomtable[zoomidx];
            FormResize();
            invalidate();
            UpdateScrollbar();
        }

        void ViewZoomnormalClick()
        {
            if (zoomidx==1) return;
            zoomidx = 2;
            grid = zoomtable[zoomidx];
            FormResize();
            invalidate();
            UpdateScrollbar();
        }

        void ViewZoomoutClick()
        {
            if (zoomidx>0) zoomidx--;
            grid = zoomtable[zoomidx];
            FormResize();
            invalidate();
            UpdateScrollbar();
        }

        void ViewDraftClick()
        {
            ViewDraft.setSelected(!ViewDraft.isSelected());
            draft.setVisible(ViewDraft.isSelected());
            laDraft.setVisible(draft.isVisible());
            FormResize();
        }

        void ViewNormalClick()
        {
            ViewNormal.setSelected(!ViewNormal.isSelected());
            normal.setVisible(ViewNormal.isSelected());
            laNormal.setVisible(normal.isVisible());
            FormResize();
        }

        void ViewSimulationClick()
        {
            ViewSimulation.setSelected(!ViewSimulation.isSelected());
            simulation.setVisible(ViewSimulation.isSelected());
            laSimulation.setVisible(simulation.isVisible());
            FormResize();
        }

        void ViewReportClick()
        {
            ViewReport.setSelected(!ViewReport.isSelected());
            report.setVisible(ViewReport.isSelected());
            laReport.setVisible(report.isVisible());
            FormResize();
        }

        void FormKeyUp(KeyEvent event)
        {
            int Key = event.getKeyCode();
            if (Key==KeyEvent.VK_F5) invalidate();
            else if (Key==KeyEvent.VK_1 && event.isControlDown() && !event.isAltDown()) { sbToolPoint.setSelected(true); ToolPoint.setSelected(true); }
            else if (Key==KeyEvent.VK_2 && event.isControlDown() && !event.isAltDown()) { sbToolSelect.setSelected(true); ToolSelect.setSelected(true); }
            else if (Key==KeyEvent.VK_3 && event.isControlDown() && !event.isAltDown()) { sbToolFill.setSelected(true); ToolFill.setSelected(true); }
            else if (Key==KeyEvent.VK_4 && event.isControlDown() && !event.isAltDown()) { sbToolSniff.setSelected(true); ToolSniff.setSelected(true); }
            else if (event.getKeyChar()>='0' && event.getKeyChar()<='9') {
                color = (byte) (event.getKeyChar()-'0');
                switch (color) {
                    case 0: sbColor0.setSelected(true); break;
                    case 1: sbColor1.setSelected(true); break;
                    case 2: sbColor2.setSelected(true); break;
                    case 3: sbColor3.setSelected(true); break;
                    case 4: sbColor4.setSelected(true); break;
                    case 5: sbColor5.setSelected(true); break;
                    case 6: sbColor6.setSelected(true); break;
                    case 7: sbColor7.setSelected(true); break;
                    case 8: sbColor8.setSelected(true); break;
                    case 9: sbColor9.setSelected(true); break;
                    default: assert(false); break;
                }
            } else if (Key==KeyEvent.VK_SPACE) {
                sbToolPoint.setSelected(true);
                ToolPoint.setSelected(true);
            } else if (Key==KeyEvent.VK_ESCAPE) {
//                righttimer.Enabled = false;
//                lefttimer.Enabled = false;
            }
        }

        void RotateLeft()
        {
            shift = (shift-1+field.Width()) % field.Width();
            modified = true; 
            UpdateTitle();
            simulation.invalidate();
        }

        void RotateRight()
        {
            shift = (shift+1) % field.Width();
            modified = true; 
            UpdateTitle();
            simulation.invalidate();
        }

        // TODO split this for every color toolbar button
        void ColorClick(ActionEvent event)
        {
            Object Sender = event.getSource();
            if (Sender==sbColor0) color = 0;
            else if (Sender==sbColor1) color = 1;
            else if (Sender==sbColor2) color = 2;
            else if (Sender==sbColor3) color = 3;
            else if (Sender==sbColor4) color = 4;
            else if (Sender==sbColor5) color = 5;
            else if (Sender==sbColor6) color = 6;
            else if (Sender==sbColor7) color = 7;
            else if (Sender==sbColor8) color = 8;
            else if (Sender==sbColor9) color = 9;
        }

        // TODO split this for every color toolbar button
        void ColorDblClick(ActionEvent event)
        {
            Object Sender = event.getSource();
            int c;
            if (Sender==sbColor0) c = 0;
            else if (Sender==sbColor1) c = 1;
            else if (Sender==sbColor2) c = 2;
            else if (Sender==sbColor3) c = 3;
            else if (Sender==sbColor4) c = 4;
            else if (Sender==sbColor5) c = 5;
            else if (Sender==sbColor6) c = 6;
            else if (Sender==sbColor7) c = 7;
            else if (Sender==sbColor8) c = 8;
            else if (Sender==sbColor9) c = 9;
            if (c==0) return;
            Color color = JColorChooser.showDialog(this, "choose color", coltable[c]);
            if (color == null) return;
            undo.Snapshot (field, modified);
            coltable[c] = color;
            // TODO propagate change to all dependants (or better use observer pattern)
            modified = true; 
            UpdateTitle();
            invalidate();
            SetGlyphColors();
        }

        void coolbarResize()
        {
            FormResize();
        }

        // TODO handle out parameter
        void scrollbarScroll(AdjustmentEvent event)
        {
            int oldscroll = scroll;
//            if (ScrollPos > scrollbar.Max - scrollbar.PageSize) ScrollPos = scrollbar.Max - scrollbar.PageSize;
            scroll = scrollbar.getMaximum() - scrollbar.getBlockIncrement() - scrollbar.getValue();
            if (oldscroll!=scroll) invalidate();
        }

        // TODO handle out parameter
        void IdleHandler (boolean& Done)
        {
            // Men�- und Toolbar enablen/disablen
            EditCopy.Enabled = selection;
            sbCopy.Enabled = selection;
            EditUndo.Enabled = undo.CanUndo();
            EditRedo.Enabled = undo.CanRedo();
            sbUndo.Enabled = undo.CanUndo();
            sbRedo.Enabled = undo.CanRedo();

            // FIXME is this whole rapport stuff needed? all drawing code was commented out and thus removed...
            
            // Rapport berechnen und zeichnen
            if (rapportdirty) {

                // Musterrapport neu berechnen
                int last = -1;
                for (int j=0; j<field.Height(); j++) {
                    for (int i=0; i<field.Width(); i++) {
                        int c = field.Get(i, j);
                        if (c>0) {
                            last = j;
                            break;
                        }
                    }
                }
                if (last==-1) {
                    rapport = 0;
                    farbrapp = 0;
                    rapportdirty = false;
                    report.Invalidate();
                    return;
                }
                rapport = last+1;
                for (int j=1; j<=last; j++) {
                    if (Equal(0,j)) {
                        boolean ok = true;
                        for (int k=j+1; k<=last; k++) {
                            if (!Equal((k-j)%j, k)) {
                                ok = false;
                                break;
                            }
                        }
                        if (ok) {
                            rapport = j;
                            break;
                        }
                    }
                }

                // Farbrapport neu berechnen
                farbrapp = rapport*field.Width();
                for (int i=1; i<=rapport*field.Width(); i++) {
                    if (field.Get(i)==field.Get(0)) {
                        boolean ok = true;
                        for (int k=i+1; k<=rapport*field.Width(); k++) {
                            if (field.Get((k-i)%i)!=field.Get(k)) {
                                ok = false;
                                break;
                            }
                        }
                        if (ok) {
                            farbrapp = i;
                            break;
                        }
                    }
                }

                report.Invalidate();
                rapportdirty = false;
            }

            // Vorsorgliches Undo
            undo.PreSnapshot (field, modified);
        }

        boolean Equal (int _i, int _j)
        {
            for (int k=0; k<field.Width(); k++) {
                if (field.Get(k,_i)!=field.Get(k,_j))
                    return false;
            }
            return true;
        }

        void FormCreate()
        {
            Application.OnIdle = IdleHandler; // FIXME remove/replace with a timer?
        }

        void ToolPointClick()
        {
            ToolPoint.setSelected(true);
            sbToolPoint.setSelected(true);
            DraftSelectClear();
        }

        void ToolSelectClick()
        {
            ToolSelect.setSelected(true);
            sbToolSelect.setSelected(true);
        }

        void ToolFillClick()
        {
            ToolFill.setSelected(true);
            sbToolFill.setSelected(true);
            DraftSelectClear();
        }

        void ToolSniffClick()
        {
            ToolSniff.setSelected(true);
            sbToolSniff.setSelected(true);
            DraftSelectClear();
        }

        void sbToolPointClick()
        {
            ToolPoint.setSelected(true);
            DraftSelectClear();
        }

        void sbToolFillClick()
        {
            ToolFill.setSelected(true);
            DraftSelectClear();
        }

        void sbToolSniffClick()
        {
            ToolSniff.setSelected(true);
            DraftSelectClear();
        }

        void sbToolSelectClick()
        {
            ToolSelect.setSelected(true);
        }

        void normalMouseUp(MouseEvent event)
        {
            // TODO move this to the NormalPanel
            Point pt = new Point(event.getX(), event.getY());
            if (event.getButton() == MouseEvent.BUTTON1 && normal.mouseToField(pt)) {
                // Lineare Koordinaten berechnen
                int idx = 0;
                int m1 = field.Width();
                int m2 = m1 + 1;
                for (int j=0; j<pt.getY()+scroll; j++) {
                    if (j%2==0) idx += m1;
                    else idx += m2;
                }
                idx += pt.getX();

                // Feld setzen und Darstellung nachf�hren
                int j = idx / field.Width();
                int i = idx % field.Width();
                SetPoint (i, j-scroll);
            }
        }

        void InfoAboutClick()
        {
            new AboutBox().setVisible(true);
        }

        void lefttimerTimer()
        {
            RotateLeft();
//            Application.ProcessMessages(); // FIXME maybe just remove it?
        }

        void righttimerTimer()
        {
            RotateRight();
//            Application.ProcessMessages(); // FIXME maybe just remove it?
        }

        void sbRotaterightMouseDown(MouseEvent event)
        {
            RotateRight();
//            Application.ProcessMessages();
//            righttimer.Enabled = true;
        }

        void sbRotaterightMouseUp(MouseEvent event)
        {
//            righttimer.Enabled = false;
        }

        void sbRotateleftMouseDown(MouseEvent event)
        {
            RotateLeft();
//            Application.ProcessMessages();
//            lefttimer.Enabled = true;
        }

        void sbRotateleftMouseUp(MouseEvent event)
        {
//            lefttimer.Enabled = false;
        }

        void FormKeyDown(KeyEvent event)
        {
            if (event.getKeyCode() == KeyEvent.VK_RIGHT) {
                RotateRight();
            } else if (event.getKeyCode() == KeyEvent.VK_LEFT) {
                RotateLeft();
            }
        }

        // TODO handle out parameter
        void FormCloseQuery(boolean &CanClose)
        {
            if (modified) {
                 int r = MessageDlg (Language.STR("Do you want to save your changes?", "Sollen die �nderungen gespeichert werden?"), mtConfirmation,
                                    TMsgDlgButtons() << mbYes << mbNo << mbCancel, 0);
                 if (r==mrCancel) { CanClose = false; return; }
                 if (r==mrYes) FileSaveClick(Sender);
            }
            CanClose = true;
        }

        void EditCopyClick()
        {
            CopyForm copyform = new CopyForm();
            copyform.setVisible(true);
            if (copyform.isOK()) {
                undo.Snapshot (field, modified);
                // Aktuelle Daten in Buffer kopieren
                sel_buff.CopyFrom (field);
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
                for (int i=sel_i1; i<=sel_i2; i++) {
                    for (int j=sel_j1; j<=sel_j2; j++) {
                        byte c = sel_buff.Get(i, j);
                        if (c==0) continue;
                        int idx = GetIndex(i, j);
                        // Diesen Punkt x-mal vervielf�ltigen
                        for (int k=0; k<copyform.getCopies(); k++) {
                            idx += GetCopyOffset(copyform);
                            if (field.ValidIndex(idx)) field.Set (idx, c);
                        }
                    }
                }
                rapportdirty = true;
                modified = true; UpdateTitle();
                invalidate();
            }
        }

        int GetCopyOffset(CopyForm form)
        {
            return form.getVertOffset() * field.Width() + form.getHorzOffset();
        }

        int GetIndex (int i, int j)
        {
            return j*field.Width() + i;
        }

        void EditInsertlineClick()
        {
            undo.Snapshot(field, modified);
            field.InsertLine();
            rapportdirty = true;
            modified = true; 
            UpdateTitle();
            invalidate();
        }

        void EditDeletelineClick()
        {
            undo.Snapshot(field, modified);
            field.DeleteLine();
            rapportdirty = true;
            modified = true; 
            UpdateTitle();
            invalidate();
        }

        void setAppTitle()
        {
            UpdateTitle();
        }

        void UpdateTitle()
        {
            String c = "jbead"; //APP_TITLE;
            c += " - ";
            if (saved) {
                c += savedialog.getSelectedFile().getName();
            } else {
                c += "unnamed"; //DATEI_UNBENANNT;
            }
            if (modified) {
                c += "*";
            }
            setTitle(c);
        }

        void LanguageEnglishClick()
        {
            Language.SwitchLanguage(Language.LANG.EN);
            LanguageEnglish.setSelected(true);
            Settings settings = new Settings();
            settings.SetCategory ("Environment");
            settings.SaveInt("Language", 0);
        }

        void LanguageGermanClick()
        {
            Language.SwitchLanguage(Language.LANG.GE);
            LanguageGerman.setSelected(true);
            Settings settings = new Settings();
            settings.SetCategory ("Environment");
            settings.SaveInt("Language", 1);
        }

        int MM2PRx(int x, int sx) { return x*10*sx/254; }
        int MM2PRy(int y, int sy) { return y*10*sy/254; }

        void PrintItAll()
        {
//            Printer().BeginDoc();
            String title = "jbead"; // APP_TITLE;
            title += " - " + savedialog.getSelectedFile().getName();
//            Printer().Title = title;
            TCanvas canvas = Printer().Canvas;

            int sx = GetDeviceCaps(Printer().Handle, LOGPIXELSX);
            int sy = GetDeviceCaps(Printer().Handle, LOGPIXELSY);

            int gx = (15+zoomidx*5)*sx/254;
            int gy = (15+zoomidx*5)*sy/254;

            int draftleft, normalleft, simulationleft, reportleft;
            int reportcols;

            int m = MM2PRx(10, sx);
            if (draft.isVisible()) {
                draftleft = m;
                m += MM2PRx(13, sx) + field.Width()*gx + MM2PRx(7, sx);
            }

            if (normal.isVisible()) {
                normalleft = m;
                m += MM2PRx(7, sx) + (field.Width()+1)*gx;
            }

            if (simulation.isVisible()) {
                simulationleft = m;
                m += MM2PRx(7, sx) + (field.Width()/2+1)*gx;
            }

            if (report.isVisible()) {
                reportleft = m;
                reportcols = (Printer().PageWidth - m - 10) / (MM2PRx(5, sx) + MM2PRx(8, sx));
            }

            int h = Printer().PageHeight - MM2PRy(10, sy);

            ////////////////////////////////////////
            //
            //   Draft
            //
            ////////////////////////////////////////

            // Grid
            canvas.Pen.Color = clBlack;
            int left = draftleft+MM2PRx(13);
            if (left<0) left=0;
            int maxj = min(field.Height(), (h-MM2PRy(10, sy))/gy);
            for (int i=0; i<field.Width()+1; i++) {
                canvas.MoveTo(left+i*gx, h-(maxj)*gy);
                canvas.LineTo(left+i*gx, h-1);
            }
            for (int j=0; j<=maxj; j++) {
                canvas.MoveTo(left, h-1-j*gy);
                canvas.LineTo(left+field.Width()*gx, h-1-j*gy);
            }

            // Daten
            for (int i=0; i<field.Width(); i++)
                for (int j=0; j<maxj; j++) {
                    char c = field.Get (i, j);
                    assert(c>=0 && c<=9);
                    if (c>0) {
                        canvas.Brush.Color = coltable[c];
                        canvas.Pen.Color = canvas.Brush.Color;
                        canvas.Rectangle (left+i*gx+1, h-(j+1)*gy,
                                            left+(i+1)*gx, h-1-j*gy);
                    }
                }
            canvas.Brush.Color = clWhite;

            // Zehnermarkierungen
            canvas.Pen.Color = clBlack;
            for (int j=0; j<maxj; j++) {
                if ((j%10)==0) {
                    canvas.MoveTo (draftleft, h - j*gy - 1);
                    canvas.LineTo (left-MM2PRx(3, sx), h - j*gy - 1);
                    canvas.TextOutA (draftleft, h - j*gy + MM2PRy(1, sy), IntToStr(j));
                }
            }

            ////////////////////////////////////////
            //
            //   Korrigiert (normal)
            //
            ////////////////////////////////////////

            // Grid
            canvas.Pen.Color = clBlack;
            left = normalleft+gx/2;
            if (left<0) left=gx/2;
            maxj = min(field.Height(), (h-MM2PRy(10, sy))/gy);
            for (int i=0; i<field.Width()+1; i++) {
                for (int jj=0; jj<maxj; jj+=2) {
                    canvas.MoveTo(left+i*gx, h-(jj+1)*gy);
                    canvas.LineTo(left+i*gx, h-jj*gy);
                }
            }
            for (int i=0; i<=field.Width()+1; i++) {
                for (int jj=1; jj<maxj; jj+=2) {
                    canvas.MoveTo(left+i*gx-gx/2, h-(jj+1)*gy);
                    canvas.LineTo(left+i*gx-gx/2, h-jj*gy);
                }
            }
            canvas.MoveTo(left, h-1);
            canvas.LineTo(left+field.Width()*gx+1, h-1);
            for (int jj=1; jj<=maxj; jj++) {
                canvas.MoveTo(left-gx/2, h-1-jj*gy);
                canvas.LineTo(left+field.Width()*gx+gx/2+1, h-1-jj*gy);
            }

            // Daten
            for (int i=0; i<field.Width(); i++)
                for (int jj=0; jj<maxj; jj++) {
                    char c = field.Get (i, jj+scroll);
                    assert(c>=0 && c<=9);
                    if (c==0) continue;
                    canvas.Brush.Color = coltable[c];
                    canvas.Pen.Color = canvas.Brush.Color;
                    int ii = i;
                    int j1 = jj;
                    ii = CorrectCoordinatesX(ii, j1);
                    j1 = CorrectCoordinatesY(ii, j1);
                    if (j1%2==0) {
                        canvas.Rectangle (left+ii*gx+1,
                                                   h-(j1+1)*gy,
                                                   left+(ii+1)*gx,
                                                   h-1-j1*gy);
                    } else {
                        canvas.Rectangle (left-gx/2+ii*gx+1,
                                                   h-(j1+1)*gy,
                                                   left-gx/2+(ii+1)*gx,
                                                   h-1-j1*gy);
                    }
                }
            canvas.Brush.Color = clWhite;


            ////////////////////////////////////////
            //
            //   Simulation
            //
            ////////////////////////////////////////

            // Grid
            canvas.Pen.Color = clBlack;
            left = simulationleft+gx/2;
            if (left<0) left=gx/2;
            maxj = min(field.Height(), (h-MM2PRy(10, sy))/gy);
            int w = field.Width()/2;
            for (int j=0; j<maxj; j+=2) {
                for (int i=0; i<w+1; i++) {
                    canvas.MoveTo(left+i*gx, h-(j+1)*gy);
                    canvas.LineTo(left+i*gx, h-j*gy);
                }
                if (j>0 || scroll>0) {
                    canvas.MoveTo (left-gx/2, h-(j+1)*gy);
                    canvas.LineTo (left-gx/2, h-j*gy);
                }
            }
            for (int j=1; j<maxj; j+=2) {
                for (int i=0; i<w+1; i++) {
                    canvas.MoveTo(left+i*gx-gx/2, h-(j+1)*gy);
                    canvas.LineTo(left+i*gx-gx/2, h-j*gy);
                }
                canvas.MoveTo(left+w*gx, h-(j+1)*gy);
                canvas.LineTo(left+w*gx, h-j*gy);
            }
            canvas.MoveTo(left, h-1);
            canvas.LineTo(left+w*gx+1, h-1);
            for (int j=1; j<=maxj; j++) {
                canvas.MoveTo(left-gx/2, h-1-j*gy);
                canvas.LineTo(left+w*gx+1, h-1-j*gy);
            }

            // Daten
            for (int i=0; i<field.Width(); i++)
                for (int j=0; j<maxj; j++) {
                    char c = field.Get (i, j+scroll);
                    assert(c>=0 && c<=9);
                    if (c==0) continue;
                    canvas.Brush.Color = coltable[c];
                    canvas.Pen.Color = canvas.Brush.Color;
                    int ii = i;
                    int jj = j;
                    ii = CorrectCoordinatesX(ii, jj);
                    jj = CorrectCoordinatesY(ii, jj);
                    if (ii>w && ii!=field.Width()) continue;
                    if (jj%2==0) {
                        if (ii==w) continue;
                        canvas.Rectangle (left+ii*gx+1,
                                                   h-(jj+1)*gy,
                                                   left+(ii+1)*gx,
                                                   h-1-jj*gy);
                    } else {
                        if (ii!=field.Width() && ii!=w) {
                            canvas.Rectangle (left-gx/2+ii*gx+1,
                                                       h-(jj+1)*gy,
                                                       left-gx/2+(ii+1)*gx,
                                                       h-1-jj*gy);
                        } else if (ii==w) {
                            canvas.Rectangle (left-gx/2+ii*gx+1,
                                                       h-(jj+1)*gy,
                                                       left-gx/2+ii*gx+gx/2,
                                                       h-1-jj*gy);
                        } else {
                            canvas.Rectangle (left-gx/2+1,
                                                       h-(jj+2)*gy,
                                                       left,
                                                       h-1-(jj+1)*gy-1);
                        }
                    }
                }
            canvas.Brush.Color = clWhite;



            ////////////////////////////////////////
            //
            //   Auswertung
            //
            ////////////////////////////////////////

            int x1 = reportleft;
            int x2 = reportleft + MM2PRx(30, sx);
            int y = MM2PRy(10, sy);
            int dy = MM2PRy(5, sy);
            int dx = MM2PRx(5, sx);

            // Mustername
            canvas.Pen.Color = clBlack;
            canvas.TextOut (x1, y, Language.STR("Pattern:", "Muster:"));
            canvas.TextOut (x2, y, savedialog.getSelectedFile().getName());
            y += dy;
            // Umfang
            canvas.TextOut (x1, y, Language.STR("Circumference:", "Umfang:"));
            canvas.TextOut (x2, y, IntToStr(field.Width()));
            y += dy;
            // Farbrapport
            canvas.TextOut (x1, y, Language.STR("Repeat of colors:", "Farbrapport:"));
            canvas.TextOut (x2, y, IntToStr(farbrapp) + Language.STR(" beads", " Perlen"));
            y += dy;
            // F�delliste...
            if (farbrapp>0) {
                int page = 1;
                int column = 0;
                canvas.TextOut (x1, y, Language.STR("List of beads", "F�delliste"));
                y += dy;
                int ystart = y;
                char col = field.Get(farbrapp-1);
                int  count = 1;
                for (int i=farbrapp-2; i>=0; i--) {
                    if (field.Get(i)==col) {
                        count++;
                    } else {
                        if (col!=0) {
                            canvas.Brush.Color = coltable[col];
                            canvas.Pen.Color = clWhite;
                        } else {
                            canvas.Brush.Color = clWhite;
                            canvas.Pen.Color = clBlack;
                        }
                        canvas.Rectangle (x1, y, x1+dx-MM2PRx(1, sx), y+dy-MM2PRy(1, sy));
                        canvas.Pen.Color = clBlack;
                        canvas.Brush.Color = clWhite;
                        canvas.TextOut (x1+dx+3, y, IntToStr(count));
                        y += dy;
                        col = field.Get(i);
                        count = 1;
                    }
                    if (y>=Printer().PageHeight-MM2PRy(10, sy)) {
                        x1 += dx + MM2PRx(8, sx);
                        y = ystart;
                        column++;
                        if (column>=reportcols) { // neue Seite und weiter...
                            Printer().NewPage();
                            x1 = draftleft;
                            x2 = draftleft + MM2PRx(30, sx);
                            y = MM2PRy(10, sy);
                            reportcols = (Printer().PageWidth - draftleft - 10) / (MM2PRx(5, sx) + MM2PRx(8, sx));
                            column = 0;
                            page++;
                            canvas.Pen.Color = clBlack;
                            canvas.TextOut (x1, y, String(Language.STR("Pattern ", "Muster "))+savedialog.getSelectedFile().getName() + " - " + Language.STR("page ", "Seite ") + IntToStr(page));
                            y += dy;
                            ystart = y;
                        }
                    }
                }
                if (y<Printer().PageHeight-MM2PRy(10, sy)) {
                    if (col!=0) {
                        canvas.Brush.Color = coltable[col];
                        canvas.Pen.Color = clWhite;
                    } else {
                        canvas.Brush.Color = clWhite;
                        canvas.Pen.Color = clBlack;
                    }
                    canvas.Rectangle (x1, y, x1+dx-MM2PRx(1, sx), y+dy-MM2PRy(1, sy));
                    canvas.Pen.Color = clBlack;
                    canvas.Brush.Color = clWhite;
                    canvas.TextOut (x1+dx+3, y, IntToStr(count));
                }
            }

//            Printer().EndDoc();
        }

        void reloadLanguage()
        {
            // Men�s
              // Menu Datei
              Language.C_H(MenuFile, Language.LANG.EN, "&File");
              Language.C_H(MenuFile, Language.LANG.GE, "&Datei");
              Language.C_H(FileNew, Language.LANG.EN, "&New", "Creates a new pattern");
              Language.C_H(FileNew, Language.LANG.GE, "&Neu", "Erstellt ein neues Muster");
              Language.C_H(FileOpen, Language.LANG.EN, "&Open...", "Opens a pattern");
              Language.C_H(FileOpen, Language.LANG.GE, "�&ffnen...", "�ffnet ein Muster");
              Language.C_H(FileSave, Language.LANG.EN, "&Save", "Saves the pattern");
              Language.C_H(FileSave, Language.LANG.GE, "&Speichern", "Speichert das Muster");
              Language.C_H(FileSaveas, Language.LANG.EN, "Save &as...", "Saves the pattern to a new file");
              Language.C_H(FileSaveas, Language.LANG.GE, "Speichern &unter...", "Speichert das Muster unter einem neuen Namen");
              Language.C_H(FilePrint, Language.LANG.EN, "&Print...", "Prints the pattern");
              Language.C_H(FilePrint, Language.LANG.GE, "&Drucken...", "Druckt das Muster");
              Language.C_H(FilePrintersetup, Language.LANG.EN, "Printer set&up...", "Configures the printer");
              Language.C_H(FilePrintersetup, Language.LANG.GE, "D&ruckereinstellung...", "Konfiguriert den Drucker");
              Language.C_H(FileExit, Language.LANG.EN, "E&xit", "Exits the program");
              Language.C_H(FileExit, Language.LANG.GE, "&Beenden", "Beendet das Programm");


              // Menu Bearbeiten
              Language.C_H(MenuEdit, Language.LANG.EN, "&Edit", "");
              Language.C_H(MenuEdit, Language.LANG.GE, "&Bearbeiten", "");
              Language.C_H(EditUndo, Language.LANG.EN, "&Undo", "Undoes the last action");
              Language.C_H(EditUndo, Language.LANG.GE, "&R�ckg�ngig", "Macht die letzte �nderung r�ckg�ngig");
              Language.C_H(EditRedo, Language.LANG.EN, "&Redo", "Redoes the last undone action");
              Language.C_H(EditRedo, Language.LANG.GE, "&Wiederholen", "F�hrt die letzte r�ckg�ngig gemachte �nderung durch");
              Language.C_H(EditCopy, Language.LANG.EN, "&Arrange", "");
              Language.C_H(EditCopy, Language.LANG.GE, "&Anordnen", "");
              Language.C_H(EditLine, Language.LANG.EN, "&Empty Line", "");
              Language.C_H(EditLine, Language.LANG.GE, "&Leerzeile", "");
              Language.C_H(EditInsertline, Language.LANG.EN, "&Insert", "");
              Language.C_H(EditInsertline, Language.LANG.GE, "&Einf�gen", "");
              Language.C_H(EditDeleteline, Language.LANG.EN, "&Delete", "");
              Language.C_H(EditDeleteline, Language.LANG.GE, "E&ntfernen", "");

              // Menu Werkzeug
              Language.C_H(Werkzeug1, Language.LANG.EN, "&Tool", "");
              Language.C_H(Werkzeug1, Language.LANG.GE, "&Werkzeug", "");
              Language.C_H(ToolPoint, Language.LANG.EN, "&Pencil", "");
              Language.C_H(ToolPoint, Language.LANG.GE, "&Eingabe", "");
              Language.C_H(ToolSelect, Language.LANG.EN, "&Select", "");
              Language.C_H(ToolSelect, Language.LANG.GE, "&Auswahl", "");
              Language.C_H(ToolFill, Language.LANG.EN, "&Fill", "");
              Language.C_H(ToolFill, Language.LANG.GE, "&F�llen", "");
              Language.C_H(ToolSniff, Language.LANG.EN, "P&ipette", "");
              Language.C_H(ToolSniff, Language.LANG.GE, "&Pipette", "");


              // Menu Ansicht
              Language.C_H(MenuView, Language.LANG.EN, "&View", "");
              Language.C_H(MenuView, Language.LANG.GE, "&Ansicht", "");
              Language.C_H(ViewDraft, Language.LANG.EN, "&Design", "");
              Language.C_H(ViewDraft, Language.LANG.GE, "&Entwurf", "");
              Language.C_H(ViewNormal, Language.LANG.EN, "&Corrected", "");
              Language.C_H(ViewNormal, Language.LANG.GE, "&Korrigiert", "");
              Language.C_H(ViewSimulation, Language.LANG.EN, "&Simulation", "");
              Language.C_H(ViewSimulation, Language.LANG.GE, "&Simulation", "");
              Language.C_H(ViewReport, Language.LANG.EN, "&Report", "");
              Language.C_H(ViewReport, Language.LANG.GE, "&Auswertung", "");
              Language.C_H(ViewZoomin, Language.LANG.EN, "&Zoom in", "Zoom in");
              Language.C_H(ViewZoomin, Language.LANG.GE, "&Vergr�ssern", "Vergr�ssert die Ansicht");
              Language.C_H(ViewZoomnormal, Language.LANG.EN, "&Normal", "Sets magnification to default value");
              Language.C_H(ViewZoomnormal, Language.LANG.GE, "&Normal", "Stellt die Standardgr�sse ein");
              Language.C_H(ViewZoomout, Language.LANG.EN, "Zoo&m out", "Zoom out");
              Language.C_H(ViewZoomout, Language.LANG.GE, "Ver&kleinern", "Verkleinert die Ansicht");
              Language.C_H(ViewLanguage, Language.LANG.EN, "&Language", "");
              Language.C_H(ViewLanguage, Language.LANG.GE, "&Sprache", "");
              Language.C_H(LanguageEnglish, Language.LANG.EN, "&English", "");
              Language.C_H(LanguageEnglish, Language.LANG.GE, "&Englisch", "");
              Language.C_H(LanguageGerman, Language.LANG.EN, "&German", "");
              Language.C_H(LanguageGerman, Language.LANG.GE, "&Deutsch", "");


              // Menu Muster
              Language.C_H(MenuPattern, Language.LANG.EN, "&Pattern", "");
              Language.C_H(MenuPattern, Language.LANG.GE, "&Muster", "");
              Language.C_H(PatternWidth, Language.LANG.EN, "&Width...", "");
              Language.C_H(PatternWidth, Language.LANG.GE, "&Breite...", "");

              // Menu ?
              Language.C_H(MenuInfo, Language.LANG.EN, "&?", "");
              Language.C_H(MenuInfo, Language.LANG.GE, "&?", "");
              Language.C_H(InfoAbout, Language.LANG.EN, "About &DB-BEAD...", "Displays information about DB-BEAD");
              Language.C_H(InfoAbout, Language.LANG.GE, "�ber &DB-BEAD...", "Zeigt Informationen �ber DB-BEAD an");


            // Toolbar
            Language.C_H(sbNew, Language.LANG.EN, "", "New|Creates a new pattern");
            Language.C_H(sbNew, Language.LANG.GE, "", "Neu|Erstellt ein neues Muster");
            Language.C_H(sbOpen, Language.LANG.EN, "", "Open|Opens a pattern");
            Language.C_H(sbOpen, Language.LANG.GE, "", "�ffnen|�ffnet ein Muster");
            Language.C_H(sbSave, Language.LANG.EN, "", "Save|Saves the pattern");
            Language.C_H(sbSave, Language.LANG.GE, "", "Speichern|Speichert das Muster");
            Language.C_H(sbPrint, Language.LANG.EN, "", "Print|Prints the pattern");
            Language.C_H(sbPrint, Language.LANG.GE, "", "Drucken|Druckt das Muster");
            Language.C_H(sbUndo, Language.LANG.EN, "", "Undo|Undoes the last change");
            Language.C_H(sbUndo, Language.LANG.GE, "", "R�ckg�ngig|Macht die letzte �nderung r�ckg�ngig");
            Language.C_H(sbRedo, Language.LANG.EN, "", "Redo|Redoes the last undone change");
            Language.C_H(sbRedo, Language.LANG.GE, "", "Wiederholen|Macht die letzte r�ckg�ngig gemachte �nderung");
            Language.C_H(sbRotateleft, Language.LANG.EN, "", "Left|Rotates the pattern left");
            Language.C_H(sbRotateleft, Language.LANG.GE, "", "Links|Rotiert das Muster nach links");
            Language.C_H(sbRotateright, Language.LANG.EN, "", "Right|Rotates the pattern right");
            Language.C_H(sbRotateright, Language.LANG.GE, "", "Rechts|Rotiert das Muster nach rechts");
            Language.C_H(sbCopy, Language.LANG.EN, "", "Arrange");
            Language.C_H(sbCopy, Language.LANG.GE, "", "Anordnen");
            Language.C_H(sbColor0, Language.LANG.EN, "", "Color 0");
            Language.C_H(sbColor0, Language.LANG.GE, "", "Farbe 0");
            Language.C_H(sbColor1, Language.LANG.EN, "", "Color 1");
            Language.C_H(sbColor1, Language.LANG.GE, "", "Farbe 1");
            Language.C_H(sbColor2, Language.LANG.EN, "", "Color 2");
            Language.C_H(sbColor2, Language.LANG.GE, "", "Farbe 2");
            Language.C_H(sbColor3, Language.LANG.EN, "", "Color 3");
            Language.C_H(sbColor3, Language.LANG.GE, "", "Farbe 3");
            Language.C_H(sbColor4, Language.LANG.EN, "", "Color 4");
            Language.C_H(sbColor4, Language.LANG.GE, "", "Farbe 4");
            Language.C_H(sbColor5, Language.LANG.EN, "", "Color 5");
            Language.C_H(sbColor5, Language.LANG.GE, "", "Farbe 5");
            Language.C_H(sbColor6, Language.LANG.EN, "", "Color 6");
            Language.C_H(sbColor6, Language.LANG.GE, "", "Farbe 6");
            Language.C_H(sbColor7, Language.LANG.EN, "", "Color 7");
            Language.C_H(sbColor7, Language.LANG.GE, "", "Farbe 7");
            Language.C_H(sbColor8, Language.LANG.EN, "", "Color 8");
            Language.C_H(sbColor8, Language.LANG.GE, "", "Farbe 8");
            Language.C_H(sbColor9, Language.LANG.EN, "", "Color 9");
            Language.C_H(sbColor9, Language.LANG.GE, "", "Farbe 9");
            Language.C_H(sbToolSelect, Language.LANG.EN, "", "Select");
            Language.C_H(sbToolSelect, Language.LANG.GE, "", "Auswahl");
            Language.C_H(sbToolPoint, Language.LANG.EN, "", "Pencil");
            Language.C_H(sbToolPoint, Language.LANG.GE, "", "Eingabe");
            Language.C_H(sbToolFill, Language.LANG.EN, "", "Fill");
            Language.C_H(sbToolFill, Language.LANG.GE, "", "F�llen");
            Language.C_H(sbToolSniff, Language.LANG.EN, "", "Pipette");
            Language.C_H(sbToolSniff, Language.LANG.GE, "", "Pipette");

            Language.C_H(laDraft, Language.LANG.EN, "Draft");
            Language.C_H(laDraft, Language.LANG.GE, "Entwurf");
            Language.C_H(laNormal, Language.LANG.EN, "Corrected");
            Language.C_H(laNormal, Language.LANG.GE, "Korrigiert");
            Language.C_H(laSimulation, Language.LANG.EN, "Simulation");
            Language.C_H(laSimulation, Language.LANG.GE, "Simulation");
            Language.C_H(laReport, Language.LANG.EN, "Report");
            Language.C_H(laReport, Language.LANG.GE, "Auswertung");

            invalidate();
        }

        void AddToMRU (String _filename)
        {
            if (_filename=="") return;
            int i;

            // Wenn Datei schon in MRU: Eintrag nach oben schieben
            for (i=0; i<6; i++) {
                if (mru[i]==_filename) {
                    if (i>0) {
                        String temp = mru[i];
                        for (int j=i; j>0; j--)
                            mru[j] = mru[j-1];
                        mru[0] = temp;
                    }
                    UpdateMRU();
                    SaveMRU();
                    return;
                }
            }

            // Ansonsten wird alles um einen Platz nach unten
            // geschoben und der Dateiname im ersten Eintrag
            // vermerkt.
            for (i=5; i>0; i--)
                mru[i] = mru[i-1];
            mru[0] = _filename;

            UpdateMRU();
            SaveMRU();
        }

        void UpdateMRU()
        {
            UpdateMRUMenu (1, FileMRU1, mru[0]);
            UpdateMRUMenu (2, FileMRU2, mru[1]);
            UpdateMRUMenu (3,FileMRU3, mru[2]);
            UpdateMRUMenu (4, FileMRU4, mru[3]);
            UpdateMRUMenu (5, FileMRU5, mru[4]);
            UpdateMRUMenu (6, FileMRU6, mru[5]);
            FileMRUSeparator.setVisible(FileMRU1.isVisible() || FileMRU2.isVisible() ||
                                        FileMRU3.isVisible() || FileMRU4.isVisible() ||
                                        FileMRU5.isVisible() || FileMRU6.isVisible());
        }

        void UpdateMRUMenu (int _item, JMenuItem _menuitem, String _filename)
        {
            _menuitem.setVisible(_filename != "");

            //xxxy Eigene Dateien oder so?!
            // Bestimmen ob Datei im Daten-Verzeichnis ist, falls
            // nicht, ganzen Pfad anzeigen!
            String path = _filename;
            String datapath = System.getProperty("user.dir");
            if (path==datapath) {
                _menuitem.setText(new File(_filename).getName());
            } else {
                _menuitem.setText(_filename);
            }
            _menuitem.setAccelerator(KeyStroke.getKeyStroke(Integer.toString(_item)));
        }

        void FileMRU1Click()
        {
            // MRU 1
            opendialog.setSelectedFile(new File(mru[0]));
            LoadFile (mru[0], true);
        }

        void FileMRU2Click()
        {
            // MRU 2
            opendialog.setSelectedFile(new File(mru[1]));
            LoadFile (mru[1], true);
        }

        void FileMRU3Click()
        {
            // MRU 3
            opendialog.setSelectedFile(new File(mru[2]));
            LoadFile (mru[2], true);
        }

        void FileMRU4Click()
        {
            // MRU 4
            opendialog.setSelectedFile(new File(mru[3]));
            LoadFile (mru[3], true);
        }

        void FileMRU5Click()
        {
            // MRU 5
            opendialog.setSelectedFile(new File(mru[4]));
            LoadFile (mru[4], true);
        }

        void FileMRU6Click()
        {
            // MRU 6
            opendialog.setSelectedFile(new File(mru[5]));
            LoadFile (mru[5], true);
        }

        void SaveMRU()
        {
            Settings settings = new Settings();
            settings.SetCategory("mru");
            settings.SaveString("mru0", mru[0]);
            settings.SaveString("mru1", mru[1]);
            settings.SaveString("mru2", mru[2]);
            settings.SaveString("mru3", mru[3]);
            settings.SaveString("mru4", mru[4]);
            settings.SaveString("mru5", mru[5]);
        }

        void LoadMRU()
        {
            Settings settings = new Settings();
            settings.SetCategory("mru");
            mru[0] = settings.LoadString("mru0");
            mru[1] = settings.LoadString("mru1");
            mru[2] = settings.LoadString("mru2");
            mru[3] = settings.LoadString("mru3");
            mru[4] = settings.LoadString("mru4");
            mru[5] = settings.LoadString("mru5");
        }
}
