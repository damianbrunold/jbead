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
public class Selection {

    private Point origin;
    private Point dest;
    private boolean selection;

    private List<SelectionListener> listeners = new ArrayList<SelectionListener>();

    public Selection() {
        origin = new Point(0, 0);
        dest = new Point(0, 0);
        selection = false;
    }

    public Selection(Selection sel) {
        origin = sel.origin;
        dest = sel.dest;
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
        fireSelectionDeleted(snapshot());
        selection = false;
    }

    public boolean isActive() {
        return selection;
    }

    public void init(Point origin) {
        Selection before = snapshot();
        this.origin = this.dest = origin;
        selection = false;
        fireSelectionChanged(before, snapshot());
    }

    public void update(Point end) {
        Selection before = snapshot();
        this.dest = end;
        selection = !origin.equals(dest);
        fireSelectionChanged(before, snapshot());
    }

    public Selection snapshot() {
        return new Selection(this);
    }

    public Point getOrigin() {
        return origin;
    }

    public Point getDestination() {
        return dest;
    }

    public Point getBegin() {
        return new Point(Math.min(origin.getX(), dest.getX()), Math.min(origin.getY(), dest.getY()));
    }

    public Point getEnd() {
        return new Point(Math.max(origin.getX(), dest.getX()), Math.max(origin.getY(), dest.getY()));
    }

    public boolean isSquare() {
        return Math.abs(dest.getX() - origin.getX()) == Math.abs(dest.getY() - origin.getY());
    }

    public boolean isColumn() {
        return origin.getX() == dest.getX();
    }

    public boolean isRow() {
        return origin.getY() == dest.getY();
    }

    public boolean isNormal() {
        return isActive() && origin.getX() != dest.getX() && origin.getY() != dest.getY();
    }

    public int left() {
        return getBegin().getX();
    }

    public int right() {
        return getEnd().getX();
    }

    public int bottom() {
        return getBegin().getY();
    }

    public int top() {
        return getEnd().getY();
    }

    public int width() {
        return right() - left() + 1;
    }

    public int height() {
        return top() - bottom() + 1;
    }

    public Point getLineDest() {
        int x = dest.getX();
        int y = dest.getY();
        int ax = Math.abs(getDeltaX());
        int ay = Math.abs(getDeltaY());
        if (ax == 0 || ay == 0) return new Point(dest);
        if (ax > ay) {
            x = origin.getX() + ay * getDx();
        } else {
            y = origin.getY() + ax * getDy();
        }
        return new Point(x, y);
    }

    public int getDeltaX() {
        return dest.getX() - origin.getX();
    }

    public int getDeltaY() {
        return dest.getY() - origin.getY();
    }

    public int getDx() {
        return origin.getX() < dest.getX() ? 1 : -1;
    }

    public int getDy() {
        return origin.getY() < dest.getY() ? 1 : -1;
    }

    @Override
    public String toString() {
        return "(" + getBegin() + "-" + getEnd() + ")";
    }
}
