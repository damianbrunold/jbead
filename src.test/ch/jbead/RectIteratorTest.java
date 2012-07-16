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

public class RectIteratorTest extends TestCase {

    public void testStandard() {
        RectIterator iter = new RectIterator(new Point(0, 0), new Point(2, 2));
        assertEquals(new Point(0, 0), iter.next());
        assertEquals(new Point(1, 0), iter.next());
        assertEquals(new Point(2, 0), iter.next());
        assertEquals(new Point(0, 1), iter.next());
        assertEquals(new Point(1, 1), iter.next());
        assertEquals(new Point(2, 1), iter.next());
        assertEquals(new Point(0, 2), iter.next());
        assertEquals(new Point(1, 2), iter.next());
        assertEquals(new Point(2, 2), iter.next());
        assertFalse(iter.hasNext());
    }

    public void testReversed() {
        RectIterator iter = new RectIterator(new Point(2, 2), new Point(0, 0));
        assertEquals(new Point(0, 0), iter.next());
        assertEquals(new Point(1, 0), iter.next());
        assertEquals(new Point(2, 0), iter.next());
        assertEquals(new Point(0, 1), iter.next());
        assertEquals(new Point(1, 1), iter.next());
        assertEquals(new Point(2, 1), iter.next());
        assertEquals(new Point(0, 2), iter.next());
        assertEquals(new Point(1, 2), iter.next());
        assertEquals(new Point(2, 2), iter.next());
        assertFalse(iter.hasNext());
    }

}
