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
public class NormalPanel extends JComponent {

    private static final long serialVersionUID = 1L;

    private Model model;
    private int offsetx;
    private int maxj;
    private int left;

    public NormalPanel(Model model) {
        this.model = model;
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
        return Math.min(model.getField().getHeight(), getHeight() / model.getGrid() + 1);
    }

    private int getOffsetX() {
        int grid = model.getGrid();
        return getWidth() - 1 - (model.getField().getWidth() + 1) * grid + grid / 2;
    }

    private int x(int i) {
        return left + i * model.getGrid();
    }
    
    private int y(int j) {
        return getHeight() - 1 - j * model.getGrid();
    }
    
    private void paintGrid(Graphics g) {
        BeadField field = model.getField();
        int grid = model.getGrid();
        g.setColor(Color.DARK_GRAY);
        if (model.getScroll() % 2 == 0) {
            for (int i = 0; i < field.getWidth() + 1; i++) {
                for (int jj = 0; jj < maxj; jj += 2) {
                    g.drawLine(x(i), y(jj+1) + 1, x(i), y(jj) + 1);
                }
            }
            for (int i = 0; i <= field.getWidth() + 1; i++) {
                for (int jj = 1; jj < maxj; jj += 2) {
                    g.drawLine(x(i) - grid / 2, y(jj+1) + 1, x(i) - grid / 2, y(jj) + 1);
                }
            }
        } else {
            for (int i = 0; i <= field.getWidth() + 1; i++) {
                for (int jj = 0; jj < maxj; jj += 2) {
                    g.drawLine(x(i) - grid / 2, y(jj+1) + 1, x(i) - grid / 2, y(jj) + 1);
                }
            }
            for (int i = 0; i < field.getWidth() + 1; i++) {
                for (int jj = 1; jj < maxj; jj += 2) {
                    g.drawLine(x(i), y(jj+1) + 1, x(i), y(jj) + 1);
                }
            }
        }
        if (model.getScroll() % 2 == 0) {
            g.drawLine(x(0), y(0), x(field.getWidth()) + 1, y(0));
        }
        for (int jj = 0; jj < maxj; jj++) {
            g.drawLine(x(0) - grid / 2, y(jj), x(field.getWidth()) + grid / 2 + 1, y(jj));
        }
    }

    private void paintBeads(Graphics g) {
        BeadField field = model.getField();
        int grid = model.getGrid();
        for (int i = 0; i < field.getWidth(); i++) {
            for (int jj = 0; jj < maxj; jj++) {
                byte c = field.get(i, jj + model.getScroll());
                g.setColor(model.getColor(c));
                int ii = i;
                int j1 = jj;
                ii = correctCoordinatesX(ii, j1);
                j1 = correctCoordinatesY(ii, j1);
                if (model.getScroll() % 2 == 0) {
                    if (j1 % 2 == 0) {
                        g.fillRect(left + ii * grid + 1, getHeight() - (j1 + 1) * grid, grid, grid);
                    } else {
                        g.fillRect(left - grid / 2 + ii * grid + 1, getHeight() - (j1 + 1) * grid, grid, grid);
                    }
                } else {
                    if (j1 % 2 == 1) {
                        g.fillRect(left + ii * grid + 1, getHeight() - (j1 + 1) * grid, grid, grid);
                    } else {
                        g.fillRect(left - grid / 2 + ii * grid + 1, getHeight() - (j1 + 1) * grid, grid, grid);
                    }
                }
            }
        }
    }

    int correctCoordinatesX(int _i, int _j) {
        BeadField field = model.getField();
        int idx = _i + (_j + model.getScroll()) * field.getWidth();
        int m1 = field.getWidth();
        int m2 = field.getWidth() + 1;
        int k = 0;
        int m = (k % 2 == 0) ? m1 : m2;
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
        BeadField field = model.getField();
        int idx = _i + (_j + model.getScroll()) * field.getWidth();
        int m1 = field.getWidth();
        int m2 = field.getWidth() + 1;
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

    public void updateBead(int _i, int _j) {
        if (!isVisible()) return;

        int grid = model.getGrid();
        int scroll = model.getScroll();
        byte c = model.getField().get(_i, _j + scroll);
        assert (c >= 0 && c <= 9);

        _i = correctCoordinatesX(_i, _j);
        _j = correctCoordinatesY(_i, _j);

        Graphics g = getGraphics();
        g.setColor(model.getColor(c));

        int left = offsetx;

        if (scroll % 2 == 0) {
            if (_j % 2 == 0) {
                g.fillRect(left + _i * grid + 1, getHeight() - (_j + 1) * grid, grid, grid);
            } else {
                g.fillRect(left - grid / 2 + _i * grid + 1, getHeight() - (_j + 1) * grid, grid, grid);
            }
        } else {
            if (_j % 2 == 1) {
                g.fillRect(left + _i * grid + 1, getHeight() - (_j + 1) * grid, grid, grid);
            } else {
                g.fillRect(left - grid / 2 + _i * grid + 1, getHeight() - (_j + 1) * grid, grid, grid);
            }
        }
        g.dispose();
    }

    boolean mouseToField(Point pt) {
        int grid = model.getGrid();
        BeadField field = model.getField();
        int _i = pt.getX();
        int _j = pt.getY();
        int i;
        int jj = (getHeight() - _j) / grid;
        if (model.getScroll() % 2 == 0) {
            if (jj % 2 == 0) {
                if (_i < offsetx || _i > offsetx + field.getWidth() * grid) return false;
                i = (_i - offsetx) / grid;
            } else {
                if (_i < offsetx - grid / 2 || _i > offsetx + field.getWidth() * grid + grid / 2) return false;
                i = (_i - offsetx + grid / 2) / grid;
            }
        } else {
            if (jj % 2 == 1) {
                if (_i < offsetx || _i > offsetx + field.getWidth() * grid) return false;
                i = (_i - offsetx) / grid;
            } else {
                if (_i < offsetx - grid / 2 || _i > offsetx + field.getWidth() * grid + grid / 2) return false;
                i = (_i - offsetx + grid / 2) / grid;
            }
        }
        pt.setX(i);
        pt.setY(jj);
        return true;
    }

}