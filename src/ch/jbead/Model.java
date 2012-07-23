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

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JFileChooser;

import ch.jbead.storage.JBeadFileFormatException;

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
    private String unnamed;
    private boolean saved;
    private boolean modified;
    private String author = "";
    private String notes = "";

    private List<ModelListener> listeners = new ArrayList<ModelListener>();


    public Model(Localization localization) {
        repeatDirty = false;
        field.clear();
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

    private void fireColorsChanged() {
        for (ModelListener listener : listeners) {
            listener.colorsChanged();
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

    private void fireRepeatChanged(int repeat) {
        for (ModelListener listener : listeners) {
            listener.repeatChanged(repeat);
        }
    }

    private void defaultColors() {
        colors.clear();
        colors.addAll(new DefaultColors());
    }

    public Color getColor(byte index) {
        return colors.get(index);
    }

    public int getColorCount() {
        return colors.size();
    }

    public void setColor(byte index, Color color) {
        snapshot();
        colors.set(index, color);
        setModified();
        fireColorChanged(index);
    }

    public void setSelectedColor(byte colorIndex) {
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
        normalizeShift();
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

    public void insertRow() {
        snapshot();
        field.insertRow();
        setRepeatDirty();
        setModified();
        fireModelChanged();
    }

    public void deleteRow() {
        snapshot();
        field.deleteRow();
        setRepeatDirty();
        setModified();
        fireModelChanged();
    }

    public void drawLine(Point begin, Point end) {
        snapshot();
        setModified();
        for (Point pt : new Segment(begin.scrolled(scroll), end.scrolled(scroll))) {
            set(pt, colorIndex);
        }
        setRepeatDirty();
    }

    public void fillLine(Point pt) {
        snapshot();
        setModified();
        pt = pt.scrolled(scroll);
        byte color = colorIndex;
        byte background = get(pt);
        int startIndex = getIndex(pt);
        for (int index = startIndex; index >= 0; index--) {
            Point p = getPoint(index);
            if (get(p) != background) {
                break;
            }
            set(p, color);
        }
        int last = getIndex(new Point(getWidth() - 1, getUsedHeight() - 1));
        for (int index = startIndex + 1; index <= last; index++) {
            Point p = getPoint(index);
            if (get(p) != background) {
                break;
            }
            set(p, color);
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
        for (Point pt : new Rect(selection.getBegin(), selection.getEnd())) {
            byte c = buffer.get(pt);
            if (c == 0) continue;
            int idx = field.getIndex(pt);
            for (int k = 0; k < copies; k++) {
                idx += offset;
                if (isValidIndex(idx)) set(idx, c);
            }
        }
        setRepeatDirty();
    }

    public BeadField getCopy() {
        BeadField copy = new BeadField();
        copy.copyFrom(field);
        return copy;
    }

    public Rect getFullRect() {
        return field.getFullRect();
    }

    public Rect getRect(int starty, int endy) {
        return field.getRect(starty, endy);
    }

    public Rect getUsedRect() {
        int height = getUsedHeight();
        if (height == 0) return Rect.EMPTY;
        return field.getRect(0, height - 1);
    }

    public File getFile() {
        return file;
    }

    public File getCurrentDirectory() {
        if (isSaved()) {
            return file.getParentFile();
        } else {
            JFileChooser chooser = new JFileChooser();
            File dir = chooser.getFileSystemView().getDefaultDirectory();
            File documents = new File(dir, "Documents");
            if (documents.exists()) {
                return documents;
            } else {
                return dir;
            }
        }
    }

    public int getRepeat() {
        return repeat;
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
        shift++;
        normalizeShift();
        fireShiftChanged(shift);
    }

    public void shiftLeft() {
        shift--;
        normalizeShift();
        fireShiftChanged(shift);
    }

    public void normalizeShift() {
        while (shift < 0) shift += getWidth();
        while (shift > getWidth()) shift -= getWidth();
    }

    public void clear() {
        undo.clear();
        field.clear();
        repeat = 0;
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
        fireModelChanged();
    }

    public byte getSelectedColor() {
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

    public void updateRepeat() {
        int usedHeight = getUsedHeight();
        if (usedHeight == 0) {
            setRepeat(0);
        } else {
            setRepeat(calcRepeat(usedHeight));
        }
    }

    private void setRepeat(int repeat) {
        repeatDirty = false;
        this.repeat = repeat;
        fireRepeatChanged(repeat);
    }

    private int calcRepeat(int usedheight) {
        for (int i = 1; i < usedheight * field.getWidth(); i++) {
            if (field.get(i) == field.get(0)) {
                boolean ok = true;
                for (int k = i + 1; k < usedheight * field.getWidth(); k++) {
                    if (field.get((k - i) % i) != field.get(k)) {
                        ok = false;
                        break;
                    }
                }
                if (ok) {
                    return i;
                }
            }
        }
        return usedheight * field.getWidth();
    }

    public int getUsedHeight() {
        int usedHeight = 0;
        for (Point pt : field.getFullRect()) {
            if (field.get(pt) > 0) {
                usedHeight = pt.getY() + 1;
            }
        }
        return usedHeight;
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

    public Point correct(Point pt) {
        int idx = pt.getX() + (pt.getY() + scroll) * getWidth();
        int m1 = getWidth();
        int m2 = m1 + 1;
        int k = 0;
        int m = m1 ;
        while (idx >= m) {
            idx -= m;
            k++;
            m = (k % 2 == 0) ? m1 : m2;
        }
        return new Point(idx, k - scroll);
    }

    public int getIndex(Point pt) {
        return field.getIndex(pt);
    }

    public int getCorrectedIndex(Point pt) {
        int m1 = getWidth();
        int m2 = m1 + 1;
        int j = pt.getY() + scroll;
        if (j % 2 == 0) {
            return (j / 2) * (m1 + m2) + pt.getX();
        } else {
            return (j / 2 + 1) * m1 + (j / 2) * m2 + pt.getX();
        }
    }

    public Point getPoint(int index) {
        return field.getPoint(index);
    }

    public void setHeight(int height) {
        field.setHeight(height);
        fireModelChanged();
    }

    public void saveTo(Memento memento) {
        if (getUsedColors().size() > memento.getMaxSupportedColors()) {
            throw new JBeadFileFormatException("Too many colors, only " + memento.getMaxSupportedColors() + " are supported with this file format.");
        }
        if (memento.compactifyColors()) {
            compactifyColors();
        }
        field.saveTo(memento);
        memento.setColors(colors);
        memento.setColorIndex(colorIndex);
        memento.setZoomIndex(zoomIndex);
        memento.setShift(shift);
        memento.setScroll(scroll);
        memento.setAuthor(author);
        memento.setNotes(notes);
    }

    public void loadFrom(Memento memento) {
        field.loadFrom(memento);
        colors = memento.getColors();
        fillDefaultColorsUp();
        colorIndex = memento.getColorIndex();
        zoomIndex = memento.getZoomIndex();
        shift = memento.getShift();
        scroll = memento.getScroll();
        author = memento.getAuthor();
        notes = memento.getNotes();
        fireModelChanged();
    }

    private void fillDefaultColorsUp() {
        DefaultColors palette = new DefaultColors();
        if (colors.size() < DefaultColors.NUMBER_OF_COLORS) {
            for (int i = colors.size(); i < DefaultColors.NUMBER_OF_COLORS; i++) {
                colors.add(palette.get(i));
            }
        }
    }

    public void mirrorHorizontal(Rect rect) {
        snapshot();
        field.mirrorHorizontal(rect.scrolled(scroll));
        setModified();
        setRepeatDirty();
        fireModelChanged();
    }

    public void mirrorVertical(Rect rect) {
        snapshot();
        field.mirrorVertical(rect.scrolled(scroll));
        setModified();
        setRepeatDirty();
        fireModelChanged();
    }

    public void rotate(Rect rect) {
        if (!rect.isSquare()) return;
        snapshot();
        field.rotate(rect.scrolled(scroll));
        setModified();
        setRepeatDirty();
        fireModelChanged();
    }

    public void delete(Rect rect) {
        snapshot();
        field.delete(rect.scrolled(scroll));
        setModified();
        setRepeatDirty();
        fireModelChanged();
    }

    public Set<Byte> getUsedColors() {
        Set<Byte> usedcolors = new HashSet<Byte>();
        for (int i = 0; i < field.getLastIndex(); i++) {
            usedcolors.add(field.get(i));
        }
        return usedcolors;
    }

    public void compactifyColors() {
        snapshot();
        Set<Byte> usedcolors = getUsedColors();
        boolean[] colorInUse = new boolean[colors.size()];
        for (byte color : usedcolors) {
            colorInUse[color] = true;
        }
        for (byte color = 0; color < colors.size(); color++) {
            if (colorInUse[color]) continue;
            byte used = firstUsedAfter(colorInUse, color);
            if (used == -1) break;
            moveColor(used, color);
            colorInUse[color] = true;
            colorInUse[used] = false;
        }
    }

    private byte firstUsedAfter(boolean[] colorInUse, byte index) {
        for (byte i = (byte) (index + 1); i < colors.size(); i++) {
            if (colorInUse[i]) return i;
        }
        return -1;
    }

    private void moveColor(byte src, byte dest) {
        field.replace(src, dest);
        switchColors(src, dest);
        fireColorsChanged();
    }

    private void switchColors(byte src, byte dest) {
        Color temp = colors.get(src);
        colors.set(src, colors.get(dest));
        colors.set(dest, temp);
    }

    public String getAuthor() {
        return author;
    }

    public String getNotes() {
        return notes;
    }

}
