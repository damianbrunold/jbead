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

package ch.jbead.util;

import junit.framework.TestCase;

public class ConvertTest extends TestCase {

    public void testMm2pt() {
        assertEquals(0, Convert.mmToPoint(0));
        assertEquals(2, Convert.mmToPoint(1));
        assertEquals(8, Convert.mmToPoint(3));
        assertEquals(11, Convert.mmToPoint(4));
        assertEquals(720, Convert.mmToPoint(254));
    }

    public void testMm2ptFloat() {
        assertEquals(4, Convert.mmToPoint(1.5));
        assertEquals(9, Convert.mmToPoint(3.5));
    }

}
