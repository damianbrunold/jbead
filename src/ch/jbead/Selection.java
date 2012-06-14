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

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class Selection extends Rect {

    private boolean selection;

    private List<SelectionListener> listeners = new ArrayList<SelectionListener>();

    public Selection() {
        super(new Point(0, 0), new Point(0, 0));
        selection = false;
    }

    public Selection(Selection sel) {
        super(sel.begin, sel.end);
        selection = sel.selection;
    }

    public void addListener(SelectionListener listener) {
        listeners.add(listener);
    }

    private void fireSelectionChanged(Selection before, Selection current) {
        for (SelectionListener listener : listeners) {
            listener.selectionUpdated(before, current);
        }
    }

    private void fireSelectionDeleted(Selection sel) {
        for (SelectionListener listener : listeners) {
            listener.selectionDeleted(sel);
        }
    }

    public void clear() {
        if (!selection) return;
        fireSelectionDeleted(snapshot());
        selection = false;
    }

    public boolean isActive() {
        return selection;
    }

    public void init(Point origin) {
        Selection before = snapshot();
        this.begin = this.end = origin;
        selection = false;
        fireSelectionChanged(before, snapshot());
    }

    public void update(Point end) {
        Selection before = snapshot();
        this.end = end;
        selection = !begin.equals(end);
        fireSelectionChanged(before, snapshot());
    }

    public Selection snapshot() {
        return new Selection(this);
    }

    public Point getOrigin() {
        return begin;
    }

    public Point getDestination() {
        return end;
    }

    public boolean isNormal() {
        return isActive() && begin.getX() != end.getX() && begin.getY() != end.getY();
    }

    public Point getLineDest() {
        int x = end.getX();
        int y = end.getY();
        int ax = Math.abs(getDeltaX());
        int ay = Math.abs(getDeltaY());
        if (ax == 0 || ay == 0) return new Point(end);
        if (ax > ay) {
            x = begin.getX() + ay * getDx();
        } else {
            y = begin.getY() + ax * getDy();
        }
        return new Point(x, y);
    }

    public int getDeltaX() {
        return end.getX() - begin.getX();
    }

    public int getDeltaY() {
        return end.getY() - begin.getY();
    }

    public int getDx() {
        return begin.getX() < end.getX() ? 1 : -1;
    }

    public int getDy() {
        return begin.getY() < end.getY() ? 1 : -1;
    }

    @Override
    public String toString() {
        return "(" + getBegin() + "-" + getEnd() + ")";
    }
}
