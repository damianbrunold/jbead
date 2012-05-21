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

import javax.swing.JComponent;

public abstract class BasePanel extends JComponent implements ModelListener{

    private static final long serialVersionUID = 1L;

    public abstract void redraw(Point pt);
    
    @Override
    public void pointChanged(Point pt) {
        redraw(pt);
    }

    @Override
    public void modelChanged() {
        repaint();
    }

    @Override
    public void colorChanged(int colorIndex) {
        repaint();
    }

    @Override
    public void scrollChanged(int scroll) {
        // empty
    }

    @Override
    public void zoomChanged(int gridx, int gridy) {
        repaint();
    }

}
