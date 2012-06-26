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

package ch.jbead.print;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;

import ch.jbead.Localization;
import ch.jbead.Model;
import ch.jbead.Point;

public class SimulationPrinter extends GridPrinter {

    public SimulationPrinter(Model model, Localization localization) {
        super(model, localization);
    }

    @Override
    protected int getColumnWidth() {
        return visibleWidth() * gx;
    }

    @Override
    protected int getRows() {
        int rows = model.getUsedHeight();
        Point pt = model.correct(new Point(model.getWidth() - 1, rows - 1));
        return pt.getY() + 1;
    }

    private int visibleWidth() {
        return model.getWidth() / 2;
    }

    @Override
    public int print(Graphics2D g, PageFormat pageFormat, int x, int y, int column) {
        setStroke(g);
        int height = (int) pageFormat.getImageableHeight();
        x += border;
        int rows = getRowsPerColumn(height);
        int start = rows * column;
        for (int j = 0; j < model.getUsedHeight(); j++) {
            for (int i = 0; i < model.getWidth(); i++) {
                Point pt = new Point(i, j);
                byte c = model.get(pt);
                pt = model.correct(pt);
                if (pt.getY() < start) continue;
                if (pt.getY() >= start + rows) continue;
                if (pt.getX() > visibleWidth()) continue;
                int dx = dx(pt);
                int w = w(pt);
                if (c > 0) {
                    g.setColor(model.getColor(c));
                    g.fillRect(x + pt.getX() * gx - dx, y + (rows - (pt.getY() - start) - 1) * gy, w, gy);
                }
                g.setColor(Color.BLACK);
                g.drawRect(x + pt.getX() * gx - dx, y + (rows - (pt.getY() - start) - 1) * gy, w, gy);
            }
        }
        return x + getColumnWidth() + border;
    }

    private int dx(Point pt) {
        if (pt.getY() % 2 == 0) return 0;
        if (pt.getX() == 0) return 0;
        return gx / 2;
    }

    private int w(Point pt) {
        if (pt.getY() % 2 == 0) {
            if (pt.getX() == visibleWidth()) return gx / 2;
            else return gx;
        } else {
            if (pt.getX() == 0) return gx / 2;
            else return gx;
        }
    }

}
