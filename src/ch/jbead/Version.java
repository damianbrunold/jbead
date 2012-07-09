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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.regex.Pattern;

public class Version {

    private int major = -1;
    private int minor = -1;
    private int build = -1;
    private Date date;

    private static final Version INSTANCE = new Version();

    private Version() {
        try {
            File versionfile = new File(Version.class.getResource("/version.txt").toURI().getPath());
            readVersionFile(versionfile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void readVersionFile(File file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        try {
            String data = reader.readLine();
            if (data == null) return;
            String[] parts = data.split(Pattern.quote("."));
            if (parts == null) return;
            if (parts.length < 3) return;
            major = Integer.parseInt(parts[0]);
            minor = Integer.parseInt(parts[1]);
            build = Integer.parseInt(parts[2]);
            date = new Date(file.lastModified());
        } finally {
            reader.close();
        }
    }

    public static Version getInstance() {
        return INSTANCE;
    }

    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    public int getBuild() {
        return build;
    }

    public String getVersionString() {
        return String.format("%d.%d.%d", major, minor, build);
    }

    public Date getBuildDate() {
        return date;
    }

}
