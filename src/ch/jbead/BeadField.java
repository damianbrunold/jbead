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
        return get(pt.getX(), pt.getY());
    }

    public byte get(int x, int y) {
        assert width > 0 && height > 0;
        assert x < width;
        assert y < height;
        return field[x + width * y];
    }

    public byte get(int index) {
        assert width > 0;
        assert index >= 0 && index < width * height;
        int i = index % width;
        int j = index / width;
        return get(i, j);
    }

    public void set(Point pt, byte value) {
        set(pt.getX(), pt.getY(), value);
    }

    public void set(int x, int y, byte value) {
        assert width > 0 && height > 0;
        assert x < width;
        assert y < height;
        field[x + width * y] = value;
    }

    public void set(int index, byte value) {
        assert width > 0;
        assert index >= 0 && index < width * height;
        int i = index % width;
        int j = index / width;
        set(i, j, value);
    }

    public byte rawGet(int index) {
        return field[index];
    }

    public void rawSet(int index, byte value) {
        field[index] = value;
    }

    public void copyFrom(BeadField source) {
        setWidth(source.getWidth());
        for (int i = 0; i < width * height; i++) {
            rawSet(i, source.rawGet(i));
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
                set(i, j, get(i, j - 1));
            }
        }
        for (int i = 0; i < getWidth(); i++) {
            set(i, 0, (byte) 0);
        }
    }

    public void deleteLine() {
        for (int j = 0; j < getHeight() - 1; j++) {
            for (int i = 0; i < getWidth(); i++) {
                set(i, j, get(i, j + 1));
            }
        }
        for (int i = 0; i < getWidth(); i++) {
            set(i, getHeight() - 1, (byte) 0);
        }
    }

}
