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

/**
 *
 */
public class Point {

    private int x;
    private int y;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Point(Point pt) {
        this.x = pt.x;
        this.y = pt.y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    @Override
    public int hashCode() {
        return x ^ y;
    }

    @Override
    public boolean equals(Object obj) {
        Point other = (Point) obj;
        if (other == null) return false;
        return x == other.x && y == other.y;
    }

    public Point scrolled(int scroll) {
        return new Point(x, y + scroll);
    }

    public Point unscrolled(int scroll) {
        return new Point(x, y - scroll);
    }

    public Point shifted(int shift, int width) {
        return new Point((x + shift) % width, y + (x + shift) / width);
    }

    public Point nextLeft() {
        return new Point(x - 1, y);
    }

    public Point nextRight() {
        return new Point(x + 1, y);
    }

    public Point nextBelow() {
        return new Point(x, y - 1);
    }

    public Point nextAbove() {
        return new Point(x, y + 1);
    }

    public Point lastLeft() {
        return new Point(0, y);
    }

    public Point lastRight(int width) {
        return new Point(width - 1, y);
    }

    @Override
    public String toString() {
        return x + "," + y;
    }
}
