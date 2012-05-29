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

public class SimulationPanel extends BasePanel {

    private static final long serialVersionUID = 1L;

    private Model model;
    private int offsetx;

    public SimulationPanel(Model model, final BeadForm form) {
        this.model = model;
        model.addListener(this);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                form.simulationMouseUp(e);
            }
        });
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension((model.getWidth() / 2 + 1) * model.getGrid(), 3 * model.getGrid());
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

        int grid = model.getGrid();

        g.setColor(Color.DARK_GRAY);
        offsetx = (getWidth() - 1 - (model.getWidth() + 1) * grid / 2 + grid / 2) / 2;
        int left = offsetx;
        if (left < 0) left = grid / 2;
        int maxj = Math.min(model.getHeight(), getHeight() / grid + 1);
        int w = model.getWidth() / 2;
        if (model.getScroll() % 2 == 0) {
            for (int j = 0; j < maxj; j += 2) {
                for (int i = 0; i < w + 1; i++) {
                    if (j == 0 && model.getScroll() == 0 && i < model.getShift()) continue;
                    g.drawLine(left + i * grid, getHeight() - (j + 1) * grid, left + i * grid, getHeight() - j * grid -1);
                }
                if (j > 0 || model.getScroll() > 0) {
                    g.drawLine(left - grid / 2, getHeight() - (j + 1) * grid, left - grid / 2, getHeight() - j * grid - 1);
                }
            }
            for (int j = 1; j < maxj; j += 2) {
                for (int i = 0; i < w + 1; i++) {
                    g.drawLine(left + i * grid - grid / 2, getHeight() - (j + 1) * grid, left + i * grid - grid / 2, getHeight() - j * grid - 1);
                }
                g.drawLine(left + w * grid, getHeight() - (j + 1) * grid, left + w * grid, getHeight() - j * grid - 1);
            }
        } else {
            for (int j = 0; j < maxj; j += 2) {
                for (int i = 0; i < w + 1; i++) {
                    g.drawLine(left + i * grid - grid / 2, getHeight() - (j + 1) * grid, left + i * grid - grid / 2, getHeight() - j * grid - 1);
                }
                g.drawLine(left + w * grid, getHeight() - (j + 1) * grid, left + w * grid, getHeight() - j * grid - 1);
            }
            for (int j = 1; j < maxj; j += 2) {
                for (int i = 0; i < w + 1; i++) {
                    g.drawLine(left + i * grid, getHeight() - (j + 1) * grid, left + i * grid, getHeight() - j * grid - 1);
                }
                g.drawLine(left - grid / 2, getHeight() - (j + 1) * grid, left - grid / 2, getHeight() - j * grid - 1);
            }
        }
        if (model.getScroll() % 2 == 0) {
            if (model.getScroll() == 0) {
                g.drawLine(left + model.getShift() * grid, getHeight() - 1, left + w * grid, getHeight() - 1);
                for (int j = 1; j < maxj; j++) {
                    g.drawLine(left - grid / 2, getHeight() - 1 - j * grid, left + w * grid, getHeight() - 1 - j * grid);
                }
                g.drawLine(left + w * grid, 0, left + w * grid, getHeight() - 1 - grid);
            } else {
                for (int j = 0; j < maxj; j++) {
                    g.drawLine(left - grid / 2, getHeight() - 1 - j * grid, left + w * grid, getHeight() - 1 - j * grid);
                }
                g.drawLine(left + w * grid, 0, left + w * grid, getHeight() - 1 - grid);
            }
        } else {
            for (int j = 0; j < maxj; j++) {
                g.drawLine(left - grid / 2, getHeight() - 1 - j * grid, left + w * grid, getHeight() - 1 - j * grid);
            }
            g.drawLine(left + w * grid, 0, left + w * grid, getHeight() - 1);
        }

        // Data
        int width = model.getWidth();
        int scroll = model.getScroll();
        int shift = model.getShift();
        int grid1 = grid - 1;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < maxj; j++) {
                byte c = model.get(new Point(i, j).scrolled(scroll));
                g.setColor(model.getColor(c));
                int idx = i + width * j + shift;
                int i1 = idx % width;
                int j1 = idx / width;
                int ii = correctCoordinatesX(i1, j1);
                int jj = correctCoordinatesY(i1, j1);
                if (ii > w && ii != width) continue;
                if (scroll % 2 == 0) {
                    if (jj % 2 == 0) {
                        if (ii == w) continue;
                        g.fillRect(left + ii * grid + 1, getHeight() - (jj + 1) * grid, grid1, grid1);
                    } else {
                        if (ii != width && ii != w) {
                            g.fillRect(left - grid / 2 + ii * grid + 1, getHeight() - (jj + 1) * grid, grid1, grid1);
                        } else if (ii == width) {
                            g.fillRect(left - grid / 2 + 1, getHeight() - (jj + 2) * grid, grid / 2 - 1, grid1);
                        } else {
                            g.fillRect(left - grid / 2 + ii * grid + 1, getHeight() - (jj + 1) * grid, grid / 2 - 1, grid1);
                        }
                    }
                } else {
                    if (jj % 2 == 1) {
                        if (ii == w) continue;
                        g.fillRect(left + ii * grid + 1, getHeight() - (jj + 1) * grid, grid1, grid1);
                    } else {
                        if (ii != width && ii != w) {
                            g.fillRect(left - grid / 2 + ii * grid + 1, getHeight() - (jj + 1) * grid, grid1, grid1);
                        } else if (ii == width) {
                            g.fillRect(left - grid / 2 + 1, getHeight() - (jj + 2) * grid, grid / 2 - 1, grid1);
                        } else {
                            g.fillRect(left - grid / 2 + ii * grid + 1, getHeight() - (jj + 1) * grid, grid / 2 - 1, grid1);
                        }
                    }
                }
            }
        }
    }

    int correctCoordinatesX(int _i, int _j) {
        int idx = _i + (_j + model.getScroll()) * model.getWidth();
        int m1 = model.getWidth();
        int m2 = m1 + 1;
        int k = 0;
        int m = m1 ;
        while (idx >= m) {
            idx -= m;
            k++;
            m = (k % 2 == 0) ? m1 : m2;
        }
        return idx;
    }

    int correctCoordinatesY(int _i, int _j) {
        int idx = _i + (_j + model.getScroll()) * model.getWidth();
        int m1 = model.getWidth();
        int m2 = m1 + 1;
        int k = 0;
        int m = m1;
        while (idx >= m) {
            idx -= m;
            k++;
            m = (k % 2 == 0) ? m1 : m2;
        }
        return k - model.getScroll();
    }

    public void redraw(int _i, int _j) {
        if (!isVisible()) return;

        byte c = model.get(new Point(_i, _j).scrolled(model.getScroll()));
        assert (c >= 0 && c <= 9);

        int ii = _i;
        int jj = _j;

        int idx = ii + model.getWidth() * jj + model.getShift();
        int i1 = idx % model.getWidth();
        int j1 = idx / model.getWidth();
        _i = correctCoordinatesX(i1, j1);
        _j = correctCoordinatesY(i1, j1);

        Graphics g = getGraphics();
        g.setColor(model.getColor(c));
        int left = offsetx;
        int w = model.getWidth() / 2;
        if (_i > w && _i != model.getWidth()) return;
        int grid = model.getGrid();
        if (model.getScroll() % 2 == 0) {
            if (_j % 2 == 0) {
                if (_i == w) return;
                g.fillRect(left + _i * grid + 1, getHeight() - (_j + 1) * grid, grid - 1, grid - 1);
            } else {
                if (_i != model.getWidth() && _i != w) {
                    g.fillRect(left - grid / 2 + _i * grid + 1, getHeight() - (_j + 1) * grid, grid - 1, grid - 1);
                } else if (_i == w) {
                    g.fillRect(left - grid / 2 + _i * grid + 1, getHeight() - (_j + 1) * grid, grid / 2 - 1, grid - 1);
                } else {
                    g.fillRect(left - grid / 2 + 1, getHeight() - (_j + 2) * grid, grid / 2 - 1, grid - 1);
                }
            }
        } else {
            if (_j % 2 == 1) {
                if (_i == w) return;
                g.fillRect(left + _i * grid + 1, getHeight() - (_j + 1) * grid, grid - 1, grid - 1);
            } else {
                if (_i != model.getWidth() && _i != w) {
                    g.fillRect(left - grid / 2 + _i * grid + 1, getHeight() - (_j + 1) * grid, grid - 1, grid - 1);
                } else if (_i == w) {
                    g.fillRect(left - grid / 2 + _i * grid + 1, getHeight() - (_j + 1) * grid, grid / 2 - 1, grid - 1);
                } else {
                    g.fillRect(left - grid / 2 + 1, getHeight() - (_j + 2) * grid, grid / 2 - 1, grid - 1);
                }
            }
        }
        g.dispose();
    }

    @Override
    public void redraw(Point pt) {
        redraw(pt.getX(), pt.getY() - model.getScroll());
    }

    @Override
    public void shiftChanged(int scroll) {
        repaint();
    }

    boolean mouseToField(Point pt) {
        int w = model.getWidth() / 2;
        int grid = model.getGrid();
        int shift = model.getShift();
        int _i = pt.getX();
        int _j = pt.getY();
        int i;
        int jj = (getHeight() - _j) / grid;
        if (model.getScroll() % 2 == 0) {
            if (jj % 2 == 0) {
                if (_i < offsetx || _i > offsetx + w * grid) return false;
                i = (_i - offsetx) / grid;
            } else {
                if (_i < offsetx - grid / 2 || _i > offsetx + w * grid + grid / 2) return false;
                i = (_i - offsetx + grid / 2) / grid;
            }
        } else {
            if (jj % 2 == 1) {
                if (_i < offsetx || _i > offsetx + w * grid) return false;
                i = (_i - offsetx) / grid;
            } else {
                if (_i < offsetx - grid / 2 || _i > offsetx + w * grid + grid / 2) return false;
                i = (_i - offsetx + grid / 2) / grid;
            }
        }
        i -= shift;
        if (i >= model.getWidth()) {
            i -= model.getWidth();
            jj++;
        } else if (i < 0) {
            i += model.getWidth();
            jj--;
        }
        pt.setX(i);
        pt.setY(jj);
        return true;
    }

    public void togglePoint(Point pt) {
        if (!mouseToField(pt)) return;
        int scroll = model.getScroll();
        int idx = 0;
        int m1 = model.getWidth();
        int m2 = m1 + 1;
        for (int j = 0; j < pt.getY() + scroll; j++) {
            if (j % 2 == 0)
                idx += m1;
            else
                idx += m2;
        }
        idx += pt.getX();
        int j = idx / model.getWidth();
        int i = idx % model.getWidth();
        model.setPoint(new Point(i, j - scroll));
    }

}
