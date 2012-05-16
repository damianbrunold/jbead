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

/**
 * 
 */
public class Model implements ColorTable {

    private BeadUndo undo = new BeadUndo();
    private BeadField field = new BeadField();
    private Color colors[] = new Color[10];
    private byte colorIndex;
    private int grid;
    private int zoomtable[] = new int[5];
    private int zoomIndex;
    private int scroll;
    private int shift;
    private File file = new File(Texts.text("unnamed", "unbenannt"));
    private boolean repeatDirty;
    private int repeat;
    private int colorRepeat;


    public Model() {
        repeatDirty = false;
        field.clear();
        field.setWidth(15);
        colorIndex = 1;
        scroll = 0;
        zoomIndex = 2;
        zoomtable[0] = 6;
        zoomtable[1] = 8;
        zoomtable[2] = 10;
        zoomtable[3] = 12;
        zoomtable[4] = 14;
        grid = zoomtable[zoomIndex];
        defaultColors();
    }

    private void defaultColors() {
        colors[0] = new Color(240, 240, 240); // was clBtnFace
        colors[1] = new Color(128, 0, 0); // maroon
        colors[2] = new Color(0, 0, 128); // navy
        colors[3] = Color.GREEN;
        colors[4] = Color.YELLOW;
        colors[5] = Color.RED;
        colors[6] = Color.BLUE;
        colors[7] = new Color(128, 0, 128); // purple
        colors[8] = Color.BLACK;
        colors[9] = Color.WHITE;
    }


    @Override
    public Color getColor(int index) {
        return colors[index];
    }

    @Override
    public int getColorCount() {
        return colors.length;
    }
    
    @Override
    public void setColor(int index, Color color) {
        colors[index] = color;
    }
    
    public void setColorIndex(byte colorIndex) {
        this.colorIndex = colorIndex;
    }
    
    public BeadField getField() {
        return field;
    }

    public File getFile() {
        return file;
    }
    
    public int getColorRepeat() {
        return colorRepeat;
    }
    
    public int getGrid() {
        return grid;
    }
    
    public int getScroll() {
        return scroll;
    }
    
    public void setScroll(int scroll) {
        this.scroll = scroll;
    }
    
    public int getShift() {
        return shift;
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
        file = new File(Texts.text("unnamed", "unbenannt"));
    }
    
    public void setFile(File file) {
        this.file = file;
    }
    
    public void load(JBeadInputStream in) throws IOException {
        field.load(in);
        for (int i = 0; i < getColorCount(); i++) {
            setColor(i, in.readColor());
        }
        colorIndex = in.read();
        zoomIndex = in.readInt();
        shift = in.readInt();
        scroll = in.readInt();
    }
    
    public void save(JBeadOutputStream out) throws IOException {
        field.save(out);
        for (Color color : colors) {
            out.writeColor(color);
        }
        out.writeInt(colorIndex);
        out.writeInt(zoomIndex);
        out.writeInt(shift);
        out.writeInt(scroll);
    }

    public byte getColorIndex() {
        return colorIndex;
    }
    
    public void setRepeatDirty() {
        repeatDirty = true;
    }

    public void snapshot(boolean modified) {
        undo.snapshot(field, modified);
    }
    
    public void prepareSnapshot(boolean modified) {
        undo.prepareSnapshot(field, modified);        
    }

    public boolean undo() {
        undo.undo(field);
        return undo.isModified();
    }

    public boolean redo() {
        undo.redo(field);
        return undo.isModified();
    }

    public void zoomIn() {
        if (zoomIndex < zoomtable.length - 1) zoomIndex++;
        grid = zoomtable[zoomIndex];
    }
    
    public void zoomNormal() {
        zoomIndex = 2;
        grid = zoomtable[zoomIndex];
    }
    
    public void zoomOut() {
        if (zoomIndex > 0) zoomIndex--;
        grid = zoomtable[zoomIndex];
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
        // Musterrapport neu berechnen
        int last = -1;
        for (int j = 0; j < field.getHeight(); j++) {
            for (int i = 0; i < field.getWidth(); i++) {
                int c = field.get(i, j);
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
    }

    public boolean equalRows(int j, int k) {
        for (int i = 0; i < getField().getWidth(); i++) {
            if (getField().get(i, j) != getField().get(i, k)) return false;
        }
        return true;
    }

    public int getZoomIndex() {
        return zoomIndex;
    }
}
