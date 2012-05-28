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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.BorderFactory;

/**
 * 
 */
public class ReportPanel extends BasePanel {

    private static final long serialVersionUID = 1L;

    private Model model;
    private Localization localization;

    public ReportPanel(Model model, Localization localization) {
        this.model = model;
        this.localization = localization;
        model.addListener(this);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // TODO extract data in a model object and determine x2 according the the longest string in the locale!
        int dx = g.getFontMetrics().getHeight() + 2;
        int dy = dx;
        int x1 = 12;
        int x2 = x1 + g.getFontMetrics().stringWidth(localization.getString("report.colorrepeat")) + dx / 2;
        int y = dy;
        int colwidth = dx + 2 + g.getFontMetrics().stringWidth("999") + 3;

        drawText(g, x1, x2, y, "report.pattern", model.getFile().getPath());
        y += dy;

        drawText(g, x1, x2, y, "report.circumference", Integer.toString(model.getWidth()));
        y += dy;

        drawText(g, x1, x2, y, "report.colorrepeat", model.getColorRepeat() + " " + localization.getString("report.beads"));
        y += dy;

        // Farben
        // Faedelliste...
        if (model.getColorRepeat() > 0) {
            int height = g.getFontMetrics().getLeading() + g.getFontMetrics().getAscent();
            g.drawString(localization.getString("report.listofbeads"), x1, y);
            y += dy;
            int ystart = y;
            byte col = model.get(model.getColorRepeat() - 1);
            int count = 1;
            for (int i = model.getColorRepeat() - 2; i >= 0; i--) {
                if (model.get(i) == col) {
                    count++;
                } else {
                    drawColorCount(g, x1, y, dx, dy, height, col, count);
                    y += dy;
                    col = model.get(i);
                    count = 1;
                    if (y >= getHeight() - dy) {
                        x1 += colwidth;
                        y = ystart;
                    }
                }
            }
            if (y >= getHeight() - dy) {
                x1 += colwidth;
                y = ystart;
            }
            drawColorCount(g, x1, y, dx, dy, height, col, count);
        }
    }

    private void drawText(Graphics g, int x1, int x2, int y, String key, String value) {
        g.setColor(Color.BLACK);
        g.drawString(localization.getString(key), x1, y);
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
    public void repeatChanged(int repeat, int colorRepeat) {
        repaint();
    }

}
