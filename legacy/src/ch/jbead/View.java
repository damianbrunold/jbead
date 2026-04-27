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

public interface View {

    public void addListener(ViewListener listener);

    public boolean drawColors();
    public boolean drawSymbols();

    public boolean isDraftVisible();
    public boolean isCorrectedVisible();
    public boolean isSimulationVisible();
    public boolean isReportVisible();

    public String getSelectedTool();
    public boolean isDragging();
    public void setDragging(boolean dragging);
    public Selection getSelection();
    public void selectColor(byte colorIndex);

    public void refresh();
}
