/** jbead - http://www.jbead.ch
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

package ch.jbead.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import ch.jbead.Model;
import ch.jbead.Point;
import ch.jbead.Selection;
import ch.jbead.View;

public class SimulationPanel extends BasePanel {

    private static final long serialVersionUID = 1L;

    private int offsetx;
    private int left;
    private int w;

    public SimulationPanel(Model model, Selection selection, View view) {
        super(model, view, selection);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                handleMouseClick(e);
            }
        });
    }

    private void handleMouseClick(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            if (view.getSelectedTool().equals("fill")) {
                fillLine(new Point(e.getX(), e.getY()));
            } else {
                togglePoint(new Point(e.getX(), e.getY()));
            }
        }
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension((getVisibleWidth() + 1) * gridx, 3 * gridy);
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
        left = getLeft();
        w = getVisibleWidth();
        paintBeads(g);
    }

    private int getOffsetX() {
        return (getWidth() - 1 - (model.getWidth() + 1) * gridx / 2 + gridx / 2) / 2;
    }

    private int getLeft() {
        int left = offsetx;
        if (left < 0) left = gridx / 2;
        return left;
    }

    private int getVisibleWidth() {
        return model.getWidth() / 2;
    }

    private int x(int i) {
        return left + i * gridx;
    }

    private int y(int j) {
        return getHeight() - 1 - (j + 1) * gridy;
    }

    public int dx(int j) {
        if ((j + scroll) % 2 == 0) {
            return 0;
        } else {
            return gridx / 2;
        }
    }

    private void paintBeads(Graphics g) {
        if (scroll > model.getHeight() - 1) return;
        int width = model.getWidth();
        for (Point pt : model.getRect(scroll, model.getHeight() - 1)) {
            byte c = model.get(pt);
            pt = model.correct(pt.unscrolled(scroll).shifted(model.getShift(), width));
            if (y(pt.getY()) < -gridy) return;
            if (pt.getX() > w && pt.getX() != width) continue;
            if (scroll % 2 == 0) {
                if (pt.getY() % 2 == 0) {
                    if (pt.getX() == w) continue;
                    paintBead(g, x(pt.getX()), getHeight() - 1 - (pt.getY() + 1) * gridy, gridx, gridy, model.getColor(c));
                } else {
                    if (pt.getX() != width && pt.getX() != w) {
                        paintBead(g, x(pt.getX()) - gridx / 2, getHeight() - 1 - (pt.getY() + 1) * gridy, gridx, gridy, model.getColor(c));
                    } else if (pt.getX() == width) {
                        paintBead(g, x(0) - gridx / 2, getHeight() - 1 - (pt.getY() + 2) * gridy, gridx / 2, gridy, model.getColor(c));
                    } else {
                        paintBead(g, x(pt.getX()) - gridx / 2, getHeight() - 1 - (pt.getY() + 1) * gridy, gridx / 2, gridy, model.getColor(c));
                    }
                }
            } else {
                if (pt.getY() % 2 == 1) {
                    if (pt.getX() == w) continue;
                    paintBead(g, x(pt.getX()), getHeight() - 1 - (pt.getY() + 1) * gridy, gridx, gridy, model.getColor(c));
                } else {
                    if (pt.getX() != width && pt.getX() != w) {
                        paintBead(g, x(pt.getX()) - gridx / 2, getHeight() - 1 - (pt.getY() + 1) * gridy, gridx, gridy, model.getColor(c));
                    } else if (pt.getX() == width) {
                        paintBead(g, x(0) - gridx / 2, getHeight() - 1 - (pt.getY() + 2) * gridy, gridx / 2, gridy, model.getColor(c));
                    } else {
                        paintBead(g, x(pt.getX()) - gridx / 2, getHeight() - 1 - (pt.getY() + 1) * gridy, gridx / 2, gridy, model.getColor(c));
                    }
                }
            }
        }
    }

    private void paintBead(Graphics g, int i, int j, int w, int h, Color color) {
        g.setColor(color);
        g.fillRect(i + 1, j + 1, w - 1, h - 1);
        g.setColor(Color.DARK_GRAY);
        g.drawRect(i, j, w, h);
    }

    private int gw(int i) {
        if (i != model.getWidth() && i != w) {
            return gridx;
        } else if (i == w) {
            return gridx / 2;
        } else {
            return gridx / 2;
        }
    }

    @Override
    public void redraw(Point pt) {
        if (!isVisible()) return;
        byte c = model.get(pt);
        pt = pt.unscrolled(scroll);
        int idx = pt.getX() + model.getWidth() * pt.getY() + model.getShift();
        pt = model.correct(model.getPoint(idx));

        Graphics g = getGraphics();
        int w = getVisibleWidth();
        if (pt.getX() > w && pt.getX() != model.getWidth()) return;
        if (pt.getX() == w && dx(pt.getY()) == 0) return;
        paintBead(g, x(pt.getX()) - dx(pt.getY()), y(pt.getY()), gw(pt.getX()), gridy, model.getColor(c));
        g.dispose();
    }

    @Override
    public void shiftChanged(int scroll) {
        repaint();
    }

    public Point mouseToField(Point pt) {
        int w = getVisibleWidth();
        int j = (getHeight() - pt.getY()) / gridy;
        if (pt.getX() < offsetx || pt.getX() > offsetx + w * gridx) return null;
        int i = (pt.getX() - offsetx + dx(j)) / gridx - model.getShift();
        if (i >= model.getWidth()) {
            i -= model.getWidth();
            j++;
        } else if (i < 0) {
            i += model.getWidth();
            j--;
        }
        return new Point(i, j);
    }

    public void togglePoint(Point pt) {
        pt = mouseToField(pt);
        if (pt == null) return;
        int idx = model.getCorrectedIndex(pt);
        pt = model.getPoint(idx);
        selection.clear();
        model.setPoint(pt.unscrolled(scroll));
    }

    public void fillLine(Point pt) {
        pt = mouseToField(pt);
        if (pt == null) return;
        int idx = model.getCorrectedIndex(pt);
        pt = model.getPoint(idx);
        selection.clear();
        model.fillLine(pt.unscrolled(scroll));
    }

    public void drawColorsChanged(boolean drawColors) {
        // empty
    }

    public void drawSymbolsChanged(boolean drawSymbols) {
        // empty
    }

}
