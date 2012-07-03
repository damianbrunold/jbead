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

import java.util.Locale;
import java.util.ResourceBundle;

import junit.framework.TestCase;

public class ReportInfosTest extends TestCase {

    private Localization localization;
    private Model model;

    @Override
    protected void setUp() {
        localization = new Localization() {
            @Override
            public String getString(String key) {
                return getBundle().getString(key).trim();
            }
            @Override
            public ResourceBundle getBundle() {
                return ResourceBundle.getBundle("jbead", Locale.ENGLISH);
            }
        };
        model = new Model(localization);
        model.setWidth(15);
    }

    public void testInfosEmpty() {
        ReportInfos info = new ReportInfos(model, localization);
        assertEquals("Pattern: Unnamed, " +
                "Circumference: 15, " +
                "Repeat of colors: 0 beads, " +
                "Rows per repeat: 0, " +
                "Total number of rows: 0, " +
                "Total number of beads: 0 beads, ", infosAsString(info));
    }

    public void testInfosOneRow() {
        model.set(new Point(0, 0), (byte) 1);
        model.updateRepeat();
        ReportInfos info = new ReportInfos(model, localization);
        assertEquals("Pattern: Unnamed, " +
                "Circumference: 15, " +
                "Repeat of colors: 15 beads, " +
                "Rows per repeat: 1, " +
                "Total number of rows: 1, " +
                "Total number of beads: 15 beads, ", infosAsString(info));
    }

    public void testInfosIrregularRepeat() {
        model.set(new Point(0, 0), (byte) 1);
        model.set(new Point(6, 1), (byte) 1);
        model.set(new Point(12, 2), (byte) 1);
        model.updateRepeat();
        ReportInfos info = new ReportInfos(model, localization);
        assertEquals("Pattern: Unnamed, " +
                "Circumference: 15, " +
                "Repeat of colors: 21 beads, " +
                "Rows per repeat: 1 row and 6 beads, " +
                "Total number of rows: 3, " +
                "Total number of beads: 45 beads, ", infosAsString(info));
    }

    public void testInfosRegularRepeat() {
        for (int i = 0; i < 15; i++) {
            model.set(new Point(i, i), (byte) 1);
        }
        for (int i = 0; i < 15; i++) {
            model.set(new Point(i, 15 + i), (byte) 1);
        }
        model.updateRepeat();
        ReportInfos info = new ReportInfos(model, localization);
        assertEquals("Pattern: Unnamed, " +
                "Circumference: 15, " +
                "Repeat of colors: 16 beads, " +
                "Rows per repeat: 1 row and 1 bead, " +
                "Total number of rows: 30, " +
                "Total number of beads: 450 beads, ", infosAsString(info));
    }

    public void testInfosTwoRowsRepeat() {
        model.set(new Point(0, 0), (byte) 1);
        model.set(new Point(0, 1), (byte) 1);
        model.set(new Point(1, 2), (byte) 1);
        model.set(new Point(1, 3), (byte) 1);
        model.updateRepeat();
        ReportInfos info = new ReportInfos(model, localization);
        assertEquals("Pattern: Unnamed, " +
                "Circumference: 15, " +
                "Repeat of colors: 31 beads, " +
                "Rows per repeat: 2 rows and 1 bead, " +
                "Total number of rows: 4, " +
                "Total number of beads: 60 beads, ", infosAsString(info));
    }

    private String infosAsString(ReportInfos infos) {
        StringBuilder result = new StringBuilder();
        for(String label : infos) {
            result.append(label).append(" ").append(infos.getInfo(label)).append(", ");
        }
        return result.toString();
    }

}
