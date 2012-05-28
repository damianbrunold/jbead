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

public class BeadRun {

    private byte color;
    private int count;

    public BeadRun(byte color, int count) {
        this.color = color;
        this.count = count;
    }

    public byte getColor() {
        return color;
    }

    public int getCount() {
        return count;
    }

    @Override
    public int hashCode() {
        return color ^ count;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        BeadRun other = (BeadRun) obj;
        if (color != other.color) return false;
        if (count != other.count) return false;
        return true;
    }

    @Override
    public String toString() {
        return color + "x" + count;
    }

}
