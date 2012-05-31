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

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class Model implements ColorTable {

    private BeadUndo undo = new BeadUndo();
    private BeadField field = new BeadField();
    private List<Color> colors = new ArrayList<Color>();
    private byte colorIndex;
    private int gridx;
    private int gridy;
    private int zoomtable[] = new int[5];
    private int zoomIndex;
    private int scroll;
    private int shift;
    private File file;
    private boolean repeatDirty;
    private int repeat;
    private int colorRepeat;
    private String unnamed;
    private boolean saved;
    private boolean modified;

    private List<ModelListener> listeners = new ArrayList<ModelListener>();


    public Model(Localization localization) {
        repeatDirty = false;
        field.clear();
        field.setWidth(15);
        colorIndex = 1;
        scroll = 0;
        initZoomTable();
        gridx = gridy = zoomtable[zoomIndex];
        defaultColors();
        unnamed = localization.getString("unnamed");
        file = new File(unnamed);
    }

    private void initZoomTable() {
        zoomIndex = 2;
        zoomtable[0] = 6;
        zoomtable[1] = 8;
        zoomtable[2] = 10;
        zoomtable[3] = 12;
        zoomtable[4] = 14;
    }

    public void addListener(ModelListener listener) {
        listeners.add(listener);
    }

    private void firePointChanged(Point pt) {
        for (ModelListener listener : listeners) {
            listener.pointChanged(pt);
        }
    }

    private void fireModelChanged() {
        for (ModelListener listener : listeners) {
            listener.modelChanged();
        }
    }

    private void fireColorChanged(byte colorIndex) {
        for (ModelListener listener : listeners) {
            listener.colorChanged(colorIndex);
        }
    }

    private void fireScrollChanged(int scroll) {
        for (ModelListener listener : listeners) {
            listener.scrollChanged(scroll);
        }
    }

    private void fireShiftChanged(int scroll) {
        for (ModelListener listener : listeners) {
            listener.shiftChanged(shift);
        }
    }

    private void fireZoomChanged(int gridx, int gridy) {
        for (ModelListener listener : listeners) {
            listener.zoomChanged(gridx, gridy);
        }
    }

    private void fireRepeatChanged(int repeat, int colorRepeat) {
        for (ModelListener listener : listeners) {
            listener.repeatChanged(repeat, colorRepeat);
        }
    }
    
    private static Color WHITE = Color.WHITE;
    private static Color MAROON = new Color(128, 0, 0);
    private static Color DKBLUE = new Color(0, 0, 128);
    private static Color GREEN = Color.GREEN;
    private static Color YELLOW = Color.YELLOW;
    private static Color RED = Color.RED;
    private static Color BLUE = Color.BLUE;
    private static Color PURPLE = new Color(128, 0, 128);
    private static Color BLACK = Color.BLACK;
    private static Color CYAN = Color.CYAN;
    private static Color SILVER = new Color(192, 192, 192);

    private void defaultColors() {
        colors.clear();
        colors.add(WHITE);
        colors.add(MAROON);
        colors.add(DKBLUE);
        colors.add(GREEN);
        colors.add(YELLOW);
        colors.add(RED);
        colors.add(BLUE);
        colors.add(PURPLE);
        colors.add(BLACK);
        colors.add(CYAN);
        //colors.add(SILVER);
    }

    @Override
    public Color getColor(byte index) {
        return colors.get(index);
    }

    @Override
    public int getColorCount() {
        return colors.size();
    }

    @Override
    public void setColor(byte index, Color color) {
        snapshot();
        colors.set(index, color);
        setModified();
        fireColorChanged(index);
    }

    public void setColorIndex(byte colorIndex) {
        this.colorIndex = colorIndex;
    }

    public int getHeight() {
        return field.getHeight();
    }

    public int getWidth() {
        return field.getWidth();
    }

    public void setWidth(int width) {
        snapshot();
        field.setWidth(width);
        setModified();
        setRepeatDirty();
        fireModelChanged();
    }

    public byte get(Point pt) {
        return field.get(pt);
    }

    public void set(Point pt, byte value) {
        field.set(pt, value);
        firePointChanged(pt);
    }

    public boolean isValidIndex(int idx) {
        return field.isValidIndex(idx);
    }

    public void set(int idx, byte value) {
        field.set(idx, value);
        firePointChanged(field.getPoint(idx));
    }

    public byte get(int idx) {
        return field.get(idx);
    }

    public void insertLine() {
        snapshot();
        field.insertLine();
        setRepeatDirty();
        setModified();
        fireModelChanged();
    }

    public void deleteLine() {
        snapshot();
        field.deleteLine();
        setRepeatDirty();
        setModified();
        fireModelChanged();
    }

    public void drawLine(Point begin, Point end) {
        snapshot();
        setModified();
        for (Point pt : new Segment(begin.scrolled(scroll), end.scrolled(scroll))) {
            set(pt.scrolled(scroll), colorIndex);
        }
        setRepeatDirty();
    }

    public void fillLine(Point pt) {
        snapshot();
        setModified();
        pt = pt.scrolled(scroll);
        byte color = colorIndex;
        byte background = get(pt);
        for (Point point : new Segment(pt, pt.lastRight(getWidth()))) {
            if (get(point) != background) {
                break;
            }
            set(point, color);
        }
        if (pt.getX() != 0) {
            for (Point point : new Segment(pt.nextLeft(), pt.lastLeft())) {
                if (get(point) != background) {
                    break;
                }
                set(point, color);
            }
        }
        setRepeatDirty();
    }

    public void setPoint(Point pt) {
        snapshot();
        setModified();
        pt = pt.scrolled(scroll);
        if (get(pt) == colorIndex) {
            set(pt, (byte) 0);
        } else {
            set(pt, colorIndex);
        }
        setRepeatDirty();
    }

    public void arrangeSelection(Selection selection, int copies, int offset) {
        snapshot();
        setModified();
        BeadField buffer = getCopy();
        for (int i = selection.left(); i <= selection.right(); i++) {
            for (int j = selection.bottom(); j <= selection.top(); j++) {
                byte c = buffer.get(new Point(i, j));
                if (c == 0) continue;
                int idx = field.getIndex(new Point(i, j));
                for (int k = 0; k < copies; k++) {
                    idx += offset;
                    if (isValidIndex(idx)) set(idx, c);
                }
            }
        }
        setRepeatDirty();
    }

    public BeadField getCopy() {
        BeadField copy = new BeadField();
        copy.copyFrom(field);
        return copy;
    }

    public File getFile() {
        return file;
    }

    public File getCurrentDirectory() {
        if (isSaved()) {
            return file.getParentFile();
        } else {
            // TODO maybe use the Documents folder on windows...
            return new File(System.getProperty("user.home"));
        }
    }

    public int getColorRepeat() {
        return colorRepeat;
    }

    public int getGridx() {
        return gridx;
    }

    public int getGridy() {
        return gridy;
    }

    public int getScroll() {
        return scroll;
    }

    public void setScroll(int scroll) {
        this.scroll = scroll;
        fireScrollChanged(scroll);
    }

    public int getShift() {
        return shift;
    }

    public void shiftRight() {
        shift = (shift + 1) % getWidth();
        fireShiftChanged(shift);
    }

    public void shiftLeft() {
        shift = (shift - 1 + getWidth()) % getWidth();
        fireShiftChanged(shift);
    }

    public void clear() {
        undo.clear();
        field.clear();
        repeat = 0;
        colorRepeat = 0;
        colorIndex = 1;
        defaultColors();
        zoomIndex = 2;
        scroll = 0;
        shift = 0;
        file = new File(unnamed);
        saved = false;
        modified = false;
        fireModelChanged();
    }

    public void setSaved() {
        saved = true;
    }

    public boolean isSaved() {
        return saved;
    }

    public void setModified() {
        modified = true;
    }

    public void setModified(boolean modified) {
        this.modified = modified;
    }

    public boolean isModified() {
        return modified;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public void load(JBeadInputStream in, boolean compatible) throws IOException {
        field.load(in);
        colors.clear();
        if (compatible) {
            colors.add(in.readBackgroundColor());
            for (int i = 1; i < 10; i++) {
                colors.add(in.readColor());
            }
        } else {
            int colorCount = readInt(in, "colorCount");
            for (int i = 0; i < colorCount; i++) {
                colors.add(in.readColor());
            }
        }
        colorIndex = readByte(in, "colorIndex");
        zoomIndex = readInt(in, "zoomIndex");
        shift = readInt(in, "shift");
        scroll = readInt(in, "scroll");
        fireModelChanged();
    }

    private byte readByte(JBeadInputStream in, String name) throws IOException {
        byte result = in.read();
        if (result < 0) throw new RuntimeException("file format error: byte " + name + " was negative");
        return result;
    }

    private int readInt(JBeadInputStream in, String name) throws IOException {
        int result = in.readInt();
        if (result < 0) throw new RuntimeException("file format error: int " + name + " was negative");
        return result;
    }

    public void save(JBeadOutputStream out) throws IOException {
        field.save(out);
        if (colors.size() > 10) {
            out.writeInt(colors.size());
        }
        for (Color color : colors) {
            out.writeColor(color);
        }
        out.write(colorIndex);
        out.writeInt(zoomIndex);
        out.writeInt(shift);
        out.writeInt(scroll);
        fireModelChanged();
    }

    public byte getColorIndex() {
        return colorIndex;
    }

    public void setRepeatDirty() {
        repeatDirty = true;
    }

    public void snapshot() {
        undo.snapshot(field, modified);
    }

    public void prepareSnapshot() {
        undo.prepareSnapshot(field, modified);
    }

    public void undo() {
        undo.undo(field);
        modified = undo.isModified();
        setRepeatDirty();
        fireModelChanged();
    }

    public void redo() {
        undo.redo(field);
        modified = undo.isModified();
        setRepeatDirty();
        fireModelChanged();
    }

    public void zoomIn() {
        if (zoomIndex >= zoomtable.length - 1) return;
        zoomIndex++;
        gridx = gridy = zoomtable[zoomIndex];
        fireZoomChanged(gridx, gridy);
    }

    public void zoomNormal() {
        if (isNormalZoom()) return;
        zoomIndex = 2;
        gridx = gridy = zoomtable[zoomIndex];
        fireZoomChanged(gridx, gridy);
    }

    public void zoomOut() {
        if (zoomIndex <= 0) return;
        zoomIndex--;
        gridx = gridy = zoomtable[zoomIndex];
        fireZoomChanged(gridx, gridy);
    }

    public boolean isNormalZoom() {
        return zoomIndex == 2;
    }

    public boolean canUndo() {
        return undo.canUndo();
    }

    public boolean canRedo() {
        return undo.canRedo();
    }

    public boolean isRepeatDirty() {
        return repeatDirty;
    }

    public int getRepeat() {
        return repeat;
    }

    public void updateRepeat() {
        int oldRepeat = repeat;
        int oldColorRepeat = colorRepeat;

        int last = -1;
        for (int j = 0; j < field.getHeight(); j++) {
            for (int i = 0; i < field.getWidth(); i++) {
                int c = field.get(new Point(i, j));
                if (c > 0) {
                    last = j;
                    break;
                }
            }
        }
        if (last == -1) {
            repeat = 0;
            colorRepeat = 0;
            repeatDirty = false;
            if (oldRepeat != repeat || oldColorRepeat != colorRepeat) {
                fireRepeatChanged(repeat, colorRepeat);
            }
            return;
        }
        repeat = last + 1;
        for (int j = 1; j <= last; j++) {
            if (equalRows(0, j)) {
                boolean ok = true;
                for (int k = j + 1; k <= last; k++) {
                    if (!equalRows((k - j) % j, k)) {
                        ok = false;
                        break;
                    }
                }
                if (ok) {
                    repeat = j;
                    break;
                }
            }
        }

        // Farbrapport neu berechnen
        colorRepeat = repeat * field.getWidth();
        for (int i = 1; i <= repeat * field.getWidth(); i++) {
            if (field.get(i) == field.get(0)) {
                boolean ok = true;
                for (int k = i + 1; k <= repeat * field.getWidth(); k++) {
                    if (field.get((k - i) % i) != field.get(k)) {
                        ok = false;
                        break;
                    }
                }
                if (ok) {
                    colorRepeat = i;
                    break;
                }
            }
        }

        repeatDirty = false;
        if (oldRepeat != repeat || oldColorRepeat != colorRepeat) {
            fireRepeatChanged(repeat, colorRepeat);
        }
    }

    public boolean equalRows(int j, int k) {
        for (int i = 0; i < getWidth(); i++) {
            if (get(new Point(i, j)) != get(new Point(i, k))) return false;
        }
        return true;
    }

    public int getZoomIndex() {
        return zoomIndex;
    }
}
