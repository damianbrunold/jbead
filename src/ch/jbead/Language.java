/** jbead - http://www.brunoldsoftware.ch
    Copyright (C) 2001-2012  Damian Brunold

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

package ch.jbead;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JToggleButton;

/**
 * 
 */
public class Language {

    public static enum LANG {
        EN, GE
    }

    public static LANG active_language = LANG.EN;

    public static void SwitchLanguage(LANG _language, BeadForm form) {
        if (active_language == _language) return;

        active_language = _language;

        form.reloadLanguage();
        form.setAppTitle();
    }

    public static String STR(String en, String ge) {
        if (Language.active_language == Language.LANG.EN) {
            return en;
        } else {
            return ge;
        }
    }

    public static void C_H(JFrame frame, Language.LANG language, String caption) {
        if (Language.active_language == language) {
            frame.setTitle(caption);
        }
    }

    public static void C_H(JDialog dialog, Language.LANG language, String caption) {
        if (Language.active_language == language) {
            dialog.setTitle(caption);
        }
    }

    public static void C_H(JButton button, Language.LANG language, String caption, String hint) {
        if (Language.active_language == language) {
            button.setText(caption);
            button.setToolTipText(hint);
        }
    }

    public static void C_H(JLabel label, Language.LANG language, String caption) {
        if (Language.active_language == language) {
            label.setText(caption);
        }
    }

    public static void C_H(JMenu menu, Language.LANG language, String caption) {
        if (Language.active_language == language) {
            menu.setText(caption);
        }
    }

    public static void C_H(JMenuItem item, Language.LANG language, String caption, String description) {
        if (Language.active_language == language) {
            item.setText(caption);
            item.setToolTipText(description);
        }
    }

    public static void C_H(JToggleButton button, Language.LANG language, String text, String tooltip) {
        if (Language.active_language == language) {
            button.setText(text);
            button.setToolTipText(tooltip);
        }
    }
}
