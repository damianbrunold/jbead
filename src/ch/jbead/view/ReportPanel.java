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

package ch.jbead.view;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import ch.jbead.BeadCounts;
import ch.jbead.BeadList;
import ch.jbead.BeadPainter;
import ch.jbead.BeadRun;
import ch.jbead.Localization;
import ch.jbead.Model;
import ch.jbead.Point;
import ch.jbead.ReportInfos;
import ch.jbead.Selection;
import ch.jbead.SimpleCoordinateCalculator;
import ch.jbead.View;

public class ReportPanel extends BasePanel {

    private static final long serialVersionUID = 1L;

    private Localization localization;
    private Font defaultfont;

    public ReportPanel(Model model, View view, Selection selection, Localization localization) {
        super(model, view, selection);
        this.localization = localization;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        defaultfont = g.getFont();
        enableAntialiasing(g);
        ReportInfos infos = new ReportInfos(model, localization);
        int dy = dy(g);
        int y = drawInfos(g, infos, dy);
        if (model.getRepeat() > 0) {
            y += dy / 2;
            y = drawColorList(g, y);
            y += dy / 2;
            drawBeadList(g, y);
        }
    }

    private int drawInfos(Graphics g, ReportInfos infos, int y) {
        g.setFont(defaultfont);
        FontMetrics metrics = g.getFontMetrics();
        int x1 = x1();
        int x2 = x1 + infos.getMaxLabelWidth(metrics) + metrics.stringWidth(" ");
        int dy = dy(g);
        for (String label : infos) {
            drawText(g, x1, x2, y, label, infos.getInfo(label));
            y += dy;
        }
        return y;
    }

    private int drawColorList(Graphics g, int y) {
        FontMetrics metrics = g.getFontMetrics();
        BeadCounts counts = new BeadCounts(model);
        int x = x1();
        int bx = dx(g);
        int countw = metrics.stringWidth("9999 x");
        int w = countw + 4 + bx + 1 + bx;
        SimpleCoordinateCalculator coord = new SimpleCoordinateCalculator(bx, bx);
        Font symbolfont = new Font("SansSerif", Font.PLAIN, bx - 2);
        BeadPainter painter = new BeadPainter(coord, model, view, symbolfont);
        for (byte color = 0; color < model.getColorCount(); color++) {
            if (!drawColorCount(g, x, y, bx, color, counts, metrics, painter, coord, symbolfont))
                continue;
            x += w;
            if (x + w > getWidth()) {
                x = x1();
                y += dy(g) + 3;
            }
        }
        if (x != x1()) {
            y += dy(g) + 3;
        }
        return y;
    }

    private boolean drawColorCount(Graphics g, int x, int y, int bx, byte color, BeadCounts counts,
            FontMetrics metrics, BeadPainter painter, SimpleCoordinateCalculator coord, Font symbolfont) {
        int count = counts.getCount(color);
        if (count == 0) return false;
        String s = String.format("%d x", count);
        g.setColor(Color.BLACK);
        g.setFont(defaultfont);
        int cw = metrics.stringWidth("9999 x");
        g.drawString(s, x + cw - metrics.stringWidth(s), y);
        coord.setOffsetX(x + cw + 4);
        coord.setOffsetY(y);
        g.setFont(symbolfont);
        painter.paint(g, new Point(0, 0), color);
        return true;
    }

    private void drawBeadList(Graphics g, int y) {
        FontMetrics metrics = g.getFontMetrics();
        BeadList beads = new BeadList(model);
        int height = metrics.getLeading() + g.getFontMetrics().getAscent();
        g.setFont(defaultfont);
        g.drawString(localization.getString("report.listofbeads"), x1(), y);
        y += 3;
        int ystart = y;
        int x1 = x1();
        int dx = dx(g);
        int dy = dy(g);
        int colwidth = colwidth(g);
        SimpleCoordinateCalculator coord = new SimpleCoordinateCalculator(dx, dy);
        Font symbolfont = new Font("SansSerif", Font.PLAIN, dx - 2);
        BeadPainter painter = new BeadPainter(coord, model, view, symbolfont);
        for (BeadRun bead : beads) {
            drawBeadCount(g, x1, y, dx, dy, height, bead.getColor(), bead.getCount(), painter, coord, symbolfont);
            y += dy + 3;
            if (y >= getHeight() - dy) {
                x1 += colwidth;
                y = ystart;
            }
        }
    }

    private int colwidth(Graphics g) {
        return dx(g) + 2 + g.getFontMetrics().stringWidth("9999") + 3;
    }

    private void enableAntialiasing(Graphics g) {
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    }

    private int x1() {
        return 12;
    }

    private int dx(Graphics g) {
        return g.getFontMetrics(defaultfont).getHeight();
    }

    private int dy(Graphics g) {
        return dx(g);
    }

    private void drawText(Graphics g, int x1, int x2, int y, String label, String value) {
        g.setColor(Color.BLACK);
        g.drawString(label, x1, y);
        g.drawString(value, x2, y);
    }

    private void drawBeadCount(Graphics g, int x, int y, int dx, int dy, int height, byte color, int count,
            BeadPainter painter, SimpleCoordinateCalculator coord, Font symbolfont) {
        coord.setOffsetX(x);
        coord.setOffsetY(y + dy);
        g.setFont(symbolfont);
        painter.paint(g, new Point(0, 0), color);
        g.setColor(Color.BLACK);
        g.setFont(defaultfont);
        g.drawString(Integer.toString(count), x + dx + 3, y + height);
    }

    @Override
    public void redraw(Point pt) {
        // empty
    }

    @Override
    public void repeatChanged(int repeat) {
        repaint();
    }

}
