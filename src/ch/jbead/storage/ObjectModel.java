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

    public void add(String path, Object... values) {
        Path path_ = new Path(path);
        Node current = root;
        for (String node : path_) {
            current = current.getOrAdd(node);
        }
        current.add(new Leaf(path_.getLeaf(), values));
    }

    public Node get(String path) {
        Path path_ = new Path(path);
        Node current = root;
        for (String node : path_) {
            current = current.get(node);
        }
        return current.get(path_.getLeaf());
    }

    public List<Node> getAll(String path) {
        Path path_ = new Path(path);
        Node current = root;
        for (String node : path_) {
            current = current.get(node);
        }
        return current.getAll(path_.getLeaf());
    }

    public Object getValue(String path) {
        Leaf leaf = (Leaf) get(path);
        return leaf.getValue();
    }

    public int getIntValue(String path) {
        Leaf leaf = (Leaf) get(path);
        return leaf.getIntValue();
    }

    public String getStringValue(String path) {
        Leaf leaf = (Leaf) get(path);
        return leaf.getStringValue();
    }

    public boolean getBoolValue(String path) {
        Leaf leaf = (Leaf) get(path);
        return leaf.getBoolValue();
    }

    public List<Object> getValues(String path) {
        Leaf leaf = (Leaf) get(path);
        return leaf.getValues();
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
