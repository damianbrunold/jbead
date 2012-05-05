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

#ifndef settingsH
#define settingsH

#include <vcl\registry.hpp>

class Settings
{
protected:
    AnsiString category;
    TRegistry* registry;

public:
    __fastcall Settings();
    virtual __fastcall ~Settings();

    void SetCategory (const AnsiString& _category) { category = _category; }
    AnsiString Category() const { return category; }

    int __fastcall Load (const AnsiString& _name, int _default=0);
    AnsiString __fastcall Load (const AnsiString& _name, const AnsiString& _default="");

    void __fastcall Save (const AnsiString& _name, int _value);
    void __fastcall Save (const AnsiString& _name, const AnsiString& _value);
};

#endif

