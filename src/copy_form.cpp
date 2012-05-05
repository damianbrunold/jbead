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

#include "copy_form.h"
#include "language.h"

#pragma package(smart_init)
#pragma resource "*.dfm"
TCopyForm *CopyForm;

__fastcall TCopyForm::TCopyForm(TComponent* Owner)
    : TForm(Owner)
{
}

void __fastcall TCopyForm::reloadLanguage()
{
    LANG_C_H(this, EN, "Arrangement", "")
    LANG_C_H(this, GE, "Anordnen", "")
    LANG_C_H(lCopies, EN, "&Number of copies:", "")
    LANG_C_H(lCopies, GE, "&Anzahl Kopien:", "")
    LANG_C_H(lHorz, EN, "&Horizontal displacement:", "")
    LANG_C_H(lHorz, GE, "&Horizontaler Versatz:", "")
    LANG_C_H(lVert, EN, "&Vertical displacement:", "")
    LANG_C_H(lVert, GE, "&Vertikaler Versatz:", "")
    LANG_C_H(bOK, EN, "OK", "")
    LANG_C_H(bOK, GE, "OK", "")
    LANG_C_H(bCancel, EN, "Cancel", "")
    LANG_C_H(bCancel, GE, "Abbrechen", "")
}

void __fastcall TCopyForm::FormShow(TObject *Sender)
{
    reloadLanguage();
}

