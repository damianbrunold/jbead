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

import java.util.Iterator;

public class Rect implements Iterable<Point> {

    protected Point begin;
    protected Point end;

    public Rect(Point begin, Point end) {
        this.begin = begin;
        this.end = end;
    }

    @Override
    public Iterator<Point> iterator() {
        return new RectIterator(begin, end);
    }

    @Override
    public String toString() {
        return begin + "-" + end;
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

    public Point getBegin() {
        return new Point(Math.min(begin.getX(), end.getX()), Math.min(begin.getY(), end.getY()));
    }

    public Point getEnd() {
        return new Point(Math.max(begin.getX(), end.getX()), Math.max(begin.getY(), end.getY()));
    }

    public boolean isSquare() {
        return Math.abs(end.getX() - begin.getX()) == Math.abs(end.getY() - begin.getY());
    }

    public boolean isColumn() {
        return begin.getX() == end.getX();
    }

    public boolean isRow() {
        return begin.getY() == end.getY();
    }

    public int size() {
        return width() * height();
    }

    public Rect scrolled(int scroll) {
        return new Rect(begin.scrolled(scroll), end.scrolled(scroll));
    }

}
