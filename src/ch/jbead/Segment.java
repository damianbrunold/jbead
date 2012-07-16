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
public class Segment implements Iterable<Point> {

    private Point begin;
    private Point end;
    
    public Segment(Point begin, Point end) {
        this.begin = begin;
        this.end = end;
    }
    
    @Override
    public Iterator<Point> iterator() {
        return new SegmentIterator(begin, end);
    }
    
    @Override
    public String toString() {
        return begin + "->" + end;
    }

}
