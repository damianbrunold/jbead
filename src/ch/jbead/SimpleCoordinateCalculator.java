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

public class SimpleCoordinateCalculator implements CoordinateCalculator {

    private int offsetx = 0;
    private int offsety = 0;
    private int gridx;
    private int gridy;

    public SimpleCoordinateCalculator(int gridx, int gridy) {
        this.gridx = gridx;
        this.gridy = gridy;
    }

    public void setOffsetX(int offsetx) {
        this.offsetx = offsetx;
    }

    public void setOffsetY(int offsety) {
        this.offsety = offsety;
    }

    public int getGridx() {
        return gridx;
    }

    public int getGridy() {
        return gridy;
    }

    public int x(Point pt) {
        return offsetx + pt.getX() * gridx;
    }

    public int y(Point pt) {
        return offsety + pt.getY() * gridy;
    }

    public int dx(Point pt) {
        return 0;
    }

    public int dx(int j) {
        return 0;
    }

    public int w(Point pt) {
        return gridx;
    }

}
