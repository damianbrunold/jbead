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

package ch.jbead.action;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import ch.jbead.JBeadFrame;
import ch.jbead.version.Version;
import ch.jbead.version.VersionChecker;
import ch.jbead.version.VersionListener;

public class InfoUpdateCheckAction extends BaseAction implements VersionListener {

    private static final long serialVersionUID = 1L;

    private static final String NAME = "info.updatecheck";

    public InfoUpdateCheckAction(JBeadFrame frame) {
        super(NAME, frame);
        putValue(SHORT_DESCRIPTION, localization.getString("action.info.updatecheck.description"));
        putValue(MNEMONIC_KEY, localization.getMnemonic("action.info.updatecheck.mnemonic"));
    }

    public void actionPerformed(ActionEvent e) {
        new VersionChecker(this).check();
    }

    public void versionAvailable(final Version version) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JOptionPane.showMessageDialog(frame, localization.getString("updatecheck.updateavailable").replace("{1}", version.getVersionString()));
            }
        });
    }

    public void versionUpToDate() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JOptionPane.showMessageDialog(frame, localization.getString("updatecheck.uptodate"));
            }
        });
    }

    public void failure(final String msg) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JOptionPane.showMessageDialog(frame, localization.getString("updatecheck.failure").replace("{1}", msg));
            }
        });
    }

}
