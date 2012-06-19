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
import java.io.IOException;

public class DbbMemento extends Memento {

    @Override
    public void save(JBeadOutputStream out) throws IOException {
        out.writeInt(width);
        out.write(data, 0, Math.min(BeadField.DEFAULT_SIZE, data.length));
        if (data.length <= BeadField.DEFAULT_SIZE) {
            for (int i = data.length; i < BeadField.DEFAULT_SIZE; i++) {
                out.write(0);
            }
        }
        if (colors.size() > 10) {
            throw new RuntimeException("Cannot save pattern with more than 10 colors in DB-BEAD file format");
        }
        for (Color color : colors) {
            out.writeColor(color);
        }
        out.write(colorIndex);
        out.writeInt(zoomIndex);
        out.writeInt(shift);
        out.writeInt(scroll);
        out.writeBool(draftVisible);
        out.writeBool(correctedVisible);
        out.writeBool(simulationVisible);
        // report flag is not saved
    }

    @Override
    public void load(JBeadInputStream in) throws IOException {
        width = in.readInt();
        height = BeadField.DEFAULT_SIZE / width;
        data = new byte[BeadField.DEFAULT_SIZE + BeadField.DEFAULT_SIZE % width];
        in.read(data, 0, BeadField.DEFAULT_SIZE);
        colors.clear();
        colors.add(in.readBackgroundColor());
        for (int i = 1; i < 10; i++) {
            colors.add(in.readColor());
        }
        colorIndex = readByte(in, "colorIndex");
        zoomIndex = readInt(in, "zoomIndex");
        shift = readInt(in, "shift");
        scroll = readInt(in, "scroll");
        draftVisible = in.readBool();
        correctedVisible = in.readBool();
        simulationVisible = in.readBool();
        reportVisible = true;
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

}
