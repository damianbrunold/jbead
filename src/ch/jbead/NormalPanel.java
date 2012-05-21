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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class NormalPanel extends BasePanel {

    private static final long serialVersionUID = 1L;

    private Model model;
    private int offsetx;
    private int maxj;
    private int left;

    public NormalPanel(Model model, final BeadForm form) {
        this.model = model;
        model.addListener(this);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                form.normalMouseUp(e);
            }
        });
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension((model.getWidth() + 2) * model.getGrid(), 3 * model.getGrid());
    }

    @Override
    public Dimension getPreferredSize() {
        return getMinimumSize();
    }

    @Override
    public Dimension getMaximumSize() {
        return getMinimumSize();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        offsetx = getOffsetX();
        maxj = getMaxJ();
        left = getLeft();
        paintGrid(g);
        paintBeads(g);
    }

    private int getLeft() {
        return offsetx < 0 ? model.getGrid() / 2 : offsetx;
    }

    private int getMaxJ() {
        return Math.min(model.getHeight(), getHeight() / model.getGrid() + 1);
    }

    private int getOffsetX() {
        int grid = model.getGrid();
        return grid / 2 + (getWidth() - 1 - model.getWidth() * grid - grid / 2) / 2;
    }

    private int x(int i) {
        return left + i * model.getGrid();
    }
    
    private int y(int j) {
        return getHeight() - 1 - (j + 1) * model.getGrid();
    }
    
    private void paintGrid(Graphics g) {
        int grid = model.getGrid();
        g.setColor(Color.DARK_GRAY);
        if (model.getScroll() % 2 == 0) {
            for (int i = 0; i < model.getWidth() + 1; i++) {
                for (int jj = -1; jj < maxj; jj += 2) {
                    g.drawLine(x(i), y(jj+1) + 1, x(i), y(jj));
                }
            }
            for (int i = 0; i <= model.getWidth() + 1; i++) {
                for (int jj = 0; jj < maxj; jj += 2) {
                    g.drawLine(x(i) - grid / 2, y(jj+1) + 1, x(i) - grid / 2, y(jj));
                }
            }
            g.drawLine(x(0), y(-1), x(model.getWidth()), y(-1));
        } else {
            for (int i = 0; i <= model.getWidth() + 1; i++) {
                for (int jj = -1; jj < maxj; jj += 2) {
                    g.drawLine(x(i) - grid / 2, y(jj+1) + 1, x(i) - grid / 2, y(jj));
                }
            }
            for (int i = 0; i < model.getWidth() + 1; i++) {
                for (int jj = 0; jj < maxj; jj += 2) {
                    g.drawLine(x(i), y(jj+1) + 1, x(i), y(jj));
                }
            }
            g.drawLine(x(0) - grid / 2, y(-1), x(model.getWidth()) + grid / 2, y(-1));
        }
        for (int jj = 0; jj < maxj; jj++) {
            g.drawLine(x(0) - grid / 2, y(jj), x(model.getWidth()) + grid / 2, y(jj));
        }
    }

    private void paintBeads(Graphics g) {
        int grid = model.getGrid();
        int scroll = model.getScroll();
        for (int i = 0; i < model.getWidth(); i++) {
            for (int j = 0; j < maxj; j++) {
                byte c = model.get(new Point(i, j).scrolled(scroll));
                g.setColor(model.getColor(c));
                int i1 = correctCoordinatesX(i, j);
                int j1 = correctCoordinatesY(i, j);
                if ((j1 + scroll) % 2 == 0) {
                    g.fillRect(x(i1) + 1, y(j1) + 1, grid - 1, grid - 1);
                } else {
                    g.fillRect(x(i1) + 1 - grid / 2, y(j1) + 1, grid - 1, grid - 1);
                }
            }
        }
    }

    int correctCoordinatesX(int _i, int _j) {
        int idx = _i + (_j + model.getScroll()) * model.getWidth();
        int m1 = model.getWidth();
        int m2 = model.getWidth() + 1;
        int k = 0;
        int m = m1;
        while (idx >= m) {
            idx -= m;
            k++;
            m = (k % 2 == 0) ? m1 : m2;
        }
        _i = idx;
        _j = k - model.getScroll();
        return _i;
    }

    int correctCoordinatesY(int _i, int _j) {
        int idx = _i + (_j + model.getScroll()) * model.getWidth();
        int m1 = model.getWidth();
        int m2 = model.getWidth() + 1;
        int k = 0;
        int m = (k % 2 == 0) ? m1 : m2;
        while (idx >= m) {
            idx -= m;
            k++;
            m = (k % 2 == 0) ? m1 : m2;
        }
        _i = idx;
        _j = k - model.getScroll();
        return _j;
    }

    public void redraw(int i, int j) {
        if (!isVisible()) return;
        int grid = model.getGrid();
        int scroll = model.getScroll();
        byte c = model.get(new Point(i, j).scrolled(scroll));
        int _i = correctCoordinatesX(i, j);
        int _j = correctCoordinatesY(i, j);
        Graphics g = getGraphics();
        g.setColor(model.getColor(c));
        if ((scroll + _j) % 2 == 0) {
            g.fillRect(x(_i) + 1, y(_j) + 1, grid - 1, grid - 1);
        } else {
            g.fillRect(x(_i) + 1 - grid / 2, y(_j) + 1, grid - 1, grid - 1);
        }
        g.dispose();
    }

    @Override
    public void redraw(Point pt) {
        redraw(pt.getX(), pt.getY() - model.getScroll());
    }

    boolean mouseToField(Point pt) {
        int grid = model.getGrid();
        int _i = pt.getX();
        int _j = pt.getY();
        int i;
        int jj = (getHeight() - _j) / grid;
        if (model.getScroll() % 2 == 0) {
            if (jj % 2 == 0) {
                if (_i < offsetx || _i > offsetx + model.getWidth() * grid) return false;
                i = (_i - offsetx) / grid;
            } else {
                if (_i < offsetx - grid / 2 || _i > offsetx + model.getWidth() * grid + grid / 2) return false;
                i = (_i - offsetx + grid / 2) / grid;
            }
        } else {
            if (jj % 2 == 1) {
                if (_i < offsetx || _i > offsetx + model.getWidth() * grid) return false;
                i = (_i - offsetx) / grid;
            } else {
                if (_i < offsetx - grid / 2 || _i > offsetx + model.getWidth() * grid + grid / 2) return false;
                i = (_i - offsetx + grid / 2) / grid;
            }
        }
        pt.setX(i);
        pt.setY(jj);
        return true;
    }

}
