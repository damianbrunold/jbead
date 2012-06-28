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
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.print.PageFormat;
import java.util.ArrayList;
import java.util.List;

import ch.jbead.BeadCounts;
import ch.jbead.BeadList;
import ch.jbead.BeadRun;
import ch.jbead.Localization;
import ch.jbead.Model;
import ch.jbead.ReportInfos;

public class ReportPrinter extends PartPrinter {

    private static final int INFOS_WIDTH = 120;

    private ReportInfos infos;
    private BeadCounts beadcounts;
    private BeadList beadlist;

    public ReportPrinter(Model model, Localization localization) {
        super(model, localization);
        infos = new ReportInfos(model, localization);
        beadcounts = new BeadCounts(model);
        beadlist = new BeadList(model);
    }

    @Override
    public List<Integer> layoutColumns(int width, int height) {
        List<Integer> columns = new ArrayList<Integer>();
        int infowidth = Convert.mm2pt(INFOS_WIDTH);
        columns.add(infowidth);
        int remaining = height - infos.getLineCount() * font.getSize() - font.getSize() / 2;
        remaining -= (getColorCountRows(infowidth) * font.getSize() + font.getSize() / 2);
        addBeadColumns(columns, height, infowidth, remaining);
        return columns;
    }

    private int getColorCountRows(int infowidth) {
        FontRenderContext context = new FontRenderContext(null, false, false);
        int colors = beadcounts.getColorCount();
        int colorwidth = font.getSize() * 8 / 10 + (int) font.getStringBounds("9999x,", context).getWidth();
        int colorsPerRow = infowidth / colorwidth;
        return (colors + colorsPerRow - 1) / colorsPerRow;
    }

    private void addBeadColumns(List<Integer> columns, int height, int infowidth, int remaining) {
        FontRenderContext context = new FontRenderContext(null, false, false);
        int d = font.getSize();
        int beadsPerColumn = remaining / (d + 2);
        int beadwidth = font.getSize() + (int) font.getStringBounds("9999x ", context).getWidth();
        int beadColumns = infowidth / beadwidth;
        int remainingBeads = beadlist.size() - beadsPerColumn * beadColumns;
        if (remainingBeads > 0) {
            // we have more bead runs than we can add to this columns, so that we need additional columns
            int beadsPerPageColumn = height / (d + 2);
            int cols = (remainingBeads + beadsPerPageColumn - 1) / beadsPerPageColumn;
            for (int i = 0; i < cols; i++) {
                columns.add(beadwidth);
            }
        }
    }

    @Override
    public int print(Graphics2D g, PageFormat pageFormat, int x, int y, int column) {
        int height = (int) pageFormat.getImageableHeight();
        g.setFont(font);
        if (column == 0) {
            y = drawInfos(g, x, y);
            y += font.getSize() / 2;
            y = drawBeadColors(g, x, y);
            y += font.getSize() / 2;
            y = drawBeadListStart(g, x, y, height);
            return x + Convert.mm2pt(INFOS_WIDTH);
        } else {
            y = drawBeadList(g, x, y, height, column);
            int beadwidth = font.getSize() + 3 + g.getFontMetrics().stringWidth("9999x ");
            return x + beadwidth;
        }
    }

    private int drawInfos(Graphics g, int x, int y) {
        FontMetrics metrics = g.getFontMetrics();
        int labelx = x;
        int infox = x + infos.getMaxLabelWidth(metrics) + metrics.stringWidth(" ");
        int dy = metrics.getHeight();
        y += metrics.getLeading() + metrics.getAscent();
        g.setColor(Color.BLACK);
        for (String label : infos) {
            g.drawString(label, labelx, y);
            g.drawString(infos.getInfo(label), infox, y);
            y += dy;
        }
        return y;
    }

    private int drawBeadColors(Graphics2D g, int x, int y) {
        FontMetrics metrics = g.getFontMetrics();
        int infowidth = Convert.mm2pt(INFOS_WIDTH);
        int colorwidth = font.getSize() * 8 / 10 + metrics.stringWidth("999x,");
        int colorsPerRow = infowidth / colorwidth;
        int bx = metrics.getAscent();
        int xx = x;
        int current = 0;
        for (byte color = 0; color < model.getColorCount(); color++) {
            int count = beadcounts.getCount(color);
            if (count == 0) continue;
            String s = String.format("%d x ", count);
            String t = ", ";
            g.drawString(s, xx, y);
            xx += metrics.stringWidth(s);
            g.setColor(model.getColor(color));
            g.fillRect(xx, y - bx, bx, bx);
            g.setColor(Color.BLACK);
            g.drawRect(xx, y - bx, bx, bx);
            g.setColor(Color.BLACK);
            xx += bx + 1;
            g.drawString(t, xx, y);
            xx += metrics.stringWidth(t);
            current++;
            if (current == colorsPerRow) {
                xx = x;
                current = 0;
                y += font.getSize();
            }
        }
        if (current > 0) {
            y += font.getSize();
        }
        return y;
    }

    private int drawBeadListStart(Graphics2D g, int x, int y, int height) {
        FontMetrics metrics = g.getFontMetrics();
        int infowidth = Convert.mm2pt(INFOS_WIDTH);
        int remaining = height - y;
        int d = font.getSize();
        int beadsPerColumn = remaining / d;
        int beadwidth = d + 3 + metrics.stringWidth("9999x ");
        int beadColumns = infowidth / beadwidth;
        int column = 0;
        int row = 0;
        int maxy = y;
        for (BeadRun bead : beadlist) {
            int index = column * beadsPerColumn + row;
            if (index >= beadColumns * beadsPerColumn) break;
            int xx = x + column * beadwidth;
            int yy = y + row * (d + 2);
            maxy = Math.max(maxy, yy);
            g.setColor(model.getColor(bead.getColor()));
            g.fillRect(xx, yy, d, d);
            g.setColor(Color.BLACK);
            g.drawRect(xx, yy, d, d);
            g.setColor(Color.BLACK);
            g.drawString(Integer.toString(bead.getCount()), xx + d + 3, yy + metrics.getLeading() + metrics.getAscent());
            row++;
            if (row == beadsPerColumn) {
                row = 0;
                column++;
            }
        }
        return maxy;
    }

    private int drawBeadList(Graphics2D g, int x, int y, int height, int column) {
        FontMetrics metrics = g.getFontMetrics();
        int infowidth = Convert.mm2pt(INFOS_WIDTH);
        int remaining = height - y;
        int d = font.getSize();
        int beadsPerColumn = remaining / d;
        int beadwidth = d + 3 + metrics.stringWidth("9999x ");
        int beadColumns = infowidth / beadwidth;
        int remainingBeads = beadlist.size() - beadsPerColumn * beadColumns;
        if (remainingBeads > 0) {
            int beadsPerPageColumn = height / d;
            int start = beadsPerColumn * beadColumns + column * beadsPerPageColumn;
            for (int index = start; index < start + beadsPerPageColumn; index++) {
                if (index >= beadlist.size()) break;
                BeadRun bead = beadlist.get(index);
                g.setColor(model.getColor(bead.getColor()));
                g.fillRect(x, y, d, d);
                g.setColor(Color.BLACK);
                g.drawRect(x, y, d, d);
                g.setColor(Color.BLACK);
                g.drawString(Integer.toString(bead.getCount()), x + d + 3, y + metrics.getLeading() + metrics.getAscent());
                y += d + 2;
            }
        }
        return y;
    }

}
