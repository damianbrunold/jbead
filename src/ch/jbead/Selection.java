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

/**
 * 
 */
public class Selection {

    private Point begin = new Point(0, 0);
    private Point end = new Point(0, 0);
    private boolean selection;

    public void clear() {
        selection = false;
    }
    
    public boolean isActive() {
        return selection;
    }
    
    public void init(Point origin) {
        begin = end = origin;
        selection = false;
    }
    
    public void update(Point end) {
        this.end = end;
        selection = !begin.equals(end);
    }

    public Point getOrigin() {
        return begin;
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
    
    public boolean isNormal() {
        return begin.getX() != end.getX() && begin.getY() != end.getY();
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
    
    public Point getLineEnd() {
        int _i1 = begin.getX();
        int _j1 = begin.getY();
        int _i2 = end.getX();
        int _j2 = end.getY();
        int dx = Math.abs(_i2 - _i1);
        int dy = Math.abs(_j2 - _j1);
        if (2 * dy < dx) {
            _j2 = _j1;
        } else if (2 * dx < dy) {
            _i2 = _i1;
        } else {
            int d = Math.min(dx, dy);
            if (_i2 - _i1 > d)
                _i2 = _i1 + d;
            else if (_i1 - _i2 > d) _i2 = _i1 - d;
            if (_j2 - _j1 > d)
                _j2 = _j1 + d;
            else if (_j1 - _j2 > d) _j2 = _j1 - d;
        }
        return new Point(_i2, _j2);
    }
    
}
