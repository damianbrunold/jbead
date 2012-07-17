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

package ch.jbead;

import java.util.Iterator;

/**
 *
 */
public class SegmentIterator implements Iterator<Point> {

    private Point begin;
    private Point end;
    private Point next;

    private int dx;
    private int dy;

    private int sx;
    private int sy;

    public SegmentIterator(Point begin, Point end) {
        this.begin = begin;
        this.end = end;
        this.next = this.begin;
        this.dx = end.getX() - begin.getX();
        this.dy = end.getY() - begin.getY();
        this.sx = dx > 0 ? 1 : -1;
        this.sy = dy > 0 ? 1 : -1;
    }

    public boolean hasNext() {
        return next != null;
    }

    public Point next() {
        Point result = next;
        if (next.equals(end)) {
            next = null;
        } else if (dx == 0) {
            next = new Point(next.getX(), next.getY() + sy);
        } else if (dy == 0) {
            next = new Point(next.getX() + sx, next.getY());
        } else if (Math.abs(dx) > Math.abs(dy)) {
            next = new Point(next.getX() + sx, begin.getY() + Math.abs(next.getX() + sx - begin.getX()) * dy / dx);
        } else if (Math.abs(dx) < Math.abs(dy)) {
            next = new Point(begin.getX() + Math.abs(next.getY() + sy - begin.getY()) * dx / dy, next.getY() + sy);
        } else {
            next = new Point(next.getX() + sx, next.getY() + sy);
        }
        return result;
    }

    public void remove() {
        // ignore
    }

}
