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
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.print.PageFormat;
import java.util.ArrayList;
import java.util.List;

import ch.jbead.BeadCounts;
import ch.jbead.Localization;
import ch.jbead.Model;
import ch.jbead.ReportInfos;

public class ReportInfosPrinter extends PartPrinter {

    private ReportInfos infos;
    private BeadCounts beadcounts;

    private FontMetrics metrics;
    private int countwidth;
    private int bx;

    public ReportInfosPrinter(Model model, Localization localization) {
        super(model, localization);
        infos = new ReportInfos(model, localization);
        beadcounts = new BeadCounts(model);
    }

    @Override
    public List<Integer> layoutColumns(int width, int height) {
        List<Integer> columns = new ArrayList<Integer>();
        FontRenderContext context = new FontRenderContext(null, false, false);
        columns.add(infos.getWidth(font, context) + 10);
        return columns;
    }

    @Override
    public int print(Graphics2D g, PageFormat pageFormat, int x, int y, int column) {
        g.setFont(font);
        metrics = g.getFontMetrics(font);
        bx = font.getSize();
        countwidth = metrics.stringWidth("9999 x");
        y = drawInfos(g, x, y);
        drawBeadColors(g, x, y + bx / 2);
        return x + infos.getWidth(metrics) + 10;
    }

    private int drawInfos(Graphics2D g, int x, int y) {
        int labelx = x + 1;
        int infox = x + 1 + infos.getMaxLabelWidth(metrics) + metrics.stringWidth(" ");
        int dy = bx + 1;
        y += dy;
        g.setColor(Color.BLACK);
        for (String label : infos) {
            g.drawString(label, labelx, y);
            g.drawString(infos.getInfo(label), infox, y);
            y += dy;
        }
        return y;
    }

    private int drawBeadColors(Graphics2D g, int x, int y) {
        g.setStroke(new BasicStroke(0.3f));
        int colorwidth = countwidth + 3 + bx + 5;
        int infowidth = infos.getWidth(metrics);
        int colorsPerRow = infowidth / colorwidth;
        int xx = x;
        int current = 0;
        for (byte color = 0; color < model.getColorCount(); color++) {
            if (!drawBeadColor(g, xx, y, color)) continue;
            xx += colorwidth;
            current++;
            if (current == colorsPerRow) {
                xx = x;
                current = 0;
                y += bx + 3;
            }
        }
        if (current > 0) {
            y += bx;
        }
        return y;
    }

    private boolean drawBeadColor(Graphics2D g, int x, int y, byte color) {
        int count = beadcounts.getCount(color);
        if (count == 0) return false;
        String s = String.format("%d x", count);
        g.drawString(s, x + countwidth - metrics.stringWidth(s), y);
        g.setColor(model.getColor(color));
        g.fillRect(x + countwidth + 3, y - bx, bx, bx);
        g.setColor(Color.BLACK);
        g.drawRect(x + countwidth + 3, y - bx, bx, bx);
        g.setColor(Color.BLACK);
        return true;
    }

}
