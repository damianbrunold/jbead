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
import java.awt.print.PageFormat;
import java.util.ArrayList;
import java.util.List;

import ch.jbead.BeadCounts;
import ch.jbead.BeadList;
import ch.jbead.Localization;
import ch.jbead.Model;
import ch.jbead.ReportInfos;

public class ReportPrinter extends PartPrinter {

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
    public List<Integer> layoutColumns(int height) {
        List<Integer> columns = new ArrayList<Integer>();
        columns.add(Convert.mm2pt(120));
        // TODO add bead counts
        // TODO fill in bead list and add more columns as appropriate
        return columns;
    }

    @Override
    public int print(Graphics2D g, PageFormat pageFormat, int x, int y, int column) {
        if (column == 0) {
            y = drawInfos(g, x, y);
            // TODO bead counts
            // TODO start of bead list
        } else {
            // TODO rest of bead list
        }
        return y;
    }

    private int drawInfos(Graphics g, int x, int y) {
        FontMetrics metrics = g.getFontMetrics();
        int labelx = x;
        int infox = x + infos.getMaxLabelWidth(g) + metrics.stringWidth(" ");
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
