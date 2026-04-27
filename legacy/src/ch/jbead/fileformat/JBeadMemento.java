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

import ch.jbead.BeadSymbols;
import ch.jbead.storage.Node;
import ch.jbead.storage.ObjectModel;

public class JBeadMemento extends Memento {

    public static final int VERSION = 1;

    @Override
    public void save(JBeadOutputStream out) throws IOException {
        ObjectModel om = new ObjectModel("jbb");
        saveInfos(om);
        saveColors(om);
        saveView(om);
        savePattern(om);
        out.write(om.toString());
    }

    private void saveInfos(ObjectModel om) {
        om.add("version", VERSION);
        om.add("author", author);
        om.add("organization", organization);
        om.add("notes", notes);
    }

    private void saveColors(ObjectModel om) {
        for (Color color: colors) {
            om.add("colors/rgb", color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
        }
    }

    private void saveView(ObjectModel om) {
        om.add("view/draft-visible", draftVisible);
        om.add("view/corrected-visible", correctedVisible);
        om.add("view/simulation-visible", simulationVisible);
        om.add("view/report-visible", reportVisible);
        om.add("view/selected-tool", selectedTool);
        om.add("view/selected-color", colorIndex);
        om.add("view/zoom", zoomIndex);
        om.add("view/scroll", scroll);
        om.add("view/shift", shift);
        om.add("view/draw-colors", drawColors);
        om.add("view/draw-symbols", drawSymbols);
        om.add("view/symbols", symbols);
    }

    private void savePattern(ObjectModel om) {
        for (int j = 0; j < height; j++) {
            List<Byte> row = new ArrayList<Byte>();
            for (int i = 0; i < width; i++) {
                row.add(data[j * width + i]);
            }
            om.add("model/row", row.toArray());
        }
    }

    @Override
    public void load(JBeadInputStream in) throws IOException {
        ObjectModel om = ObjectModel.fromData(in.readAll());
        loadInfos(om);
        loadColors(om);
        loadView(om);
        loadPattern(om);
    }

    private void loadInfos(ObjectModel om) {
        int version = om.getIntValue("version", 1);
        if (version < VERSION) {
            upgrade(om, version);
        }
        author = (String) om.getStringValue("author", "");
        organization = (String) om.getStringValue("organization", "");
        notes = (String) om.getStringValue("notes", "");
    }

    private void loadColors(ObjectModel om) {
        colors.clear();
        for (Node color : om.getAll("colors/rgb")) {
            colors.add(getColor(color));
        }
    }

    private void loadView(ObjectModel om) {
        draftVisible = om.getBoolValue("view/draft-visible", true);
        correctedVisible = om.getBoolValue("view/corrected-visible", true);
        simulationVisible = om.getBoolValue("view/simulation-visible", true);
        reportVisible = om.getBoolValue("view/report-visible", true);
        colorIndex = (byte) om.getIntValue("view/selected-color", 1);
        selectedTool = om.getStringValue("view/selected-tool", "pencil");
        zoomIndex = om.getIntValue("view/zoom", 2);
        scroll = om.getIntValue("view/scroll", 0);
        shift = om.getIntValue("view/shift", 0);
        drawColors = om.getBoolValue("view/draw-colors", true);
        drawSymbols = om.getBoolValue("view/draw-symbols", false);
        symbols = om.getStringValue("view/symbols", BeadSymbols.SAVED_SYMBOLS);
    }

    private void loadPattern(ObjectModel om) {
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
        if (color.asLeaf().size() == 4) {
            int alpha = color.asLeaf().getIntValue(3);
            return new Color(red, green, blue, alpha);
        } else {
            return new Color(red, green, blue);
        }
    }

}
