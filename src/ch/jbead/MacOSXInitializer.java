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

import java.awt.event.ActionEvent;
import java.io.File;

import com.apple.eawt.Application;
import com.apple.eawt.ApplicationEvent;
import com.apple.eawt.ApplicationListener;

public class MacOSXInitializer {

    public static class JBeadApplicationListener implements ApplicationListener {
        private JBeadFrame frame;

        public JBeadApplicationListener(JBeadFrame frame) {
            this.frame = frame;
        }

        public void handleAbout(ApplicationEvent event) {
            frame.getAction("info.about").actionPerformed(new ActionEvent(frame, 0, null));
            event.setHandled(true);
        }

        public void handleOpenApplication(ApplicationEvent event) {
            // empty
        }

        public void handleOpenFile(ApplicationEvent event) {
            File file = new File(event.getFilename());
            if (!file.exists()) return;
            frame.loadFile(file, true);
            event.setHandled(true);
        }

        public void handlePreferences(ApplicationEvent event) {
            frame.getAction("pattern.preferences").actionPerformed(new ActionEvent(this, 0, null));
            event.setHandled(true);
        }

        public void handlePrintFile(ApplicationEvent event) {
            File file = new File(event.getFilename());
            if (!file.exists()) return;
            frame.loadFile(file, false);
            frame.getAction("file.print").actionPerformed(new ActionEvent(this, 0, null));
            event.setHandled(true);
        }

        public void handleQuit(ApplicationEvent event) {
            frame.getAction("file.exit").actionPerformed(new ActionEvent(this, 0, null));
            event.setHandled(true);
        }

        public void handleReOpenApplication(ApplicationEvent event) {
            // empty
        }
    }

    public void initialize(JBeadFrame frame) {
        Application.getApplication().addApplicationListener(new JBeadApplicationListener(frame));
        Application.getApplication().setEnabledPreferencesMenu(true);
    }

}
