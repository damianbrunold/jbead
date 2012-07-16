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

package ch.jbead.print;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.util.List;

import ch.jbead.Localization;
import ch.jbead.Model;

public abstract class PartPrinter {

    protected Model model;
    protected Localization localization;

    protected Font font = new Font(Font.SANS_SERIF, Font.PLAIN, 8);
    protected int border = Convert.mm2pt(4);

    public PartPrinter(Model model, Localization localization) {
        this.model = model;
        this.localization = localization;
    }

    public abstract List<Integer> layoutColumns(int width, int height);
    public abstract int print(Graphics2D g, PageFormat pageFormat, int x, int y, int column);

}
