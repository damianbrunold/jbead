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

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.net.URL;

import junit.framework.TestCase;

public class VersionCheckerTest extends TestCase {

    public static class FakeVersionChecker extends VersionChecker {
        private URL url;

        public FakeVersionChecker(VersionListener listener, URL url) {
            super(listener);
            this.url = url;
        }

        protected URL getLatestVersionURL() {
            return url;
        }
    }

    public void testNewVersion() throws Exception {
        Version current = Version.getInstance();
        Version newversion = new Version(current.getMajor(), current.getMinor(), current.getBuild() + 1);
        String[] result = new String[1];
        checkVersion(createVersionFile(newversion), result);
        assertEquals("new version " + newversion.getVersionString(), result[0]);
    }

    public void testSameVersion() throws Exception {
        Version current = Version.getInstance();
        Version newversion = new Version(current.getMajor(), current.getMinor(), current.getBuild());
        String[] result = new String[1];
        checkVersion(createVersionFile(newversion), result);
        assertEquals("up to date", result[0]);
    }

    public void testOlderVersion() throws Exception {
        Version current = Version.getInstance();
        Version newversion = new Version(current.getMajor() - 1, current.getMinor(), current.getBuild());
        String[] result = new String[1];
        checkVersion(createVersionFile(newversion), result);
        assertEquals("up to date", result[0]);
    }

    public void testFailure() throws Exception {
        String[] result = new String[1];
        checkVersion(new URL("file:///nonexisting"), result);
        assertEquals("failure", result[0]);
    }

    private void checkVersion(URL url, final String[] result) throws Exception {
        VersionChecker checker = new FakeVersionChecker(new VersionListener() {
            public void versionAvailable(Version version) {
                synchronized (result) {
                    result[0] = "new version " + version.getVersionString();
                }
            }
            public void versionUpToDate() {
                synchronized (result) {
                    result[0] = "up to date";
                }
            }
            public void failure(String msg) {
                synchronized (result) {
                    result[0] = "failure";
                }
            }
        }, url);
        checker.check();
        while (true) {
            synchronized (result) {
                if (result[0] != null) break;
            }
            Thread.sleep(10);
        }
    }

    private URL createVersionFile(Version version) throws Exception {
        File file = File.createTempFile("temp_jbead_version", ".txt");
        Writer out = new FileWriter(file);
        try {
            out.write(version.getVersionString());
            out.write("\n");
        } finally {
            out.close();
        }
        return file.toURL();
    }
}
