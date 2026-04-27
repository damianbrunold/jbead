# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project overview

JBead is a bead-pattern designer (peyote / brick stitch / etc.) — http://www.brunoldsoftware.ch.
Cross-platform desktop application written in C++17 against Qt 6.5 LTS.
Licensed under GPL v3 or later.

This repository is a port of the original Java/Swing JBead (under `legacy/`)
to C++/Qt 6, modelled on the parallel Qt 6 port of DB-WEAVE (`../dbweave`).
The redesigned bead-list panel, straight-line drawing tool, and PDF export
are taken from the JBead web editor in `../textile` (`static/js/jbead.js`).

The Java original lives under `legacy/` while the port is in flight; once
the port stabilises it will be moved to a `legacy` git branch (mirroring
DB-WEAVE).

## Repository layout

```
src/
  domain/    # pure domain logic (Phase 2)
  io/        # .jbb / .dbb file I/O (Phase 2)
  ui/        # widgets, main window, dialogs (Phase 3)
  print/     # QPrinter output (Phase 5)
  compat/    # small residual helpers as needed
tests/       # Qt Test unit tests
i18n/        # jbead_de.ts, jbead_fr.ts (Qt Linguist)
resources/   # icons, .desktop, Info.plist template
packaging/   # per-platform bundle scripts (linux/macos/windows)
samples/     # .jbb sample files (used by file-I/O round-trip tests)
legacy/      # original Java/Swing source (not built; for reference)
```

## Build / run

- Requires **Qt 6.5 LTS or newer**, CMake 3.21+, a C++17 compiler.
- Linux dev-package names (Debian/Ubuntu): `qt6-base-dev`, `qt6-base-dev-tools`,
  `qt6-tools-dev`, `qt6-tools-dev-tools`, plus `cmake` and `ninja-build`.
- Build:
  ```
  cmake -S . -B build -G Ninja
  cmake --build build
  ctest --test-dir build --output-on-failure
  ```
- CMake options: `-DJBEAD_BUILD_TESTS=ON/OFF` (default ON).
- Running: `./build/src/jbead`.

## Localization

Qt's native `tr()` / `.ts` / `.qm` pipeline. Source language is English;
shipped translations are German (`i18n/jbead_de.ts`) and French
(`i18n/jbead_fr.ts`). `qt_add_translations()` in `src/ui/CMakeLists.txt`
attaches the bundle to the `jbead_ui` static library so test binaries
also pick it up; lrelease compiles the `.ts` files into `.qm` and
embeds them at the resource path `:/i18n/jbead_<lang>.qm`.

User language is picked in `src/main.cpp` from
`QSettings("Brunold Software", "JBead")` key `Environment/Language`
(values: `de` / `fr` / `en`); falls back to the OS locale.

Translation seeding workflow (run after every change to a `tr()` string):

```
cmake --build build --target update_translations
tools/properties_to_ts.py
```

`update_translations` runs lupdate, which scans every `tr()` call
across the project and writes/updates `<source>` entries in the two
`.ts` files. `tools/properties_to_ts.py` then walks each `.ts` file
and fills `<translation>` entries by:

1. Looking the source string up in `ENGLISH_TO_KEY` (mapping to a
   legacy `legacy/src/jbead*.properties` key) and reading the
   translated label + mnemonic from the German / French `.properties`
   files. Mnemonics are reattached by inserting `&` before the letter
   named in `<key>.mnemonic` (case-insensitive, falls back gracefully
   if the letter doesn't appear in the translated label).
2. Falling back to the per-locale `MANUAL_TRANSLATIONS` table inside
   the script for strings the legacy bundle did not cover (dialog
   titles, status-bar field labels, message-box copy).

The script prints a `WARNING:` block listing any source string it
couldn't translate so they're easy to add to `MANUAL_TRANSLATIONS`.

## Working on this codebase

- New code goes into `src/<sub-module>/`. When adding a source file, add
  it to the matching `CMakeLists.txt` under `src/` or its sub-module.
- Mirror DB-WEAVE conventions wherever feasible — the build/packaging
  harness, install rules, and test layout are deliberately parallel so
  that fixes can flow between projects.
- The legacy Java implementation under `legacy/` is the source of truth
  for behaviour. When porting a feature, diff against the original
  `legacy/src/ch/jbead/` class.
- Action labels, mnemonics, shortcuts, and descriptions in
  `legacy/src/jbead*.properties` are the contract for menubar / toolbar /
  statusbar text. Keep them byte-identical in the port (translations
  carry the same wording).

## Phased port status

- Phase 0 — move legacy code into `legacy/`. **Done.**
- Phase 1 — skeleton CMake + Qt 6 build harness, packaging scripts,
  empty MainWindow that builds and launches. **Done.**
- Phase 2 — domain model + .jbb / .dbb file I/O.
- Phase 3 — main window, four pattern canvases, action registry. **Done.**
- Phase 4 — Qt Linguist-based i18n (de/fr seeded from `.properties`). **Done.**
- Phase 5 — printing pipeline (port of legacy `print/`, full QPrinter
  + QPrintPreviewDialog + QPrintDialog). **Done.**
- Phase 6 — polish, MRU, dialogs, samples. **Done.**
- Phase 7 — test pass. **Done.**

## Manual smoke checklist

Run before tagging a release. Headless ctest covers the domain,
file I/O, print pipeline, and i18n; the items below need a real
display because they exercise interactive behaviour.

1. **Launch.** `./build/src/jbead`. Window appears at the
   geometry from the previous run (or 1280x800 on first launch).
   Title reads `unnamed - JBead`.
2. **Open every sample.** File -> Open -> `samples/{stripes,hearts,small_hearts,green_yellow_diagonal}.jbb`.
   Each loads without error. Repeat count appears in the report
   panel ("Bead list:" + pills) immediately, not "(No repeat detected.)".
3. **Edit + title asterisk.** Click a cell with the pencil tool;
   the title gains ` *`. Save (Ctrl+S); the asterisk drops.
4. **Undo / redo.** Edit, wait ~½ s for the idle timer, then
   Ctrl+Z then Ctrl+Y; the bead toggles off then back on.
5. **Tools.** Cycle pencil (P), select (S), fill (F), pipette (E)
   from the toolbar; the status bar shows the chosen tool. Drag
   with select; the rectangle previews live. Drag with pencil +
   Shift; the line preview snaps to 8 directions.
6. **Scroll.** Drag the scrollbar; the pattern stays in view —
   never disappears past the last used row + 10 rows of headroom.
   Initial position is at the bottom.
7. **Zoom.** Ctrl++ / Ctrl+- / Ctrl+0; cells resize live, scrollbar
   range updates with the new visible row count.
8. **Recent files.** File -> Recent Files now lists the patterns
   you opened. Selecting one re-opens it (with `maybeSave` guard).
9. **Print preview.** File -> Print Preview... renders one page
   per enabled section with the header `JBead - <file> - <section>
   - page X of Y`.
10. **PDF export.** File -> Export PDF...; the resulting file is
    > 1 KB and opens in any PDF viewer.
11. **Page setup + print.** File -> Page Setup; pick A4 landscape;
    File -> Print -> the system print dialog shows the new orientation.
12. **Preferences.** Pattern -> Preferences. Switch theme to Dark;
    panels redraw with palette-aware chrome (markers, pill borders,
    arrow). Switch language to Deutsch; the "restart required"
    hint is shown.
13. **Tech info.** Info -> Technical Information; per-colour
    counts table populated, totals line non-zero for non-empty
    patterns.
14. **Save extension.** File -> Save As -> type `foo`; the file
    on disk is named `foo.jbb`.
15. **Close + relaunch.** Window position, size, splitter
    proportions, and toolbar layout all restore.
