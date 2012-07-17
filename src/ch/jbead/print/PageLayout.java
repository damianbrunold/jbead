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

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.util.ArrayList;
import java.util.List;


public class PageLayout implements Printable {

    private int width;
    private List<PagePart> parts = new ArrayList<PagePart>();

    public PageLayout(int width) {
        this.width = width;
    }

    public int getUnusedWidth() {
        int unused = width;
        for (PagePart part: parts) {
            unused -= part.getWidth();
        }
        return unused;
    }

    public void addPart(PagePart part) {
        parts.add(part);
    }

    public void printPage(Graphics2D g, PageFormat pageFormat) {
        int x = (int) pageFormat.getImageableX();
        int y = (int) pageFormat.getImageableY();
        for (PagePart part : parts) {
            x = part.print(g, pageFormat, x, y);
        }
    }

    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
        int x = (int) pageFormat.getImageableX();
        int y = (int) pageFormat.getImageableY();
        for (PagePart part : parts) {
            x = part.print((Graphics2D) graphics, pageFormat, x, y);
        }
        return PAGE_EXISTS;
    }
}
