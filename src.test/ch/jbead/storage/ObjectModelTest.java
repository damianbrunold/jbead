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

package ch.jbead.storage;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

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

    public void testGet() {
        ObjectModel om = ObjectModel.fromData("(jbb (author \"Damian Brunold\"))");
        assertEquals("Damian Brunold", om.getValue("author"));
    }

    public void testGetString() {
        ObjectModel om = ObjectModel.fromData("(jbb (author \"Damian Brunold\"))");
        assertEquals("Damian Brunold", om.getStringValue("author", "joe"));
    }

    public void testGetStringDefault() {
        ObjectModel om = ObjectModel.fromData("(jbb)");
        assertEquals("joe", om.getStringValue("author", "joe"));
    }

    public void testGetInt() {
        ObjectModel om = ObjectModel.fromData("(jbb (version 1))");
        assertEquals(1, om.getIntValue("version", -1));
    }

    public void testGetBool() {
        ObjectModel om = ObjectModel.fromData("(jbb (visible true))");
        assertEquals(true, om.getBoolValue("visible", false));
    }

    public void testGetAll() {
        ObjectModel om = ObjectModel.fromData("(jbb (colors (rgb 1 2 3) (rgb 4 5 6)))");
        List<Node> colors = om.getAll("colors/rgb");
        assertEquals(2, colors.size());
        assertEquals("[1, 2, 3]", colors.get(0).asLeaf().getValues().toString());
        assertEquals("[4, 5, 6]", colors.get(1).asLeaf().getValues().toString());
    }

    public void testGetPath() {
        ObjectModel om = ObjectModel.fromData("(jbb (a (b (c 1 2 3))))");
        assertEquals(1, om.getValue("a/b/c"));
    }

    public void testGetInvalidPath() {
        ObjectModel om = ObjectModel.fromData("(jbb (a (b (c 1 2 3))))");
        try {
            om.getValue("a/x/c");
        } catch (JBeadFileFormatException e) {
            assertEquals("Path a/x/c cannot be resolved, node x not found", e.getMessage());
        }
    }

    public void testGetInvalidLeaf() {
        ObjectModel om = ObjectModel.fromData("(jbb (a (b (c 1 2 3))))");
        try {
            om.getValue("a/b/x");
        } catch (JBeadFileFormatException e) {
            assertEquals("Path a/b/x cannot be resolved, node x not found", e.getMessage());
        }
    }

}
