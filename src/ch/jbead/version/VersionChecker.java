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

package ch.jbead.version;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

public class VersionChecker {

    private VersionListener listener;

    public VersionChecker(VersionListener listener) {
        this.listener = listener;
    }

    protected String getLatestVersionURL() {
        return "http://www.jbead.ch/latestversion";
    }

    public void check() {
        Thread thread = new Thread(new Runnable() {
            public void run() {
                try {
                    URL url = new URL(getLatestVersionURL());
                    BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"));
                    try {
                        String latestversion = reader.readLine();
                        if (Version.getInstance().isOlderThan(latestversion)) {
                            listener.versionAvailabe(new Version(latestversion));
                        } else {
                            listener.versionUpToDate();
                        }
                    } finally {
                        reader.close();
                    }
                } catch (Exception e) {
                    listener.failure(e.toString());
                }
            }
        });
        thread.start();
    }
}
