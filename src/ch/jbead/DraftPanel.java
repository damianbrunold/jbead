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
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;

/**
 * 
 */
public class DraftPanel extends JComponent {

    private static final long serialVersionUID = 1L;

    private static final int GAP = 6;
    private static final int MARKER_WIDTH = 30;

    private Model model;
    private int offsetx;
    private int maxj;

    public DraftPanel(Model model, final BeadForm form) {
        this.model = model;
        setBackground(Color.LIGHT_GRAY);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                form.draftMouseDown(e);
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                form.draftMouseMove(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                form.draftMouseUp(e);
            }
        });
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

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(model.getField().getWidth() * model.getGrid() + MARKER_WIDTH + GAP, 3 * model.getGrid());
    }

    @Override
    public Dimension getPreferredSize() {
        return getMinimumSize();
    }

    @Override
    public Dimension getMaximumSize() {
        return getMinimumSize();
    }

    private int getOffsetX() {
        return Math.max(3 + MARKER_WIDTH + GAP, (getWidth() - model.getField().getWidth() * model.getGrid() - 1) / 2);
    }

    private int getMaxJ() {
        return Math.min(model.getField().getHeight(), getHeight() / model.getGrid() + 1);
    }

    private int x(int i) {
        return offsetx + i * model.getGrid();
    }
    
    private int y(int j) {
        return getHeight() - 1 - (j + 1) * model.getGrid();
    }
    
    private int paintGrid(Graphics g) {
        BeadField field = model.getField();
        g.setColor(Color.DARK_GRAY);
        for (int i = 0; i < field.getWidth() + 1; i++) {
            g.drawLine(x(i), 0, x(i), getHeight() - 1);
        }
        for (int j = -1; j < maxj; j++) {
            g.drawLine(x(0), y(j), x(field.getWidth()), y(j));
        }
        return maxj;
    }

    private void paintBeads(Graphics g) {
        int grid = model.getGrid();
        int scroll = model.getScroll();
        BeadField field = model.getField();
        for (int i = 0; i < field.getWidth(); i++) {
            for (int j = 0; j < maxj; j++) {
                byte c = field.get(i, j + scroll);
                g.setColor(model.getColor(c));
                g.fillRect(x(i) + 1, y(j) + 1, grid - 1, grid - 1);
            }
        }
    }

    private void paintMarkers(Graphics g) {
        int scroll = model.getScroll();
        g.setColor(Color.DARK_GRAY);
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int fontHeight = g.getFontMetrics().getAscent();
        int grid = model.getGrid();
        for (int j = 0; j < maxj; j++) {
            if (((j + scroll) % 10) == 0) {
                g.drawLine(offsetx - GAP - MARKER_WIDTH, y(j) + grid, offsetx - GAP, y(j) + grid);
                String label = Integer.toString(j + scroll);
                int labelWidth = g.getFontMetrics().stringWidth(label);
                g.drawString(label, offsetx - GAP - MARKER_WIDTH + (MARKER_WIDTH - labelWidth) / 2, y(j) + grid + fontHeight + 1);
            }
        }
    }

    private void paintSelection(Graphics g) {
        // TODO
        // DraftSelectDraw();
    }

    public void redraw(int i, int j) {
        if (!isVisible()) return;
        byte c = model.getField().get(i, j + model.getScroll());
        Graphics g = getGraphics();
        g.setColor(model.getColor(c));
        g.fillRect(x(i) + 1, y(j) + 1, model.getGrid() - 1, model.getGrid() - 1);
        g.dispose();
    }

    public void selectPreview(boolean draw, Point pt1, Point pt2) {
        Graphics g = getGraphics();
        g.setColor(draw ? Color.RED : Color.DARK_GRAY);
        g.drawRect(x(pt1.getX()), y(pt2.getY()), (pt2.getX() - pt1.getX()) * model.getGrid(), (pt2.getY() - pt1.getY()) * model.getGrid());
        g.dispose();
    }

    public void linePreview(Point pt1, Point pt2) {
        int grid = model.getGrid();
        Graphics g = getGraphics();
        g.setColor(Color.BLACK);
        g.setXORMode(Color.BLACK);
        g.drawLine(x(pt1.getX()) + grid / 2, y(pt1.getY()) - grid / 2, x(pt2.getX()) + grid / 2, y(pt2.getY()) - grid / 2);
        g.dispose();
    }

    public void drawPrepress(Point pt) {
        Graphics g = getGraphics();
        int grid = model.getGrid();
        int x0 = x(pt.getX());
        int y0 = y(pt.getY());
        g.setColor(Color.BLACK);
        g.drawLine(x0 + 1, y0 + grid - 1, x0 + 1, y0 + 1);
        g.drawLine(x0 + 1, y0 + 1, x0 + grid - 1, y0 + 1);
        g.setColor(Color.WHITE);
        g.drawLine(x0 + 1 + 1, y0 + grid - 1, x0 + grid - 1, y0 + grid - 1);
        g.drawLine(x0 + grid - 1, y0 + grid - 1, x0 + grid - 1, y0 + 1);
        g.dispose();
    }

    boolean mouseToField(Point pt) {
        BeadField field = model.getField();
        int grid = model.getGrid();
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
