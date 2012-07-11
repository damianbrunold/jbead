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

import java.awt.Frame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class JBeadApp {

    public static void main(String[] args) throws UnsupportedLookAndFeelException {
        UIManager.put("swing.boldMetal", Boolean.FALSE);
        final BeadForm beadform = new BeadForm(args);
        beadform.setMaximizedBounds(beadform.getMaxBounds());
        beadform.setVisible(true);
        if (beadform.isConfigMaximized()) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    beadform.setExtendedState(beadform.getExtendedState() | Frame.MAXIMIZED_BOTH);
                }
            });
        }

    }

}
