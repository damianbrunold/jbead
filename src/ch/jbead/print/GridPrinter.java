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

package ch.jbead.print;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import ch.jbead.Localization;
import ch.jbead.Model;

public abstract class GridPrinter extends PartPrinter {

    protected int gx = Convert.mm2pt(3);
    protected int gy = gx;
    protected boolean fullPattern;

    public GridPrinter(Model model, Localization localization, boolean fullPattern) {
        super(model, localization);
        this.fullPattern = fullPattern;
    }

    @Override
    public List<Integer> layoutColumns(int width, int height) {
        List<Integer> columns = new ArrayList<Integer>();
        int rows = getRows(height);
        int rowsPerColumn = getRowsPerColumn(height);
        int cols = (rows + rowsPerColumn - 1) / rowsPerColumn;
        if (cols > 0) {
            int colwidth = getColumnWidth() + 2 * border;
            for (int i = 0; i < cols; i++) {
                columns.add(colwidth);
            }
        }
        return columns;
    }

    protected int getRowsPerColumn(int height) {
        return height / gy;
    }

    protected abstract int getRows(int height);
    protected abstract int getColumnWidth();

    protected int getPrintableRows(int height) {
        if (getUsedRows() <= getRowsPerColumn(height) || fullPattern) {
            return getUsedRows();
        } else {
            return Math.min(getRepeatRowsFullColumn(height), getUsedRows());
        }
    }

    protected int getRepeatRowsFullColumn(int height) {
        return ((getRepeatRows() + getRowsPerColumn(height) - 1) / getRowsPerColumn(height)) * getRowsPerColumn(height);
    }

    protected int getRepeatRows() {
        return model.getPoint(model.getRepeat()).getY() + 1;
    }

    protected int getUsedRows() {
        return model.getUsedHeight();
    }

    protected void setStroke(Graphics2D g) {
        g.setStroke(new BasicStroke(0.3f));
    }

}
