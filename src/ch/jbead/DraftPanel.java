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
public class DraftPanel extends JComponent {

    private static final long serialVersionUID = 1L;

    private BeadField field;
    private Color[] colors;
    private int grid;
    private int scroll;
    private int offsetx;

    public DraftPanel(BeadField field, Color[] colors, int grid, int scroll) {
        this.field = field;
        this.colors = colors;
        this.grid = grid;
        this.scroll = scroll;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Grid
        g.setColor(Color.DARK_GRAY);
        offsetx = getWidth() - field.getWidth() * grid - 1;
        int left = offsetx;
        if (left < 0) left = 0;
        int maxj = Math.min(field.getHeight(), getHeight() / grid + 1);
        for (int i = 0; i < field.getWidth() + 1; i++) {
            g.drawLine(left + i * grid, 0, left + i * grid, getHeight() - 1);
        }
        for (int j = 0; j < maxj; j++) {
            g.drawLine(left, getHeight() - 1 - j * grid, left + field.getWidth() * grid, getHeight() - 1 - j * grid);
        }

        // Daten
        for (int i = 0; i < field.getWidth(); i++)
            for (int j = 0; j < maxj; j++) {
                byte c = field.get(i, j + scroll);
                assert (c >= 0 && c <= 9);
                g.setColor(colors[c]);
                g.fillRect(left + i * grid + 1, getHeight() - (j + 1) * grid, grid, grid);
            }

        // Zehnermarkierungen
        g.setColor(Color.DARK_GRAY);
        for (int j = 0; j < maxj; j++) {
            if (((j + scroll) % 10) == 0) {
                g.drawLine(0, getHeight() - j * grid - 1, left - 6, getHeight() - j * grid - 1);
                g.drawString(Integer.toString(j + scroll), 6, getHeight() - j * grid + 1);
            }
        }

        // Auswahl
        // TODO
        // DraftSelectDraw();
    }

    public void redraw(int _i, int _j) {
        if (!isVisible()) return;

        byte c = field.get(_i, _j + scroll);
        assert (c >= 0 && c <= 9);

        Graphics g = getGraphics();
        g.setColor(colors[c]);
        g.fillRect(offsetx + _i * grid + 1, getHeight() - (_j + 1) * grid, grid, grid);
        g.dispose();
    }

    boolean mouseToField(Point pt) {
        int _i = pt.getX();
        int _j = pt.getY();
        int i, jj;
        if (_i < offsetx || _i > offsetx + field.getWidth() * grid) return false;
        i = (_i - offsetx) / grid;
        if (i >= field.getWidth()) return false;
        jj = (getHeight() - _j) / grid;
        _i = i;
        _j = jj;
        return true;
    }

    public void selectPreview(boolean draw, Point pt1, Point pt2) {
        Graphics g = getGraphics();
        g.setColor(draw ? Color.BLACK : Color.DARK_GRAY);
        g.drawRect(offsetx + pt1.getX() * grid, getHeight() - pt1.getY() * grid - 1, offsetx + (pt2.getX() + 1) * grid, getHeight()
                - (pt2.getY() + 1) * grid - 1);
        g.dispose();
    }

    public void linePreview(Point pt1, Point pt2) {
        Graphics g = getGraphics();
        g.setColor(Color.DARK_GRAY);
        g.setXORMode(Color.BLACK);
        g.drawLine(offsetx + pt1.getX() * grid + grid / 2, getHeight() - pt1.getY() * grid - grid / 2, offsetx + pt2.getX() * grid + grid / 2,
                getHeight() - pt2.getY() * grid - grid / 2);
        g.dispose();
    }

    public void drawPrepress(Point pt) {
        Graphics g = getGraphics();
        g.setColor(Color.BLACK);
        g.drawLine(offsetx + pt.getX() * grid + 1, getHeight() - pt.getY() * grid - 2, offsetx + pt.getX() * grid + 1, getHeight()
                - (pt.getY() + 1) * grid);
        g.drawLine(offsetx + pt.getX() * grid + 1, getHeight() - (pt.getY() + 1) * grid, offsetx + (pt.getX() + 1) * grid - 1,
                getHeight() - (pt.getY() + 1) * grid);
        g.setColor(Color.WHITE);
        g.drawLine(offsetx + (pt.getX() + 1) * grid - 1, getHeight() - (pt.getY() + 1) * grid + 1, offsetx + (pt.getX() + 1) * grid - 1,
                getHeight() - pt.getY() * grid - 2);
        g.drawLine(offsetx + (pt.getX() + 1) * grid - 1, getHeight() - pt.getY() * grid - 2, offsetx + pt.getX() * grid, getHeight() - pt.getY()
                * grid - 2);
        g.dispose();
    }

}
