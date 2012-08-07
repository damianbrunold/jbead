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

import javax.swing.JComponent;

import ch.jbead.Model;
import ch.jbead.ModelListener;
import ch.jbead.Point;
import ch.jbead.Selection;

public abstract class BasePanel extends JComponent implements ModelListener {

    private static final long serialVersionUID = 1L;

    protected Model model;
    protected Selection selection;

    protected int gridx;
    protected int gridy;
    protected int scroll;

    protected BasePanel(Model model, Selection selection) {
        this.model = model;
        this.selection = selection;
    }

    public abstract void redraw(Point pt);

    public void pointChanged(Point pt) {
        redraw(pt);
    }

    public void modelChanged() {
        this.scroll = model.getScroll();
        this.gridx = model.getGridx();
        this.gridy = model.getGridy();
        repaint();
    }

    public void colorChanged(byte colorIndex) {
        repaint();
    }

    public void colorsChanged() {
        repaint();
    }

    public void scrollChanged(int scroll) {
        this.scroll = scroll;
        repaint();
    }

    public void shiftChanged(int shift) {
        // empty
    }

    public void zoomChanged(int gridx, int gridy) {
        this.gridx = gridx;
        this.gridy = gridy;
        repaint();
    }

    public void repeatChanged(int repeat) {
        // empty
    }

    public int getGridx() {
        return gridx;
    }

    public int getGridy() {
        return gridy;
    }

    public int dx(Point pt) {
        return dx(pt.getY());
    }

    public int dx(int j) {
        return 0;
    }

    public int w(Point pt) {
        return gridx;
    }

}
