#pragma once

#include <QString>

namespace jbead {

/*  Apply a color-scheme preference at startup or when the user
    changes it via Preferences. Three accepted values:

      "light"   force light: install Fusion style + a light palette.
      "dark"    force dark: install Fusion style + a dark palette.
      "system"  let Qt do whatever the platform wants — clears the
                application palette so the platform plugin paints
                with its native theme.

    Why both Fusion AND a palette: on Linux (Breeze / Adwaita), the
    native style plugin reads the desktop's color scheme directly
    and ignores QStyleHints::setColorScheme. Swapping in Fusion
    bypasses the plugin so the palette we install actually gets
    drawn — matching the trick dbweave uses for the same reason. */
void applyColorScheme(const QString& scheme);

} // namespace jbead
