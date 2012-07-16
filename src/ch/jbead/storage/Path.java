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

package ch.jbead.storage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class Path implements Iterable<String> {

    private List<String> path = new ArrayList<String>();
    private String leaf;

    public Path(String path) {
        String[] parts = path.split("/");
        for (int i = 0; i < parts.length; i++) {
            this.path.add(parts[i]);
        }
        this.leaf = parts[parts.length - 1];
    }

    public List<String> getNodes() {
        return Collections.unmodifiableList(path.subList(0, path.size() - 1));
    }

    public String getLeaf() {
        return leaf;
    }

    @Override
    public Iterator<String> iterator() {
        return path.iterator();
    }

}
