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

package ch.jbead.fileformat;

import java.io.File;

import junit.framework.TestCase;

public class JbbFileFilterTest extends TestCase {

    public void testAcceptJbbFile() {
        assertTrue(new JbbFileFilter().accept(new File("test.jbb")));
    }

    public void testRejectJbbFile() {
        assertFalse(new JbbFileFilter().accept(new File("test.dbb")));
    }

    public void testRejectDatFileWithJbbInFilename() {
        assertFalse(new JbbFileFilter().accept(new File("test.jbb.dat")));
    }

    public void testAcceptDir() {
        assertTrue(new JbbFileFilter().accept(new File(System.getProperty("user.dir"))));
    }

    public void testGetDescription() {
        assertEquals("jbead files (*.jbb)", new JbbFileFilter().getDescription());
    }

}
