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

#include "assert.h"
#include "settings.h"

#pragma package(smart_init)

#define DBB_REGBASE      "Software\\Brunold Software\\DB-BEAD\\"

__fastcall Settings::Settings()
{
    category = "General";
    try {
        registry = new TRegistry;
        registry->RootKey = HKEY_CURRENT_USER;
    } catch (...) {
        registry = NULL;
    }
}

__fastcall Settings::~Settings()
{
    delete registry;
}

int __fastcall Settings::Load (const AnsiString& _name, int _default/*=0*/)
{
    int value = _default;
    try {
        AnsiString key = AnsiString(DBB_REGBASE) + category;
        if (registry->OpenKey (key, false)) {
            if (registry->ValueExists (_name))
                value = registry->ReadInteger (_name);
        }
    } catch(...) {
    }
    registry->CloseKey();
    return value;
}

AnsiString __fastcall Settings::Load (const AnsiString& _name, const AnsiString& _default/*=""*/)
{
    AnsiString value = _default;
    try {
        AnsiString key = AnsiString(DBB_REGBASE) + category;
        if (registry->OpenKey (key, false)) {
            if (registry->ValueExists (_name))
                value = registry->ReadString (_name);
        }
    } catch(...) {
    }
    registry->CloseKey();
    return value;
}

void __fastcall Settings::Save (const AnsiString& _name, int _value)
{
    try {
        AnsiString key = AnsiString(DBB_REGBASE) + category;
        if (registry->OpenKey (key, true)) {
            registry->WriteInteger (_name, _value);
        }
    } catch(...) {
    }
    registry->CloseKey();
}

void __fastcall Settings::Save (const AnsiString& _name, const AnsiString& _value)
{
    try {
        AnsiString key = AnsiString(DBB_REGBASE) + category;
        if (registry->OpenKey (key, true)) {
            registry->WriteString (_name, _value);
        }
    } catch(...) {
    }
    registry->CloseKey();
}

