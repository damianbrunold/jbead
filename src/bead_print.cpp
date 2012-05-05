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

#define MM2PRx(x) (x*10*sx/254)
#define MM2PRy(y) (y*10*sy/254)

void __fastcall TBeadForm::PrintItAll()
{
    Printer()->BeginDoc();
    String title = APP_TITLE;
    title += " - " + ExtractFileName(savedialog->FileName);
    Printer()->Title = title;
    TCanvas* canvas = Printer()->Canvas;

    int sx = GetDeviceCaps(Printer()->Handle, LOGPIXELSX);
    int sy = GetDeviceCaps(Printer()->Handle, LOGPIXELSY);

    int gx = (15+zoomidx*5)*sx/254;
    int gy = (15+zoomidx*5)*sy/254;

    int draftleft, normalleft, simulationleft, reportleft;
    int reportcols;

    int m = MM2PRx(10);
    if (draft->Visible) {
        draftleft = m;
        m += MM2PRx(13) + field.Width()*gx + MM2PRx(7);
    }

    if (normal->Visible) {
        normalleft = m;
        m += MM2PRx(7) + (field.Width()+1)*gx;
    }

    if (simulation->Visible) {
        simulationleft = m;
        m += MM2PRx(7) + (field.Width()/2+1)*gx;
    }

    if (report->Visible) {
        reportleft = m;
        reportcols = (Printer()->PageWidth - m - 10) / (MM2PRx(5) + MM2PRx(8));
    }

    int h = Printer()->PageHeight - MM2PRy(10);

    ////////////////////////////////////////
    //
    //   Draft
    //
    ////////////////////////////////////////

    // Grid
    canvas->Pen->Color = clBlack;
    int left = draftleft+MM2PRx(13);
    if (left<0) left=0;
    int maxj = min(field.Height(), (h-MM2PRy(10))/gy);
    for (int i=0; i<field.Width()+1; i++) {
        canvas->MoveTo(left+i*gx, h-(maxj)*gy);
        canvas->LineTo(left+i*gx, h-1);
    }
    for (int j=0; j<=maxj; j++) {
        canvas->MoveTo(left, h-1-j*gy);
        canvas->LineTo(left+field.Width()*gx, h-1-j*gy);
    }

    // Daten
    for (int i=0; i<field.Width(); i++)
        for (int j=0; j<maxj; j++) {
            char c = field.Get (i, j);
            assert(c>=0 && c<=9);
            if (c>0) {
                canvas->Brush->Color = coltable[c];
                canvas->Pen->Color = canvas->Brush->Color;
                canvas->Rectangle (left+i*gx+1, h-(j+1)*gy,
                                    left+(i+1)*gx, h-1-j*gy);
            }
        }
    canvas->Brush->Color = clWhite;

    // Zehnermarkierungen
    canvas->Pen->Color = clBlack;
    for (int j=0; j<maxj; j++) {
        if ((j%10)==0) {
            canvas->MoveTo (draftleft, h - j*gy - 1);
            canvas->LineTo (left-MM2PRx(3), h - j*gy - 1);
            canvas->TextOutA (draftleft, h - j*gy + MM2PRy(1), IntToStr(j));
        }
    }

    // Rapportmarkierung
#if(0)
    if (rapport!=0) {
        canvas->Pen->Color = clRed;
        canvas->MoveTo (draftleft, h - (rapport)*gx - 1);
        canvas->LineTo (left-MM2PRx(3), h - (rapport)*gx - 1);
    }
#endif

    ////////////////////////////////////////
    //
    //   Korrigiert (normal)
    //
    ////////////////////////////////////////

    // Grid
    canvas->Pen->Color = clBlack;
    left = normalleft+gx/2;
    if (left<0) left=gx/2;
    maxj = min(field.Height(), (h-MM2PRy(10))/gy);
    for (int i=0; i<field.Width()+1; i++) {
        for (int jj=0; jj<maxj; jj+=2) {
            canvas->MoveTo(left+i*gx, h-(jj+1)*gy);
            canvas->LineTo(left+i*gx, h-jj*gy);
        }
    }
    for (int i=0; i<=field.Width()+1; i++) {
        for (int jj=1; jj<maxj; jj+=2) {
            canvas->MoveTo(left+i*gx-gx/2, h-(jj+1)*gy);
            canvas->LineTo(left+i*gx-gx/2, h-jj*gy);
        }
    }
    canvas->MoveTo(left, h-1);
    canvas->LineTo(left+field.Width()*gx+1, h-1);
    for (int jj=1; jj<=maxj; jj++) {
        canvas->MoveTo(left-gx/2, h-1-jj*gy);
        canvas->LineTo(left+field.Width()*gx+gx/2+1, h-1-jj*gy);
    }

    // Daten
    for (int i=0; i<field.Width(); i++)
        for (int jj=0; jj<maxj; jj++) {
            char c = field.Get (i, jj+scroll);
            assert(c>=0 && c<=9);
            if (c==0) continue;
            canvas->Brush->Color = coltable[c];
            canvas->Pen->Color = canvas->Brush->Color;
            int ii = i;
            int j1 = jj;
            CorrectCoordinates (ii, j1);
            if (j1%2==0) {
                canvas->Rectangle (left+ii*gx+1,
                                           h-(j1+1)*gy,
                                           left+(ii+1)*gx,
                                           h-1-j1*gy);
            } else {
                canvas->Rectangle (left-gx/2+ii*gx+1,
                                           h-(j1+1)*gy,
                                           left-gx/2+(ii+1)*gx,
                                           h-1-j1*gy);
            }
        }
    canvas->Brush->Color = clWhite;


    ////////////////////////////////////////
    //
    //   Simulation
    //
    ////////////////////////////////////////

    // Grid
    canvas->Pen->Color = clBlack;
    left = simulationleft+gx/2;
    if (left<0) left=gx/2;
    maxj = min(field.Height(), (h-MM2PRy(10))/gy);
    int w = field.Width()/2;
    for (int j=0; j<maxj; j+=2) {
        for (int i=0; i<w+1; i++) {
            canvas->MoveTo(left+i*gx, h-(j+1)*gy);
            canvas->LineTo(left+i*gx, h-j*gy);
        }
        if (j>0 || scroll>0) {
            canvas->MoveTo (left-gx/2, h-(j+1)*gy);
            canvas->LineTo (left-gx/2, h-j*gy);
        }
    }
    for (int j=1; j<maxj; j+=2) {
        for (int i=0; i<w+1; i++) {
            canvas->MoveTo(left+i*gx-gx/2, h-(j+1)*gy);
            canvas->LineTo(left+i*gx-gx/2, h-j*gy);
        }
        canvas->MoveTo(left+w*gx, h-(j+1)*gy);
        canvas->LineTo(left+w*gx, h-j*gy);
    }
    canvas->MoveTo(left, h-1);
    canvas->LineTo(left+w*gx+1, h-1);
    for (int j=1; j<=maxj; j++) {
        canvas->MoveTo(left-gx/2, h-1-j*gy);
        canvas->LineTo(left+w*gx+1, h-1-j*gy);
    }

    // Daten
    for (int i=0; i<field.Width(); i++)
        for (int j=0; j<maxj; j++) {
            char c = field.Get (i, j+scroll);
            assert(c>=0 && c<=9);
            if (c==0) continue;
            canvas->Brush->Color = coltable[c];
            canvas->Pen->Color = canvas->Brush->Color;
            int ii = i;
            int jj = j;
            CorrectCoordinates (ii, jj);
            if (ii>w && ii!=field.Width()) continue;
            if (jj%2==0) {
                if (ii==w) continue;
                canvas->Rectangle (left+ii*gx+1,
                                           h-(jj+1)*gy,
                                           left+(ii+1)*gx,
                                           h-1-jj*gy);
            } else {
                if (ii!=field.Width() && ii!=w) {
                    canvas->Rectangle (left-gx/2+ii*gx+1,
                                               h-(jj+1)*gy,
                                               left-gx/2+(ii+1)*gx,
                                               h-1-jj*gy);
                } else if (ii==w) {
                    canvas->Rectangle (left-gx/2+ii*gx+1,
                                               h-(jj+1)*gy,
                                               left-gx/2+ii*gx+gx/2,
                                               h-1-jj*gy);
                } else {
                    canvas->Rectangle (left-gx/2+1,
                                               h-(jj+2)*gy,
                                               left,
                                               h-1-(jj+1)*gy-1);
                }
            }
        }
    canvas->Brush->Color = clWhite;



    ////////////////////////////////////////
    //
    //   Auswertung
    //
    ////////////////////////////////////////

    int x1 = reportleft;
    int x2 = reportleft + MM2PRx(30);
    int y = MM2PRy(10);
    int dy = MM2PRy(5);
    int dx = MM2PRx(5);

    // Mustername
    canvas->Pen->Color = clBlack;
    canvas->TextOut (x1, y, LANG_STR("Pattern:", "Muster:"));
    canvas->TextOut (x2, y, ExtractFileName(savedialog->FileName));
    y += dy;
    // Umfang
    canvas->TextOut (x1, y, LANG_STR("Circumference:", "Umfang:"));
    canvas->TextOut (x2, y, IntToStr(field.Width()));
    y += dy;
    // Musterrapport
#if(0)
    canvas->TextOut (x1, y, LANG_STR("Repeat of pattern:", "Musterrapport:"));
    canvas->TextOut (x2, y, IntToStr(rapport) + LANG_STR(" turns", " Umgänge"));
    y += dy;
#endif
    // Farbrapport
    canvas->TextOut (x1, y, LANG_STR("Repeat of colors:", "Farbrapport:"));
    canvas->TextOut (x2, y, IntToStr(farbrapp) + LANG_STR(" beads", " Perlen"));
    y += dy;
    // Fädelliste...
    if (farbrapp>0) {
        int page = 1;
        int column = 0;
        canvas->TextOut (x1, y, LANG_STR("List of beads", "Fädelliste"));
        y += dy;
        int ystart = y;
        char col = field.Get(farbrapp-1);
        int  count = 1;
        for (signed int i=farbrapp-2; i>=0; i--) {
            if (field.Get(i)==col) {
                count++;
            } else {
                if (col!=0) {
                    canvas->Brush->Color = coltable[col];
                    canvas->Pen->Color = clWhite;
                } else {
                    canvas->Brush->Color = clWhite;
                    canvas->Pen->Color = clBlack;
                }
                canvas->Rectangle (x1, y, x1+dx-MM2PRx(1), y+dy-MM2PRy(1));
                canvas->Pen->Color = clBlack;
                canvas->Brush->Color = clWhite;
                canvas->TextOut (x1+dx+3, y, IntToStr(count));
                y += dy;
                col = field.Get(i);
                count = 1;
            }
            if (y>=Printer()->PageHeight-MM2PRy(10)) {
                x1 += dx + MM2PRx(8);
                y = ystart;
                column++;
                if (column>=reportcols) { // neue Seite und weiter...
                    Printer()->NewPage();
                    x1 = draftleft;
                    x2 = draftleft + MM2PRx(30);
                    y = MM2PRy(10);
                    reportcols = (Printer()->PageWidth - draftleft - 10) / (MM2PRx(5) + MM2PRx(8));
                    column = 0;
                    page++;
                    canvas->Pen->Color = clBlack;
                    canvas->TextOut (x1, y, String(LANG_STR("Pattern ", "Muster "))+ExtractFileName(savedialog->FileName) + " - " + LANG_STR("page ", "Seite ") + IntToStr(page));
                    y += dy;
                    ystart = y;
                }
            }
        }
        if (y<Printer()->PageHeight-MM2PRy(10)) {
            if (col!=0) {
                canvas->Brush->Color = coltable[col];
                canvas->Pen->Color = clWhite;
            } else {
                canvas->Brush->Color = clWhite;
                canvas->Pen->Color = clBlack;
            }
            canvas->Rectangle (x1, y, x1+dx-MM2PRx(1), y+dy-MM2PRy(1));
            canvas->Pen->Color = clBlack;
            canvas->Brush->Color = clWhite;
            canvas->TextOut (x1+dx+3, y, IntToStr(count));
        }
    }

g_exit:
    Printer()->EndDoc();
}

