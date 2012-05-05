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

#include "language.h"

USERES("dbbead.res");
USEFORM("bead_form.cpp", BeadForm);
USEFORM("patternwidth_form.cpp", PatternWidthForm);
USEUNIT("assert.cpp");
USEUNIT("mru.cpp");
USEFORM("aboutbox_form.cpp", Aboutbox);
USEUNIT("bead_print.cpp");
USEFORM("copy_form.cpp", CopyForm);
USEUNIT("lang_main.cpp");
USEUNIT("language.cpp");
USEUNIT("settings.cpp");
//---------------------------------------------------------------------------
WINAPI WinMain(HINSTANCE, HINSTANCE, LPSTR, int)
{
    try
    {
        Application->Initialize();
        Application->Title = APP_TITLE;
        Application->CreateForm(__classid(TBeadForm), &BeadForm);
        Application->CreateForm(__classid(TPatternWidthForm), &PatternWidthForm);
        Application->CreateForm(__classid(TAboutbox), &Aboutbox);
        Application->CreateForm(__classid(TCopyForm), &CopyForm);
        Application->Run();
    }
    catch (Exception &exception)
    {
        Application->ShowException(&exception);
    }
    return 0;
}

