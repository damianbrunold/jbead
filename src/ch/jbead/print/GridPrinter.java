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

package ch.jbead.print;

import java.util.ArrayList;
import java.util.List;

import ch.jbead.Localization;
import ch.jbead.Model;

public abstract class GridPrinter extends PartPrinter {

    protected int gx = 12;
    protected int gy = gx;

    public GridPrinter(Model model, Localization localization) {
        super(model, localization);
    }

    @Override
    public List<Integer> layoutColumns(int height) {
        List<Integer> columns = new ArrayList<Integer>();
        int rows = model.getUsedHeight();
        int rowsPerColumn = height / gy;
        int cols = rows / rowsPerColumn;
        if (cols > 0) {
            int colwidth = getColumnWidth() + 2 * border;
            for (int i = 0; i < cols; i++) {
                int width = adjustColumn(i, colwidth);
                columns.add(width);
            }
        }
        return columns;
    }

    protected abstract int getColumnWidth();

    protected int adjustColumn(int column, int width) {
        return width;
    }

}
