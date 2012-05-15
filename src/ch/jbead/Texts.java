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
public class Texts {

    public static Language active_language = Language.EN;

    public static void setLanguage(Language language, BeadForm form) {
        if (active_language == language) return;
        forceLanguage(language, form);
    }

    public static void forceLanguage(Language language, BeadForm form) {
        active_language = language;
        form.reloadLanguage();
        form.setAppTitle();
    }
    
    public static String text(String en, String ge) {
        if (Texts.active_language == Language.EN) {
            return en;
        } else {
            return ge;
        }
    }

    public static void update(JFrame frame, Language language, String caption) {
        if (Texts.active_language == language) {
            frame.setTitle(caption);
        }
    }

    public static void update(JDialog dialog, Language language, String caption) {
        if (Texts.active_language == language) {
            dialog.setTitle(caption);
        }
    }

    public static void update(JButton button, Language language, String caption, String hint) {
        if (Texts.active_language == language) {
            button.setText(caption);
            button.setToolTipText(hint);
        }
    }

    public static void update(JLabel label, Language language, String caption) {
        if (Texts.active_language == language) {
            label.setText(caption);
        }
    }

    public static void update(JMenu menu, Language language, String caption) {
        if (Texts.active_language == language) {
            menu.setText(caption);
        }
    }

    public static void update(JMenuItem item, Language language, String caption, String description) {
        if (Texts.active_language == language) {
            item.setText(caption);
            item.setToolTipText(description);
        }
    }

    public static void update(JToggleButton button, Language language, String text, String tooltip) {
        if (Texts.active_language == language) {
            button.setText(text);
            button.setToolTipText(tooltip);
        }
    }

}
