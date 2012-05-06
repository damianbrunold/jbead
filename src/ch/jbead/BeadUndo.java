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

/**
 * 
 */
public class BeadUndo {

	private static final int MAXUNDO = 100;

	private BeadField[] data = new BeadField[MAXUNDO];
	private boolean modified[] = new boolean[MAXUNDO];
	private int first;
	private int last;
	private int current;

	public BeadUndo() {
		first = 0;
		last = 0;
		current = 0;
	}

	public boolean CanUndo() {
		return current != first;
	}

	public boolean CanRedo() {
		return current != last;
	}

	public void Clear() {
		first = 0;
		last = 0;
		current = 0;
	}

	public void Snapshot(BeadField _data, boolean _modified) {
		data[current].CopyFrom(_data);
		modified[current] = _modified;
		current = (current + 1) % MAXUNDO;
		if (current == first) first = (first + 1) % MAXUNDO;
		last = current;
	}

	public void PreSnapshot(BeadField _data, boolean _modified) {
		if (!_modified) return;
		data[current].CopyFrom(_data);
		modified[current] = _modified;
	}

	public void Undo(BeadField _data) {
		if (current == first) return; // nothing to undo
		current = (current - 1 + MAXUNDO) % MAXUNDO;
		_data.CopyFrom(data[current]);
	}

	public void Redo(BeadField _data) {
		if (current == last) return; // nothing to redo
		current = (current + 1) % MAXUNDO;
		_data.CopyFrom(data[current]);
	}

	public boolean Modified() {
		return modified[current];
	}
}
