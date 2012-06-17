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

package ch.jbead.storage;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import junit.framework.TestCase;

public class ObjectModelTest extends TestCase {

    public void testEmpty() {
        ObjectModel om = new ObjectModel("jbb");
        assertEquals(
                "(jbb)\n",
                om.toString());
    }

    public void testSetInt() {
        ObjectModel om = new ObjectModel("jbb");
        om.add("version", 1);
        assertEquals(
                "(jbb\n" +
                "    (version 1))\n",
                om.toString());
    }

    public void testSetMultipleInts() {
        ObjectModel om = new ObjectModel("jbb");
        om.add("colors/rgb", 0, 0, 0);
        assertEquals(
                "(jbb\n" +
                "    (colors\n" +
                "        (rgb 0 0 0)))\n",
                om.toString());
    }

    public void testSetMultipleLeafs() {
        ObjectModel om = new ObjectModel("jbb");
        om.add("colors/rgb", 0, 0, 0);
        om.add("colors/rgb", 1, 1, 1);
        assertEquals(
                "(jbb\n" +
                "    (colors\n" +
                "        (rgb 0 0 0)\n" +
                "        (rgb 1 1 1)))\n",
                om.toString());
    }

    public void testSetString() {
        ObjectModel om = new ObjectModel("jbb");
        om.add("author", "Damian Brunold");
        assertEquals(
                "(jbb\n" +
                "    (author \"Damian Brunold\"))\n",
                om.toString());
    }

    public void testSetDate() {
        Calendar cal = new GregorianCalendar(2012, 5, 17, 16, 30, 55);
        String zone = String.format("%+05d", cal.getTimeZone().getOffset(cal.getTimeInMillis()) / 1000 / 36);
        Date date = cal.getTime();
        ObjectModel om = new ObjectModel("jbb");
        om.add("savedate", date);
        assertEquals(
                "(jbb\n" +
                "    (savedate 2012-06-17T16:30:55" + zone + "))\n",
                om.toString());
    }

    public void testSetStringEscape() {
        ObjectModel om = new ObjectModel("jbb");
        om.add("author", "Damian \" \\ Brunold");
        assertEquals(
                "(jbb\n" +
                "    (author \"Damian \\\" \\\\ Brunold\"))\n",
                om.toString());
    }

    public void testSetBoolean() {
        ObjectModel om = new ObjectModel("jbb");
        om.add("view/draft-visible", true);
        assertEquals(
                "(jbb\n" +
                "    (view\n" +
                "        (draft-visible true)))\n",
                om.toString());
    }

}
