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

    public NormalPanel(Model model) {
        this.model = model;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Grid
        int grid = model.getGrid();
        BeadField field = model.getField();
        g.setColor(Color.DARK_GRAY);
        offsetx = getWidth() - 1 - (field.getWidth() + 1) * grid + grid / 2;
        int left = offsetx;
        if (left < 0) left = grid / 2;
        int maxj = Math.min(field.getHeight(), getHeight() / grid + 1);
        if (model.getScroll() % 2 == 0) {
            for (int i = 0; i < field.getWidth() + 1; i++) {
                for (int jj = 0; jj < maxj; jj += 2) {
                    g.drawLine(left + i * grid, getHeight() - (jj + 1) * grid, left + i * grid, getHeight() - jj * grid);
                }
            }
            for (int i = 0; i <= field.getWidth() + 1; i++) {
                for (int jj = 1; jj < maxj; jj += 2) {
                    g.drawLine(left + i * grid - grid / 2, getHeight() - (jj + 1) * grid, left + i * grid - grid / 2, getHeight() - jj * grid);
                }
            }
        } else {
            for (int i = 0; i <= field.getWidth() + 1; i++) {
                for (int jj = 0; jj < maxj; jj += 2) {
                    g.drawLine(left + i * grid - grid / 2, getHeight() - (jj + 1) * grid, left + i * grid - grid / 2, getHeight() - jj * grid);
                }
            }
            for (int i = 0; i < field.getWidth() + 1; i++) {
                for (int jj = 1; jj < maxj; jj += 2) {
                    g.drawLine(left + i * grid, getHeight() - (jj + 1) * grid, left + i * grid, getHeight() - jj * grid);
                }
            }
        }
        if (model.getScroll() % 2 == 0) {
            g.drawLine(left, getHeight() - 1, left + field.getWidth() * grid + 1, getHeight() - 1);
            for (int jj = 1; jj < maxj; jj++) {
                g.drawLine(left - grid / 2, getHeight() - 1 - jj * grid, left + field.getWidth() * grid + grid / 2 + 1, getHeight() - 1 - jj * grid);
            }
        } else {
            for (int jj = 0; jj < maxj; jj++) {
                g.drawLine(left - grid / 2, getHeight() - 1 - jj * grid, left + field.getWidth() * grid + grid / 2 + 1, getHeight() - 1 - jj * grid);
            }
        }

        // Data
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
