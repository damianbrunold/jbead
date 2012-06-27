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

package ch.jbead;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public class ReportPanel extends BasePanel {

    private static final long serialVersionUID = 1L;

    private Localization localization;

    public ReportPanel(Model model, Selection selection, Localization localization) {
        super(model, selection);
        this.localization = localization;
        model.addListener(this);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        enableAntialiasing(g);
        ReportInfos infos = new ReportInfos(model, localization);
        int dy = dy(g);
        int y = dy;
        y = drawInfos(g, infos, y);
        if (model.getRepeat() > 0) {
            y += dy / 2;
            y = drawBeadCounts(g, y);
            y += dy / 2;
            drawBeadList(g, y);
        }
    }

    private int drawInfos(Graphics g, ReportInfos infos, int y) {
        int x1 = x1();
        int x2 = x1 + infos.getMaxLabelWidth(g) + g.getFontMetrics().stringWidth(" ");
        int dy = dy(g);
        for (String label : infos) {
            drawText(g, x1, x2, y, label, infos.getInfo(label));
            y += dy;
        }
        return y;
    }

    private int drawBeadCounts(Graphics g, int y) {
        FontMetrics fm = g.getFontMetrics();
        BeadCounts counts = new BeadCounts(model);
        g.setColor(Color.BLACK);
        int xx = x1();
        int bx = fm.getAscent();
        for (byte color = 0; color < model.getColorCount(); color++) {
            int count = counts.getCount(color);
            if (count == 0) continue;
            String s = String.format("%d x ", count);
            String t = ", ";
            g.drawString(s, xx, y);
            xx += fm.stringWidth(s);
            g.drawRect(xx, y - bx, bx, bx);
            g.setColor(model.getColor(color));
            g.fillRect(xx + 1, y - bx + 1, bx - 1, bx - 1);
            g.setColor(Color.BLACK);
            xx += bx + 1;
            g.drawString(t, xx, y);
            xx += fm.stringWidth(t);
        }
        return y + dy(g);
    }

    private void drawBeadList(Graphics g, int y) {
        FontMetrics fm = g.getFontMetrics();
        BeadList beads = new BeadList(model);
        int height = fm.getLeading() + g.getFontMetrics().getAscent();
        g.drawString(localization.getString("report.listofbeads"), x1(), y);
        y += 3;
        int ystart = y;
        int x1 = x1();
        int dx = dx(g);
        int dy = dy(g);
        int colwidth = colwidth(g);
        for (BeadRun bead : beads) {
            drawColorCount(g, x1, y, dx, dy, height, bead.getColor(), bead.getCount());
            y += dy;
            if (y >= getHeight() - dy) {
                x1 += colwidth;
                y = ystart;
            }
        }
    }

    private int colwidth(Graphics g) {
        return dx(g) + 2 + g.getFontMetrics().stringWidth("999") + 3;
    }

    private void enableAntialiasing(Graphics g) {
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    }

    private int x1() {
        return 12;
    }

    private int dx(Graphics g) {
        return g.getFontMetrics().getHeight() + 2;
    }

    private int dy(Graphics g) {
        return g.getFontMetrics().getHeight();
    }

    private void drawText(Graphics g, int x1, int x2, int y, String label, String value) {
        g.setColor(Color.BLACK);
        g.drawString(label, x1, y);
        g.drawString(value, x2, y);
    }

    private void drawColorCount(Graphics g, int x1, int y, int dx, int dy, int height, byte col, int count) {
        g.setColor(model.getColor(col));
        g.fillRect(x1 + 1, y + 1, dx - 1, dy - 1);
        g.setColor(Color.DARK_GRAY);
        g.drawRect(x1, y, dx, dy);
        g.setColor(Color.BLACK);
        g.drawString(Integer.toString(count), x1 + dx + 3, y + height);
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
