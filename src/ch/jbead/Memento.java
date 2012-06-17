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
import java.util.List;

public interface Memento {

    String getAuthor();
    void setAuthor(String author);

    String getNotes();
    void setNotes(String notes);

    int getWidth();
    int getHeight();
    byte[] getData();

    List<Color> getColors();
    byte getColorIndex();
    int getZoomIndex();
    int getShift();
    int getScroll();

    boolean isDraftVisible();
    boolean isCorrectedVisible();
    boolean isSimulationVisible();
    boolean isReportVisible();

    void setWidth(int width);
    void setHeight(int height);
    void setData(byte[] data);

    void setColors(List<Color> colors);
    void setColorIndex(byte colorIndex);
    void setZoomIndex(int zoomIndex);
    void setShift(int shift);
    void setScroll(int scroll);

    void setDraftVisible(boolean visible);
    void setCorrectedVisible(boolean visible);
    void setSimulationVisible(boolean visible);
    void setReportVisible(boolean visible);

    void save(JBeadOutputStream out) throws IOException;
    void load(JBeadInputStream in) throws IOException;

}
