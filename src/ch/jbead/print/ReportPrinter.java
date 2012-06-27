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
        int remaining = height - infos.getLineCount() * font.getSize();
        remaining -= getColorCountRows(infowidth) * font.getSize();
        addBeadColumns(columns, height, infowidth, remaining);
        return columns;
    }

    private int getColorCountRows(int infowidth) {
        FontRenderContext context = new FontRenderContext(null, false, false);
        int colors = beadcounts.getColorCount();
        int colorwidth = font.getSize() * 8 / 10 + (int) font.getStringBounds("9999x,", context).getWidth();
        int colorsPerRow = infowidth / colorwidth;
        int rows = (colors + colorsPerRow - 1) / colorsPerRow;
        return rows;
    }

    private void addBeadColumns(List<Integer> columns, int height, int infowidth, int remaining) {
        FontRenderContext context = new FontRenderContext(null, false, false);
        int dx = font.getSize();
        int beadsPerColumn = remaining / dx;
        int beadwidth = font.getSize() + (int) font.getStringBounds("9999x ", context).getWidth();
        int beadColumns = infowidth / beadwidth;
        int remainingBeads = beadlist.size() - beadsPerColumn * beadColumns;
        if (remainingBeads > 0) {
            // we have more bead runs than we can add to this columns, so that we need additional columns
            int beadsPerPageColumn = height / dx;
            int cols = (remainingBeads + beadsPerPageColumn - 1) / beadsPerPageColumn;
            for (int i = 0; i < cols; i++) {
                columns.add(beadwidth);
            }
        }
    }

    @Override
    public int print(Graphics2D g, PageFormat pageFormat, int x, int y, int column) {
        g.setFont(font);
        if (column == 0) {
            y = drawInfos(g, x, y);
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
            // TODO start of bead list
        } else {
            // TODO rest of bead list
        }
        return y;
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

    /*
    private void printReport(Graphics g, PageFormat pageFormat, int reportleft, int reportcols) {
        int x1 = reportleft;
        int x2 = reportleft + mm2px(30);
        int y = mm2py(10);
        int dy = mm2py(5);
        int dx = mm2px(5);

        // Mustername
        g.setColor(Color.BLACK);
        g.drawString(localization.getString("report.pattern"), x1, y);
        g.drawString(model.getFile().getName(), x2, y);
        y += dy;
        // Umfang
        g.drawString(localization.getString("report.circumference"), x1, y);
        g.drawString(Integer.toString(model.getWidth()), x2, y);
        y += dy;
        // Farbrapport
        g.drawString(localization.getString("report.colorrepeat"), x1, y);
        g.drawString(Integer.toString(model.getRepeat()) + " " + localization.getString("report.beads"), x2, y);
        y += dy;
        int repeat = model.getRepeat();
        // Faedelliste...
        if (repeat > 0) {
            //int page = 1;
            int column = 0;
            g.drawString(localization.getString("report.listofbeads"), x1, y);
            y += dy;
            int ystart = y;
            BeadList beads = new BeadList(model);
            for (BeadRun bead : beads) {
                if (bead.getColor() != 0) {
                    g.setColor(model.getColor(bead.getColor()));
                    g.fillRect(x1, y, dx - mm2px(1), dy - mm2py(1));
                    g.setColor(Color.WHITE);
                    g.drawRect(x1, y, dx - mm2px(1), dy - mm2py(1));
                } else {
                    g.setColor(Color.WHITE);
                    g.fillRect(x1, y, dx - mm2px(1), dy - mm2py(1));
                    g.setColor(Color.BLACK);
                    g.drawRect(x1, y, dx - mm2px(1), dy - mm2py(1));
                }
                g.setColor(Color.BLACK);
                g.drawString(Integer.toString(bead.getCount()), x1 + dx + 3, y);
                y += dy;
                if (y >= (int) pageFormat.getHeight() - mm2py(10)) {
                    x1 += dx + mm2px(8);
                    y = ystart;
                    column++;
                    if (column >= reportcols) { // neue Seite und weiter...
                        // TODO handle multipage output, sigh...
                        break;
                        // Printer().NewPage();
                        // x1 = draftleft;
                        // x2 = draftleft + MM2PRx(30);
                        // y = MM2PRy(10);
                        // reportcols = (Printer().PageWidth - draftleft - 10) /
                        // (MM2PRx(5) + MM2PRx(8));
                        // column = 0;
                        // page++;
                        // canvas.Pen.Color = clBlack;
                        // canvas.TextOut (x1, y,
                        // String(Language.STR("Pattern ",
                        // "Muster "))+savedialog.getSelectedFile().getName() +
                        // " - " + Language.STR("page ", "Seite ") +
                        // IntToStr(page));
                        // y += dy;
                        // ystart = y;
                    }
                }
            }
        }
    }
     */
}
