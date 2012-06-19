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

package ch.jbead.storage;

import java.util.List;

public class ObjectModel {

    private Node root;

    public ObjectModel(String root) {
        this.root = new Node(root);
    }

    private ObjectModel(Node root) {
        this.root = root;
    }

    public void add(String pathstr, Object... values) {
        Path path = new Path(pathstr);
        Node current = root;
        for (String node : path.getNodes()) {
            current = current.getOrAdd(node);
        }
        current.add(new Leaf(path.getLeaf(), values));
    }

    public Node get(String pathstr) {
        Path path = new Path(pathstr);
        Node current = root;
        for (String node : path) {
            current = current.get(node);
            if (current == null) throw new JBeadFileFormatException("Path " + pathstr + " cannot be resolved, node " + node + " not found");
        }
        return current;
    }

    public List<Node> getAll(String pathstr) {
        Path path = new Path(pathstr);
        Node current = root;
        for (String node : path.getNodes()) {
            current = current.get(node);
            if (current == null) throw new JBeadFileFormatException("Path " + pathstr + " cannot be resolved, node " + node + " not found");
        }
        return current.getAll(path.getLeaf());
    }

    public Object getValue(String path) {
        return get(path).asLeaf().getValue();
    }

    public int getIntValue(String path) {
        return get(path).asLeaf().getIntValue();
    }

    public String getStringValue(String path) {
        return get(path).asLeaf().getStringValue();
    }

    public boolean getBoolValue(String path) {
        return get(path).asLeaf().getBoolValue();
    }

    public List<Object> getValues(String path) {
        return get(path).asLeaf().getValues();
    }

    @Override
    public String toString() {
        return root.format("");
    }

    public static ObjectModel fromData(String data) {
        Node node = new Parser(new Tokens(data)).parse();
        return new ObjectModel(node);
    }

}
