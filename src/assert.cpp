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

#include <vcl\vcl.h>
#pragma hdrstop

#include "assert.h"

// Falls TRACELEVEL nicht definiert ist, wird es so gesetzt, dass
// keine traces ausgegeben werden.
#ifndef TRACELEVEL
#define TRACELEVEL 0
#endif

int g_tracelevel = TRACELEVEL;

#ifndef NDEBUG
void _db_assert (bool _cond)
{
	if (!_cond) {
	    int res = ::MessageBox (NULL, "Assertion failed!\n\nPress cancel to debug", "DBW3", MB_OKCANCEL);
    	if (res==IDCANCEL) ::DebugBreak();
	}
}
#endif

#ifndef NDEBUG
void _db_trace (const char* _msg)
{
	::OutputDebugString (_msg);
}
#endif

#ifndef NDEBUG
void _db_trace (int _level, const char* _msg)
{
	if (_level<=g_tracelevel) ::OutputDebugString (_msg);
}
#endif

