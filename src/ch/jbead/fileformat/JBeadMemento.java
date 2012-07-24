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

package ch.jbead.fileformat;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ch.jbead.storage.Node;
import ch.jbead.storage.ObjectModel;

public class JBeadMemento extends Memento {

    public static final int VERSION = 1;

    @Override
    public void save(JBeadOutputStream out) throws IOException {
        ObjectModel om = new ObjectModel("jbb");
        om.add("version", VERSION);
        om.add("author", author);
        om.add("notes", notes);
        for (Color color: colors) {
            om.add("colors/rgb", color.getRed(), color.getGreen(), color.getBlue());
        }
        om.add("view/draft-visible", draftVisible);
        om.add("view/corrected-visible", correctedVisible);
        om.add("view/simulation-visible", simulationVisible);
        om.add("view/report-visible", reportVisible);
        om.add("view/selected-tool", selectedTool);
        om.add("view/selected-color", colorIndex);
        om.add("view/zoom", zoomIndex);
        om.add("view/scroll", scroll);
        om.add("view/shift", shift);
        for (int j = 0; j < height; j++) {
            List<Byte> row = new ArrayList<Byte>();
            for (int i = 0; i < width; i++) {
                row.add(data[j * width + i]);
            }
            om.add("model/row", row.toArray());
        }
        out.write(om.toString());
    }

    @Override
    public void load(JBeadInputStream in) throws IOException {
        ObjectModel om = ObjectModel.fromData(in.readAll());
        int version = om.getIntValue("version", 1);
        if (version < VERSION) {
            upgrade(om, version);
        }
        author = (String) om.getValue("author");
        notes = (String) om.getValue("notes");
        colors.clear();
        for (Node color : om.getAll("colors/rgb")) {
            colors.add(getColor(color));
        }
        draftVisible = om.getBoolValue("view/draft-visible", true);
        correctedVisible = om.getBoolValue("view/corrected-visible", true);
        simulationVisible = om.getBoolValue("view/simulation-visible", true);
        reportVisible = om.getBoolValue("view/report-visible", true);
        colorIndex = (byte) om.getIntValue("view/selected-color", 1);
        selectedTool = om.getStringValue("view/selected-tool", "pencil");
        zoomIndex = om.getIntValue("view/zoom", 2);
        scroll = om.getIntValue("view/scroll", 0);
        shift = om.getIntValue("view/shift", 0);
        List<Node> rows = om.getAll("model/row");
        height = rows.size();
        width = rows.get(0).size();
        data = new byte[width * height];
        int idx = 0;
        for (Node row : rows) {
            for (int i = 0; i < width; i++) {
                data[idx++] = (byte) row.asLeaf().getIntValue(i);
            }
        }
    }

    private void upgrade(ObjectModel om, int version) {
        // in the future, here will be conversion code that upgrades from earlier versions
    }

    private Color getColor(Node color) {
        int red = color.asLeaf().getIntValue(0);
        int green = color.asLeaf().getIntValue(1);
        int blue = color.asLeaf().getIntValue(2);
        return new Color(red, green, blue);
    }

}
