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

import javax.swing.JComponent;

/**
 * 
 */
public class ReportPanel extends JComponent {

    private static final long serialVersionUID = 1L;

    private BeadField field;
    private Color[] coltable;
    private int farbrapp;
    private String filename;

    public ReportPanel(BeadField field, Color[] coltable, int farbrapp, String filename) {
        this.field = field;
        this.coltable = coltable;
        this.farbrapp = farbrapp;
        this.filename = filename;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int x1 = 12;
        int x2 = x1 + 100;
        int y = 0;
        int dx = 15;
        int dy = dx;

        // Mustername
        g.setColor(Color.BLACK);
        g.drawString(Language.STR("Pattern:", "Muster:"), x1, y);
        g.drawString(filename, x2, y);
        y += dy;

        // Umfang
        g.drawString(Language.STR("Circumference:", "Umfang:"), x1, y);
        g.drawString(Integer.toString(field.Width()), x2, y);
        y += dy;

        // Farbrapport
        g.drawString(Language.STR("repeat of colors:", "Farbrapport:"), x1, y);
        g.drawString(farbrapp + Language.STR(" beads", " Perlen"), x2, y);
        y += dy;

        // Farben
        // Faedelliste...
        if (farbrapp > 0) {
            g.drawString(Language.STR("List of beads", "Fädelliste"), x1, y);
            y += dy;
            int ystart = y;
            byte col = field.Get(farbrapp - 1);
            int count = 1;
            for (int i = farbrapp - 2; i >= 0; i--) {
                if (field.Get(i) == col) {
                    count++;
                } else {
                    if (col != 0) {
                        g.setColor(coltable[col]);
                        g.fillRect(x1, y, dx, dy);
                    } else {
                        g.setColor(Color.DARK_GRAY);
                        g.drawRect(x1, y, dx, dy);
                    }
                    g.setColor(Color.BLACK);
                    g.drawString(Integer.toString(count), x1 + dx + 3, y);
                    y += dy;
                    col = field.Get(i);
                    count = 1;
                }
                if (y >= getHeight() - 10) {
                    x1 += dx + 24;
                    y = ystart;
                }
            }
            if (y < getHeight() - 3) {
                g.setColor(coltable[col]);
                g.fillRect(x1, y, dx, dy);
                g.setColor(Color.BLACK);
                g.drawString(Integer.toString(count), x1 + dx + 3, y);
            }
        }
    }

}
