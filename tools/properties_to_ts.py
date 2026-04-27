#!/usr/bin/env python3
"""Seed Qt Linguist .ts files from the legacy Java .properties files.

This script bridges the two i18n systems:

  legacy/src/jbead.properties           — English source (ISO-8859-1)
  legacy/src/jbead_de.properties        — German       (ISO-8859-1)
  legacy/src/jbead_fr.properties        — French       (UTF-8)

The Qt port wraps user-visible strings in tr(); lupdate scans those
calls and writes them as <source> elements into i18n/jbead_de.ts and
jbead_fr.ts. This script then walks each .ts file and, for every
<source> string the port matches against a property key in the
ENGLISH_TO_KEY map below, fills in the corresponding translation
from the German / French .properties files.

Mnemonics are reconstructed by inserting "&" before the letter named
in `<key>.mnemonic` (case-insensitive, first match). When the
mnemonic letter is absent from the translated label the script falls
back to the original Qt source's accelerator position, then to no
mnemonic at all.

Run after every change to a tr() string:

    /usr/lib/qt6/bin/lupdate -project build/.lupdate/jbead_lupdate_project.json
    tools/properties_to_ts.py

(or `cmake --build build --target update_translations` followed by
this script).
"""

from __future__ import annotations

import re
import sys
import xml.etree.ElementTree as ET
from pathlib import Path
from typing import Dict, Optional

ROOT = Path(__file__).resolve().parent.parent
LEGACY = ROOT / "legacy" / "src"
I18N   = ROOT / "i18n"

# -------------------------------------------------------------------
# Map every translatable Qt source string to its legacy property key.
# An entry of None means: do not translate (keep the English source)
# — used for strings that don't have a sensible legacy counterpart
# (e.g. file-format filter lines, status-bar field labels).
# -------------------------------------------------------------------

ENGLISH_TO_KEY: Dict[str, Optional[str]] = {
    # File menu --------------------------------------------------
    "&New":                              "action.file.new",
    "Creates a new pattern":             "action.file.new.description",
    "&Open...":                          "action.file.open",
    "Opens a pattern":                   "action.file.open.description",
    "&Save":                             "action.file.save",
    "Saves the pattern":                 "action.file.save.description",
    "Save &As...":                       "action.file.saveas",
    "Saves a pattern to a new file":     "action.file.saveas.description",
    "&Print...":                         "action.file.print",
    "Prints the pattern":                "action.file.print.description",
    "Print Pre&view...":                 None,   # translated via MANUAL_TRANSLATIONS
    "Previews the print output":         None,
    "Page Set&up...":                    "action.file.pagesetup",
    "Configures the page format":        "action.file.pagesetup.description",
    "Export &PDF...":                    None,
    "Exports the pattern as a PDF document": None,
    "E&xit":                             "action.file.exit",
    "Exits the program":                 "action.file.exit.description",

    # Edit menu --------------------------------------------------
    "&Undo":                             "action.edit.undo",
    "Undoes the last change":            "action.edit.undo.description",
    "&Redo":                             "action.edit.redo",
    "Redoes the last undone change":     "action.edit.redo.description",
    "&Arrange...":                       "action.edit.arrange",
    "Arranges copies of the selected part":  "action.edit.arrange.description",
    "&Insert Row":                       "action.edit.insertrow",
    "Inserts an empty row at the bottom":    "action.edit.insertrow.description",
    "&Delete Row":                       "action.edit.deleterow",
    "Deletes the row at the bottom":     "action.edit.deleterow.description",
    "Mirror &Horizontal":                "action.edit.mirrorhorizontal",
    "Mirrors the selection horizontally":    "action.edit.mirrorhorizontal.description",
    "Mirror &Vertical":                  "action.edit.mirrorvertical",
    "Mirrors the selection vertically":  "action.edit.mirrorvertical.description",
    "Rotate &90°":                       "action.edit.rotate",
    "Rotates the selection 90 degrees clockwise":  "action.edit.rotate.description",
    "Delete Selection":                  "action.edit.delete",
    "Deletes the selected region":       "action.edit.delete.description",
    "Ro&w":                              "action.edit.row",

    # View menu --------------------------------------------------
    "&Draft":                            "action.view.draft",
    "Show the draft view":               "action.view.draft.description",
    "&Corrected":                        "action.view.corrected",
    "Show the corrected view":           "action.view.corrected.description",
    "&Simulation":                       "action.view.simulation",
    "Show the simulation view":          "action.view.simulation.description",
    "&Report":                           "action.view.report",
    "Show the report view":              "action.view.report.description",
    "Draw Colo&rs":                      "action.view.drawcolors",
    "Render bead colors":                "action.view.drawcolors.description",
    "Draw S&ymbols":                     "action.view.drawsymbols",
    "Render bead symbols":               "action.view.drawsymbols.description",
    "Zoom &In":                          "action.view.zoomin",
    "Zooms in":                          "action.view.zoomin.description",
    "Zoom &Out":                         "action.view.zoomout",
    "Zooms out":                         "action.view.zoomout.description",
    "&Normal Zoom":                      "action.view.zoomnormal",
    "Resets the zoom":                   "action.view.zoomnormal.description",

    # Tools -------------------------------------------------------
    "&Pencil":                           "action.tool.pencil",
    "Draws beads":                       "action.tool.pencil.description",
    "&Select":                           "action.tool.select",
    "Selects a region":                  "action.tool.select.description",
    "&Fill":                             "action.tool.fill",
    "Fills a contiguous region":         "action.tool.fill.description",
    "Pip&ette":                          "action.tool.pipette",
    "Picks a color from the pattern":    "action.tool.pipette.description",

    # Pattern menu ------------------------------------------------
    "&Width...":                         "action.pattern.width",
    "Sets the pattern width":            "action.pattern.width.description",
    "&Height...":                        "action.pattern.height",
    "Sets the pattern height":           "action.pattern.height.description",
    "&Preferences...":                   "action.pattern.preferences",
    "Opens the preferences dialog":      "action.pattern.preferences.description",

    # Info menu ---------------------------------------------------
    "Technical &Infos...":               "action.info.techinfos",
    "Shows technical information about the pattern":  "action.info.techinfos.description",
    "&About JBead...":                   "action.info.about",
    "Shows the about dialog":            "action.info.about.description",

    # Toolbar-only rotation actions ------------------------------
    "Rotate &Left":                      "action.view.rotateleft",
    "Rotates the simulation tube to the left":   "action.view.rotateleft.description",
    "Rotate &Right":                     "action.view.rotateright",
    "Rotates the simulation tube to the right":  "action.view.rotateright.description",

    # Top-level menus --------------------------------------------
    "&File":                             "action.file",
    "&Edit":                             "action.edit",
    "&View":                             "action.view",
    "&Tools":                            "action.tool",
    "&Pattern":                          "action.pattern",
    "&Info":                             "action.info",

    # Canvas labels ----------------------------------------------
    "Draft":                             "draft",
    "Corrected":                         "corrected",
    "Simulation":                        "simulation",
    "Report":                            "report",

    # Strings the legacy property bundle does not cover. Translated
    # via MANUAL_TRANSLATIONS below, not via the .properties files. */
    "JBead":                                                None,
    "JBead files (*.jbb)":                                  None,
    "DB-BEAD files (*.dbb)":                                None,
    "JBead and DB-BEAD files (*.jbb *.dbb)":                None,
    "Colors":                                               None,
    "Pick color %1":                                        None,
    "unnamed":                                              None,
    "Ready":                                                None,
    "JBead Qt 6 port — skeleton":                           None,
}

# Translations for strings the legacy bundle does not cover (dialog
# titles, status-bar field labels, message-box copy, etc.). Keyed by
# locale; missing entries fall through to the English source.
MANUAL_TRANSLATIONS: Dict[str, Dict[str, str]] = {
    "de": {
        "Open":         "Öffnen",
        "Save":         "Speichern",
        "Open failed":  "Öffnen fehlgeschlagen",
        "Save failed":  "Speichern fehlgeschlagen",
        "The pattern has unsaved changes. Save before continuing?":
            "Das Muster hat ungespeicherte Änderungen. Vor dem Fortfahren speichern?",

        "Arrange":      "Anordnen",
        "&Copies:":     "&Kopien:",
        "&Offset:":     "&Versatz:",
        "Pattern Width":  "Musterbreite",
        "Pattern Height": "Musterhöhe",
        "&Width:":      "&Breite:",
        "&Height:":     "&Höhe:",

        "About JBead":  "Über JBead",
        "<h3>JBead</h3><p>Bead-pattern designer (Qt 6 port).</p>"
        "<p>© 2009–2026 Damian Brunold. Licensed under GPL v3 or later.</p>"
        "<p><a href=\"http://www.brunoldsoftware.ch\">brunoldsoftware.ch</a></p>":
            "<h3>JBead</h3><p>Designer für Perlenmuster (Qt 6-Portierung).</p>"
            "<p>© 2009–2026 Damian Brunold. Lizenziert unter GPL v3 oder später.</p>"
            "<p><a href=\"http://www.brunoldsoftware.ch\">brunoldsoftware.ch</a></p>",

        "Main":         "Hauptleiste",

        "Circumference:":   "Umfang:",
        "Rows:":            "Reihen:",
        "Repeat:":          "Rapport:",
        "Beads by color:":  "Perlen nach Farbe:",
        "Bead list:":       "Perlenliste:",
        " ×":               " ×",
        "Repeat: %1":       "Rapport: %1",
        "Sel: %1 × %2":     "Ausw: %1 × %2",
        "Sel: —":           "Ausw: —",
        "Scroll: %1":       "Scroll: %1",
        "Tool: %1":         "Werkzeug: %1",
        "Pencil":           "Stift",
        "Select":           "Auswahl",
        "Fill":             "Füllen",
        "Pipette":          "Pipette",

        "Print Pre&view...":                "Druck&vorschau...",
        "Previews the print output":        "Zeigt eine Druckvorschau an",
        "Export &PDF...":                   "Als &PDF exportieren...",
        "Exports the pattern as a PDF document":  "Exportiert das Muster als PDF-Dokument",
        "Print":                            "Drucken",
        "Print Preview":                    "Druckvorschau",
        "Page Setup":                       "Seite einrichten",
        "Export PDF":                       "PDF exportieren",
        "PDF Documents (*.pdf)":            "PDF-Dokumente (*.pdf)",
        "Print job produced no output.":    "Der Druckauftrag hat keine Ausgabe erzeugt.",
        "Could not write PDF to %1":        "PDF konnte nicht nach %1 geschrieben werden",
        "Page %1 of %2":                    "Seite %1 von %2",
        "(No repeat detected.)":            "(Kein Rapport erkannt.)",
        "Bead list":                        "Perlenliste",
        "Color palette:":                   "Farbpalette:",
        "File:":                            "Datei:",
        "Author:":                          "Autor:",
        "Organization:":                    "Organisation:",

        "Preferences":                      "Einstellungen",
        "&Language:":                       "&Sprache:",
        "&Color scheme:":                   "&Farbschema:",
        "System default":                   "Systemstandard",
        "Follow system":                    "System folgen",
        "Light":                            "Hell",
        "Dark":                             "Dunkel",
        "Language changes take effect after restarting JBead.":
            "Sprachänderungen werden erst nach einem Neustart von JBead wirksam.",

        "Technical Information":            "Technische Informationen",
        "Used rows:":                       "Verwendete Reihen:",
        "Repeat (beads):":                  "Rapport (Perlen):",
        "Color":                            "Farbe",
        "Index":                            "Index",
        "Symbol":                           "Symbol",
        "Count":                            "Anzahl",
        "Total beads (excluding background): %1":
            "Perlen insgesamt (ohne Hintergrund): %1",

        "&Recent Files":                    "&Zuletzt geöffnet",
        "(empty)":                          "(leer)",
        "<h3>JBead %1</h3>"
        "<p>Bead-pattern designer (Qt 6 port of the original Java/Swing app).</p>"
        "<p>© 2009–2026 Damian Brunold. Licensed under GPL v3 or later.</p>"
        "<p>Built against Qt %2.</p>"
        "<p><a href=\"http://www.brunoldsoftware.ch\">brunoldsoftware.ch</a></p>":
            "<h3>JBead %1</h3>"
            "<p>Designer für Perlenmuster (Qt 6-Portierung der Java/Swing-Originalversion).</p>"
            "<p>© 2009–2026 Damian Brunold. Lizenziert unter GPL v3 oder später.</p>"
            "<p>Erstellt mit Qt %2.</p>"
            "<p><a href=\"http://www.brunoldsoftware.ch\">brunoldsoftware.ch</a></p>",

        "&Export":                          "&Exportieren",
        "&PNG image...":                    "&PNG-Bild...",
        "&JPEG image...":                   "&JPEG-Bild...",
        "&SVG vector...":                   "&SVG-Vektor...",
        "PD&F document...":                 "PD&F-Dokument...",
        "Exports the pattern as a PNG image":
            "Exportiert das Muster als PNG-Bild",
        "Exports the pattern as a JPEG image":
            "Exportiert das Muster als JPEG-Bild",
        "Exports the pattern as an SVG document":
            "Exportiert das Muster als SVG-Dokument",
        "Export PNG":                       "PNG exportieren",
        "Export JPEG":                      "JPEG exportieren",
        "Export SVG":                       "SVG exportieren",
        "PNG images (*.png)":               "PNG-Bilder (*.png)",
        "JPEG images (*.jpg *.jpeg)":       "JPEG-Bilder (*.jpg *.jpeg)",
        "SVG documents (*.svg)":            "SVG-Dokumente (*.svg)",
        "PDF documents (*.pdf)":            "PDF-Dokumente (*.pdf)",
        "Could not write %1":               "%1 konnte nicht geschrieben werden",
    },
    "fr": {
        "Open":         "Ouvrir",
        "Save":         "Sauver",
        "Open failed":  "Échec de l'ouverture",
        "Save failed":  "Échec de la sauvegarde",
        "The pattern has unsaved changes. Save before continuing?":
            "Le patron a des modifications non sauvegardées. Sauver avant de continuer ?",

        "Arrange":      "Disposer",
        "&Copies:":     "&Copies :",
        "&Offset:":     "&Décalage :",
        "Pattern Width":  "Largeur du patron",
        "Pattern Height": "Hauteur du patron",
        "&Width:":      "&Largeur :",
        "&Height:":     "&Hauteur :",

        "About JBead":  "À propos de JBead",
        "<h3>JBead</h3><p>Bead-pattern designer (Qt 6 port).</p>"
        "<p>© 2009–2026 Damian Brunold. Licensed under GPL v3 or later.</p>"
        "<p><a href=\"http://www.brunoldsoftware.ch\">brunoldsoftware.ch</a></p>":
            "<h3>JBead</h3><p>Éditeur de patrons de perles (portage Qt 6).</p>"
            "<p>© 2009–2026 Damian Brunold. Sous licence GPL v3 ou ultérieure.</p>"
            "<p><a href=\"http://www.brunoldsoftware.ch\">brunoldsoftware.ch</a></p>",

        "Main":         "Principal",

        "Circumference:":   "Circonférence :",
        "Rows:":            "Rangées :",
        "Repeat:":          "Rapport :",
        "Beads by color:":  "Perles par couleur :",
        "Bead list:":       "Liste des perles :",
        " ×":               " ×",
        "Repeat: %1":       "Rapport : %1",
        "Sel: %1 × %2":     "Sél : %1 × %2",
        "Sel: —":           "Sél : —",
        "Scroll: %1":       "Défil : %1",
        "Tool: %1":         "Outil : %1",
        "Pencil":           "Crayon",
        "Select":           "Sélection",
        "Fill":             "Remplir",
        "Pipette":          "Pipette",

        "Print Pre&view...":                "&Aperçu avant impression...",
        "Previews the print output":        "Affiche un aperçu de l'impression",
        "Export &PDF...":                   "Exporter en &PDF...",
        "Exports the pattern as a PDF document":  "Exporte le patron en PDF",
        "Print":                            "Imprimer",
        "Print Preview":                    "Aperçu avant impression",
        "Page Setup":                       "Mise en page",
        "Export PDF":                       "Exporter en PDF",
        "PDF Documents (*.pdf)":            "Documents PDF (*.pdf)",
        "Print job produced no output.":    "L'impression n'a produit aucune sortie.",
        "Could not write PDF to %1":        "Impossible d'écrire le PDF dans %1",
        "Page %1 of %2":                    "Page %1 sur %2",
        "(No repeat detected.)":            "(Aucun rapport détecté.)",
        "Bead list":                        "Liste des perles",
        "Color palette:":                   "Palette de couleurs :",
        "File:":                            "Fichier :",
        "Author:":                          "Auteur :",
        "Organization:":                    "Organisation :",

        "Preferences":                      "Préférences",
        "&Language:":                       "&Langue :",
        "&Color scheme:":                   "&Thème :",
        "System default":                   "Par défaut du système",
        "Follow system":                    "Suivre le système",
        "Light":                            "Clair",
        "Dark":                             "Sombre",
        "Language changes take effect after restarting JBead.":
            "Les changements de langue prennent effet au prochain redémarrage de JBead.",

        "Technical Information":            "Informations techniques",
        "Used rows:":                       "Rangées utilisées :",
        "Repeat (beads):":                  "Rapport (perles) :",
        "Color":                            "Couleur",
        "Index":                            "Index",
        "Symbol":                           "Symbole",
        "Count":                            "Nombre",
        "Total beads (excluding background): %1":
            "Total des perles (hors arrière-plan) : %1",

        "&Recent Files":                    "Fichiers &récents",
        "(empty)":                          "(vide)",
        "<h3>JBead %1</h3>"
        "<p>Bead-pattern designer (Qt 6 port of the original Java/Swing app).</p>"
        "<p>© 2009–2026 Damian Brunold. Licensed under GPL v3 or later.</p>"
        "<p>Built against Qt %2.</p>"
        "<p><a href=\"http://www.brunoldsoftware.ch\">brunoldsoftware.ch</a></p>":
            "<h3>JBead %1</h3>"
            "<p>Éditeur de patrons de perles (portage Qt 6 de l'application Java/Swing originale).</p>"
            "<p>© 2009–2026 Damian Brunold. Sous licence GPL v3 ou ultérieure.</p>"
            "<p>Compilé avec Qt %2.</p>"
            "<p><a href=\"http://www.brunoldsoftware.ch\">brunoldsoftware.ch</a></p>",

        "&Export":                          "&Exporter",
        "&PNG image...":                    "Image &PNG...",
        "&JPEG image...":                   "Image &JPEG...",
        "&SVG vector...":                   "Vecteur &SVG...",
        "PD&F document...":                 "Document PD&F...",
        "Exports the pattern as a PNG image":
            "Exporte le patron en image PNG",
        "Exports the pattern as a JPEG image":
            "Exporte le patron en image JPEG",
        "Exports the pattern as an SVG document":
            "Exporte le patron en document SVG",
        "Export PNG":                       "Exporter en PNG",
        "Export JPEG":                      "Exporter en JPEG",
        "Export SVG":                       "Exporter en SVG",
        "PNG images (*.png)":               "Images PNG (*.png)",
        "JPEG images (*.jpg *.jpeg)":       "Images JPEG (*.jpg *.jpeg)",
        "SVG documents (*.svg)":            "Documents SVG (*.svg)",
        "PDF documents (*.pdf)":            "Documents PDF (*.pdf)",
        "Could not write %1":               "Impossible d'écrire %1",
    },
}


def load_properties(path: Path, encoding: str) -> Dict[str, str]:
    """Parse a Java .properties file (key = value, comments start with #)."""
    out: Dict[str, str] = {}
    text = path.read_text(encoding=encoding)
    for raw in text.splitlines():
        line = raw.strip()
        if not line or line.startswith("#") or line.startswith("!"):
            continue
        if "=" not in line:
            continue
        key, _, value = line.partition("=")
        out[key.strip()] = value.strip()
    return out


def insert_mnemonic(label: str, mnemonic: Optional[str]) -> str:
    """Insert "&" before the first occurrence of `mnemonic` in `label`.

    Case-insensitive. When the mnemonic letter is absent from the
    translated label the original label is returned untouched.    """
    if not mnemonic:
        return label
    needle = mnemonic[0]
    idx = label.lower().find(needle.lower())
    if idx < 0:
        return label
    return label[:idx] + "&" + label[idx:]


def translate_for(key: str, props: Dict[str, str], source_has_amp: bool) -> Optional[str]:
    """Look up `key` and (re)insert the mnemonic if the Qt source had one."""
    label = props.get(key)
    if label is None or label == "":
        return None
    if not source_has_amp:
        return label
    mnemonic = props.get(key + ".mnemonic")
    return insert_mnemonic(label, mnemonic)


def patch_ts(ts_path: Path, props: Dict[str, str], manual: Dict[str, str]) -> int:
    """Update <translation> nodes in `ts_path` based on `props` + `manual`."""
    # Preserve the DOCTYPE that ElementTree won't round-trip.
    raw = ts_path.read_text(encoding="utf-8")
    tree = ET.fromstring(raw)
    filled = 0
    for message in tree.iter("message"):
        source_el = message.find("source")
        translation_el = message.find("translation")
        if source_el is None or translation_el is None:
            continue
        src = source_el.text or ""
        translated: Optional[str] = None
        if src in ENGLISH_TO_KEY and ENGLISH_TO_KEY[src] is not None:
            translated = translate_for(ENGLISH_TO_KEY[src], props, "&" in src)
        if translated is None and src in manual:
            translated = manual[src]
        if translated is None:
            continue
        translation_el.text = translated
        if "type" in translation_el.attrib:
            del translation_el.attrib["type"]
        filled += 1

    # Round-trip with the leading XML + DOCTYPE preserved.
    body = ET.tostring(tree, encoding="unicode")
    out = (
        '<?xml version="1.0" encoding="utf-8"?>\n'
        '<!DOCTYPE TS>\n'
        + body
        + "\n"
    )
    ts_path.write_text(out, encoding="utf-8")
    return filled


def main() -> int:
    de_props = load_properties(LEGACY / "jbead_de.properties", "iso-8859-1")
    fr_props = load_properties(LEGACY / "jbead_fr.properties", "utf-8")

    de_filled = patch_ts(I18N / "jbead_de.ts", de_props, MANUAL_TRANSLATIONS["de"])
    fr_filled = patch_ts(I18N / "jbead_fr.ts", fr_props, MANUAL_TRANSLATIONS["fr"])

    print(f"jbead_de.ts: filled {de_filled} translations from {len(de_props)} property entries")
    print(f"jbead_fr.ts: filled {fr_filled} translations from {len(fr_props)} property entries")

    # Sanity-check: every coverable English source string was mapped.
    sources = set()
    for ts_path in (I18N / "jbead_de.ts", I18N / "jbead_fr.ts"):
        tree = ET.fromstring(ts_path.read_text(encoding="utf-8"))
        for s in tree.iter("source"):
            if s.text:
                sources.add(s.text)
    covered = set(ENGLISH_TO_KEY.keys()) | set(MANUAL_TRANSLATIONS["de"].keys()) \
                                          | set(MANUAL_TRANSLATIONS["fr"].keys())
    unmapped = sources - covered
    if unmapped:
        print()
        print("WARNING: source strings without a mapping (will stay English):")
        for s in sorted(unmapped):
            print(f"  - {s!r}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
