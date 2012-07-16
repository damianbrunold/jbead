/** jbead - http://www.jbead.ch
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

import javax.swing.JToolBar;

public class ColorsToolbar extends JToolBar {

    private static final long serialVersionUID = 1L;

    private Model model;

    public ColorsToolbar(Localization localization, Model model) {
        this.model = model;
        add(new ColorPalette(model, localization));
    }

    public void selectDefaultColor() {
        model.setSelectedColor((byte) 1);
        repaint();
    }

    public void selectColor(byte colorindex) {
        model.setSelectedColor(colorindex);
        repaint();
    }

    public void updateColorIcon(byte colorindex) {
        repaint();
    }

    public void updateAll() {
        repaint();
    }

}
