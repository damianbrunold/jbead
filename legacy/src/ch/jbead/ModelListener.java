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

public interface ModelListener {

    public void pointChanged(Point pt);
    public void modelChanged();
    public void colorChanged(byte colorIndex);
    public void colorsChanged();
    public void scrollChanged(int scroll);
    public void shiftChanged(int shift);
    public void zoomChanged(int gridx, int gridy);
    public void repeatChanged(int repeat);

}
