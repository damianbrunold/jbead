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

public class CorrectedPanel extends BasePanel {

    private static final long serialVersionUID = 1L;

    private int offsetx;
    private int maxj;
    private int left;

    public CorrectedPanel(Model model, Selection selection, final BeadForm form) {
        super(model, selection);
        model.addListener(this);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                form.correctedMouseUp(e);
            }
        });
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension((model.getWidth() + 2) * gridx, 3 * gridy);
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
        return offsetx < 0 ? gridx / 2 : offsetx;
    }

    private int getMaxJ() {
        return Math.min(model.getHeight(), getHeight() / gridy + 1);
    }

    private int getOffsetX() {
        return gridx / 2 + (getWidth() - 1 - model.getWidth() * gridx - gridx / 2) / 2;
    }

    private int x(int i) {
        return left + i * gridx;
    }

    private int y(int j) {
        return getHeight() - 1 - (j + 1) * gridy;
    }

    private void paintGrid(Graphics g) {
        paintHorizontalLines(g);
        paintVerticalLines(g);
    }

    private void paintHorizontalLines(Graphics g) {
        g.setColor(Color.DARK_GRAY);
        g.drawLine(x(0), y(-1), x(model.getWidth()), y(-1));
        for (int j = 0; j < maxj; j++) {
            g.drawLine(x(0) - gridx / 2, y(j), x(model.getWidth()) + gridx / 2, y(j));
        }
    }

    private int dx(int j) {
        if ((j + scroll) % 2 == 0) {
            return 0;
        } else {
            return gridx / 2;
        }
    }

    private int count(int j) {
        if ((j + scroll) % 2 == 0) {
            return model.getWidth();
        } else {
            return model.getWidth() + 1;
        }
    }

    private void paintVerticalLines(Graphics g) {
        g.setColor(Color.DARK_GRAY);
        for (int j = 0; j <= maxj; j++) {
            int dx = dx(j);
            int count = count(j);
            for (int i = 0; i <= count; i++) {
                g.drawLine(x(i) - dx, y(j), x(i) - dx, y(j - 1));
            }
        }
    }

    private void paintBeads(Graphics g) {
        for (Point pt : model.getFullRect()) {
            byte c = model.get(pt.scrolled(scroll));
            g.setColor(model.getColor(c));
            pt = correct(pt);
            int y = y(pt.getY());
            if (y > getHeight()) break;
            if (y < -gridy) break;
            paintBead(g, pt);
        }
    }

    private void paintBead(Graphics g, Point pt) {
        g.fillRect(x(pt.getX()) + 1 - dx(pt.getY()), y(pt.getY()) + 1, gridx - 1, gridy - 1);
    }

    private Point correct(Point pt) {
        return new Point(correctX(pt.getX(), pt.getY()),
                          correctY(pt.getX(), pt.getY()));
    }

    int correctX(int _i, int _j) {
        int idx = _i + (_j + scroll) * model.getWidth();
        int m1 = model.getWidth();
        int m2 = model.getWidth() + 1;
        int k = 0;
        int m = m1;
        while (idx >= m) {
            idx -= m;
            k++;
            m = (k % 2 == 0) ? m1 : m2;
        }
        return idx;
    }

    int correctY(int _i, int _j) {
        int idx = _i + (_j + scroll) * model.getWidth();
        int m1 = model.getWidth();
        int m2 = model.getWidth() + 1;
        int k = 0;
        int m = (k % 2 == 0) ? m1 : m2;
        while (idx >= m) {
            idx -= m;
            k++;
            m = (k % 2 == 0) ? m1 : m2;
        }
        return k - scroll;
    }

    @Override
    public void redraw(Point pt) {
        if (!isVisible()) return;
        Point _pt = pt.unscrolled(scroll);
        byte c = model.get(pt);
        _pt = correct(_pt);
        Graphics g = getGraphics();
        g.setColor(model.getColor(c));
        paintBead(g, _pt);
        g.dispose();
    }

    boolean mouseToField(Point pt) {
        int _i = pt.getX();
        int _j = pt.getY();
        int i;
        int jj = (getHeight() - _j) / gridy;
        if (scroll % 2 == 0) {
            if (jj % 2 == 0) {
                if (_i < offsetx || _i > offsetx + model.getWidth() * gridx) return false;
                i = (_i - offsetx) / gridx;
            } else {
                if (_i < offsetx - gridx / 2 || _i > offsetx + model.getWidth() * gridx + gridx / 2) return false;
                i = (_i - offsetx + gridx / 2) / gridx;
            }
        } else {
            if (jj % 2 == 1) {
                if (_i < offsetx || _i > offsetx + model.getWidth() * gridx) return false;
                i = (_i - offsetx) / gridx;
            } else {
                if (_i < offsetx - gridy / 2 || _i > offsetx + model.getWidth() * gridx + gridx / 2) return false;
                i = (_i - offsetx + gridy / 2) / gridx;
            }
        }
        pt.setX(i);
        pt.setY(jj);
        return true;
    }

    public void togglePoint(Point pt) {
        if (!mouseToField(pt)) return;
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
