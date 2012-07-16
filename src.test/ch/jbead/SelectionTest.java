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

import junit.framework.TestCase;

/**
 * 
 */
public class SelectionTest extends TestCase {

    public void testBottomLeftTopRight() {
        Selection sel = new Selection();
        sel.init(new Point(0, 0));
        sel.update(new Point(2, 1));
        assertEquals(new Point(0, 0), sel.getBegin());
        assertEquals(new Point(2, 1), sel.getEnd());
    }
    
    public void testTopLeftBottomRight() {
        Selection sel = new Selection();
        sel.init(new Point(0, 1));
        sel.update(new Point(2, 0));
        assertEquals(new Point(0, 0), sel.getBegin());
        assertEquals(new Point(2, 1), sel.getEnd());
    }
    
    public void testTopRightBottomLeft() {
        Selection sel = new Selection();
        sel.init(new Point(2, 1));
        sel.update(new Point(0, 0));
        assertEquals(new Point(0, 0), sel.getBegin());
        assertEquals(new Point(2, 1), sel.getEnd());
    }
    
    public void testBottomRightTopLeft() {
        Selection sel = new Selection();
        sel.init(new Point(2, 0));
        sel.update(new Point(0, 1));
        assertEquals(new Point(0, 0), sel.getBegin());
        assertEquals(new Point(2, 1), sel.getEnd());
    }

    public void testLineVertical() {
        Selection sel = new Selection();
        sel.init(new Point(0, 0));
        sel.update(new Point(0, 2));
        assertEquals(new Point(0, 2), sel.getLineDest());
    }

    public void testLineHorizontal() {
        Selection sel = new Selection();
        sel.init(new Point(0, 0));
        sel.update(new Point(2, 0));
        assertEquals(new Point(2, 0), sel.getLineDest());
    }

    public void testLineDiagonal() {
        Selection sel = new Selection();
        sel.init(new Point(0, 0));
        sel.update(new Point(2, 2));
        assertEquals(new Point(2, 2), sel.getLineDest());
    }

    public void testLineBelowDiagonal() {
        Selection sel = new Selection();
        sel.init(new Point(0, 0));
        sel.update(new Point(2, 1));
        assertEquals(new Point(1, 1), sel.getLineDest());
    }

    public void testLineAboveDiagonal() {
        Selection sel = new Selection();
        sel.init(new Point(0, 0));
        sel.update(new Point(2, 3));
        assertEquals(new Point(2, 2), sel.getLineDest());
    }
    
    public void testCrossDiagonal1() {
        Selection sel = new Selection();
        sel.init(new Point(2, 0));
        sel.update(new Point(0, 3));
        assertEquals(new Point(0, 2), sel.getLineDest());
    }

    public void testCrossDiagonal2() {
        Selection sel = new Selection();
        sel.init(new Point(3, 0));
        sel.update(new Point(0, 2));
        assertEquals(new Point(1, 2), sel.getLineDest());
    }

}
