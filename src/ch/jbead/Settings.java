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

	public void SetCategory(String _category) {
		category = _category;
	}

	public String Category() {
		return category;
	}

	public int LoadInt(String _name) {
		return LoadInt(_name, 0);
	}

	public int LoadInt(String _name, int _default) {
		return preferences.node(category).getInt(_name, _default);
	}

	public String LoadString(String _name) {
		return LoadString(_name, "");
	}

	public String LoadString(String _name, String _default) {
		return preferences.node(category).get(_name, _default);
	}

	public void SaveInt(String _name, int _value) {
		preferences.node(category).putInt(_name, _value);
	}

	public void SaveString(String _name, String _value) {
		preferences.node(category).put(_name, _value);
	}

	public void Flush() throws BackingStoreException {
		preferences.flush();
	}

}
