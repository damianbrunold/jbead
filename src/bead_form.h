/*  DB-BEAD - http://www.brunoldsoftware.ch
    Copyright (C) 2001  Damian Brunold
    Copyright (C) 2009  Damian Brunold

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

#ifndef bead_formH
#define bead_formH

#include <Classes.hpp>
#include <Controls.hpp>
#include <StdCtrls.hpp>
#include <Forms.hpp>
#include <ExtCtrls.hpp>
#include <Menus.hpp>
#include <Dialogs.hpp>
#include <Buttons.hpp>
#include <ComCtrls.hpp>
#include <ToolWin.hpp>

#include "language.h"

class TCopyForm;

class TBeadField
{
private:
    enum { SIZE=25*1000 };
    enum { SAVEZONE=100 };
    char field[SIZE+SAVEZONE];
    int width;
    int height;
    int shift; // 0 - (width-1) Drehung nach rechts
public:
    __fastcall TBeadField();
    void __fastcall Clear();
    int __fastcall Width() const { return width; }
    int __fastcall Height() const { return height; }
    int __fastcall LastIndex() const { return width*height - 1; }
    bool __fastcall ValidIndex (int idx) { return idx>=0 && idx<=LastIndex(); }
    void __fastcall SetWidth (int _width);
    char __fastcall Get(int _x, int _y) const;
    char __fastcall Get(int _idx) const;
    void __fastcall Set(int _x, int _y, char _data);
    void __fastcall Set(int _idx, char _data);
    char __fastcall RawGet(int _idx) const;
    void __fastcall RawSet(int _idx, char _data);
    void __fastcall CopyFrom (const TBeadField& _source);
    void __fastcall Save (TFileStream* _f);
    void __fastcall Load (TFileStream* _f);
    void __fastcall InsertLine();
    void __fastcall DeleteLine();
};

class TBeadUndo
{
private:
    enum { MAXUNDO=100 };
    TBeadField data[MAXUNDO];
    bool modified[MAXUNDO];
    int first, last;
    int current;
public:
    __fastcall TBeadUndo();
    void __fastcall Snapshot(const TBeadField& _data, bool _modified);
    void __fastcall PreSnapshot(const TBeadField& _data, bool _modified);
    void __fastcall Undo(TBeadField& _data);
    void __fastcall Redo(TBeadField& _data);
    bool __fastcall CanUndo() const { return current!=first; }
    bool __fastcall CanRedo() const { return current!=last; }
    bool __fastcall Modified() const;
    void __fastcall Clear();
};

class TBeadForm : public TForm
{
__published:	// IDE-managed Components
    TPaintBox *draft;
    TPaintBox *normal;
    TPaintBox *simulation;
    TMainMenu *mainmenu;
    TMenuItem *MenuFile;
    TMenuItem *FileOpen;
    TMenuItem *FileSave;
    TMenuItem *FileSaveas;
    TMenuItem *N1;
    TMenuItem *FileExit;
    TMenuItem *FileNew;
    TMenuItem *FilePrint;
    TMenuItem *N2;
    TMenuItem *FilePrintersetup;
    TMenuItem *MenuInfo;
    TMenuItem *InfoAbout;
    TMenuItem *MenuPattern;
    TMenuItem *PatternWidth;
    TMenuItem *MenuEdit;
    TMenuItem *EditUndo;
    TMenuItem *EditRedo;
    TMenuItem *MenuView;
    TMenuItem *ViewZoomin;
    TMenuItem *ViewZoomout;
    TMenuItem *ViewZoomnormal;
    TMenuItem *N3;
    TMenuItem *ViewSimulation;
    TMenuItem *ViewNormal;
    TMenuItem *ViewDraft;
    TLabel *laDraft;
    TLabel *laNormal;
    TLabel *laSimulation;
    TOpenDialog *opendialog;
    TSaveDialog *savedialog;
    TCoolBar *coolbar;
    TToolBar *tbStandard;
    TToolBar *tbColor;
    TSpeedButton *sbNew;
    TSpeedButton *sbOpen;
    TSpeedButton *sbSave;
    TToolButton *ToolButton1;
    TSpeedButton *sbUndo;
    TSpeedButton *sbRedo;
    TSpeedButton *sbColor0;
    TSpeedButton *sbColor1;
    TSpeedButton *sbColor2;
    TSpeedButton *sbColor3;
    TSpeedButton *sbColor4;
    TSpeedButton *sbColor5;
    TSpeedButton *sbColor9;
    TSpeedButton *sbColor8;
    TSpeedButton *sbColor7;
    TSpeedButton *sbColor6;
    TToolButton *ToolButton2;
    TSpeedButton *sbRotateright;
    TSpeedButton *sbRotateleft;
    TSpeedButton *sbPrint;
    TToolButton *ToolButton4;
    TColorDialog *colordialog;
    TMenuItem *FileMRUSeparator;
    TMenuItem *FileMRU1;
    TMenuItem *FileMRU2;
    TMenuItem *FileMRU3;
    TMenuItem *FileMRU4;
    TMenuItem *FileMRU5;
    TMenuItem *FileMRU6;
    TScrollBar *scrollbar;
    TToolBar *tbTools;
    TSpeedButton *sbToolPoint;
    TSpeedButton *sbToolFill;
    TSpeedButton *sbToolSniff;
    TMenuItem *Werkzeug1;
    TMenuItem *ToolPoint;
    TMenuItem *ToolFill;
    TMenuItem *ToolSniff;
    TPaintBox *report;
    TMenuItem *ViewReport;
    TLabel *laReport;
    TPrintDialog *printdialog;
    TPrinterSetupDialog *printersetupdialog;
    TTimer *lefttimer;
    TTimer *righttimer;
    TSpeedButton *sbToolSelect;
    TMenuItem *ToolSelect;
    TMenuItem *N4;
    TMenuItem *EditCopy;
    TToolButton *ToolButton3;
    TSpeedButton *sbCopy;
    TMenuItem *EditLine;
    TMenuItem *EditInsertline;
    TMenuItem *EditDeleteline;
        TMenuItem *N5;
        TMenuItem *ViewLanguage;
        TMenuItem *LanguageEnglish;
        TMenuItem *LanguageGerman;
    void __fastcall FormResize(TObject *Sender);
    void __fastcall draftPaint(TObject *Sender);
    void __fastcall simulationPaint(TObject *Sender);
    void __fastcall normalPaint(TObject *Sender);
    void __fastcall FileNewClick(TObject *Sender);
    void __fastcall FileOpenClick(TObject *Sender);
    void __fastcall FileSaveClick(TObject *Sender);
    void __fastcall FileSaveasClick(TObject *Sender);
    void __fastcall FilePrintClick(TObject *Sender);
    void __fastcall FilePrintersetupClick(TObject *Sender);
    void __fastcall FileExitClick(TObject *Sender);
    void __fastcall PatternWidthClick(TObject *Sender);
    void __fastcall draftMouseDown(TObject *Sender, TMouseButton Button,
          TShiftState Shift, int X, int Y);
    void __fastcall draftMouseMove(TObject *Sender, TShiftState Shift,
          int X, int Y);
    void __fastcall draftMouseUp(TObject *Sender, TMouseButton Button,
          TShiftState Shift, int X, int Y);
    void __fastcall EditUndoClick(TObject *Sender);
    void __fastcall EditRedoClick(TObject *Sender);
    void __fastcall ViewZoominClick(TObject *Sender);
    void __fastcall ViewZoomnormalClick(TObject *Sender);
    void __fastcall ViewZoomoutClick(TObject *Sender);
    void __fastcall ViewDraftClick(TObject *Sender);
    void __fastcall ViewNormalClick(TObject *Sender);
    void __fastcall ViewSimulationClick(TObject *Sender);
    void __fastcall FormKeyUp(TObject *Sender, WORD &Key,
          TShiftState Shift);
    void __fastcall ColorClick(TObject *Sender);
    void __fastcall coolbarResize(TObject *Sender);
    void __fastcall ColorDblClick(TObject *Sender);
    void __fastcall FileMRU1Click(TObject *Sender);
    void __fastcall FileMRU2Click(TObject *Sender);
    void __fastcall FileMRU3Click(TObject *Sender);
    void __fastcall FileMRU4Click(TObject *Sender);
    void __fastcall FileMRU5Click(TObject *Sender);
    void __fastcall FileMRU6Click(TObject *Sender);
    void __fastcall scrollbarScroll(TObject *Sender,
          TScrollCode ScrollCode, int &ScrollPos);
    void __fastcall FormCreate(TObject *Sender);
    void __fastcall ToolPointClick(TObject *Sender);
    void __fastcall ToolFillClick(TObject *Sender);
    void __fastcall ToolSniffClick(TObject *Sender);
    void __fastcall sbToolPointClick(TObject *Sender);
    void __fastcall sbToolFillClick(TObject *Sender);
    void __fastcall sbToolSniffClick(TObject *Sender);
    void __fastcall normalMouseUp(TObject *Sender, TMouseButton Button,
          TShiftState Shift, int X, int Y);
    void __fastcall InfoAboutClick(TObject *Sender);
    void __fastcall ViewReportClick(TObject *Sender);
    void __fastcall reportPaint(TObject *Sender);
    void __fastcall lefttimerTimer(TObject *Sender);
    void __fastcall sbRotaterightMouseDown(TObject *Sender,
          TMouseButton Button, TShiftState Shift, int X, int Y);
    void __fastcall sbRotaterightMouseUp(TObject *Sender,
          TMouseButton Button, TShiftState Shift, int X, int Y);
    void __fastcall sbRotateleftMouseDown(TObject *Sender,
          TMouseButton Button, TShiftState Shift, int X, int Y);
    void __fastcall sbRotateleftMouseUp(TObject *Sender,
          TMouseButton Button, TShiftState Shift, int X, int Y);
    void __fastcall righttimerTimer(TObject *Sender);
    void __fastcall FormKeyDown(TObject *Sender, WORD &Key,
          TShiftState Shift);
    void __fastcall FormCloseQuery(TObject *Sender, bool &CanClose);
    void __fastcall ToolSelectClick(TObject *Sender);
    void __fastcall sbToolSelectClick(TObject *Sender);
    void __fastcall EditCopyClick(TObject *Sender);
    void __fastcall EditInsertlineClick(TObject *Sender);
    void __fastcall EditDeletelineClick(TObject *Sender);
        void __fastcall LanguageEnglishClick(TObject *Sender);
        void __fastcall LanguageGermanClick(TObject *Sender);
private:	// User declarations
    TBeadUndo  undo;
    TBeadField field;
    TColor coltable[10];
    char color;
    int begin_i;
    int begin_j;
    int end_i;
    int end_j;
    int sel_i1, sel_i2, sel_j1, sel_j2;
    bool selection;
    TBeadField sel_buff;
    bool dragging;
    int draftleft;
    int normalleft;
    int simulationleft;
    int grid;
    int zoomtable[5];
    int zoomidx;
    int scroll;
    signed int shift;
    bool saved;
    bool modified;
    bool rapportdirty;
    int rapport;
    int farbrapp;
    String mru[6];
    void __fastcall UpdateBead (int _i, int _j);
    void __fastcall UpdateTitle();
    void __fastcall UpdateScrollbar();
    void __fastcall CorrectCoordinates (int& _i, int& _j);
    void __fastcall DefaultColors();
    void __fastcall SetGlyphColors();
    void __fastcall AddToMRU (const String& _filename);
    void __fastcall UpdateMRU();
    void __fastcall UpdateMRUMenu (int _item, TMenuItem* _menuitem, String _filename);
    void __fastcall SaveMRU();
    void __fastcall LoadMRU();
    void __fastcall LoadFile (const String& _filename, bool _addtomru);
    void __fastcall IdleHandler (TObject* Sender, bool& Done);
    bool __fastcall Equal (int _i, int _j);
    void __fastcall DraftLinePreview();
    void __fastcall DraftSelectPreview (bool _draw=true, bool _doit=false);
    void __fastcall DraftSelectDraw();
    void __fastcall DraftSelectClear();
    void __fastcall CalcLineCoord (int _i1, int _j1, int& _i2, int& _j2);
    void __fastcall SetPoint (int _i, int _j);
    void __fastcall PrintItAll();
    void __fastcall OnShowPrintDialog (TObject* Sender);
    void __fastcall OnShowPrintersetupDialog (TObject* Sender);
    void __fastcall FillLine (int _i, int _j);
    void __fastcall RotateLeft();
    void __fastcall RotateRight();


    int __fastcall GetCopyOffset(TCopyForm* form);
    void __fastcall ArrangeIncreasing (int& i1, int& i2);
    int __fastcall GetIndex (int i, int j);
    int __fastcall GetNumberOfCopies (TCopyForm* form);

public:		// User declarations
    __fastcall TBeadForm(TComponent* Owner);
    bool __fastcall draftMouseToField (int& _i, int& _j);
    bool __fastcall normalMouseToField (int& _i, int& _j);
    void __fastcall reloadLanguage();
    void __fastcall setAppTitle();
};

extern PACKAGE TBeadForm *BeadForm;

#endif

