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
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;

import ch.jbead.BeadPainter;
import ch.jbead.Localization;
import ch.jbead.Model;
import ch.jbead.Point;
import ch.jbead.SimpleCoordinateCalculator;
import ch.jbead.View;
import ch.jbead.util.Convert;


public class DraftPrinter extends GridPrinter {

    private int markerWidth = Convert.mmToPoint(10);

    public DraftPrinter(Model model, View view, Localization localization, boolean fullPattern) {
        super(model, view, localization, fullPattern);
    }

    protected int getColumnWidth() {
        return model.getWidth() * gx + markerWidth;
    }

    protected int getRows(int height) {
        return getPrintableRows(height);
    }

    @Override
    public int print(Graphics2D g, PageFormat pageFormat, int x, int y, int column) {
        setStroke(g);
        SimpleCoordinateCalculator coord = new SimpleCoordinateCalculator(gx, gy);
        Font symbolfont = new Font("SansSerif", Font.PLAIN, gx - 2);
        BeadPainter painter = new BeadPainter(coord, model, view, symbolfont);
        int height = (int) pageFormat.getImageableHeight();
        int rows = getRowsPerColumn(height);
        int start = rows * column;
        coord.setOffsetX(x + border + markerWidth);
        coord.setOffsetY(y + rows * gy);
        g.setFont(symbolfont);
        for (int j = 0; j < rows; j++) {
            if (start + j >= getRows(height)) break;
            for (int i = 0; i < model.getWidth(); i++) {
                byte c = model.get(new Point(i, start + j));
                painter.paint(g, new Point(i, j), c);
            }
        }
        g.setFont(font);
        for (int j = 0; j < rows; j++) {
            if (start + j >= getRows(height)) break;
            if ((start + j) % 10 == 0) {
                drawLabel(g, x + border, y + (rows - j) * gy, start + j);
            }
        }
        return x + border + getColumnWidth() + border;
    }

    private void drawLabel(Graphics2D g, int x, int y, int row) {
        g.setColor(Color.BLACK);
        g.drawLine(x, y, x + markerWidth - gx, y);
        g.drawString(Integer.toString(row), x, y - gy / 3);
    }
}
