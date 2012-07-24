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

public class VersionTest extends TestCase {

    public void testIsOlderThan() {
        Version v = new Version(1, 2, 3);
        assertTrue(v.isOlderThan("1.2.4"));
    }

    public void testIsOlderThanWithEqualVersion() {
        Version v = new Version(1, 2, 3);
        assertFalse(v.isOlderThan("1.2.3"));
    }

    public void testIsOlderThanWithOlderVersion() {
        Version v = new Version(1, 2, 3);
        assertFalse(v.isOlderThan("1.2.2"));
    }

    public void testIsOlderThanWithLargeBuild() {
        Version v = new Version(1, 2, 3);
        assertTrue(v.isOlderThan("1.2.10"));
    }

    public void testBump() {
        Version v = new Version(1, 2, 3);
        assertEquals("1.2.4", v.bump().getVersionString());
    }

    public void testBumpMinor() {
        Version v = new Version(1, 2, 3);
        assertEquals("1.3.0", v.bumpMinor().getVersionString());
    }

    public void testBumpMajor() {
        Version v = new Version(1, 2, 3);
        assertEquals("2.0.0", v.bumpMajor().getVersionString());
    }

}
