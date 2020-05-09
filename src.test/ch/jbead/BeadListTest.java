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

import java.util.ResourceBundle;

import javax.swing.KeyStroke;

import junit.framework.TestCase;

public class BeadListTest extends TestCase implements Localization {
    
    private ResourceBundle bundle = ResourceBundle.getBundle("jbead");
    private Model model = new Model(this);

    @Override
    protected void setUp() {
        model.setWidth(5);
        model.set(4, (byte) 1);
        model.updateRepeat();
    }

    public void testColorRepeat() {
        BeadList repeat = new BeadList(model);
        assertEquals(2, repeat.size());
        assertEquals(new BeadRun((byte) 1, 1), repeat.get(0));
        assertEquals(new BeadRun((byte) 0, 4), repeat.get(1));
    }

    public void testIterable() {
        BeadList repeat = new BeadList(model);
        StringBuilder result = new StringBuilder();
        for (BeadRun run : repeat) {
            result.append(run.toString()).append(" ");
        }
        assertEquals("1x1 0x4 ", result.toString());
    }

    public ResourceBundle getBundle() {
        return bundle;
    }

    public String getString(String key) {
        return bundle.getString(key);
    }

    public int getMnemonic(String key) {
        return bundle.getString(key).charAt(0);
    }

    public KeyStroke getKeyStroke(String key) {
        return KeyStroke.getKeyStroke(bundle.getString(key));
    }

}
