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
import java.util.ArrayList;
import java.util.List;

import ch.jbead.storage.ObjectModel;

public class JBeadMemento extends DbbMemento {

    private static final int VERSION = 1;

    private int width;
    private int height;
    private byte[] data;

    private List<Color> colors = new ArrayList<Color>();
    private byte colorIndex;
    private int zoomIndex;
    private int scroll;
    private int shift;

    private boolean draftVisible;
    private boolean correctedVisible;
    private boolean simulationVisible;
    private boolean reportVisible;

    @Override
    public void save(JBeadOutputStream out) throws IOException {
        ObjectModel om = new ObjectModel("jbb");
        om.add("version", VERSION);
        for (Color color: colors) {
            om.add("colors/rgb", color.getRed(), color.getGreen(), color.getBlue());
        }
        // TODO
    }

    @Override
    public void load(JBeadInputStream in) throws IOException {
        // TODO
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public byte[] getData() {
        return data;
    }

    @Override
    public void setWidth(int width) {
        this.width = width;
    }

    @Override
    public void setHeight(int height) {
        this.height = height;
    }

    @Override
    public void setData(byte[] data) {
        this.data = data;
    }

    @Override
    public boolean isDraftVisible() {
        return draftVisible;
    }

    @Override
    public boolean isCorrectedVisible() {
        return correctedVisible;
    }

    @Override
    public boolean isSimulationVisible() {
        return simulationVisible;
    }

    @Override
    public boolean isReportVisible() {
        return reportVisible;
    }

    @Override
    public void setDraftVisible(boolean visible) {
        draftVisible = visible;
    }

    @Override
    public void setCorrectedVisible(boolean visible) {
        correctedVisible = visible;
    }

    @Override
    public void setSimulationVisible(boolean visible) {
        simulationVisible = visible;
    }

    @Override
    public void setReportVisible(boolean visible) {
        reportVisible = visible;
    }

    @Override
    public List<Color> getColors() {
        return colors;
    }

    @Override
    public byte getColorIndex() {
        return colorIndex;
    }

    @Override
    public int getZoomIndex() {
        return zoomIndex;
    }

    @Override
    public int getShift() {
        return shift;
    }

    @Override
    public int getScroll() {
        return scroll;
    }

    @Override
    public void setColors(List<Color> colors) {
        this.colors = colors;
    }

    @Override
    public void setColorIndex(byte colorIndex) {
        this.colorIndex = colorIndex;
    }

    @Override
    public void setZoomIndex(int zoomIndex) {
        this.zoomIndex = zoomIndex;
    }

    @Override
    public void setShift(int shift) {
        this.shift = shift;
    }

    @Override
    public void setScroll(int scroll) {
        this.scroll = scroll;
    }

}
