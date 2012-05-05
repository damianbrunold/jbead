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

#include "bead_form.h"
#include "language.h"

void __fastcall TBeadForm::reloadLanguage()
{
    // Menüs
      // Menu Datei
      LANG_C_H(MenuFile, EN, "&File", "")
      LANG_C_H(MenuFile, GE, "&Datei", "")
      LANG_C_H(FileNew, EN, "&New", "Creates a new pattern")
      LANG_C_H(FileNew, GE, "&Neu", "Erstellt ein neues Muster")
      LANG_C_H(FileOpen, EN, "&Open...", "Opens a pattern")
      LANG_C_H(FileOpen, GE, "Ö&ffnen...", "Öffnet ein Muster")
      LANG_C_H(FileSave, EN, "&Save", "Saves the pattern")
      LANG_C_H(FileSave, GE, "&Speichern", "Speichert das Muster")
      LANG_C_H(FileSaveas, EN, "Save &as...", "Saves the pattern to a new file")
      LANG_C_H(FileSaveas, GE, "Speichern &unter...", "Speichert das Muster unter einem neuen Namen")
      LANG_C_H(FilePrint, EN, "&Print...", "Prints the pattern")
      LANG_C_H(FilePrint, GE, "&Drucken...", "Druckt das Muster")
      LANG_C_H(FilePrintersetup, EN, "Printer set&up...", "Configures the printer")
      LANG_C_H(FilePrintersetup, GE, "D&ruckereinstellung...", "Konfiguriert den Drucker")
      LANG_C_H(FileExit, EN, "E&xit", "Exits the program")
      LANG_C_H(FileExit, GE, "&Beenden", "Beendet das Programm")


      // Menu Bearbeiten
      LANG_C_H(MenuEdit, EN, "&Edit", "")
      LANG_C_H(MenuEdit, GE, "&Bearbeiten", "")
      LANG_C_H(EditUndo, EN, "&Undo", "Undoes the last action")
      LANG_C_H(EditUndo, GE, "&Rückgängig", "Macht die letzte Änderung rückgängig")
      LANG_C_H(EditRedo, EN, "&Redo", "Redoes the last undone action")
      LANG_C_H(EditRedo, GE, "&Wiederholen", "Führt die letzte rückgängig gemachte Änderung durch")
      LANG_C_H(EditCopy, EN, "&Arrange", "")
      LANG_C_H(EditCopy, GE, "&Anordnen", "")
      LANG_C_H(EditLine, EN, "&Empty Line", "")
      LANG_C_H(EditLine, GE, "&Leerzeile", "")
      LANG_C_H(EditInsertline, EN, "&Insert", "")
      LANG_C_H(EditInsertline, GE, "&Einfügen", "")
      LANG_C_H(EditDeleteline, EN, "&Delete", "")
      LANG_C_H(EditDeleteline, GE, "E&ntfernen", "")

      // Menu Werkzeug
      LANG_C_H(Werkzeug1, EN, "&Tool", "")
      LANG_C_H(Werkzeug1, GE, "&Werkzeug", "")
      LANG_C_H(ToolPoint, EN, "&Pencil", "")
      LANG_C_H(ToolPoint, GE, "&Eingabe", "")
      LANG_C_H(ToolSelect, EN, "&Select", "")
      LANG_C_H(ToolSelect, GE, "&Auswahl", "")
      LANG_C_H(ToolFill, EN, "&Fill", "")
      LANG_C_H(ToolFill, GE, "&Füllen", "")
      LANG_C_H(ToolSniff, EN, "P&ipette", "")
      LANG_C_H(ToolSniff, GE, "&Pipette", "")


      // Menu Ansicht
      LANG_C_H(MenuView, EN, "&View", "")
      LANG_C_H(MenuView, GE, "&Ansicht", "")
      LANG_C_H(ViewDraft, EN, "&Design", "")
      LANG_C_H(ViewDraft, GE, "&Entwurf", "")
      LANG_C_H(ViewNormal, EN, "&Corrected", "")
      LANG_C_H(ViewNormal, GE, "&Korrigiert", "")
      LANG_C_H(ViewSimulation, EN, "&Simulation", "")
      LANG_C_H(ViewSimulation, GE, "&Simulation", "")
      LANG_C_H(ViewReport, EN, "&Report", "")
      LANG_C_H(ViewReport, GE, "&Auswertung", "")
      LANG_C_H(ViewZoomin, EN, "&Zoom in", "Zoom in")
      LANG_C_H(ViewZoomin, GE, "&Vergrössern", "Vergrössert die Ansicht")
      LANG_C_H(ViewZoomnormal, EN, "&Normal", "Sets magnification to default value")
      LANG_C_H(ViewZoomnormal, GE, "&Normal", "Stellt die Standardgrösse ein")
      LANG_C_H(ViewZoomout, EN, "Zoo&m out", "Zoom out")
      LANG_C_H(ViewZoomout, GE, "Ver&kleinern", "Verkleinert die Ansicht")
      LANG_C_H(ViewLanguage, EN, "&Language", "")
      LANG_C_H(ViewLanguage, GE, "&Sprache", "")
      LANG_C_H(LanguageEnglish, EN, "&English", "")
      LANG_C_H(LanguageEnglish, GE, "&Englisch", "")
      LANG_C_H(LanguageGerman, EN, "&German", "")
      LANG_C_H(LanguageGerman, GE, "&Deutsch", "")


      // Menu Muster
      LANG_C_H(MenuPattern, EN, "&Pattern", "")
      LANG_C_H(MenuPattern, GE, "&Muster", "")
      LANG_C_H(PatternWidth, EN, "&Width...", "")
      LANG_C_H(PatternWidth, GE, "&Breite...", "")

      // Menu ?
      LANG_C_H(MenuInfo, EN, "&?", "")
      LANG_C_H(MenuInfo, GE, "&?", "")
      LANG_C_H(InfoAbout, EN, "About &DB-BEAD...", "Displays information about DB-BEAD")
      LANG_C_H(InfoAbout, GE, "Über &DB-BEAD...", "Zeigt Informationen über DB-BEAD an")


    // Toolbar

    LANG_C_H(sbNew, EN, "", "New|Creates a new pattern")
    LANG_C_H(sbNew, GE, "", "Neu|Erstellt ein neues Muster")
    LANG_C_H(sbOpen, EN, "", "Open|Opens a pattern")
    LANG_C_H(sbOpen, GE, "", "Öffnen|Öffnet ein Muster")
    LANG_C_H(sbSave, EN, "", "Save|Saves the pattern")
    LANG_C_H(sbSave, GE, "", "Speichern|Speichert das Muster")
    LANG_C_H(sbPrint, EN, "", "Print|Prints the pattern")
    LANG_C_H(sbPrint, GE, "", "Drucken|Druckt das Muster")
    LANG_C_H(sbUndo, EN, "", "Undo|Undoes the last change")
    LANG_C_H(sbUndo, GE, "", "Rückgängig|Macht die letzte Änderung rückgängig")
    LANG_C_H(sbRedo, EN, "", "Redo|Redoes the last undone change")
    LANG_C_H(sbRedo, GE, "", "Wiederholen|Macht die letzte rückgängig gemachte Änderung")
    LANG_C_H(sbRotateleft, EN, "", "Left|Rotates the pattern left")
    LANG_C_H(sbRotateleft, GE, "", "Links|Rotiert das Muster nach links")
    LANG_C_H(sbRotateright, EN, "", "Right|Rotates the pattern right")
    LANG_C_H(sbRotateright, GE, "", "Rechts|Rotiert das Muster nach rechts")
    LANG_C_H(sbCopy, EN, "", "Arrange")
    LANG_C_H(sbCopy, GE, "", "Anordnen")
    LANG_C_H(sbColor0, EN, "", "Color 0")
    LANG_C_H(sbColor0, GE, "", "Farbe 0")
    LANG_C_H(sbColor1, EN, "", "Color 1")
    LANG_C_H(sbColor1, GE, "", "Farbe 1")
    LANG_C_H(sbColor2, EN, "", "Color 2")
    LANG_C_H(sbColor2, GE, "", "Farbe 2")
    LANG_C_H(sbColor3, EN, "", "Color 3")
    LANG_C_H(sbColor3, GE, "", "Farbe 3")
    LANG_C_H(sbColor4, EN, "", "Color 4")
    LANG_C_H(sbColor4, GE, "", "Farbe 4")
    LANG_C_H(sbColor5, EN, "", "Color 5")
    LANG_C_H(sbColor5, GE, "", "Farbe 5")
    LANG_C_H(sbColor6, EN, "", "Color 6")
    LANG_C_H(sbColor6, GE, "", "Farbe 6")
    LANG_C_H(sbColor7, EN, "", "Color 7")
    LANG_C_H(sbColor7, GE, "", "Farbe 7")
    LANG_C_H(sbColor8, EN, "", "Color 8")
    LANG_C_H(sbColor8, GE, "", "Farbe 8")
    LANG_C_H(sbColor9, EN, "", "Color 9")
    LANG_C_H(sbColor9, GE, "", "Farbe 9")
    LANG_C_H(sbToolSelect, EN, "", "Select")
    LANG_C_H(sbToolSelect, GE, "", "Auswahl")
    LANG_C_H(sbToolPoint, EN, "", "Pencil")
    LANG_C_H(sbToolPoint, GE, "", "Eingabe")
    LANG_C_H(sbToolFill, EN, "", "Fill")
    LANG_C_H(sbToolFill, GE, "", "Füllen")
    LANG_C_H(sbToolSniff, EN, "", "Pipette")
    LANG_C_H(sbToolSniff, GE, "", "Pipette")

    LANG_C_H(laDraft, EN, "Draft", "")
    LANG_C_H(laDraft, GE, "Entwurf", "")
    LANG_C_H(laNormal, EN, "Corrected", "")
    LANG_C_H(laNormal, GE, "Korrigiert", "")
    LANG_C_H(laSimulation, EN, "Simulation", "")
    LANG_C_H(laSimulation, GE, "Simulation", "")
    LANG_C_H(laReport, EN, "Report", "")
    LANG_C_H(laReport, GE, "Auswertung", "")

    Invalidate();
}

