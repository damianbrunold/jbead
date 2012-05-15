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

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * 
 */
public class Settings {

	private static final String BASE = "Brunold Software/JBEAD";

	private Preferences preferences = Preferences.userRoot().node(BASE);
	private String category = "General";

	public Settings() {
		category = "General";
	}

	public void SetCategory(String category) {
		this.category = category;
	}

	public String Category() {
		return category;
	}

	public int LoadInt(String name) {
		return LoadInt(name, 0);
	}

	public int LoadInt(String name, int defaultvalue) {
		return preferences.node(category).getInt(name, defaultvalue);
	}

	public String LoadString(String name) {
		return LoadString(name, "");
	}

	public String LoadString(String name, String defaultvalue) {
		return preferences.node(category).get(name, defaultvalue);
	}

	public void SaveInt(String name, int value) {
		preferences.node(category).putInt(name, value);
	}

	public void SaveString(String name, String value) {
		preferences.node(category).put(name, value);
	}

	public void Flush() throws BackingStoreException {
		preferences.flush();
	}

}
