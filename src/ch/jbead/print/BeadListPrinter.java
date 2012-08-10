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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.print.PageFormat;
import java.util.ArrayList;
import java.util.List;

import ch.jbead.BeadList;
import ch.jbead.BeadPainter;
import ch.jbead.BeadRun;
import ch.jbead.Localization;
import ch.jbead.Model;
import ch.jbead.Point;
import ch.jbead.SimpleCoordinateCalculator;
import ch.jbead.View;

public class BeadListPrinter extends PartPrinter {

    private BeadList beadlist;

    public BeadListPrinter(Model model, View view, Localization localization) {
        super(model, view, localization);
        beadlist = new BeadList(model);
    }

    @Override
    public List<Integer> layoutColumns(int width, int height) {
        List<Integer> columns = new ArrayList<Integer>();
        addBeadColumns(columns, height);
        return columns;
    }

    private int getBeadsPerColumn(int height) {
        return height / (font.getSize() + 2);
    }

    private void addBeadColumns(List<Integer> columns, int height) {
        FontRenderContext context = new FontRenderContext(null, false, false);
        int beadsPerColumn = getBeadsPerColumn(height);
        int colWidth = font.getSize() + 3 + (int) font.getStringBounds("9999x ", context).getWidth();
        int cols = (beadlist.size() + beadsPerColumn - 1) / beadsPerColumn;
        for (int i = 0; i < cols; i++) {
            columns.add(colWidth);
        }
    }

    @Override
    public int print(Graphics2D g, PageFormat pageFormat, int x, int y, int column) {
        int height = (int) pageFormat.getImageableHeight();
        g.setFont(font);
        FontMetrics metrics = g.getFontMetrics(font);
        y = drawBeadList(g, x, y, height, column);
        int colWidth = font.getSize() + 3 + metrics.stringWidth("9999x ");
        return x + colWidth;
    }

    private int drawBeadList(Graphics2D g, int x, int y, int height, int column) {
        g.setStroke(new BasicStroke(0.3f));
        int d = font.getSize();
        SimpleCoordinateCalculator coord = new SimpleCoordinateCalculator(d, d);
        Font symbolfont = new Font("SansSerif", Font.PLAIN, d - 2);
        BeadPainter painter = new BeadPainter(coord, model, view, symbolfont);
        int beadsPerColumn = getBeadsPerColumn(height);
        int start = beadsPerColumn * column;
        for (int index = start; index < start + beadsPerColumn; index++) {
            if (index >= beadlist.size()) break;
            BeadRun bead = beadlist.get(index);
            coord.setOffsetX(x);
            coord.setOffsetY(y + d);
            g.setFont(symbolfont);
            painter.paint(g, new Point(0, 0), bead.getColor());
            g.setColor(Color.BLACK);
            g.setFont(font);
            g.drawString(Integer.toString(bead.getCount()), x + d + 3, y + font.getSize());
            y += d + 2;
        }
        return y;
    }

}
