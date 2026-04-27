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

import java.net.MalformedURLException;
import java.net.URL;

public class VersionChecker {

    private VersionListener listener;

    public VersionChecker(VersionListener listener) {
        this.listener = listener;
    }

    protected URL getLatestVersionURL() throws MalformedURLException {
        return new URL("http://www.jbead.ch/latestversion");
    }

    public void check() {
        try {
            VersionCheckerThread thread = new VersionCheckerThread(listener, getLatestVersionURL());
            thread.start();
        } catch (MalformedURLException e) {
            listener.failure(e.toString());
        }

    }
}
