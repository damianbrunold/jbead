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
import java.io.File;
import java.util.Locale;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollBar;
import javax.swing.JToolBar;

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
    
    JComponent draft = new DraftPanel(field, coltable, grid, scroll);
    JComponent normal = new NormalPanel(field, coltable, grid, scroll);
    JComponent simulation = new SimulationPanel(field, coltable, grid, scroll, shift);
    JComponent report = new JPanel();
    
    JLabel laDraft = new JLabel("draft");
    JLabel laNormal = new JLabel("normal");
    JLabel laSimulation = new JLabel("simulation");
    JLabel laReport = new JLabel("report");
    
    JCheckBoxMenuItem ViewDraft = new JCheckBoxMenuItem("draft");
    JCheckBoxMenuItem ViewNormal = new JCheckBoxMenuItem("normal");
    JCheckBoxMenuItem ViewSimulation = new JCheckBoxMenuItem("simulation");
    JCheckBoxMenuItem ViewReport = new JCheckBoxMenuItem("report");
    

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

        void CorrectCoordinates (int& _i, int& _j)
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
        }

        void UpdateBead (int _i, int _j)
        {
            char c = field.Get (_i, _j+scroll);
            assert(c>=0 && c<=9);

            int ii = _i;
            int jj = _j;
            CorrectCoordinates (_i, _j);

            // Normal
            if (normal.Visible) {
                normal.Canvas.Brush.Color = coltable[c];
                normal.Canvas.Pen.Color = normal.Canvas.Brush.Color;
                int left = normalleft;
                if (scroll%2==0) {
                    if (_j%2==0) {
                        normal.Canvas.Rectangle (left+_i*grid+1,
                                                   normal.ClientHeight-(_j+1)*grid,
                                                   left+(_i+1)*grid,
                                                   normal.ClientHeight-1-_j*grid);
                    } else {
                        normal.Canvas.Rectangle (left-grid/2+_i*grid+1,
                                                   normal.ClientHeight-(_j+1)*grid,
                                                   left-grid/2+(_i+1)*grid,
                                                   normal.ClientHeight-1-_j*grid);
                    }
                } else {
                    if (_j%2==1) {
                        normal.Canvas.Rectangle (left+_i*grid+1,
                                                   normal.ClientHeight-(_j+1)*grid,
                                                   left+(_i+1)*grid,
                                                   normal.ClientHeight-1-_j*grid);
                    } else {
                        normal.Canvas.Rectangle (left-grid/2+_i*grid+1,
                                                   normal.ClientHeight-(_j+1)*grid,
                                                   left-grid/2+(_i+1)*grid,
                                                   normal.ClientHeight-1-_j*grid);
                    }
                }
            }

            // Simulation
            int idx = ii+field.Width()*jj + shift;
            _i = idx % field.Width();
            _j = idx / field.Width();
            CorrectCoordinates (_i, _j);
            if (simulation.Visible) {
                simulation.Canvas.Brush.Color = coltable[c];
                simulation.Canvas.Pen.Color = simulation.Canvas.Brush.Color;
                int left = simulationleft;
                int w = field.Width()/2;
                if (_i>w && _i!=field.Width()) return;
                if (scroll%2==0) {
                    if (_j%2==0) {
                        if (_i==w) return;
                        simulation.Canvas.Rectangle (left+_i*grid+1,
                                                   simulation.ClientHeight-(_j+1)*grid,
                                                   left+(_i+1)*grid,
                                                   simulation.ClientHeight-1-_j*grid);
                    } else {
                        if (_i!=field.Width() && _i!=w) {
                            simulation.Canvas.Rectangle (left-grid/2+_i*grid+1,
                                                       simulation.ClientHeight-(_j+1)*grid,
                                                       left-grid/2+(_i+1)*grid,
                                                       simulation.ClientHeight-1-_j*grid);
                        } else if (_i==w) {
                            simulation.Canvas.Rectangle (left-grid/2+_i*grid+1,
                                                       simulation.ClientHeight-(_j+1)*grid,
                                                       left-grid/2+_i*grid+grid/2,
                                                       simulation.ClientHeight-1-_j*grid);
                        } else {
                            simulation.Canvas.Rectangle (left-grid/2+1,
                                                       simulation.ClientHeight-(_j+2)*grid,
                                                       left,
                                                       simulation.ClientHeight-1-(_j+1)*grid);
                        }
                    }
                } else {
                    if (_j%2==1) {
                        if (_i==w) return;
                        simulation.Canvas.Rectangle (left+_i*grid+1,
                                                   simulation.ClientHeight-(_j+1)*grid,
                                                   left+(_i+1)*grid,
                                                   simulation.ClientHeight-1-_j*grid);
                    } else {
                        if (_i!=field.Width() && _i!=w) {
                            simulation.Canvas.Rectangle (left-grid/2+_i*grid+1,
                                                       simulation.ClientHeight-(_j+1)*grid,
                                                       left-grid/2+(_i+1)*grid,
                                                       simulation.ClientHeight-1-_j*grid);
                        } else if (_i==w) {
                            simulation.Canvas.Rectangle (left-grid/2+_i*grid+1,
                                                       simulation.ClientHeight-(_j+1)*grid,
                                                       left-grid/2+_i*grid+grid/2,
                                                       simulation.ClientHeight-1-_j*grid);
                        } else {
                            simulation.Canvas.Rectangle (left-grid/2+1,
                                                       simulation.ClientHeight-(_j+2)*grid,
                                                       left,
                                                       simulation.ClientHeight-1-(_j+1)*grid);
                        }
                    }
                }
            }
        }

        void FileNewClick()
        {
            // Fragen ob speichern
            if (modified && MessageDlg (Language.STR("Do you want to save your changes?", "Sollen die �nderungen gespeichert werden?"), mtConfirmation,
                                    TMsgDlgButtons() << mbYes << mbNo, 0)==mrYes)
            {
                FileSaveClick (Sender);
            }
            // Alles l�schen
            undo.Clear();
            field.Clear();
            rapport = 0;
            farbrapp = 0;
            Invalidate();
            color = 1;
            sbColor1.Down = true;
            DefaultColors();
            SetGlyphColors();
            scroll = 0;
            UpdateScrollbar();
            selection = false;
            sbToolPoint.Down = true;
            ToolPoint.Checked = true;
            opendialog.FileName = "*.dbb";
            savedialog.FileName = Language.STR("unnamed", "unbenannt");
            saved = false;
            modified = false;
            UpdateTitle();
        }

        void LoadFile(String _filename, boolean _addtomru)
        {
            // Fragen ob speichern
            if (modified && MessageDlg (Language.STR("Do you want to save your changes?", "Sollen die �nderungen gespeichert werden?"), mtConfirmation,
                                    TMsgDlgButtons() << mbYes << mbNo, 0)==mrYes)
            {
                FileSaveClick(this);
            }
            // Datei laden
            try {
                TFileStream* f = new TFileStream(_filename, fmOpenRead|fmShareDenyWrite);
                    f.Write ("DB-BEAD/01:\r\n", 13);
                char id[14];
                id[13] = '\0';
                f.Read (id, 13);
                if (String(id)!="DB-BEAD/01:\r\n") {
                    ShowMessage (Language.STR("The file is not a DB-BEAD pattern file. It cannot be loaded", "Die Datei ist keine DB-BEAD Musterdatei. Sie kann nicht geladen werden."));
                    delete f;
                    return;
                }
                undo.Clear();
                field.Clear();
                rapport = 0;
                farbrapp = 0;
                field.Load (f);
                f.Read (coltable, sizeof(coltable));
                f.Read (&color, sizeof(color));
                f.Read (&zoomidx, sizeof(zoomidx));
                f.Read (&shift, sizeof(shift));
                f.Read (&scroll, sizeof(scroll));
                boolean vis; f.Read (&vis, sizeof(vis)); ViewDraft.Checked = vis;
                f.Read (&vis, sizeof(vis)); ViewNormal.Checked = vis;
                f.Read (&vis, sizeof(vis)); ViewSimulation.Checked = vis;
                switch (color) {
                    case 0: sbColor0.Down = true; break;
                    case 1: sbColor1.Down = true; break;
                    case 2: sbColor2.Down = true; break;
                    case 3: sbColor3.Down = true; break;
                    case 4: sbColor4.Down = true; break;
                    case 5: sbColor5.Down = true; break;
                    case 6: sbColor6.Down = true; break;
                    case 7: sbColor7.Down = true; break;
                    case 8: sbColor8.Down = true; break;
                    case 9: sbColor9.Down = true; break;
                    default: assert(false); break;
                }
                SetGlyphColors();
                UpdateScrollbar();
                delete f;
            } catch (...) {
                //xxx
                undo.Clear();
                field.Clear();
                rapport = 0;
                farbrapp = 0;
            }
            saved = true;
            modified = false;
            rapportdirty = true;
            savedialog.FileName = _filename;
            UpdateTitle();
            FormResize(this);
            Invalidate();
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
                    TFileStream* f = new TFileStream(savedialog.FileName, fmCreate|fmShareExclusive);
                    f.Write ("DB-BEAD/01:\r\n", 13);
                    field.Save (f);
                    f.Write (coltable, sizeof(coltable)); 
                    assert(sizeof(coltable)==sizeof(TColor)*10);
                    f.Write (&color, sizeof(color));
                    f.Write (&zoomidx, sizeof(zoomidx));
                    f.Write (&shift, sizeof(shift));
                    f.Write (&scroll, sizeof(scroll));
                    boolean vis = ViewDraft.Checked; 
                    f.Write (&vis, sizeof(vis));
                    vis = ViewNormal.Checked; 
                    f.Write (&vis, sizeof(vis));
                    vis = ViewSimulation.Checked; 
                    f.Write (&vis, sizeof(vis));
                    delete f;
                    modified = false;
                    UpdateTitle();
                } catch (...) {
                    //xxx
                }
            } else {
                FileSaveasClick (Sender);
            }
        }

        void FileSaveasClick()
        {
            if (savedialog.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                if (savedialog.getSelectedFile().exists()) {
                    String msg = Language.STR("The file ", "Die Datei ") + savedialog.getSelectedFile().getName() +
                                 Language.STR(" already exists. Do you want to overwrite it?", " existiert bereits. Soll sie �berschrieben werden?");
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
            SetFocus (printdialog.Handle);
        }

        // TODO may possibly be removed
        void OnShowPrintersetupDialog ()
        {
            SetFocus (printersetupdialog.Handle);
        }

        void FilePrintClick()
        {
            if (Sender!=sbPrint) {
                TNotifyEvent old = printdialog.OnShow;
                printdialog.OnShow = OnShowPrintDialog;
                if (printdialog.Execute()) {
                    Cursor = crHourGlass;
                    PrintItAll();
                    Cursor = crDefault;
                }
                printdialog.OnShow = old;
            } else {
                Cursor = crHourGlass;
                PrintItAll();
                Cursor = crDefault;
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
                 int r = MessageDlg (Language.STR("Do you want to save your changes?", "Sollen die �nderungen gespeichert werden?"), mtConfirmation,
                                    TMsgDlgButtons() << mbYes << mbNo << mbCancel, 0);
                 if (r==mrCancel) return;
                 if (r==mrYes) FileSaveClick(Sender);
            }
            Application.Terminate();
        }

        void PatternWidthClick()
        {
            int old = field.Width();
            PatternWidthForm.upWidth.Position = field.Width();
            if (PatternWidthForm.ShowModal()==mrOk) {
                undo.Snapshot (field, modified);
                field.SetWidth(PatternWidthForm.upWidth.Position);
                FormResize(Sender);
                invalidate();
                if (!modified) modified = (old!=field.Width());
                UpdateTitle();
                rapportdirty = true;
            }
        }

        // TODO handle out parameters
        boolean draftMouseToField (int& _i, int& _j)
        {
            int i, jj;
            if (_i<draftleft || _i>draftleft+field.Width()*grid) return false;
            i = (_i-draftleft)/grid;
            if (i>=field.Width()) return false;
            jj = (draft.ClientHeight-_j)/grid;
            _i = i;
            _j = jj;
            return true;
        }

        // TODO handle out parameters
        void CalcLineCoord (int _i1, int _j1, int& _i2, int& _j2)
        {
            int dx = abs(_i2-_i1);
            int dy = abs(_j2-_j1);
            if (2*dy<dx) {
                _j2 = _j1;
            } else if (2*dx<dy) {
                _i2 = _i1;
            } else {
                int d = min(dx, dy);
                if (_i2-_i1 > d) _i2 = _i1 + d;
                else if (_i1-_i2 > d) _i2 = _i1 - d;
                if (_j2-_j1 > d) _j2 = _j1 + d;
                else if (_j1-_j2 > d) _j2 = _j1 - d;
            }
        }

        void DraftLinePreview()
        {
            if (!sbToolPoint.Down) return;
            if (begin_i==end_i && begin_j==end_j) return;

            int ei = end_i;
            int ej = end_j;
            CalcLineCoord (begin_i, begin_j, ei, ej);

            TPenMode oldmode = draft.Canvas.Pen.Mode;
            draft.Canvas.Pen.Mode = pmNot;
            draft.Canvas.MoveTo (draftleft + begin_i*grid+grid/2, draft.ClientHeight - begin_j*grid - grid/2);
            draft.Canvas.LineTo (draftleft + ei*grid+grid/2, draft.ClientHeight - ej*grid - grid/2);
            draft.Canvas.Pen.Mode = oldmode;
        }

        void DraftSelectPreview (boolean _draw, boolean _doit)
        {
            if (!sbToolSelect.Down && !_doit) return;
            if (begin_i==end_i && begin_j==end_j) return;

            int i1 = min(begin_i, end_i);
            int i2 = max(begin_i, end_i);
            int j1 = min(begin_j, end_j);
            int j2 = max(begin_j, end_j);

            TColor oldcolor = draft.Canvas.Pen.Color;
            draft.Canvas.Pen.Color = _draw ? clBlack : clDkGray;
            draft.Canvas.MoveTo (draftleft + i1*grid, draft.ClientHeight - j1*grid - 1);
            draft.Canvas.LineTo (draftleft + i1*grid, draft.ClientHeight - (j2+1)*grid - 1);
            draft.Canvas.LineTo (draftleft + (i2+1)*grid, draft.ClientHeight - (j2+1)*grid - 1);
            draft.Canvas.LineTo (draftleft + (i2+1)*grid, draft.ClientHeight - j1*grid - 1);
            draft.Canvas.LineTo (draftleft + i1*grid, draft.ClientHeight - j1*grid - 1);
            draft.Canvas.Pen.Color = oldcolor;
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

        void draftMouseDown(TMouseButton Button, TShiftState Shift, int X, int Y)
        {
            if (dragging) return;
            if (Button==mbLeft && draftMouseToField(X, Y)) {
                DraftSelectClear();
                dragging = true;
                begin_i = X;
                begin_j = Y;
                end_i = X;
                end_j = Y;
                // Prepress
                if (sbToolPoint.Down) {
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

        void draftMouseMove(TShiftState Shift, int X, int Y)
        {
            if (dragging && draftMouseToField(X, Y)) {
                DraftSelectPreview(false);
                DraftLinePreview();
                end_i = X;
                end_j = Y;
                DraftLinePreview();
                DraftSelectPreview(true);
            }
        }

        void draftMouseUp(TMouseButton Button, TShiftState Shift, int X, int Y)
        {
            if (dragging && draftMouseToField (X, Y)) {
                DraftLinePreview();
                end_i = X;
                end_j = Y;
                dragging = false;

                if (sbToolPoint.Down) {
                    if (begin_i==end_i && begin_j==end_j) {
                        SetPoint (begin_i, begin_j);
                    } else {
                        CalcLineCoord (begin_i, begin_j, end_i, end_j);
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
                                draft.Canvas.Brush.Color = coltable[color];
                                draft.Canvas.Pen.Color = draft.Canvas.Brush.Color;
                                draft.Canvas.Rectangle (draftleft+i*grid+1, draft.ClientHeight-(jj+1)*grid,
                                                    draftleft+(i+1)*grid, draft.ClientHeight-jj*grid-1);
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
                                draft.Canvas.Brush.Color = coltable[color];
                                draft.Canvas.Pen.Color = draft.Canvas.Brush.Color;
                                draft.Canvas.Rectangle (draftleft+begin_i*grid+1, draft.ClientHeight-(jj+1)*grid,
                                                    draftleft+(begin_i+1)*grid, draft.ClientHeight-jj*grid-1);
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
                                draft.Canvas.Brush.Color = coltable[color];
                                draft.Canvas.Pen.Color = draft.Canvas.Brush.Color;
                                draft.Canvas.Rectangle (draftleft+i*grid+1, draft.ClientHeight-(begin_j+1)*grid,
                                                    draftleft+(i+1)*grid, draft.ClientHeight-begin_j*grid-1);
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
                        case 0: sbColor0.Down = true; break;
                        case 1: sbColor1.Down = true; break;
                        case 2: sbColor2.Down = true; break;
                        case 3: sbColor3.Down = true; break;
                        case 4: sbColor4.Down = true; break;
                        case 5: sbColor5.Down = true; break;
                        case 6: sbColor6.Down = true; break;
                        case 7: sbColor7.Down = true; break;
                        case 8: sbColor8.Down = true; break;
                        case 9: sbColor9.Down = true; break;
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
            draft.Canvas.Brush.Color = coltable[color];
            draft.Canvas.Pen.Color = draft.Canvas.Brush.Color;
            byte bk = field.Get (_i, _j+scroll);
            int i = _i;
            while (i>=0 && field.Get(i, _j+scroll)==bk) {
                field.Set (i, _j+scroll, color);
            	// TODO make draft an observer of field!
                draft.Canvas.Rectangle (draftleft+i*grid+1, draft.ClientHeight-(_j+1)*grid,
                                    draftleft+(i+1)*grid, draft.ClientHeight-_j*grid-1);
                UpdateBead (i, _j+scroll);
                i--;
            }
            i = begin_i+1;
            while (i<field.Width() && field.Get(i, _j+scroll)==bk) {
                field.Set (i, _j+scroll, color);
            	// TODO make draft an observer of field!
                draft.Canvas.Rectangle (draftleft+i*grid+1, draft.ClientHeight-(_j+1)*grid,
                                    draftleft+(i+1)*grid, draft.ClientHeight-_j*grid-1);
                UpdateBead (i, _j+scroll);
                i++;
            }
        }

        void SetPoint (int _i, int _j)
        {
            undo.Snapshot (field, modified);
            byte s = field.Get(_i, _j+scroll);
            if (s==color) {
                field.Set (_i, _j+scroll, (byte) 0);
                if (draft.isVisible()) {
                	// TODO make draft an observer of field!
                    draft.Canvas.Brush.Color = clBtnFace;
                    draft.Canvas.Pen.Color = draft.Canvas.Brush.Color;
                    draft.Canvas.Rectangle (draftleft+_i*grid+1, draft.ClientHeight-(_j+1)*grid,
                                        draftleft+(_i+1)*grid, draft.ClientHeight-_j*grid-1);
                }
            }else {
                field.Set(_i, _j+scroll, color);
                if (draft.isVisible()) {
                	// TODO make draft an observer of field!
                    draft.Canvas.Brush.Color = coltable[color];
                    draft.Canvas.Pen.Color = draft.Canvas.Brush.Color;
                    draft.Canvas.Rectangle (draftleft+_i*grid+1, draft.ClientHeight-(_j+1)*grid,
                                        draftleft+(_i+1)*grid, draft.ClientHeight-_j*grid-1);
                }
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

        void FormKeyUp(WORD Key, TShiftState Shift)
        {
            if (Key==VK_F5) Invalidate();
            else if (Key=='1' && Shift.Contains(ssCtrl) && !Shift.Contains(ssAlt)) { sbToolPoint.Down = true; ToolPoint.Checked = true; }
            else if (Key=='2' && Shift.Contains(ssCtrl) && !Shift.Contains(ssAlt)) { sbToolSelect.Down = true; ToolSelect.Checked = true; }
            else if (Key=='3' && Shift.Contains(ssCtrl) && !Shift.Contains(ssAlt)) { sbToolFill.Down = true; ToolFill.Checked = true; }
            else if (Key=='4' && Shift.Contains(ssCtrl) && !Shift.Contains(ssAlt)) { sbToolSniff.Down = true; ToolSniff.Checked = true; }
            else if (Key>='0' && Key<='9') {
                color = Key-'0';
                switch (color) {
                    case 0: sbColor0.Down = true; break;
                    case 1: sbColor1.Down = true; break;
                    case 2: sbColor2.Down = true; break;
                    case 3: sbColor3.Down = true; break;
                    case 4: sbColor4.Down = true; break;
                    case 5: sbColor5.Down = true; break;
                    case 6: sbColor6.Down = true; break;
                    case 7: sbColor7.Down = true; break;
                    case 8: sbColor8.Down = true; break;
                    case 9: sbColor9.Down = true; break;
                    default: assert(false); break;
                }
            } else if (Key==VK_SPACE) {
                sbToolPoint.Down = true;
                ToolPoint.Checked = true;
            } else if (Key==VK_ESCAPE) {
                righttimer.Enabled = false;
                lefttimer.Enabled = false;
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
        void ColorClick()
        {
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
        void ColorDblClick()
        {
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
            colordialog.Color = coltable[c];
            if (colordialog.Execute()) {
                undo.Snapshot (field, modified);
                coltable[c] = colordialog.Color;
                modified = true; UpdateTitle();
                Invalidate();
                SetGlyphColors();
            }
        }

        void coolbarResize()
        {
            FormResize();
        }

        // TODO handle out parameter
        void scrollbarScroll(TScrollCode ScrollCode, int &ScrollPos)
        {
            int oldscroll = scroll;
            if (ScrollPos > scrollbar.Max - scrollbar.PageSize) ScrollPos = scrollbar.Max - scrollbar.PageSize;
            scroll = scrollbar.Max - scrollbar.PageSize - ScrollPos;
            if (oldscroll!=scroll) Invalidate();
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
            ToolPoint.Checked = true;
            sbToolPoint.Down = true;
            DraftSelectClear();
        }

        void ToolSelectClick()
        {
            ToolSelect.Checked = true;
            sbToolSelect.Down = true;
        }

        void ToolFillClick()
        {
            ToolFill.Checked = true;
            sbToolFill.Down = true;
            DraftSelectClear();
        }

        void ToolSniffClick()
        {
            ToolSniff.Checked = true;
            sbToolSniff.Down = true;
            DraftSelectClear();
        }

        void sbToolPointClick()
        {
            ToolPoint.Checked = true;
            DraftSelectClear();
        }

        void sbToolFillClick()
        {
            ToolFill.Checked = true;
            DraftSelectClear();
        }

        void sbToolSniffClick()
        {
            ToolSniff.Checked = true;
            DraftSelectClear();
        }

        void sbToolSelectClick()
        {
            ToolSelect.Checked = true;
        }

        // TODO handle out parameter
        boolean normalMouseToField (int& _i, int& _j)
        {
            int i;
            int jj = (normal.ClientHeight-_j)/grid;
            if (scroll%2==0) {
                if (jj%2==0) {
                    if (_i<normalleft || _i>normalleft+field.Width()*grid) return false;
                    i = (_i-normalleft) / grid;
                } else {
                    if (_i<normalleft-grid/2 || _i>normalleft+field.Width()*grid+grid/2) return false;
                    i = (_i-normalleft+grid/2) / grid;
                }
            } else {
                if (jj%2==1) {
                    if (_i<normalleft || _i>normalleft+field.Width()*grid) return false;
                    i = (_i-normalleft) / grid;
                } else {
                    if (_i<normalleft-grid/2 || _i>normalleft+field.Width()*grid+grid/2) return false;
                    i = (_i-normalleft+grid/2) / grid;
                }
            }
            _i = i;
            _j = jj;
            return true;
        }

        void normalMouseUp(TMouseButton Button, TShiftState Shift, int X, int Y)
        {
            if (Button==mbLeft && normalMouseToField (X, Y)) {
                // Lineare Koordinaten berechnen
                int idx = 0;
                int m1 = field.Width();
                int m2 = m1 + 1;
                for (int j=0; j<Y+scroll; j++) {
                    if (j%2==0) idx += m1;
                    else idx += m2;
                }
                idx += X;

                // Feld setzen und Darstellung nachf�hren
                int j = idx / field.Width();
                int i = idx % field.Width();
                SetPoint (i, j-scroll);
            }
        }

        void InfoAboutClick()
        {
            new AboutBox().FormShow();
        }

        void reportPaint()
        {
            int x1 = 12;
            int x2 = x1 + 100;
            int y = 0;
            int dy = 15;

            // Mustername
            report.Canvas.Pen.Color = clBlack;
            report.Canvas.TextOut (x1, y, Language.STR("Pattern:", "Muster:"));
            report.Canvas.TextOut (x2, y, ExtractFileName(savedialog.FileName));
            y += dy;
            // Umfang
            report.Canvas.TextOut (x1, y, Language.STR("Circumference:", "Umfang:"));
            report.Canvas.TextOut (x2, y, IntToStr(field.Width()));
            y += dy;
            // Farbrapport
            report.Canvas.TextOut (x1, y, Language.STR("repeat of colors:", "Farbrapport:"));
            report.Canvas.TextOut (x2, y, IntToStr(farbrapp)+Language.STR(" beads", " Perlen"));
            y += dy;
            // Farben
            // F�delliste...
            if (farbrapp>0) {
                report.Canvas.TextOut (x1, y, Language.STR("List of beads", "F�delliste"));
                y += dy;
                int ystart = y;
                char col = field.Get(farbrapp-1);
                int  count = 1;
                for (int i=farbrapp-2; i>=0; i--) {
                    if (field.Get(i)==col) {
                        count++;
                    } else {
                        if (col!=0) {
                            report.Canvas.Brush.Color = coltable[col];
                            report.Canvas.Pen.Color = report.Color;
                        } else {
                            report.Canvas.Brush.Color = report.Color;
                            report.Canvas.Pen.Color = clDkGray;
                        }
                        report.Canvas.Rectangle (x1, y, x1+dy, y+dy);
                        report.Canvas.Pen.Color = clBlack;
                        report.Canvas.Brush.Color = report.Color;
                        report.Canvas.TextOut (x1+dy+3, y, IntToStr(count));
                        y += dy;
                        col = field.Get(i);
                        count = 1;
                    }
                    if (y>=report.ClientHeight-10) {
                        x1 += dy + 24;
                        y = ystart;
                    }
                }
                if (y<report.ClientHeight-3) {
                    report.Canvas.Brush.Color = coltable[col];
                    report.Canvas.Pen.Color = report.Color;
                    report.Canvas.Rectangle (x1, y, x1+dy, y+dy);
                    report.Canvas.Pen.Color = clBlack;
                    report.Canvas.Brush.Color = report.Color;
                    report.Canvas.TextOut (x1+dy+3, y, IntToStr(count));
                }
            }
        }

        void lefttimerTimer()
        {
            RotateLeft();
            Application.ProcessMessages(); // FIXME maybe just remove it?
        }

        void righttimerTimer()
        {
            RotateRight();
            Application.ProcessMessages(); // FIXME maybe just remove it?
        }

        void sbRotaterightMouseDown(TMouseButton Button, TShiftState Shift, int X, int Y)
        {
            RotateRight();
            Application.ProcessMessages();
            righttimer.Enabled = true;
        }

        void sbRotaterightMouseUp(TMouseButton Button, TShiftState Shift, int X, int Y)
        {
            righttimer.Enabled = false;
        }

        void sbRotateleftMouseDown(TMouseButton Button, TShiftState Shift, int X, int Y)
        {
            RotateLeft();
            Application.ProcessMessages();
            lefttimer.Enabled = true;
        }

        void sbRotateleftMouseUp(TMouseButton Button, TShiftState Shift, int X, int Y)
        {
            lefttimer.Enabled = false;
        }

        void FormKeyDown(WORD Key, TShiftState Shift)
        {
            if (Key==VK_RIGHT) RotateRight();
            else if (Key==VK_LEFT) RotateLeft();
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
            if (CopyForm.ShowModal()==mrOk) {
                undo.Snapshot (field, modified);
                // Aktuelle Daten in Buffer kopieren
                sel_buff.CopyFrom (field);
                // Daten vervielf�ltigen
                ArrangeIncreasing (sel_i1, sel_i2);
                ArrangeIncreasing (sel_j1, sel_j2);
                for (int i=sel_i1; i<=sel_i2; i++) {
                    for (int j=sel_j1; j<=sel_j2; j++) {
                        char c = sel_buff.Get(i, j);
                        if (c==0) continue;
                        int idx = GetIndex(i, j);
                        // Diesen Punkt x-mal vervielf�ltigen
                        for (int k=0; k<GetNumberOfCopies(CopyForm); k++) {
                            idx += GetCopyOffset(CopyForm);
                            if (field.ValidIndex(idx)) field.Set (idx, c);
                        }
                    }
                }
                rapportdirty = true;
                modified = true; UpdateTitle();
                Invalidate();
            }
        }

        int GetCopyOffset(CopyForm form)
        {
            return form.getVertOffset() * field.Width() + form.getHorzOffset();
        }

        // TODO use parameter object?
        void ArrangeIncreasing (int& i1, int& i2)
        {
            if (i1>i2) {
                int temp = i1;
                i1 = i2;
                i2 = temp;
            }
        }

        int GetIndex (int i, int j)
        {
            return j*field.Width() + i;
        }

        int GetNumberOfCopies (CopyForm form)
        {
            return form.getCopies();
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
            String c = APP_TITLE;
            c += " - ";
            if (saved) c += ExtractFileName(savedialog.FileName);
            else c += DATEI_UNBENANNT;
            if (modified) c += "*";
            Caption = c;
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
            Printer().BeginDoc();
            String title = APP_TITLE;
            title += " - " + ExtractFileName(savedialog.FileName);
            Printer().Title = title;
            TCanvas canvas = Printer().Canvas;

            int sx = GetDeviceCaps(Printer().Handle, LOGPIXELSX);
            int sy = GetDeviceCaps(Printer().Handle, LOGPIXELSY);

            int gx = (15+zoomidx*5)*sx/254;
            int gy = (15+zoomidx*5)*sy/254;

            int draftleft, normalleft, simulationleft, reportleft;
            int reportcols;

            int m = MM2PRx(10, sx);
            if (draft.Visible) {
                draftleft = m;
                m += MM2PRx(13, sx) + field.Width()*gx + MM2PRx(7, sx);
            }

            if (normal.Visible) {
                normalleft = m;
                m += MM2PRx(7, sx) + (field.Width()+1)*gx;
            }

            if (simulation.Visible) {
                simulationleft = m;
                m += MM2PRx(7, sx) + (field.Width()/2+1)*gx;
            }

            if (report.Visible) {
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
                    CorrectCoordinates (ii, j1);
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
                    CorrectCoordinates (ii, jj);
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
            canvas.TextOut (x2, y, ExtractFileName(savedialog.FileName));
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
                for (signed int i=farbrapp-2; i>=0; i--) {
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
                            canvas.TextOut (x1, y, String(Language.STR("Pattern ", "Muster "))+ExtractFileName(savedialog.FileName) + " - " + Language.STR("page ", "Seite ") + IntToStr(page));
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

        g_exit:
            Printer().EndDoc();
        }

        void reloadLanguage()
        {
            // Men�s
              // Menu Datei
              Language.C_H(MenuFile, Language.LANG.EN, "&File", "");
              Language.C_H(MenuFile, Language.LANG.GE, "&Datei", "");
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

            Language.C_H(laDraft, Language.LANG.EN, "Draft", "");
            Language.C_H(laDraft, Language.LANG.GE, "Entwurf", "");
            Language.C_H(laNormal, Language.LANG.EN, "Corrected", "");
            Language.C_H(laNormal, Language.LANG.GE, "Korrigiert", "");
            Language.C_H(laSimulation, Language.LANG.EN, "Simulation", "");
            Language.C_H(laSimulation, Language.LANG.GE, "Simulation", "");
            Language.C_H(laReport, Language.LANG.EN, "Report", "");
            Language.C_H(laReport, Language.LANG.GE, "Auswertung", "");

            Invalidate();
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
            FileMRUSeparator.Visible = FileMRU1.Visible || FileMRU2.Visible ||
                                        FileMRU3.Visible || FileMRU4.Visible ||
                                        FileMRU5.Visible || FileMRU6.Visible;
        }

        void UpdateMRUMenu (int _item, TMenuItem _menuitem, String _filename)
        {
            _menuitem.Visible = _filename!="";

            //xxxy Eigene Dateien oder so?!
            // Bestimmen ob Datei im Daten-Verzeichnis ist, falls
            // nicht, ganzen Pfad anzeigen!
            String path = ExtractFilePath(_filename).LowerCase();
            String datapath = (ExtractFilePath(Application.ExeName)).LowerCase();
            if (path==datapath)
                _menuitem.Caption = (String)"&" + IntToStr(_item) + " " + ExtractFileName (_filename);
            else
                _menuitem.Caption = (String)"&" + IntToStr(_item) + " " + _filename;
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
