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
        left = getLeft();
        paintBeads(g);
    }

    private int getLeft() {
        return offsetx < 0 ? gridx / 2 : offsetx;
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

    private int dx(int j) {
        if ((j + scroll) % 2 == 0) {
            return 0;
        } else {
            return gridx / 2;
        }
    }

    private void paintBeads(Graphics g) {
        for (Point pt : model.getRect(scroll, model.getHeight() - 1)) {
            byte c = model.get(pt);
            pt = model.correct(pt.unscrolled(scroll));
            if (aboveTop(pt)) break;
            paintBead(g, pt, model.getColor(c));
        }
    }

    private boolean aboveTop(Point pt) {
        return y(pt.getY()) < -gridy;
    }

    private void paintBead(Graphics g, Point pt, Color color) {
        g.setColor(color);
        g.fillRect(x(pt.getX()) + 1 - dx(pt.getY()), y(pt.getY()) + 1, gridx - 1, gridy - 1);
        g.setColor(Color.DARK_GRAY);
        g.drawRect(x(pt.getX()) - dx(pt.getY()), y(pt.getY()), gridx, gridy);
    }

    @Override
    public void redraw(Point pt) {
        if (!isVisible()) return;
        Point _pt = pt.unscrolled(scroll);
        byte c = model.get(pt);
        _pt = model.correct(_pt);
        Graphics g = getGraphics();
        g.setColor(model.getColor(c));
        paintBead(g, _pt, model.getColor(c));
        g.dispose();
    }

    public Point mouseToField(Point pt) {
        int _i = pt.getX();
        int _j = pt.getY();
        int i;
        int jj = (getHeight() - _j) / gridy;
        if (scroll % 2 == 0) {
            if (jj % 2 == 0) {
                if (_i < offsetx || _i > offsetx + model.getWidth() * gridx) return null;
                i = (_i - offsetx) / gridx;
            } else {
                if (_i < offsetx - gridx / 2 || _i > offsetx + model.getWidth() * gridx + gridx / 2) return null;
                i = (_i - offsetx + gridx / 2) / gridx;
            }
        } else {
            if (jj % 2 == 1) {
                if (_i < offsetx || _i > offsetx + model.getWidth() * gridx) return null;
                i = (_i - offsetx) / gridx;
            } else {
                if (_i < offsetx - gridy / 2 || _i > offsetx + model.getWidth() * gridx + gridx / 2) return null;
                i = (_i - offsetx + gridy / 2) / gridx;
            }
        }
        return new Point(i, jj);
    }

    public void togglePoint(Point pt) {
        pt = mouseToField(pt);
        if (pt == null) return;
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
