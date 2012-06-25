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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;

import ch.jbead.Localization;
import ch.jbead.Model;
import ch.jbead.Point;


public class DraftPrinter extends GridPrinter {

    private int markerWidth = 72;

    public DraftPrinter(Model model, Localization localization) {
        super(model, localization);
    }

    protected int getColumnWidth() {
        return model.getWidth() * gx + markerWidth;
    }

    @Override
    public int print(Graphics2D g, PageFormat pageFormat, int x, int y, int column) {
        g.setStroke(new BasicStroke(0.0f));
        int height = (int) pageFormat.getImageableHeight();
        x += border;
        int rows = getRowsPerColumn(height);
        int start = rows * column;
        for (int j = 0; j < rows; j++) {
            for (int i = 0; i < model.getWidth(); i++) {
                byte c = model.get(new Point(i, start + j));
                if (c > 0) {
                    g.setColor(model.getColor(c));
                    g.fillRect(x + markerWidth + i * gx, y + (rows - j - 1) * gy, gx, gy);
                }
                g.setColor(Color.BLACK);
                g.drawRect(x + markerWidth + i * gx, y + (rows - j - 1) * gy, gx, gy);
            }
            if ((start + j) % 10 == 0) {
                g.setColor(Color.BLACK);
                g.drawLine(x, y + (rows - j) * gy, x + markerWidth - gx, y + (rows -j) * gy);
                g.drawString(Integer.toString(start + j), x, y + (rows - j) * gy);
            }
        }
        return x + border;
    }

}
