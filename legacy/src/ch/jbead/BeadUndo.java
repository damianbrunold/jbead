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
        for (int i = 0; i < MAXUNDO; i++) {
            data[i] = new BeadField();
            modified[i] = false;
        }
    }

    public boolean canUndo() {
        return current != first;
    }

    public boolean canRedo() {
        return current != last;
    }

    public void clear() {
        first = 0;
        last = 0;
        current = 0;
    }

    public void snapshot(BeadField data, boolean modified) {
        this.data[current].copyFrom(data);
        this.modified[current] = modified;
        current = (current + 1) % MAXUNDO;
        if (current == first) first = (first + 1) % MAXUNDO;
        last = current;
    }

    public void prepareSnapshot(BeadField data, boolean modified) {
        if (!modified) return;
        this.data[current].copyFrom(data);
        this.modified[current] = modified;
    }

    public void undo(BeadField data) {
        if (current == first) return;
        current = (current - 1 + MAXUNDO) % MAXUNDO;
        data.copyFrom(this.data[current]);
    }

    public void redo(BeadField data) {
        if (current == last) return;
        current = (current + 1) % MAXUNDO;
        data.copyFrom(this.data[current]);
    }

    public boolean isModified() {
        return modified[current];
    }

}
