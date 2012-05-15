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
public class SimulationPanel extends JComponent {

    private static final long serialVersionUID = 1L;

    private Model model;
    private int offsetx;

    public SimulationPanel(Model model) {
        this.model = model;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // model.getGrid()
        g.setColor(Color.DARK_GRAY);
        int grid = model.getGrid();
        offsetx = getWidth() - 1 - (model.getField().getWidth() + 1) * grid / 2 + grid / 2;
        int left = offsetx;
        if (left < 0) left = grid / 2;
        int maxj = Math.min(model.getField().getHeight(), getHeight() / grid + 1);
        int w = model.getField().getWidth() / 2;
        if (model.getScroll() % 2 == 0) {
            for (int j = 0; j < maxj; j += 2) {
                for (int i = 0; i < w + 1; i++) {
                    if (j == 0 && model.getScroll() == 0 && i < model.getShift()) continue;
                    g.drawLine(left + i * grid, getHeight() - (j + 1) * grid, left + i * grid, getHeight() - j * grid);
                }
                if (j > 0 || model.getScroll() > 0) {
                    g.drawLine(left - grid / 2, getHeight() - (j + 1) * grid, left - grid / 2, getHeight() - j * grid);
                }
            }
            for (int j = 1; j < maxj; j += 2) {
                for (int i = 0; i < w + 1; i++) {
                    g.drawLine(left + i * grid - grid / 2, getHeight() - (j + 1) * grid, left + i * grid - grid / 2, getHeight() - j * grid);
                }
                g.drawLine(left + model.getField().getWidth() * grid, getHeight() - (j + 1) * grid, left + model.getField().getWidth() * grid, getHeight() - j * grid);
            }
        } else {
            for (int j = 0; j < maxj; j += 2) {
                for (int i = 0; i < w + 1; i++) {
                    g.drawLine(left + i * grid - grid / 2, getHeight() - (j + 1) * grid, left + i * grid - grid / 2, getHeight() - j * grid);
                }
                g.drawLine(left + model.getField().getWidth() * grid, getHeight() - (j + 1) * grid, left + model.getField().getWidth() * grid, getHeight() - j * grid);
            }
            for (int j = 1; j < maxj; j += 2) {
                for (int i = 0; i < w + 1; i++) {
                    g.drawLine(left + i * grid, getHeight() - (j + 1) * grid, left + i * grid, getHeight() - j * grid);
                }
                g.drawLine(left - grid / 2, getHeight() - (j + 1) * grid, left - grid / 2, getHeight() - j * grid);
            }
        }
        if (model.getScroll() % 2 == 0) {
            if (model.getScroll() == 0) {
                g.drawLine(left + model.getShift() * grid, getHeight() - 1, left + w * grid + 1, getHeight() - 1);
                for (int j = 1; j < maxj; j++) {
                    g.drawLine(left - grid / 2, getHeight() - 1 - j * grid, left + w * grid + 1, getHeight() - 1 - j * grid);
                }
                g.drawLine(left + w * grid, 0, left + w * grid, getHeight() - 1 - grid);
            } else {
                for (int j = 0; j < maxj; j++) {
                    g.drawLine(left - grid / 2, getHeight() - 1 - j * grid, left + w * grid + 1, getHeight() - 1 - j * grid);
                }
                g.drawLine(left + w * grid, 0, left + w * grid, getHeight() - 1 - grid);
            }
        } else {
            for (int j = 0; j < maxj; j++) {
                g.drawLine(left - grid / 2, getHeight() - 1 - j * grid, left + w * grid + 1, getHeight() - 1 - j * grid);
            }
            g.drawLine(left + w * grid, 0, left + w * grid, getHeight() - 1);
        }

        // Data
        for (int i = 0; i < model.getField().getWidth(); i++) {
            for (int j = 0; j < maxj; j++) {
                byte c = model.getField().get(i, j + model.getScroll());
                assert (c >= 0 && c <= 9);
                g.setColor(model.getColor(c));
                int idx = i + model.getField().getWidth() * j + model.getShift();
                int ii = idx % model.getField().getWidth();
                int jj = idx / model.getField().getWidth();
                ii = correctCoordinatesX(ii, jj);
                jj = correctCoordinatesY(ii, jj);
                if (ii > w && ii != model.getField().getWidth()) continue;
                if (model.getScroll() % 2 == 0) {
                    if (jj % 2 == 0) {
                        if (ii == w) continue;
                        g.fillRect(left + ii * grid + 1, getHeight() - (jj + 1) * grid, grid, grid);
                    } else {
                        if (ii != model.getField().getWidth() && ii != w) {
                            g.fillRect(left - grid / 2 + ii * grid + 1, getHeight() - (jj + 1) * grid, grid, grid);
                        } else if (ii == model.getField().getWidth()) {
                            g.fillRect(left - grid / 2 + 1, getHeight() - (jj + 2) * grid, grid / 2, grid);
                        } else {
                            g.fillRect(left - grid / 2 + ii * grid + 1, getHeight() - (jj + 1) * grid, grid / 2, grid);
                        }
                    }
                } else {
                    if (jj % 2 == 1) {
                        if (ii == w) continue;
                        g.fillRect(left + ii * grid + 1, getHeight() - (jj + 1) * grid, grid, grid);
                    } else {
                        if (ii != model.getField().getWidth() && ii != w) {
                            g.fillRect(left - grid / 2 + ii * grid + 1, getHeight() - (jj + 1) * grid, grid, grid);
                        } else if (ii == model.getField().getWidth()) {
                            g.fillRect(left - grid / 2 + 1, getHeight() - (jj + 2) * grid, grid / 2, grid);
                        } else {
                            g.fillRect(left - grid / 2 + ii * grid + 1, getHeight() - (jj + 1) * grid, grid / 2, grid);
                        }
                    }
                }
            }
        }
    }

    int correctCoordinatesX(int _i, int _j) {
        int idx = _i + (_j + model.getScroll()) * model.getField().getWidth();
        int m1 = model.getField().getWidth();
        int m2 = model.getField().getWidth() + 1;
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
        int idx = _i + (_j + model.getScroll()) * model.getField().getWidth();
        int m1 = model.getField().getWidth();
        int m2 = model.getField().getWidth() + 1;
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

        byte c = model.getField().get(_i, _j + model.getScroll());
        assert (c >= 0 && c <= 9);

        int ii = _i;
        int jj = _j;

        int idx = ii + model.getField().getWidth() * jj + model.getShift();
        _i = idx % model.getField().getWidth();
        _j = idx / model.getField().getWidth();
        _i = correctCoordinatesX(_i, _j);
        _j = correctCoordinatesY(_i, _j);

        Graphics g = getGraphics();
        g.setColor(model.getColor(c));
        int left = offsetx;
        int w = model.getField().getWidth() / 2;
        if (_i > w && _i != model.getField().getWidth()) return;
        int grid = model.getGrid();
        if (model.getScroll() % 2 == 0) {
            if (_j % 2 == 0) {
                if (_i == w) return;
                g.fillRect(left + _i * grid + 1, getHeight() - (_j + 1) * grid, grid, grid);
            } else {
                if (_i != model.getField().getWidth() && _i != w) {
                    g.fillRect(left - grid / 2 + _i * grid + 1, getHeight() - (_j + 1) * grid, grid, grid);
                } else if (_i == w) {
                    g.fillRect(left - grid / 2 + _i * grid + 1, getHeight() - (_j + 1) * grid, grid / 2, grid);
                } else {
                    g.fillRect(left - grid / 2 + 1, getHeight() - (_j + 2) * grid, grid / 2, grid);
                }
            }
        } else {
            if (_j % 2 == 1) {
                if (_i == w) return;
                g.fillRect(left + _i * grid + 1, getHeight() - (_j + 1) * grid, grid, grid);
            } else {
                if (_i != model.getField().getWidth() && _i != w) {
                    g.fillRect(left - grid / 2 + _i * grid + 1, getHeight() - (_j + 1) * grid, grid, grid);
                } else if (_i == w) {
                    g.fillRect(left - grid / 2 + _i * grid + 1, getHeight() - (_j + 1) * grid, grid / 2, grid);
                } else {
                    g.fillRect(left - grid / 2 + 1, getHeight() - (_j + 2) * grid, grid / 2, grid);
                }
            }
        }
        g.dispose();
    }

}
