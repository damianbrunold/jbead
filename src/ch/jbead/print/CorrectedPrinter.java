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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;

import ch.jbead.Localization;
import ch.jbead.Model;
import ch.jbead.Point;

public class CorrectedPrinter extends GridPrinter {

    public CorrectedPrinter(Model model, Localization localization, boolean fullPattern) {
        super(model, localization, fullPattern);
    }

    @Override
    protected int getColumnWidth() {
        return (model.getWidth() + 1) * gx;
    }

    @Override
    protected int getRows(int height) {
        int rows = getPrintableRows(height);
        Point pt = model.correct(new Point(model.getWidth() - 1, rows - 1));
        return pt.getY() + 1;
    }

    @Override
    public int print(Graphics2D g, PageFormat pageFormat, int x, int y, int column) {
        setStroke(g);
        int height = (int) pageFormat.getImageableHeight();
        x += border + gx / 2;
        int rows = getRowsPerColumn(height);
        int start = rows * column;
        for (int j = 0; j < model.getUsedHeight(); j++) {
            for (int i = 0; i < model.getWidth(); i++) {
                Point pt = new Point(i, j);
                byte c = model.get(pt);
                pt = model.correct(pt);
                if (pt.getY() < start) continue;
                if (pt.getY() >= start + rows) continue;
                int dx = pt.getY() % 2 == 0 ? 0 : gx / 2;
                g.setColor(model.getColor(c));
                g.fillRect(x + pt.getX() * gx - dx, y + (rows - (pt.getY() - start) - 1) * gy, gx, gy);
                g.setColor(Color.BLACK);
                g.drawRect(x + pt.getX() * gx - dx, y + (rows - (pt.getY() - start) - 1) * gy, gx, gy);
            }
        }
        return x + getColumnWidth() + border;
    }

}
