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

#include <vcl.h>
#include <printers.hpp>
#pragma hdrstop

#include "assert.h"
#include "bead_form.h"
#include "patternwidth_form.h"
#include "aboutbox_form.h"
#include "copy_form.h"
#include "settings.h"

#pragma package(smart_init)
#pragma resource "*.dfm"
TBeadForm *BeadForm;

__fastcall TBeadField::TBeadField()
{
    SetWidth(15);
    Clear();
}

void __fastcall TBeadField::Clear()
{
    for (int i=0; i<SIZE; i++) field[i] = 0;
#ifndef NDEBUG
    for (int i=SIZE; i<SIZE+SAVEZONE; i++) field[i] = 127;
#endif
}

void __fastcall TBeadField::SetWidth (int _width)
{
    assert(_width>0 && _width<SIZE/10);
    width = _width;
    height = SIZE/width;
    assert(width>0 && height>0);
}

char __fastcall TBeadField::Get(int _x, int _y) const
{
    assert(width>0 && height>0);
    assert(_x<width);
    assert(_y<height);
    assert(_x+width*_y<SIZE);
    char c = field[_x + width*_y];
    assert(c!=127);
    return c;
}

char __fastcall TBeadField::Get(int _idx) const
{
    assert (width>0);
    assert (_idx>=0 && _idx<width*height);
    int i = _idx % width;
    int j = _idx / width;
    return Get (i, j);
}

void __fastcall TBeadField::Set(int _x, int _y, char _data)
{
    assert(width>0 && height>0);
    assert(_x<width);
    assert(_y<height);
    assert(_x+width*_y<SIZE);
    field[_x + width*_y] = _data;
#ifndef NDEBUG
    for (int i=SIZE; i<SIZE+SAVEZONE; i++) assert(field[i]==127);
#endif
}

void __fastcall TBeadField::Set(int _idx, char _data)
{
    assert (width>0);
    assert (_idx>=0 && _idx<width*height);
    int i = _idx % width;
    int j = _idx / width;
    Set (i, j, _data);
}

char __fastcall TBeadField::RawGet(int _idx) const
{
    assert(_idx<width*height);
    return field[_idx];
}

void __fastcall TBeadField::RawSet(int _idx, char _data)
{
    assert(_idx<width*height);
    field[_idx] = _data;
}

void __fastcall TBeadField::CopyFrom (const TBeadField& _source)
{
    SetWidth(_source.Width());
    for (int i=0; i<width*height; i++)
        RawSet (i, _source.RawGet(i));
}

void __fastcall TBeadField::Save (TFileStream* _f)
{
    _f->Write (&width, sizeof(width));
    _f->Write (field, SIZE);
}

void __fastcall TBeadField::Load (TFileStream* _f)
{
    _f->Read (&width, sizeof(width));
    _f->Read (field, SIZE);
    SetWidth (width);
}

void __fastcall TBeadField::InsertLine()
{
    for (int j=Height()-1; j>0; j--)
        for (int i=0; i<Width(); i++)
            Set (i, j, Get(i, j-1));
    for (int i=0; i<Width(); i++)
        Set (i, 0, 0);
}

void __fastcall TBeadField::DeleteLine()
{
    for (int j=0; j<Height()-1; j++)
        for (int i=0; i<Width(); i++)
            Set (i, j, Get(i, j+1));
    for (int i=0; i<Width(); i++)
        Set (i, Height()-1, 0);
}

__fastcall TBeadUndo::TBeadUndo()
{
    first = 0;
    last = 0;
    current = 0;
}

void __fastcall TBeadUndo::Clear()
{
    first = 0;
    last = 0;
    current = 0;
}

void __fastcall TBeadUndo::Snapshot(const TBeadField& _data, bool _modified)
{
    data[current].CopyFrom (_data);
    modified[current] = _modified;
    current = (current+1) % MAXUNDO;
    if (current==first) first = (first+1) % MAXUNDO;
    last = current;
}

void __fastcall TBeadUndo::PreSnapshot(const TBeadField& _data, bool _modified)
{
    if (!_modified) return;
    data[current].CopyFrom (_data);
    modified[current] = _modified;
}

void __fastcall TBeadUndo::Undo(TBeadField& _data)
{
    if (current==first) return; // Nichts rückgängig zu machen
    current = (current-1+MAXUNDO)%MAXUNDO;
    _data.CopyFrom (data[current]);
}

void __fastcall TBeadUndo::Redo(TBeadField& _data)
{
    if (current==last) return; // Nichts wiederzumachen
    current = (current+1)%MAXUNDO;
//    if (current==last) return; // dito
    _data.CopyFrom (data[current]);
}

bool __fastcall TBeadUndo::Modified() const
{
    return modified[current];
}

__fastcall TBeadForm::TBeadForm(TComponent* Owner)
    : TForm(Owner)
{
    opendialog->FileName = "*.dbb";
    savedialog->FileName = LANG_STR("unnamed", "unbenannt");
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
    Printer()->Orientation = poLandscape;

    Settings settings;
    settings.SetCategory ("Environment");
    LANGUAGES language;
    int lang = settings.Load ("Language", -1);
    if (lang==-1) { // Windows-Spracheinstellung abfragen
        char loc[4];
        if (GetLocaleInfo (LOCALE_USER_DEFAULT, LOCALE_SABBREVLANGNAME, loc, 4)!=0) {
            if ((loc[0]=='D' || loc[0]=='d') && (loc[1]=='E' || loc[1]=='e'))
                lang = 1;
            else
                lang = 0;
        } else lang = 0;
    }
    language = lang==0 ? EN : GE;
    if (active_language==language) active_language = language==EN ? GE : EN; // Update erzwingen
    SwitchLanguage (language);
    if (active_language == EN) LanguageEnglish->Checked = true;
    else LanguageGerman->Checked = true;
}

void __fastcall TBeadForm::DefaultColors()
{
    coltable[0] = clBtnFace;
    coltable[1] = clMaroon;
    coltable[2] = clNavy;
    coltable[3] = clGreen;
    coltable[4] = clYellow;
    coltable[5] = clRed;
    coltable[6] = clBlue;
    coltable[7] = clPurple;
    coltable[8] = clBlack;
    coltable[9] = clWhite;
}

void __fastcall TBeadForm::SetGlyphColors()
{
    sbColor0->Glyph->TransparentMode = tmFixed;
    sbColor0->Glyph->TransparentColor = clOlive;
    sbColor0->Glyph->Canvas->Pen->Color = clBlack;
    sbColor0->Glyph->Canvas->MoveTo (1, 1);
    sbColor0->Glyph->Canvas->LineTo (15, 15);
    sbColor0->Glyph->Canvas->MoveTo (1, 14);
    sbColor0->Glyph->Canvas->LineTo (15, 0);

    sbColor1->Glyph->Canvas->Brush->Color = coltable[1];
    sbColor1->Glyph->Canvas->Pen->Color = coltable[1];
    sbColor1->Glyph->Canvas->Rectangle (1, 0, sbColor1->Glyph->Width, sbColor1->Glyph->Height);

    sbColor2->Glyph->Canvas->Brush->Color = coltable[2];
    sbColor2->Glyph->Canvas->Pen->Color = coltable[2];
    sbColor2->Glyph->Canvas->Rectangle (1, 0, sbColor2->Glyph->Width, sbColor2->Glyph->Height);

    sbColor3->Glyph->Canvas->Brush->Color = coltable[3];
    sbColor3->Glyph->Canvas->Pen->Color = coltable[3];
    sbColor3->Glyph->Canvas->Rectangle (1, 0, sbColor3->Glyph->Width, sbColor3->Glyph->Height);

    sbColor4->Glyph->Canvas->Brush->Color = coltable[4];
    sbColor4->Glyph->Canvas->Pen->Color = coltable[4];
    sbColor4->Glyph->Canvas->Rectangle (1, 0, sbColor4->Glyph->Width, sbColor4->Glyph->Height);

    sbColor5->Glyph->Canvas->Brush->Color = coltable[5];
    sbColor5->Glyph->Canvas->Pen->Color = coltable[5];
    sbColor5->Glyph->Canvas->Rectangle (1, 0, sbColor5->Glyph->Width, sbColor5->Glyph->Height);

    sbColor6->Glyph->Canvas->Brush->Color = coltable[6];
    sbColor6->Glyph->Canvas->Pen->Color = coltable[6];
    sbColor6->Glyph->Canvas->Rectangle (1, 0, sbColor6->Glyph->Width, sbColor6->Glyph->Height);

    sbColor7->Glyph->Canvas->Brush->Color = coltable[7];
    sbColor7->Glyph->Canvas->Pen->Color = coltable[7];
    sbColor7->Glyph->Canvas->Rectangle (1, 0, sbColor7->Glyph->Width, sbColor7->Glyph->Height);

    sbColor8->Glyph->Canvas->Brush->Color = coltable[8];
    sbColor8->Glyph->Canvas->Pen->Color = coltable[8];
    sbColor8->Glyph->Canvas->Rectangle (1, 0, sbColor8->Glyph->Width, sbColor8->Glyph->Height);

    sbColor9->Glyph->Canvas->Brush->Color = coltable[9];
    sbColor9->Glyph->Canvas->Pen->Color = coltable[9];
    sbColor9->Glyph->Canvas->Rectangle (1, 0, sbColor9->Glyph->Width, sbColor9->Glyph->Height);
}

void __fastcall TBeadForm::FormResize(TObject *Sender)
{
    if (Application->Terminated) return;

    int cheight = ClientHeight - coolbar->Height;
    int cwidth = ClientWidth - scrollbar->Width;
    int top = coolbar->Height + 6;

    int nr = 0;
    if (ViewDraft->Checked) nr++;
    if (ViewNormal->Checked) nr++;
    if (ViewSimulation->Checked) nr++;
    if (ViewReport->Checked) nr += 2;
    if (nr==0) {
        ViewDraft->Checked = true;
        draft->Visible = true;
        laDraft->Visible = true;
        nr = 1;
    }

    int m = 6;

    if (ViewDraft->Checked) {
        draft->Left = m;
        draft->Top = top;
        draft->Width = field.Width()*grid + 35;
        draft->Height = cheight - 6 - laDraft->Height - 3;
        laDraft->Left = m + (draft->Width-laDraft->Width)/2;
        laDraft->Top = draft->Top + draft->Height + 2;
        m += draft->Width + 12;
    }

    if (ViewNormal->Checked) {
        normal->Left = m;
        normal->Top = top;
        normal->Width = (field.Width()+1)*grid + 10;
        normal->Height = cheight - 6 - laNormal->Height - 3;
        laNormal->Left = m + (normal->Width-laNormal->Width)/2;
        laNormal->Top = normal->Top + normal->Height + 2;
        m += normal->Width + 12;
    }

    if (ViewSimulation->Checked) {
        simulation->Left = m;
        simulation->Top = top;
        simulation->Width = (field.Width()+2)*grid/2 + 10;
        simulation->Height = cheight - 6 - laSimulation->Height - 3;
        laSimulation->Left = m + (simulation->Width-laSimulation->Width)/2;
        laSimulation->Top = simulation->Top + simulation->Height + 2;
        m += simulation->Width + 12;
    }

    if (ViewReport->Checked) {
        report->Left = m;
        report->Top = top;
        report->Width = cwidth - m - 6;
        report->Height = cheight - 6 - laReport->Height - 3;
        laReport->Left = m + 5;
        laReport->Top = report->Top + report->Height + 2;
    }

    scrollbar->Left = ClientWidth - scrollbar->Width;
    scrollbar->Top = top;
    scrollbar->Height = cheight - 6 - laDraft->Height - 3;

    UpdateScrollbar();
}

void __fastcall TBeadForm::UpdateScrollbar()
{
    int h = draft->Height/grid;
    assert(h<field.Height());
    scrollbar->Min = 0;
    scrollbar->Max = field.Height()-h;
    if (scrollbar->Max<0) scrollbar->Max = 0;
    scrollbar->PageSize = h;
    scrollbar->LargeChange = h;
    scrollbar->Position = scrollbar->Max - scrollbar->PageSize - scroll;
}

void __fastcall TBeadForm::draftPaint(TObject *Sender)
{
    // Grid
    draft->Canvas->Pen->Color = clDkGray;
    draftleft = draft->ClientWidth-field.Width()*grid-1;
    int left = draftleft;
    if (left<0) left=0;
    int maxj = min(field.Height(), draft->ClientHeight/grid+1);
    for (int i=0; i<field.Width()+1; i++) {
        draft->Canvas->MoveTo(left+i*grid, 0);
        draft->Canvas->LineTo(left+i*grid, draft->ClientHeight-1);
    }
    for (int j=0; j<maxj; j++) {
        draft->Canvas->MoveTo(left, draft->ClientHeight-1-j*grid);
        draft->Canvas->LineTo(left+field.Width()*grid, draft->ClientHeight-1-j*grid);
    }


    // Daten
    for (int i=0; i<field.Width(); i++)
        for (int j=0; j<maxj; j++) {
            char c = field.Get (i, j+scroll);
            assert(c>=0 && c<=9);
            draft->Canvas->Brush->Color = coltable[c];
            draft->Canvas->Pen->Color = draft->Canvas->Brush->Color;
            draft->Canvas->Rectangle (left+i*grid+1, draft->ClientHeight-(j+1)*grid,
                                left+(i+1)*grid, draft->ClientHeight-1-j*grid);
        }

    // Zehnermarkierungen
    draft->Canvas->Pen->Color = clDkGray;
    draft->Canvas->Brush->Color = draft->Color;
    for (int j=0; j<maxj; j++) {
        if (((j+scroll)%10)==0) {
            draft->Canvas->MoveTo (0, draft->ClientHeight - j*grid - 1);
            draft->Canvas->LineTo (left-6, draft->ClientHeight - j*grid - 1);
            draft->Canvas->TextOutA (6, draft->ClientHeight - j*grid + 1, IntToStr(j+scroll));
        }
    }

    // Rapportmarkierung
#if(0)
    if (rapport!=0) {
        draft->Canvas->Pen->Color = clRed;
        draft->Canvas->MoveTo (0, draft->ClientHeight - (rapport-scroll)*grid - 1);
        draft->Canvas->LineTo (left-6, draft->ClientHeight - (rapport-scroll)*grid - 1);
    }
#endif

    // Auswahl
    DraftSelectDraw();
}

void __fastcall TBeadForm::normalPaint(TObject *Sender)
{
    // Grid
    normal->Canvas->Pen->Color = clDkGray;
    normalleft = normal->ClientWidth-1-(field.Width()+1)*grid + grid/2;
    int left = normalleft;
    if (left<0) left=grid/2;
    int maxj = min(field.Height(), normal->ClientHeight/grid+1);
    if (scroll%2==0) {
        for (int i=0; i<field.Width()+1; i++) {
            for (int jj=0; jj<maxj; jj+=2) {
                normal->Canvas->MoveTo(left+i*grid, normal->ClientHeight-(jj+1)*grid);
                normal->Canvas->LineTo(left+i*grid, normal->ClientHeight-jj*grid);
            }
        }
        for (int i=0; i<=field.Width()+1; i++) {
            for (int jj=1; jj<maxj; jj+=2) {
                normal->Canvas->MoveTo(left+i*grid-grid/2, normal->ClientHeight-(jj+1)*grid);
                normal->Canvas->LineTo(left+i*grid-grid/2, normal->ClientHeight-jj*grid);
            }
        }
    } else {
        for (int i=0; i<=field.Width()+1; i++) {
            for (int jj=0; jj<maxj; jj+=2) {
                normal->Canvas->MoveTo(left+i*grid-grid/2, normal->ClientHeight-(jj+1)*grid);
                normal->Canvas->LineTo(left+i*grid-grid/2, normal->ClientHeight-jj*grid);
            }
        }
        for (int i=0; i<field.Width()+1; i++) {
            for (int jj=1; jj<maxj; jj+=2) {
                normal->Canvas->MoveTo(left+i*grid, normal->ClientHeight-(jj+1)*grid);
                normal->Canvas->LineTo(left+i*grid, normal->ClientHeight-jj*grid);
            }
        }
    }
    if (scroll%2==0) {
        normal->Canvas->MoveTo(left, normal->ClientHeight-1);
        normal->Canvas->LineTo(left+field.Width()*grid+1, normal->ClientHeight-1);
        for (int jj=1; jj<maxj; jj++) {
            normal->Canvas->MoveTo(left-grid/2, normal->ClientHeight-1-jj*grid);
            normal->Canvas->LineTo(left+field.Width()*grid+grid/2+1, normal->ClientHeight-1-jj*grid);
        }
    } else {
        for (int jj=0; jj<maxj; jj++) {
            normal->Canvas->MoveTo(left-grid/2, normal->ClientHeight-1-jj*grid);
            normal->Canvas->LineTo(left+field.Width()*grid+grid/2+1, normal->ClientHeight-1-jj*grid);
        }
    }

    // Daten
    for (int i=0; i<field.Width(); i++)
        for (int jj=0; jj<maxj; jj++) {
            char c = field.Get (i, jj+scroll);
            assert(c>=0 && c<=9);
            normal->Canvas->Brush->Color = coltable[c];
            normal->Canvas->Pen->Color = normal->Canvas->Brush->Color;
            int ii = i;
            int j1 = jj;
            CorrectCoordinates (ii, j1);
            if (scroll%2==0) {
                if (j1%2==0) {
                    normal->Canvas->Rectangle (left+ii*grid+1,
                                               normal->ClientHeight-(j1+1)*grid,
                                               left+(ii+1)*grid,
                                               normal->ClientHeight-1-j1*grid);
                } else {
                    normal->Canvas->Rectangle (left-grid/2+ii*grid+1,
                                               normal->ClientHeight-(j1+1)*grid,
                                               left-grid/2+(ii+1)*grid,
                                               normal->ClientHeight-1-j1*grid);
                }
            } else {
                if (j1%2==1) {
                    normal->Canvas->Rectangle (left+ii*grid+1,
                                               normal->ClientHeight-(j1+1)*grid,
                                               left+(ii+1)*grid,
                                               normal->ClientHeight-1-j1*grid);
                } else {
                    normal->Canvas->Rectangle (left-grid/2+ii*grid+1,
                                               normal->ClientHeight-(j1+1)*grid,
                                               left-grid/2+(ii+1)*grid,
                                               normal->ClientHeight-1-j1*grid);
                }
            }
        }
}

void __fastcall TBeadForm::simulationPaint(TObject *Sender)
{
    // Grid
    simulation->Canvas->Pen->Color = clDkGray;
    simulationleft = simulation->ClientWidth-1-(field.Width()+1)*grid/2 + grid/2;
    int left = simulationleft;
    if (left<0) left=grid/2;
    int maxj = min(field.Height(), simulation->ClientHeight/grid+1);
    int w = field.Width()/2;
    if (scroll%2==0) {
        for (int j=0; j<maxj; j+=2) {
            for (int i=0; i<w+1; i++) {
                if (j==0 && scroll==0 && i<shift) continue;
                simulation->Canvas->MoveTo(left+i*grid, simulation->ClientHeight-(j+1)*grid);
                simulation->Canvas->LineTo(left+i*grid, simulation->ClientHeight-j*grid);
            }
            if (j>0 || scroll>0) {
                simulation->Canvas->MoveTo (left-grid/2, simulation->ClientHeight-(j+1)*grid);
                simulation->Canvas->LineTo (left-grid/2, simulation->ClientHeight-j*grid);
            }
        }
        for (int j=1; j<maxj; j+=2) {
            for (int i=0; i<w+1; i++) {
                simulation->Canvas->MoveTo(left+i*grid-grid/2, simulation->ClientHeight-(j+1)*grid);
                simulation->Canvas->LineTo(left+i*grid-grid/2, simulation->ClientHeight-j*grid);
            }
            simulation->Canvas->MoveTo(left+field.Width()*grid, simulation->ClientHeight-(j+1)*grid);
            simulation->Canvas->LineTo(left+field.Width()*grid, simulation->ClientHeight-j*grid);
        }
    } else {
        for (int j=0; j<maxj; j+=2) {
            for (int i=0; i<w+1; i++) {
                simulation->Canvas->MoveTo(left+i*grid-grid/2, simulation->ClientHeight-(j+1)*grid);
                simulation->Canvas->LineTo(left+i*grid-grid/2, simulation->ClientHeight-j*grid);
            }
            simulation->Canvas->MoveTo(left+field.Width()*grid, simulation->ClientHeight-(j+1)*grid);
            simulation->Canvas->LineTo(left+field.Width()*grid, simulation->ClientHeight-j*grid);
        }
        for (int j=1; j<maxj; j+=2) {
            for (int i=0; i<w+1; i++) {
                simulation->Canvas->MoveTo(left+i*grid, simulation->ClientHeight-(j+1)*grid);
                simulation->Canvas->LineTo(left+i*grid, simulation->ClientHeight-j*grid);
            }
            simulation->Canvas->MoveTo (left-grid/2, simulation->ClientHeight-(j+1)*grid);
            simulation->Canvas->LineTo (left-grid/2, simulation->ClientHeight-j*grid);
        }
    }
    if (scroll%2==0) {
        if (scroll==0) {
            simulation->Canvas->MoveTo(left+shift*grid, simulation->ClientHeight-1);
            simulation->Canvas->LineTo(left+w*grid+1, simulation->ClientHeight-1);
            for (int j=1; j<maxj; j++) {
                simulation->Canvas->MoveTo(left-grid/2, simulation->ClientHeight-1-j*grid);
                simulation->Canvas->LineTo(left+w*grid+1, simulation->ClientHeight-1-j*grid);
            }
            simulation->Canvas->MoveTo (left+w*grid, 0);
            simulation->Canvas->LineTo (left+w*grid, simulation->ClientHeight-1-grid);
        } else {
            for (int j=0; j<maxj; j++) {
                simulation->Canvas->MoveTo(left-grid/2, simulation->ClientHeight-1-j*grid);
                simulation->Canvas->LineTo(left+w*grid+1, simulation->ClientHeight-1-j*grid);
            }
            simulation->Canvas->MoveTo (left+w*grid, 0);
            simulation->Canvas->LineTo (left+w*grid, simulation->ClientHeight-1-grid);
        }
    } else {
        for (int j=0; j<maxj; j++) {
            simulation->Canvas->MoveTo(left-grid/2, simulation->ClientHeight-1-j*grid);
            simulation->Canvas->LineTo(left+w*grid+1, simulation->ClientHeight-1-j*grid);
        }
        simulation->Canvas->MoveTo (left+w*grid, 0);
        simulation->Canvas->LineTo (left+w*grid, simulation->ClientHeight-1);
    }

    // Daten
    for (int i=0; i<field.Width(); i++)
        for (int j=0; j<maxj; j++) {
            char c = field.Get (i, j+scroll);
            assert(c>=0 && c<=9);
            simulation->Canvas->Brush->Color = coltable[c];
            simulation->Canvas->Pen->Color = simulation->Canvas->Brush->Color;
            int idx = i+field.Width()*j + shift;
            int ii = idx % field.Width();
            int jj = idx / field.Width();
            CorrectCoordinates (ii, jj);
            if (ii>w && ii!=field.Width()) continue;
            if (scroll%2==0) {
                if (jj%2==0) {
                    if (ii==w) continue;
                    simulation->Canvas->Rectangle (left+ii*grid+1,
                                               simulation->ClientHeight-(jj+1)*grid,
                                               left+(ii+1)*grid,
                                               simulation->ClientHeight-1-jj*grid);
                } else {
                    if (ii!=field.Width() && ii!=w) {
                        simulation->Canvas->Rectangle (left-grid/2+ii*grid+1,
                                                   simulation->ClientHeight-(jj+1)*grid,
                                                   left-grid/2+(ii+1)*grid,
                                                   simulation->ClientHeight-1-jj*grid);
                    } else if (ii==field.Width()) {
                        simulation->Canvas->Rectangle (left-grid/2+1,
                                                   simulation->ClientHeight-(jj+2)*grid,
                                                   left,
                                                   simulation->ClientHeight-1-(jj+1)*grid);
                    } else {
                        simulation->Canvas->Rectangle (left-grid/2+ii*grid+1,
                                                   simulation->ClientHeight-(jj+1)*grid,
                                                   left-grid/2+ii*grid+grid/2,
                                                   simulation->ClientHeight-1-jj*grid);
                    }
                }
            } else {
                if (jj%2==1) {
                    if (ii==w) continue;
                    simulation->Canvas->Rectangle (left+ii*grid+1,
                                               simulation->ClientHeight-(jj+1)*grid,
                                               left+(ii+1)*grid,
                                               simulation->ClientHeight-1-jj*grid);
                } else {
                    if (ii!=field.Width() && ii!=w) {
                        simulation->Canvas->Rectangle (left-grid/2+ii*grid+1,
                                                   simulation->ClientHeight-(jj+1)*grid,
                                                   left-grid/2+(ii+1)*grid,
                                                   simulation->ClientHeight-1-jj*grid);
                    } else if (ii==field.Width()) {
                        simulation->Canvas->Rectangle (left-grid/2+1,
                                                   simulation->ClientHeight-(jj+2)*grid,
                                                   left,
                                                   simulation->ClientHeight-1-(jj+1)*grid);
                    } else {
                        simulation->Canvas->Rectangle (left-grid/2+ii*grid+1,
                                                   simulation->ClientHeight-(jj+1)*grid,
                                                   left-grid/2+ii*grid+grid/2,
                                                   simulation->ClientHeight-1-jj*grid);
                    }
                }
            }
        }
}

void __fastcall TBeadForm::CorrectCoordinates (int& _i, int& _j)
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

void __fastcall TBeadForm::UpdateBead (int _i, int _j)
{
    char c = field.Get (_i, _j+scroll);
    assert(c>=0 && c<=9);

    int ii = _i;
    int jj = _j;
    CorrectCoordinates (_i, _j);

    // Normal
    if (normal->Visible) {
        normal->Canvas->Brush->Color = coltable[c];
        normal->Canvas->Pen->Color = normal->Canvas->Brush->Color;
        int left = normalleft;
        if (scroll%2==0) {
            if (_j%2==0) {
                normal->Canvas->Rectangle (left+_i*grid+1,
                                           normal->ClientHeight-(_j+1)*grid,
                                           left+(_i+1)*grid,
                                           normal->ClientHeight-1-_j*grid);
            } else {
                normal->Canvas->Rectangle (left-grid/2+_i*grid+1,
                                           normal->ClientHeight-(_j+1)*grid,
                                           left-grid/2+(_i+1)*grid,
                                           normal->ClientHeight-1-_j*grid);
            }
        } else {
            if (_j%2==1) {
                normal->Canvas->Rectangle (left+_i*grid+1,
                                           normal->ClientHeight-(_j+1)*grid,
                                           left+(_i+1)*grid,
                                           normal->ClientHeight-1-_j*grid);
            } else {
                normal->Canvas->Rectangle (left-grid/2+_i*grid+1,
                                           normal->ClientHeight-(_j+1)*grid,
                                           left-grid/2+(_i+1)*grid,
                                           normal->ClientHeight-1-_j*grid);
            }
        }
    }

    // Simulation
    int idx = ii+field.Width()*jj + shift;
    _i = idx % field.Width();
    _j = idx / field.Width();
    CorrectCoordinates (_i, _j);
    if (simulation->Visible) {
        simulation->Canvas->Brush->Color = coltable[c];
        simulation->Canvas->Pen->Color = simulation->Canvas->Brush->Color;
        int left = simulationleft;
        int w = field.Width()/2;
        if (_i>w && _i!=field.Width()) return;
        if (scroll%2==0) {
            if (_j%2==0) {
                if (_i==w) return;
                simulation->Canvas->Rectangle (left+_i*grid+1,
                                           simulation->ClientHeight-(_j+1)*grid,
                                           left+(_i+1)*grid,
                                           simulation->ClientHeight-1-_j*grid);
            } else {
                if (_i!=field.Width() && _i!=w) {
                    simulation->Canvas->Rectangle (left-grid/2+_i*grid+1,
                                               simulation->ClientHeight-(_j+1)*grid,
                                               left-grid/2+(_i+1)*grid,
                                               simulation->ClientHeight-1-_j*grid);
                } else if (_i==w) {
                    simulation->Canvas->Rectangle (left-grid/2+_i*grid+1,
                                               simulation->ClientHeight-(_j+1)*grid,
                                               left-grid/2+_i*grid+grid/2,
                                               simulation->ClientHeight-1-_j*grid);
                } else {
                    simulation->Canvas->Rectangle (left-grid/2+1,
                                               simulation->ClientHeight-(_j+2)*grid,
                                               left,
                                               simulation->ClientHeight-1-(_j+1)*grid);
                }
            }
        } else {
            if (_j%2==1) {
                if (_i==w) return;
                simulation->Canvas->Rectangle (left+_i*grid+1,
                                           simulation->ClientHeight-(_j+1)*grid,
                                           left+(_i+1)*grid,
                                           simulation->ClientHeight-1-_j*grid);
            } else {
                if (_i!=field.Width() && _i!=w) {
                    simulation->Canvas->Rectangle (left-grid/2+_i*grid+1,
                                               simulation->ClientHeight-(_j+1)*grid,
                                               left-grid/2+(_i+1)*grid,
                                               simulation->ClientHeight-1-_j*grid);
                } else if (_i==w) {
                    simulation->Canvas->Rectangle (left-grid/2+_i*grid+1,
                                               simulation->ClientHeight-(_j+1)*grid,
                                               left-grid/2+_i*grid+grid/2,
                                               simulation->ClientHeight-1-_j*grid);
                } else {
                    simulation->Canvas->Rectangle (left-grid/2+1,
                                               simulation->ClientHeight-(_j+2)*grid,
                                               left,
                                               simulation->ClientHeight-1-(_j+1)*grid);
                }
            }
        }
    }
}

void __fastcall TBeadForm::FileNewClick(TObject *Sender)
{
    // Fragen ob speichern
    if (modified && MessageDlg (LANG_STR("Do you want to save your changes?", "Sollen die Änderungen gespeichert werden?"), mtConfirmation,
                            TMsgDlgButtons() << mbYes << mbNo, 0)==mrYes)
    {
        FileSaveClick (Sender);
    }
    // Alles löschen
    undo.Clear();
    field.Clear();
    rapport = 0;
    farbrapp = 0;
    Invalidate();
    color = 1;
    sbColor1->Down = true;
    DefaultColors();
    SetGlyphColors();
    scroll = 0;
    UpdateScrollbar();
    selection = false;
    sbToolPoint->Down = true;
    ToolPoint->Checked = true;
    opendialog->FileName = "*.dbb";
    savedialog->FileName = LANG_STR("unnamed", "unbenannt");
    saved = false;
    modified = false;
    UpdateTitle();
}

void __fastcall TBeadForm::LoadFile (const String& _filename, bool _addtomru)
{
    // Fragen ob speichern
    if (modified && MessageDlg (LANG_STR("Do you want to save your changes?", "Sollen die Änderungen gespeichert werden?"), mtConfirmation,
                            TMsgDlgButtons() << mbYes << mbNo, 0)==mrYes)
    {
        FileSaveClick(this);
    }
    // Datei laden
    try {
        TFileStream* f = new TFileStream(_filename, fmOpenRead|fmShareDenyWrite);
            f->Write ("DB-BEAD/01:\r\n", 13);
        char id[14];
        id[13] = '\0';
        f->Read (id, 13);
        if (String(id)!="DB-BEAD/01:\r\n") {
            ShowMessage (LANG_STR("The file is not a DB-WEAVE pattern file. It cannot be loaded", "Die Datei ist keine DB-BEAD Musterdatei. Sie kann nicht geladen werden."));
            delete f;
            return;
        }
        undo.Clear();
        field.Clear();
        rapport = 0;
        farbrapp = 0;
        field.Load (f);
        f->Read (coltable, sizeof(coltable));
        f->Read (&color, sizeof(color));
        f->Read (&zoomidx, sizeof(zoomidx));
        f->Read (&shift, sizeof(shift));
        f->Read (&scroll, sizeof(scroll));
        bool vis; f->Read (&vis, sizeof(vis)); ViewDraft->Checked = vis;
        f->Read (&vis, sizeof(vis)); ViewNormal->Checked = vis;
        f->Read (&vis, sizeof(vis)); ViewSimulation->Checked = vis;
        switch (color) {
            case 0: sbColor0->Down = true; break;
            case 1: sbColor1->Down = true; break;
            case 2: sbColor2->Down = true; break;
            case 3: sbColor3->Down = true; break;
            case 4: sbColor4->Down = true; break;
            case 5: sbColor5->Down = true; break;
            case 6: sbColor6->Down = true; break;
            case 7: sbColor7->Down = true; break;
            case 8: sbColor8->Down = true; break;
            case 9: sbColor9->Down = true; break;
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
    savedialog->FileName = _filename;
    UpdateTitle();
    FormResize(this);
    Invalidate();
    if (_addtomru) AddToMRU (_filename);
}

void __fastcall TBeadForm::FileOpenClick(TObject *Sender)
{
    if (opendialog->Execute()) {
        LoadFile (opendialog->FileName, true);
    }
}

void __fastcall TBeadForm::FileSaveClick(TObject *Sender)
{
    if (saved) {
        // Einfach abspeichern...
        try {
            TFileStream* f = new TFileStream(savedialog->FileName, fmCreate|fmShareExclusive);
            f->Write ("DB-BEAD/01:\r\n", 13);
            field.Save (f);
            f->Write (coltable, sizeof(coltable)); assert(sizeof(coltable)==sizeof(TColor)*10);
            f->Write (&color, sizeof(color));
            f->Write (&zoomidx, sizeof(zoomidx));
            f->Write (&shift, sizeof(shift));
            f->Write (&scroll, sizeof(scroll));
            bool vis = ViewDraft->Checked; f->Write (&vis, sizeof(vis));
            vis = ViewNormal->Checked; f->Write (&vis, sizeof(vis));
            vis = ViewSimulation->Checked; f->Write (&vis, sizeof(vis));
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

void __fastcall TBeadForm::FileSaveasClick(TObject *Sender)
{
    if (savedialog->Execute()) {
        if (FileExists(savedialog->FileName)) {
            String msg = LANG_STR("The file ", "Die Datei ") + ExtractFileName(savedialog->FileName) +
                         LANG_STR(" already exists. Do you want to overwrite it?", " existiert bereits. Soll sie überschrieben werden?");
            if (MessageDlg (msg, mtConfirmation, TMsgDlgButtons() << mbYes << mbNo, 0)!=mrYes)
                return;
        }
        saved = true;
        FileSaveClick (Sender);
        AddToMRU (savedialog->FileName);
    }
}

void __fastcall TBeadForm::OnShowPrintDialog (TObject* Sender)
{
    ::SetFocus (printdialog->Handle);
}

void __fastcall TBeadForm::OnShowPrintersetupDialog (TObject* Sender)
{
    ::SetFocus (printersetupdialog->Handle);
}

void __fastcall TBeadForm::FilePrintClick(TObject *Sender)
{
    if (Sender!=sbPrint) {
        TNotifyEvent old = printdialog->OnShow;
        printdialog->OnShow = OnShowPrintDialog;
        if (printdialog->Execute()) {
            Cursor = crHourGlass;
            PrintItAll();
            Cursor = crDefault;
        }
        printdialog->OnShow = old;
    } else {
        Cursor = crHourGlass;
        PrintItAll();
        Cursor = crDefault;
    }
}

void __fastcall TBeadForm::FilePrintersetupClick(TObject *Sender)
{
    TNotifyEvent old = printersetupdialog->OnShow;
    printersetupdialog->OnShow = OnShowPrintersetupDialog;
    printersetupdialog->Execute();
    printersetupdialog->OnShow = old;
}

void __fastcall TBeadForm::FileExitClick(TObject *Sender)
{
    if (modified) {
         int r = MessageDlg (LANG_STR("Do you want to save your changes?", "Sollen die Änderungen gespeichert werden?"), mtConfirmation,
                            TMsgDlgButtons() << mbYes << mbNo << mbCancel, 0);
         if (r==mrCancel) return;
         if (r==mrYes) FileSaveClick(Sender);
    }
    Application->Terminate();
}

void __fastcall TBeadForm::PatternWidthClick(TObject *Sender)
{
    int old = field.Width();
    PatternWidthForm->upWidth->Position = field.Width();
    if (PatternWidthForm->ShowModal()==mrOk) {
        undo.Snapshot (field, modified);
        field.SetWidth(PatternWidthForm->upWidth->Position);
        FormResize(Sender);
        Invalidate();
        if (!modified) modified = (old!=field.Width());
        UpdateTitle();
        rapportdirty = true;
    }
}

bool __fastcall TBeadForm::draftMouseToField (int& _i, int& _j)
{
    int i, jj;
    if (_i<draftleft || _i>draftleft+field.Width()*grid) return false;
    i = (_i-draftleft)/grid;
    if (i>=field.Width()) return false;
    jj = (draft->ClientHeight-_j)/grid;
    _i = i;
    _j = jj;
    return true;
}

void __fastcall TBeadForm::CalcLineCoord (int _i1, int _j1, int& _i2, int& _j2)
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

void __fastcall TBeadForm::DraftLinePreview()
{
    if (!sbToolPoint->Down) return;
    if (begin_i==end_i && begin_j==end_j) return;

    int ei = end_i;
    int ej = end_j;
    CalcLineCoord (begin_i, begin_j, ei, ej);

    TPenMode oldmode = draft->Canvas->Pen->Mode;
    draft->Canvas->Pen->Mode = pmNot;
    draft->Canvas->MoveTo (draftleft + begin_i*grid+grid/2, draft->ClientHeight - begin_j*grid - grid/2);
    draft->Canvas->LineTo (draftleft + ei*grid+grid/2, draft->ClientHeight - ej*grid - grid/2);
    draft->Canvas->Pen->Mode = oldmode;
}

void __fastcall TBeadForm::DraftSelectPreview (bool _draw/*=true*/, bool _doit/*=false*/)
{
    if (!sbToolSelect->Down && !_doit) return;
    if (begin_i==end_i && begin_j==end_j) return;

    int i1 = min(begin_i, end_i);
    int i2 = max(begin_i, end_i);
    int j1 = min(begin_j, end_j);
    int j2 = max(begin_j, end_j);

    TColor oldcolor = draft->Canvas->Pen->Color;
    draft->Canvas->Pen->Color = _draw ? clBlack : clDkGray;
    draft->Canvas->MoveTo (draftleft + i1*grid, draft->ClientHeight - j1*grid - 1);
    draft->Canvas->LineTo (draftleft + i1*grid, draft->ClientHeight - (j2+1)*grid - 1);
    draft->Canvas->LineTo (draftleft + (i2+1)*grid, draft->ClientHeight - (j2+1)*grid - 1);
    draft->Canvas->LineTo (draftleft + (i2+1)*grid, draft->ClientHeight - j1*grid - 1);
    draft->Canvas->LineTo (draftleft + i1*grid, draft->ClientHeight - j1*grid - 1);
    draft->Canvas->Pen->Color = oldcolor;
}

void __fastcall TBeadForm::DraftSelectDraw()
{
    if (!selection) return;
    begin_i = sel_i1;
    begin_j = sel_j1;
    end_i = sel_i2;
    end_j = sel_j2;
    DraftSelectPreview (true, true);
}

void __fastcall TBeadForm::DraftSelectClear()
{
    if (!selection) return;
    begin_i = sel_i1;
    begin_j = sel_j1;
    end_i = sel_i2;
    end_j = sel_j2;
    DraftSelectPreview (false, true);
    selection = false;
}

void __fastcall TBeadForm::draftMouseDown(TObject *Sender,
      TMouseButton Button, TShiftState Shift, int X, int Y)
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
        if (sbToolPoint->Down) {
            draft->Canvas->Pen->Color = clBlack;
            draft->Canvas->MoveTo (draftleft+begin_i*grid+1, draft->ClientHeight-begin_j*grid-2);
            draft->Canvas->LineTo (draftleft+begin_i*grid+1, draft->ClientHeight-(begin_j+1)*grid);
            draft->Canvas->LineTo (draftleft+(begin_i+1)*grid-1, draft->ClientHeight-(begin_j+1)*grid);
            draft->Canvas->Pen->Color = clWhite;
            draft->Canvas->MoveTo (draftleft+(begin_i+1)*grid-1, draft->ClientHeight-(begin_j+1)*grid+1);
            draft->Canvas->LineTo (draftleft+(begin_i+1)*grid-1, draft->ClientHeight-begin_j*grid-2);
            draft->Canvas->LineTo (draftleft+begin_i*grid, draft->ClientHeight-begin_j*grid-2);
        }
        DraftLinePreview();
        DraftSelectPreview(true);
    }
}

void __fastcall TBeadForm::draftMouseMove(TObject *Sender,
      TShiftState Shift, int X, int Y)
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

void __fastcall TBeadForm::draftMouseUp(TObject *Sender,
      TMouseButton Button, TShiftState Shift, int X, int Y)
{
    if (dragging && draftMouseToField (X, Y)) {
        DraftLinePreview();
        end_i = X;
        end_j = Y;
        dragging = false;

        if (sbToolPoint->Down) {
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
                        draft->Canvas->Brush->Color = coltable[color];
                        draft->Canvas->Pen->Color = draft->Canvas->Brush->Color;
                        draft->Canvas->Rectangle (draftleft+i*grid+1, draft->ClientHeight-(jj+1)*grid,
                                            draftleft+(i+1)*grid, draft->ClientHeight-jj*grid-1);
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
                        draft->Canvas->Brush->Color = coltable[color];
                        draft->Canvas->Pen->Color = draft->Canvas->Brush->Color;
                        draft->Canvas->Rectangle (draftleft+begin_i*grid+1, draft->ClientHeight-(jj+1)*grid,
                                            draftleft+(begin_i+1)*grid, draft->ClientHeight-jj*grid-1);
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
                        draft->Canvas->Brush->Color = coltable[color];
                        draft->Canvas->Pen->Color = draft->Canvas->Brush->Color;
                        draft->Canvas->Rectangle (draftleft+i*grid+1, draft->ClientHeight-(begin_j+1)*grid,
                                            draftleft+(i+1)*grid, draft->ClientHeight-begin_j*grid-1);
                        UpdateBead (i, begin_j);
                    }
                    modified = true;
                    rapportdirty = true;
                    UpdateTitle();
                }
            }
        } else if (sbToolFill->Down) {
            undo.Snapshot (field, modified);
            FillLine (end_i, end_j);
            modified = true; UpdateTitle();
            rapportdirty = true;
            report->Invalidate();
        } else if (sbToolSniff->Down) {
            color = field.Get(begin_i, begin_j+scroll);
            assert(color>=0 && color<10);
            switch (color) {
                case 0: sbColor0->Down = true; break;
                case 1: sbColor1->Down = true; break;
                case 2: sbColor2->Down = true; break;
                case 3: sbColor3->Down = true; break;
                case 4: sbColor4->Down = true; break;
                case 5: sbColor5->Down = true; break;
                case 6: sbColor6->Down = true; break;
                case 7: sbColor7->Down = true; break;
                case 8: sbColor8->Down = true; break;
                case 9: sbColor9->Down = true; break;
                default: assert(false); break;
            }
        } else if (sbToolSelect->Down) {
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

void __fastcall TBeadForm::FillLine (int _i, int _j)
{
    // Füllen
    //xxx experimentell nach links und rechts
    draft->Canvas->Brush->Color = coltable[color];
    draft->Canvas->Pen->Color = draft->Canvas->Brush->Color;
    char bk = field.Get (_i, _j+scroll);
    signed int i = _i;
    while (i>=0 && field.Get(i, _j+scroll)==bk) {
        field.Set (i, _j+scroll, color);
        draft->Canvas->Rectangle (draftleft+i*grid+1, draft->ClientHeight-(_j+1)*grid,
                            draftleft+(i+1)*grid, draft->ClientHeight-_j*grid-1);
        UpdateBead (i, _j+scroll);
        i--;
    }
    i = begin_i+1;
    while (i<field.Width() && field.Get(i, _j+scroll)==bk) {
        field.Set (i, _j+scroll, color);
        draft->Canvas->Rectangle (draftleft+i*grid+1, draft->ClientHeight-(_j+1)*grid,
                            draftleft+(i+1)*grid, draft->ClientHeight-_j*grid-1);
        UpdateBead (i, _j+scroll);
        i++;
    }
}

void __fastcall TBeadForm::SetPoint (int _i, int _j)
{
    undo.Snapshot (field, modified);
    char s = field.Get(_i, _j+scroll);
    if (s==color) {
        field.Set (_i, _j+scroll, 0);
        if (draft->Visible) {
            draft->Canvas->Brush->Color = clBtnFace;
            draft->Canvas->Pen->Color = draft->Canvas->Brush->Color;
            draft->Canvas->Rectangle (draftleft+_i*grid+1, draft->ClientHeight-(_j+1)*grid,
                                draftleft+(_i+1)*grid, draft->ClientHeight-_j*grid-1);
        }
    }else {
        field.Set(_i, _j+scroll, color);
        if (draft->Visible) {
            draft->Canvas->Brush->Color = coltable[color];
            draft->Canvas->Pen->Color = draft->Canvas->Brush->Color;
            draft->Canvas->Rectangle (draftleft+_i*grid+1, draft->ClientHeight-(_j+1)*grid,
                                draftleft+(_i+1)*grid, draft->ClientHeight-_j*grid-1);
        }
    }
    UpdateBead (_i, _j);
    modified = true;
    rapportdirty = true;
    UpdateTitle();
}

void __fastcall TBeadForm::EditUndoClick(TObject *Sender)
{
    undo.Undo(field);
    modified = undo.Modified(); UpdateTitle();
    Invalidate();
    rapportdirty = true;
}

void __fastcall TBeadForm::EditRedoClick(TObject *Sender)
{
    undo.Redo(field);
    modified = undo.Modified(); UpdateTitle();
    Invalidate();
    rapportdirty = true;
}

void __fastcall TBeadForm::ViewZoominClick(TObject *Sender)
{
    if (zoomidx<4) zoomidx++;
    grid = zoomtable[zoomidx];
    FormResize(Sender);
    Invalidate();
    UpdateScrollbar();
}

void __fastcall TBeadForm::ViewZoomnormalClick(TObject *Sender)
{
    if (zoomidx==1) return;
    zoomidx = 2;
    grid = zoomtable[zoomidx];
    FormResize(Sender);
    Invalidate();
    UpdateScrollbar();
}

void __fastcall TBeadForm::ViewZoomoutClick(TObject *Sender)
{
    if (zoomidx>0) zoomidx--;
    grid = zoomtable[zoomidx];
    FormResize(Sender);
    Invalidate();
    UpdateScrollbar();
}

void __fastcall TBeadForm::ViewDraftClick(TObject *Sender)
{
    ViewDraft->Checked = !ViewDraft->Checked;
    draft->Visible = ViewDraft->Checked;
    laDraft->Visible = draft->Visible;
    FormResize(this);
}

void __fastcall TBeadForm::ViewNormalClick(TObject *Sender)
{
    ViewNormal->Checked = !ViewNormal->Checked;
    normal->Visible = ViewNormal->Checked;
    laNormal->Visible = normal->Visible;
    FormResize(this);
}

void __fastcall TBeadForm::ViewSimulationClick(TObject *Sender)
{
    ViewSimulation->Checked = !ViewSimulation->Checked;
    simulation->Visible = ViewSimulation->Checked;
    laSimulation->Visible = simulation->Visible;
    FormResize(this);
}

void __fastcall TBeadForm::ViewReportClick(TObject *Sender)
{
    ViewReport->Checked = !ViewReport->Checked;
    report->Visible = ViewReport->Checked;
    laReport->Visible = report->Visible;
    FormResize(this);
}

void __fastcall TBeadForm::FormKeyUp(TObject *Sender, WORD &Key,
      TShiftState Shift)
{
    if (Key==VK_F5) Invalidate();
    else if (Key=='1' && Shift.Contains(ssCtrl) && !Shift.Contains(ssAlt)) { sbToolPoint->Down = true; ToolPoint->Checked = true; }
    else if (Key=='2' && Shift.Contains(ssCtrl) && !Shift.Contains(ssAlt)) { sbToolSelect->Down = true; ToolSelect->Checked = true; }
    else if (Key=='3' && Shift.Contains(ssCtrl) && !Shift.Contains(ssAlt)) { sbToolFill->Down = true; ToolFill->Checked = true; }
    else if (Key=='4' && Shift.Contains(ssCtrl) && !Shift.Contains(ssAlt)) { sbToolSniff->Down = true; ToolSniff->Checked = true; }
    else if (Key>='0' && Key<='9') {
        color = Key-'0';
        switch (color) {
            case 0: sbColor0->Down = true; break;
            case 1: sbColor1->Down = true; break;
            case 2: sbColor2->Down = true; break;
            case 3: sbColor3->Down = true; break;
            case 4: sbColor4->Down = true; break;
            case 5: sbColor5->Down = true; break;
            case 6: sbColor6->Down = true; break;
            case 7: sbColor7->Down = true; break;
            case 8: sbColor8->Down = true; break;
            case 9: sbColor9->Down = true; break;
            default: assert(false); break;
        }
    } else if (Key==VK_SPACE) {
        sbToolPoint->Down = true;
        ToolPoint->Checked = true;
    } else if (Key==VK_ESCAPE) {
        righttimer->Enabled = false;
        lefttimer->Enabled = false;
    }
}

void __fastcall TBeadForm::RotateLeft()
{
    shift = (shift-1+field.Width()) % field.Width();
    modified = true; UpdateTitle();
    simulation->Invalidate();
}

void __fastcall TBeadForm::RotateRight()
{
    shift = (shift+1) % field.Width();
    modified = true; UpdateTitle();
    simulation->Invalidate();
}

void __fastcall TBeadForm::ColorClick(TObject *Sender)
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

void __fastcall TBeadForm::ColorDblClick(TObject *Sender)
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
    colordialog->Color = coltable[c];
    if (colordialog->Execute()) {
        undo.Snapshot (field, modified);
        coltable[c] = colordialog->Color;
        modified = true; UpdateTitle();
        Invalidate();
        SetGlyphColors();
    }
}

void __fastcall TBeadForm::coolbarResize(TObject *Sender)
{
    FormResize(Sender);
}

void __fastcall TBeadForm::scrollbarScroll(TObject *Sender,
      TScrollCode ScrollCode, int &ScrollPos)
{
    int oldscroll = scroll;
    if (ScrollPos > scrollbar->Max - scrollbar->PageSize) ScrollPos = scrollbar->Max - scrollbar->PageSize;
    scroll = scrollbar->Max - scrollbar->PageSize - ScrollPos;
    if (oldscroll!=scroll) Invalidate();
}

void __fastcall TBeadForm::IdleHandler (TObject* Sender, bool& Done)
{
    // Menü- und Toolbar enablen/disablen
    EditCopy->Enabled = selection;
    sbCopy->Enabled = selection;
    EditUndo->Enabled = undo.CanUndo();
    EditRedo->Enabled = undo.CanRedo();
    sbUndo->Enabled = undo.CanUndo();
    sbRedo->Enabled = undo.CanRedo();

    // Rapport berechnen und zeichnen
    if (rapportdirty) {
        // Alten Rapport löschen
#if(0)
        if (rapport!=0) {
            draft->Canvas->Pen->Color = draft->Color;
            draft->Canvas->MoveTo (0, draft->ClientHeight - (rapport-scroll)*grid - 1);
            draft->Canvas->LineTo (draftleft-6, draft->ClientHeight - (rapport-scroll)*grid - 1);
            draft->Canvas->Pen->Color = clDkGray;
            draft->Canvas->MoveTo (0, draft->ClientHeight - (rapport-scroll)*grid - 1);
            draft->Canvas->LineTo (1, draft->ClientHeight - (rapport-scroll)*grid - 1);
            if (rapport%10==9) {
                draft->Canvas->Brush->Color = draft->Color;
                draft->Canvas->TextOutA (6, draft->ClientHeight - (rapport+1-scroll)*grid + 1, IntToStr(rapport+1));
            }
            if (rapport%10==0) {
                draft->Canvas->Pen->Color = clDkGray;
                draft->Canvas->MoveTo (0, draft->ClientHeight - (rapport-scroll)*grid - 1);
                draft->Canvas->LineTo (draftleft-6, draft->ClientHeight - (rapport-scroll)*grid - 1);
            }
        }
#endif

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
            report->Invalidate();
            return;
        }
        rapport = last+1;
        for (int j=1; j<=last; j++) {
            if (Equal(0,j)) {
                bool ok = true;
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
                bool ok = true;
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

        // Neu zeichnen
#if(0)
        if (rapport!=0) {
            draft->Canvas->Pen->Color = clRed;
            draft->Canvas->MoveTo (0, draft->ClientHeight - (rapport-scroll)*grid - 1);
            draft->Canvas->LineTo (draftleft-6, draft->ClientHeight - (rapport-scroll)*grid - 1);
        }
#endif
        report->Invalidate();
        rapportdirty = false;
    }

    // Vorsorgliches Undo
    undo.PreSnapshot (field, modified);
}

bool __fastcall TBeadForm::Equal (int _i, int _j)
{
    for (int k=0; k<field.Width(); k++) {
        if (field.Get(k,_i)!=field.Get(k,_j))
            return false;
    }
    return true;
}

void __fastcall TBeadForm::FormCreate(TObject *Sender)
{
    Application->OnIdle = IdleHandler;
}

void __fastcall TBeadForm::ToolPointClick(TObject *Sender)
{
    ToolPoint->Checked = true;
    sbToolPoint->Down = true;
    DraftSelectClear();
}

void __fastcall TBeadForm::ToolSelectClick(TObject *Sender)
{
    ToolSelect->Checked = true;
    sbToolSelect->Down = true;
}

void __fastcall TBeadForm::ToolFillClick(TObject *Sender)
{
    ToolFill->Checked = true;
    sbToolFill->Down = true;
    DraftSelectClear();
}

void __fastcall TBeadForm::ToolSniffClick(TObject *Sender)
{
    ToolSniff->Checked = true;
    sbToolSniff->Down = true;
    DraftSelectClear();
}

void __fastcall TBeadForm::sbToolPointClick(TObject *Sender)
{
    ToolPoint->Checked = true;
    DraftSelectClear();
}

void __fastcall TBeadForm::sbToolFillClick(TObject *Sender)
{
    ToolFill->Checked = true;
    DraftSelectClear();
}

void __fastcall TBeadForm::sbToolSniffClick(TObject *Sender)
{
    ToolSniff->Checked = true;
    DraftSelectClear();
}

void __fastcall TBeadForm::sbToolSelectClick(TObject *Sender)
{
    ToolSelect->Checked = true;
}

bool __fastcall TBeadForm::normalMouseToField (int& _i, int& _j)
{
    int i;
    int jj = (normal->ClientHeight-_j)/grid;
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

void __fastcall TBeadForm::normalMouseUp(TObject *Sender,
      TMouseButton Button, TShiftState Shift, int X, int Y)
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

        // Feld setzen und Darstellung nachführen
        int j = idx / field.Width();
        int i = idx % field.Width();
        SetPoint (i, j-scroll);
    }
}

void __fastcall TBeadForm::InfoAboutClick(TObject *Sender)
{
    Aboutbox->ShowModal();
}

void __fastcall TBeadForm::reportPaint(TObject *Sender)
{
    int x1 = 12;
    int x2 = x1 + 100;
    int y = 0;
    int dy = 15;

    // Mustername
    report->Canvas->Pen->Color = clBlack;
    report->Canvas->TextOut (x1, y, LANG_STR("Pattern:", "Muster:"));
    report->Canvas->TextOut (x2, y, ExtractFileName(savedialog->FileName));
    y += dy;
    // Umfang
    report->Canvas->TextOut (x1, y, LANG_STR("Circumference:", "Umfang:"));
    report->Canvas->TextOut (x2, y, IntToStr(field.Width()));
    y += dy;
    // Musterrapport
#if(0)
    report->Canvas->TextOut (x1, y, LANG_STR("Repeat of pattern:", "Musterrapport:"));
    report->Canvas->TextOut (x2, y, IntToStr(rapport)+LANG_STR(" turns", " Umgänge"));
    y += dy;
#endif
    // Farbrapport
    report->Canvas->TextOut (x1, y, LANG_STR("repeat of colors:", "Farbrapport:"));
    report->Canvas->TextOut (x2, y, IntToStr(farbrapp)+LANG_STR(" beads", " Perlen"));
    y += dy;
    // Farben
    // Fädelliste...
    if (farbrapp>0) {
        report->Canvas->TextOut (x1, y, LANG_STR("List of beads", "Fädelliste"));
        y += dy;
        int ystart = y;
        char col = field.Get(farbrapp-1);
        int  count = 1;
        for (signed int i=farbrapp-2; i>=0; i--) {
            if (field.Get(i)==col) {
                count++;
            } else {
                if (col!=0) {
                    report->Canvas->Brush->Color = coltable[col];
                    report->Canvas->Pen->Color = report->Color;
                } else {
                    report->Canvas->Brush->Color = report->Color;
                    report->Canvas->Pen->Color = clDkGray;
                }
                report->Canvas->Rectangle (x1, y, x1+dy, y+dy);
                report->Canvas->Pen->Color = clBlack;
                report->Canvas->Brush->Color = report->Color;
                report->Canvas->TextOut (x1+dy+3, y, IntToStr(count));
                y += dy;
                col = field.Get(i);
                count = 1;
            }
            if (y>=report->ClientHeight-10) {
                x1 += dy + 24;
                y = ystart;
            }
        }
        if (y<report->ClientHeight-3) {
            report->Canvas->Brush->Color = coltable[col];
            report->Canvas->Pen->Color = report->Color;
            report->Canvas->Rectangle (x1, y, x1+dy, y+dy);
            report->Canvas->Pen->Color = clBlack;
            report->Canvas->Brush->Color = report->Color;
            report->Canvas->TextOut (x1+dy+3, y, IntToStr(count));
        }
    }
}

void __fastcall TBeadForm::lefttimerTimer(TObject *Sender)
{
    RotateLeft();
    Application->ProcessMessages();
}

void __fastcall TBeadForm::righttimerTimer(TObject *Sender)
{
    RotateRight();
    Application->ProcessMessages();
}

void __fastcall TBeadForm::sbRotaterightMouseDown(TObject *Sender,
      TMouseButton Button, TShiftState Shift, int X, int Y)
{
    RotateRight();
    Application->ProcessMessages();
    righttimer->Enabled = true;
}

void __fastcall TBeadForm::sbRotaterightMouseUp(TObject *Sender,
      TMouseButton Button, TShiftState Shift, int X, int Y)
{
    righttimer->Enabled = false;
}

void __fastcall TBeadForm::sbRotateleftMouseDown(TObject *Sender,
      TMouseButton Button, TShiftState Shift, int X, int Y)
{
    RotateLeft();
    Application->ProcessMessages();
    lefttimer->Enabled = true;
}

void __fastcall TBeadForm::sbRotateleftMouseUp(TObject *Sender,
      TMouseButton Button, TShiftState Shift, int X, int Y)
{
    lefttimer->Enabled = false;
}

void __fastcall TBeadForm::FormKeyDown(TObject *Sender, WORD &Key,
      TShiftState Shift)
{
    if (Key==VK_RIGHT) RotateRight();
    else if (Key==VK_LEFT) RotateLeft();
}

void __fastcall TBeadForm::FormCloseQuery(TObject *Sender, bool &CanClose)
{
    if (modified) {
         int r = MessageDlg (LANG_STR("Do you want to save your changes?", "Sollen die Änderungen gespeichert werden?"), mtConfirmation,
                            TMsgDlgButtons() << mbYes << mbNo << mbCancel, 0);
         if (r==mrCancel) { CanClose = false; return; }
         if (r==mrYes) FileSaveClick(Sender);
    }
    CanClose = true;
}

void __fastcall TBeadForm::EditCopyClick(TObject *Sender)
{
    if (CopyForm->ShowModal()==mrOk) {
        undo.Snapshot (field, modified);
        // Aktuelle Daten in Buffer kopieren
        sel_buff.CopyFrom (field);
        // Daten vervielfältigen
        ArrangeIncreasing (sel_i1, sel_i2);
        ArrangeIncreasing (sel_j1, sel_j2);
        for (int i=sel_i1; i<=sel_i2; i++) {
            for (int j=sel_j1; j<=sel_j2; j++) {
                char c = sel_buff.Get(i, j);
                if (c==0) continue;
                int idx = GetIndex(i, j);
                // Diesen Punkt x-mal vervielfältigen
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

int __fastcall TBeadForm::GetCopyOffset(TCopyForm* form)
{
    return form->upVert->Position*field.Width() + form->upHorz->Position;
}

void __fastcall TBeadForm::ArrangeIncreasing (int& i1, int& i2)
{
    if (i1>i2) {
        int temp = i1;
        i1 = i2;
        i2 = temp;
    }
}

int __fastcall TBeadForm::GetIndex (int i, int j)
{
    return j*field.Width() + i;
}

int __fastcall TBeadForm::GetNumberOfCopies (TCopyForm* form)
{
    return form->upCopies->Position;
}

void __fastcall TBeadForm::EditInsertlineClick(TObject *Sender)
{
    undo.Snapshot(field, modified);
    field.InsertLine();
    rapportdirty = true;
    modified = true; UpdateTitle();
    Invalidate();
}

void __fastcall TBeadForm::EditDeletelineClick(TObject *Sender)
{
    undo.Snapshot(field, modified);
    field.DeleteLine();
    rapportdirty = true;
    modified = true; UpdateTitle();
    Invalidate();
}

void __fastcall TBeadForm::setAppTitle()
{
    UpdateTitle();
}

void __fastcall TBeadForm::UpdateTitle()
{
    String c = APP_TITLE;
    c += " - ";
    if (saved) c += ExtractFileName(savedialog->FileName);
    else c += DATEI_UNBENANNT;
    if (modified) c += "*";
    Caption = c;
}

void __fastcall TBeadForm::LanguageEnglishClick(TObject *Sender)
{
    SwitchLanguage(EN);
    LanguageEnglish->Checked = true;
    Settings settings;
    settings.SetCategory ("Environment");
    settings.Save("Language", 0);
}

void __fastcall TBeadForm::LanguageGermanClick(TObject *Sender)
{
    SwitchLanguage(GE);
    LanguageGerman->Checked = true;
    Settings settings;
    settings.SetCategory ("Environment");
    settings.Save("Language", 1);
}

