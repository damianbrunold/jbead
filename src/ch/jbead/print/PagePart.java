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

import java.awt.Graphics2D;
import java.awt.print.PageFormat;


public class PagePart {

    private PartPrinter printer;
    private int columnIndex;
    private int columnWidth;

    public PagePart(PartPrinter printer, int columnIndex, int columnWidth) {
        this.printer = printer;
        this.columnIndex = columnIndex;
        this.columnWidth = columnWidth;
    }

    public int getWidth() {
        return columnWidth;
    }

    public int print(Graphics2D g, PageFormat pageFormat, int x, int y) {
        return printer.print(g, pageFormat, x, y, columnIndex);
    }

}
