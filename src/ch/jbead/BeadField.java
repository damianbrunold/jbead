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

import java.io.IOException;

/**
 * 
 */
public class BeadField {
    private static final int SIZE = 25 * 1000;

    private byte[] field = new byte[SIZE];
    private int width;
    private int height;

    public BeadField() {
        setWidth(15);
        clear();
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getLastIndex() {
        return width * height - 1;
    }

    public boolean isValidIndex(int idx) {
        return idx >= 0 && idx <= getLastIndex();
    }

    public void clear() {
        for (int i = 0; i < SIZE; i++) {
            field[i] = 0;
        }
    }

    public void setWidth(int width) {
        assert width > 0 && width < SIZE / 10;
        this.width = width;
        this.height = SIZE / width;
        assert this.width > 0 && this.height > 0;
    }
    
    public byte get(Point pt) {
        return get(getIndex(pt));
    }

    public byte get(int index) {
        return field[index];
    }

    public void set(Point pt, byte value) {
        set(getIndex(pt), value);
    }

    public void set(int index, byte value) {
        field[index] = value;
    }
    
    public int getIndex(Point pt) {
        return pt.getX() + width * pt.getY();
    }

    public Point getPoint(int index) {
        return new Point(index % width, index / width);
    }

    public void copyFrom(BeadField source) {
        setWidth(source.getWidth());
        for (int i = 0; i < width * height; i++) {
            set(i, source.get(i));
        }
    }

    public void save(JBeadOutputStream out) throws IOException {
        out.writeInt(width);
        out.write(field, 0, SIZE);
    }

    public void load(JBeadInputStream in) throws IOException {
        width = in.readInt();
        in.read(field, 0, SIZE);
        setWidth(width);
    }

    public void insertLine() {
        for (int j = getHeight() - 1; j > 0; j--) {
            for (int i = 0; i < getWidth(); i++) {
                Point pt = new Point(i, j);
                set(pt, get(pt.nextBelow()));
            }
        }
        for (int i = 0; i < getWidth(); i++) {
            set(new Point(i, 0), (byte) 0);
        }
    }

    public void deleteLine() {
        for (int j = 0; j < getHeight() - 1; j++) {
            for (int i = 0; i < getWidth(); i++) {
                Point pt = new Point(i, j);
                set(pt, get(pt.nextAbove()));
            }
        }
        for (int i = 0; i < getWidth(); i++) {
            set(new Point(i, getHeight() - 1), (byte) 0);
        }
    }

}
