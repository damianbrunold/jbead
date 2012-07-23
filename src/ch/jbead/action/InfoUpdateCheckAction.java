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
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import ch.jbead.BaseAction;
import ch.jbead.JBeadFrame;
import ch.jbead.Version;

public class InfoUpdateCheckAction extends BaseAction {

    private static final long serialVersionUID = 1L;

    private static final String NAME = "info.updatecheck";

    public InfoUpdateCheckAction(JBeadFrame frame) {
        super(NAME, frame);
        putValue(SHORT_DESCRIPTION, localization.getString("action.info.updatecheck.description"));
        putValue(MNEMONIC_KEY, localization.getMnemonic("action.info.updatecheck.mnemonic"));
    }

    public void actionPerformed(ActionEvent e) {
        Thread thread = new Thread(new Runnable() {
            public void run() {
                checkVersion();
            }

            private void checkVersion() {
                try {
                    URL url = new URL("http://www.jbead.ch/latestversion");
                    BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"));
                    try {
                        String latestversion = reader.readLine();
                        reportVersions(latestversion);
                    } finally {
                        reader.close();
                    }
                } catch (Exception e) {
                    reportFailure();
                }
            }

            private void reportFailure() {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        JOptionPane.showMessageDialog(frame, localization.getString("updatecheck.failure"));
                    }
                });
            }

            private void reportVersions(final String latestversion) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        JOptionPane.showMessageDialog(frame, getMessage(latestversion));
                    }

                    private String getMessage(String latestversion) {
                        if (Version.getInstance().isOlderThan(latestversion)) {
                            return localization.getString("updatecheck.updateavailable").replace("{1}", latestversion);
                        } else {
                            return localization.getString("updatecheck.uptodate");
                        }
                    }
                });
            }
        });
        thread.start();
    }

}
