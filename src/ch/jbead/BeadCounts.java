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

import java.util.HashMap;
import java.util.Map;

public class BeadCounts {

    private Map<Byte, Integer> counts = new HashMap<Byte, Integer>();

    public BeadCounts(Model model) {
        initCounts(model);
        for (Point pt : model.getUsedRect()) {
            add(model.get(pt));
        }
    }

    private void initCounts(Model model) {
        for (byte color = 0; color < model.getColorCount(); color++) {
            counts.put(color, 0);
        }
    }

    private void add(byte color) {
        counts.put(color, counts.get(color) + 1);
    }

    public int getCount(byte color) {
        return counts.get(color);
    }

    public int getColorCount() {
        int result = 0;
        for (Map.Entry<Byte, Integer> entry : counts.entrySet()) {
            if (entry.getValue().intValue() > 0) result++;
        }
        return result;
    }
}
