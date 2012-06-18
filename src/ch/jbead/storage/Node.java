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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Node implements Iterable<Node> {

    public static final String INDENTATION = "    ";

    protected String name;
    private List<Node> children = new ArrayList<Node>();

    public Node(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Leaf asLeaf() {
        return (Leaf) this;
    }

    public int size() {
        return children.size();
    }

    public Node add(Node node) {
        children.add(node);
        return node;
    }

    public Node getOrAdd(String node) {
        for (Node child : children) {
            if (child.getName().equals(node)) return child;
        }
        Node _node = new Node(node);
        children.add(_node);
        return _node;
    }

    public Node get(String node) {
        for (Node child : children) {
            if (child.getName().equals(node)) return child;
        }
        return null;
    }

    public List<Node> getAll(String node) {
        List<Node> result = new ArrayList<Node>();
        for (Node child : children) {
            if (child.getName().equals(node)) result.add(child);
        }
        return result;
    }

    public String format(String indent) {
        StringBuilder result = new StringBuilder();
        result.append(indent).append("(").append(name).append("\n");
        for (Node child : children) {
            result.append(child.format(indent + INDENTATION));
        }
        result = stripLastNewline(result);
        result.append(")\n");
        return result.toString();
    }

    private StringBuilder stripLastNewline(StringBuilder result) {
        if (result.charAt(result.length() - 1) == '\n') {
            result.setLength(result.length() - 1);
        }
        return result;
    }

    @Override
    public Iterator<Node> iterator() {
        return children.iterator();
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("(").append(name);
        for (Node child : children) {
            result.append(" ").append(child);
        }
        result.append(")");
        return result.toString();
    }
}
