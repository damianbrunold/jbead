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
#pragma hdrstop

#include "patternwidth_form.h"
#include "language.h"

#pragma package(smart_init)
#pragma resource "*.dfm"
TPatternWidthForm *PatternWidthForm;

__fastcall TPatternWidthForm::TPatternWidthForm(TComponent* Owner)
    : TForm(Owner)
{
}

void __fastcall TPatternWidthForm::reloadLanguage()
{
    LANG_C_H(this, EN, "Width of pattern", "")
    LANG_C_H(this, GE, "Musterbreite", "")
    LANG_C_H(labelDescription, EN, "The width of pattern is equivalent to the circumference of the rope", "")
    LANG_C_H(labelWidth, GE, "Die Musterbreite entspricht dem Umfang der Kette", "")
    LANG_C_H(labelWidth, EN, "&Width of pattern:", "")
    LANG_C_H(labelWidth, GE, "&Musterbreite:", "")
    LANG_C_H(bOk, EN, "OK", "")
    LANG_C_H(bOk, GE, "OK", "")
    LANG_C_H(bCancel, EN, "Cancel", "")
    LANG_C_H(bCancel, GE, "Abbrechen", "")
}


void __fastcall TPatternWidthForm::FormShow(TObject *Sender)
{
    reloadLanguage();
}

