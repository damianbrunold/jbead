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
    private int maxj;

    public DraftPanel(BeadField field, Color[] colors, int grid, int scroll) {
        this.field = field;
        this.colors = colors;
        this.grid = grid;
        this.scroll = scroll;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        offsetx = getOffsetX();
        maxj = getMaxJ();
        paintGrid(g);
        paintBeads(g);
        paintMarkers(g);
        paintSelection(g);
    }

    private int getOffsetX() {
        return Math.max(0, getWidth() - field.getWidth() * grid - 1);
    }

    private int getMaxJ() {
        return Math.min(field.getHeight(), getHeight() / grid + 1);
    }

    private int x(int i) {
        return offsetx + i * grid;
    }
    
    private int y(int j) {
        return getHeight() - 1 - j * grid;
    }
    
    private int paintGrid(Graphics g) {
        g.setColor(Color.DARK_GRAY);
        for (int i = 0; i < field.getWidth() + 1; i++) {
            g.drawLine(x(i), 0, x(i), getHeight() - 1);
        }
        for (int j = 0; j < maxj; j++) {
            g.drawLine(x(0), y(j), x(field.getWidth()), y(j));
        }
        return maxj;
    }

    private void paintBeads(Graphics g) {
        for (int i = 0; i < field.getWidth(); i++) {
            for (int j = 0; j < maxj; j++) {
                byte c = field.get(i, j + scroll);
                g.setColor(colors[c]);
                g.fillRect(x(i) + 1, y(j + 1) + 1, grid, grid);
            }
        }
    }

    private void paintMarkers(Graphics g) {
        g.setColor(Color.DARK_GRAY);
        for (int j = 0; j < maxj; j++) {
            if (((j + scroll) % 10) == 0) {
                g.drawLine(0, getHeight() - j * grid - 1, offsetx - 6, getHeight() - j * grid - 1);
                g.drawString(Integer.toString(j + scroll), 6, getHeight() - j * grid + 1);
            }
        }
    }

    private void paintSelection(Graphics g) {
        // TODO
        // DraftSelectDraw();
    }

    public void redraw(int i, int j) {
        if (!isVisible()) return;
        byte c = field.get(i, j + scroll);
        Graphics g = getGraphics();
        g.setColor(colors[c]);
        g.fillRect(x(i) + 1, y(j) + 1, grid, grid);
        g.dispose();
    }

    public void selectPreview(boolean draw, Point pt1, Point pt2) {
        Graphics g = getGraphics();
        g.setColor(draw ? Color.BLACK : Color.DARK_GRAY);
        g.drawRect(x(pt1.getX()), y(pt1.getY()), x(pt2.getX() + 1), y(pt2.getY() + 1));
        g.dispose();
    }

    public void linePreview(Point pt1, Point pt2) {
        Graphics g = getGraphics();
        g.setColor(Color.DARK_GRAY);
        g.setXORMode(Color.BLACK);
        g.drawLine(x(pt1.getX()) + grid / 2, y(pt1.getY()) - grid / 2, x(pt2.getX()) + grid / 2, y(pt2.getY()) - grid / 2);
        g.dispose();
    }

    public void drawPrepress(Point pt) {
        Graphics g = getGraphics();
        g.setColor(Color.BLACK);
        g.drawLine(x(pt.getX()) + 1, y(pt.getY()) + 1, x(pt.getX()) + 1, y(pt.getY() + 1) + 1);
        g.drawLine(x(pt.getX()) + 1, y(pt.getY()) + 1, x(pt.getX()) - 1, y(pt.getY() + 1) + 1);
        g.setColor(Color.WHITE);
        g.drawLine(x(pt.getX() + 1) - 1, y(pt.getY() + 1) + 1, x(pt.getX() + 1) - 1, y(pt.getY()) - 1);
        g.drawLine(x(pt.getX() + 1) - 1, y(pt.getY()) - 1, x(pt.getX()), y(pt.getY()) - 1);
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
        pt.setX(i);
        pt.setY(jj);
        return true;
    }

}
