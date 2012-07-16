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

package ch.jbead.print;

import junit.framework.TestCase;

public class ConvertTest extends TestCase {

    public void testMm2pt() {
        assertEquals(0, Convert.mm2pt(0));
        assertEquals(2, Convert.mm2pt(1));
        assertEquals(8, Convert.mm2pt(3));
        assertEquals(11, Convert.mm2pt(4));
        assertEquals(720, Convert.mm2pt(254));
    }

    public void testMm2ptFloat() {
        assertEquals(4, Convert.mm2pt(1.5));
        assertEquals(9, Convert.mm2pt(3.5));
    }

}
