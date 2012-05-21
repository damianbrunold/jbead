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

import junit.framework.TestCase;

/**
 * 
 */
public class PointTest extends TestCase {
    
    public void testScrolled() {
        assertEquals(new Point(0, 1), new Point(0, 0).scrolled(1));
    }

    public void testNextLeft() {
        assertEquals(new Point(4, 0), new Point(5, 0).nextLeft());
    }

    public void testNextRight() {
        assertEquals(new Point(6, 0), new Point(5, 0).nextRight());
    }

    public void testLastLeft() {
        assertEquals(new Point(0, 0), new Point(5, 0).lastLeft());
    }

    public void testLastRight() {
        assertEquals(new Point(10, 0), new Point(1, 0).lastRight(11));
    }

}
