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

public class BeadField {

    public static final int DEFAULT_WIDTH = 15;
    private static final int DEFAULT_SIZE = 25 * 1000;

    private byte[] field = new byte[DEFAULT_SIZE];
    private int width;
    private int height;

    public BeadField() {
        setWidth(DEFAULT_WIDTH);
        clear();
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Rect getFullRect() {
        return new Rect(new Point(0, 0), new Point(width - 1, height - 1));
    }

    public Rect getRect(int starty, int endy) {
        return new Rect(new Point(0, starty), new Point(width - 1, endy));
    }

    public int getLastIndex() {
        return width * height - 1;
    }

    public boolean isValidIndex(int idx) {
        return idx >= 0 && idx <= getLastIndex();
    }

    public void clear() {
        for (int i = 0; i < field.length; i++) {
            field[i] = 0;
        }
    }

    public void setWidth(int width) {
        this.width = width;
        this.height = field.length / width;
    }

    public void setHeight(int height) {
        if (this.height == height) return;
        byte[] field = new byte[this.width * this.height];
        System.arraycopy(this.field, 0, field, 0, height * width);
        this.height = height;
        this.field = field;
    }

    public void copyFrom(BeadField source) {
        setWidth(source.getWidth());
        setHeight(source.getHeight());
        for (int i = 0; i < width * height; i++) {
            set(i, source.get(i));
        }
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

    public void save(JBeadOutputStream out) throws IOException {
        out.writeInt(width);
        out.write(field, 0, field.length);
    }

    public void load(JBeadInputStream in) throws IOException {
        width = in.readInt();
        setHeight(DEFAULT_SIZE / width);
        in.read(field, 0, field.length);
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
