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

#ifndef languageH
#define languageH

enum LANGUAGES { EN /*englisch*/,
                 GE /*deutsch*/};

// Globale Variable, die die eingestellte Sprache festhält.
// Umschalten mittels SwitchLanguage
extern LANGUAGES active_language;

void SwitchLanguage (LANGUAGES _language);

#define LANG_C_H(OBJ, LANG, CAPTION, HINT) \
    if (active_language==LANG) { \
        OBJ->Caption = CAPTION; \
        OBJ->Hint = HINT; \
    }

#define LANG_STR(STR_EN, STR_GE) \
    AnsiString(active_language==GE ? STR_GE : STR_EN)


#define APP_TITLE "DB-BEAD"
#define DATEI_UNBENANNT LANG_STR("unnamed", "unbenannt")

#endif


