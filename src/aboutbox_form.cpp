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

#include "aboutbox_form.h"
#include "language.h"

#pragma package(smart_init)
#pragma resource "*.dfm"
TAboutbox *Aboutbox;

__fastcall TAboutbox::TAboutbox(TComponent* Owner)
    : TForm(Owner)
{
    text->Align = alNone;
    text->Color = Color;
    text->Left = 6;
    text->Top = 6;
    text->Width = ClientWidth - 12;
    text->Height = ClientHeight - 12;

    bOK->Left = ClientWidth - bOK->Width - 10;
    bOK->Top = ClientHeight - bOK->Height - 10;

    reloadLanguage();
}

void __fastcall TAboutbox::setEnglishText()
{
    String t = "{\\rtf1\\ansi\\ansicpg1252\\deff0\\deflang2055{\\fonttbl{\\f0\\fswiss\\fprq2\\fcharset0 Arial;} "
        "{\\f1\\fswiss\\fcharset0 Arial;}} "
        "\\viewkind4\\uc1\\pard\\b\\f0\\fs28 DB-BEAD\\b0\\f1\\fs20\\par "
        "\\par "
        "This is DB-BEAD, a program designed to help you design crochet bead ropes. "
        "The creation of such ropes is describes in e.g. the book "
        "'Geh\\'e4kelte Glasperlenketten' written by Lotti Gygax. It is hard work to "
        "create such a rope, but the result is very beautiful.\\par "
        "\\par "
        "With DB-BEAD you simulate before you start working how your design will "
        "look like as a finished rope. You can make changes directly on the screen.\\par "
        "\\par "
        "After finishing the design, you can print out all relevant data "
        "including a 'list of beads', which is very useful for correctly arranging "
        "the beads onto the thread.\\par "
        "\\par "
        "DB-BEAD was written by Damian Brunold. It is freely available and licensed "
        "under the GPL v3. This means, you can use and copy it freely and you can "
        "create derivative works (if you are a programmer). Damian Brunold cannot "
        "assume any liability for bugs and damage caused by using the program. "
        "You have to decide for yourself whether the program is useful for you or not.\\par "
        "\\par "
        "More information is available at http://www.brunoldsoftware.ch or by sending "
        "e-mail to info@brunoldsoftware.ch. This also is the address to direct bug "
        "reports or feature requests to.\\par "
        "\\par "
        "Have fun using DB-WEAVE\\par "
        "Damian Brunold\\par "
        "}";

    try {
        TMemoryStream* ms = new TMemoryStream();
        ms->Write (t.c_str(), t.Length());
        ms->Position = 0;
        text->Lines->LoadFromStream (ms);
        delete ms;
    } catch(...) {
    }
}

void __fastcall TAboutbox::setGermanText()
{
    String t = "{\\rtf1\\ansi\\ansicpg1252\\deff0\\deflang2055{\\fonttbl{\\f0\\fswiss\\fprq2\\fcharset0 Arial;} "
        "{\\f1\\fswiss\\fcharset0 Arial;}} "
        "\\viewkind4\\uc1\\pard\\b\\f0\\fs28 DB-BEAD\\b0\\f1\\fs20\\par "
        "\\par "
        "Dies ist DB-BEAD, ein Programm, das Ihnen beim Entwurf von geh\\'e4kelten "
        "Perlenketten helfen soll. Die Erstellung solcher Ketten wird beispielsweise "
        "im Buch 'Geh\\'e4kelte Glasperlenketten' von Lotti Gygax beschrieben. Die Arbeit ist aufw\\'e4ndig und "
        "langwierig. Das Resultat entsch\\'e4digt aber f\\'fcr die erlittene M\\'fchsal.\\par "
        "\\par "
        "Mit DB-BEAD k\\'f6nnen Sie schon vor Beginn der Arbeit simulieren, wie Ihr "
        "Entwurf als Kette dann aussehen wird. Direkt am Bildschirm k\\'f6nnen Sie "
        "\\'c4nderungen vornehmen.\\par "
        "\\par "
        "Wenn Sie zufrieden mit dem Entwurf sind, k\\'f6nnen Sie alle notwendigen "
        "Daten ausdrucken lassen, inklusive einer 'F\\'e4delliste', die hilfreich "
        "f\\'fcr das Auff\\'e4deln der Perlen auf das H\\'e4kelgarn ist.\\par "
        "\\par "
        "DB-BEAD wurde von Damian Brunold geschrieben. Es steht unter der Lizenz "
        "GPL v3, was bedeutet, dass Sie es kostenlos verwenden, kopieren und \\'e4ndern "
        "d\\'fcrfen. Daf\\'fcr \\'fcbernimmt Damian Brunold absolut keine "
        "Haftung f\\'fcr Fehler und Sch\\'e4den durch Benutzung des Programmes. "
        "Sie m\\'fcssen selber entscheiden, ob das Programm f\\'fcr Sie n\\'fctzlich "
        "ist oder nicht.\\par "
        "\\par "
        "Weitere Informationen erhalten Sie unter http://www.brunoldsoftware.ch "
        "oder per E-Mail an info@brunoldsoftware.ch. An diese Adresse k\\'f6nnen "
        "Sie auch Fehler oder Verbesserungsvorschl\\'e4ge melden.\\par "
        "\\par "
        "Viel Spass mit dem Programm\\par "
        "Damian Brunold\\par "
        "}";

    try {
        TMemoryStream* ms = new TMemoryStream();
        ms->Write (t.c_str(), t.Length());
        ms->Position = 0;
        text->Lines->LoadFromStream (ms);
        delete ms;
    } catch(...) {
    }
}

void __fastcall TAboutbox::reloadLanguage()
{
    LANG_C_H(this, EN, "About DB-BEAD", "")
    LANG_C_H(this, GE, "Über DB-BEAD", "")
    if (active_language == EN) {
      setEnglishText();
    } else {
      setGermanText();
    }
    LANG_C_H(bOK, EN, "OK", "")
    LANG_C_H(bOK, GE, "OK", "")
}


void __fastcall TAboutbox::FormShow(TObject *Sender)
{
    reloadLanguage();
}

