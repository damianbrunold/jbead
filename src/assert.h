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

#ifndef assertH
#define assertH

#ifdef NDEBUG
#define assert(c)
#define trace(c)
#define trace2(i,c)
#else
#define assert(c) _db_assert(c)
#define trace(c) _db_trace(c)
#define trace2(i,c) _db_trace(i,(c))
#endif

#ifndef NDEBUG
void _db_assert (bool _cond);
void _db_trace (const char* _msg);
void _db_trace (int _level, const char* _msg);
#endif

#endif

