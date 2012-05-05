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

#ifndef aboutbox_formH
#define aboutbox_formH

#include <Classes.hpp>
#include <Controls.hpp>
#include <StdCtrls.hpp>
#include <Forms.hpp>
#include <ComCtrls.hpp>

class TAboutbox : public TForm
{
__published:	// IDE-managed Components
    TRichEdit *text;
    TButton *bOK;
        void __fastcall FormShow(TObject *Sender);
private:	// User declarations
    void __fastcall setEnglishText();
    void __fastcall setGermanText();
public:		// User declarations
    __fastcall TAboutbox(TComponent* Owner);
    void __fastcall reloadLanguage();
};

extern PACKAGE TAboutbox *Aboutbox;

#endif

