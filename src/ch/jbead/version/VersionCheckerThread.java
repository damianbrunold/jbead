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
import java.net.URLConnection;
import java.util.Locale;

public class VersionCheckerThread extends Thread {

    private VersionListener listener;
    private URL url;

    public VersionCheckerThread(VersionListener listener, URL latestVersionURL) {
        super("version checker");
        this.listener = listener;
        this.url = latestVersionURL;
    }

    public void run() {
        try {
            URLConnection connection = url.openConnection();
            connection.setRequestProperty("User-Agent", getUserAgent());
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
            try {
                String latestversion = reader.readLine();
                if (Version.getInstance().isOlderThan(latestversion)) {
                    listener.versionAvailable(new Version(latestversion));
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

    private String getUserAgent() {
        return getJbeadVersion() + ", " + getJavaVersion() + ", " + getOsVersion();
    }

    private String getJbeadVersion() {
        return "jbead " + Version.getInstance().getVersionString() + " " + Locale.getDefault();
    }

    private String getJavaVersion() {
        return "java " + System.getProperty("java.version") + " " + System.getProperty("java.vendor");
    }

    private String getOsVersion() {
        return System.getProperty("os.name") + " " +
                System.getProperty("os.version") + " " +
                System.getProperty("os.arch");
    }

}
