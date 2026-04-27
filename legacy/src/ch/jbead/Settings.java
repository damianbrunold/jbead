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

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class Settings {

	private static final String BASE = "brunoldsoft/jbead";

	private Preferences preferences = Preferences.userRoot().node(BASE);
	private String category = "general";

	public Settings() {
		category = "general";
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String category() {
		return category;
	}

	public boolean hasSetting(String name) {
	    return preferences.node(category).get(name, null) != null;
	}

	public int loadInt(String name) {
		return loadInt(name, 0);
	}

	public int loadInt(String name, int defaultvalue) {
		return preferences.node(category).getInt(name, defaultvalue);
	}

    public long loadLong(String name) {
        return loadLong(name, 0);
    }

    public long loadLong(String name, long defaultvalue) {
        return preferences.node(category).getLong(name, defaultvalue);
    }

    public boolean loadBoolean(String name) {
        return loadBoolean(name, false);
    }

    public boolean loadBoolean(String name, boolean defaultvalue) {
        return preferences.node(category).getBoolean(name, defaultvalue);
    }

	public String loadString(String name) {
		return loadString(name, "");
	}

	public String loadString(String name, String defaultvalue) {
		return preferences.node(category).get(name, defaultvalue);
	}

	public void saveInt(String name, int value) {
		preferences.node(category).putInt(name, value);
	}

    public void saveLong(String name, long value) {
        preferences.node(category).putLong(name, value);
    }

    public void saveBoolean(String name, boolean value) {
        preferences.node(category).putBoolean(name, value);
    }

	public void saveString(String name, String value) {
		preferences.node(category).put(name, value);
	}

    public void remove(String name) {
        preferences.node(category).remove(name);
    }

	public void flush() throws BackingStoreException {
		preferences.flush();
	}

}
