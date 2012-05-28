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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BeadList implements Iterable<BeadRun> {

    private List<BeadRun> runs = new ArrayList<BeadRun>();

    public BeadList(Model model) {
        byte color = model.get(model.getColorRepeat() - 1);
        int count = 1;
        for (int i = model.getColorRepeat() - 2; i >= 0; i--) {
            if (model.get(i) == color) {
                count++;
            } else {
                runs.add(new BeadRun(color, count));
                color = model.get(i);
                count = 1;
            }
        }
        runs.add(new BeadRun(color, count));
    }

    public Object size() {
        return runs.size();
    }

    public BeadRun get(int idx) {
        return runs.get(idx);
    }

    @Override
    public Iterator<BeadRun> iterator() {
        return runs.iterator();
    }

}
