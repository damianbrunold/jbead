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

import java.util.Iterator;

public class Parser {

    private Iterator<Object> iter;

    public Parser(Tokens tokens) {
        iter = tokens.iterator();
    }

    public Node parse() {
        return parseNode(iter.next());
    }

    private Node parseNode(Object token) {
        match("(", token);
        token = iter.next();
        String name = (String) token;
        token = iter.next();
        if (token.equals(")")) {
            return new Node(name);
        } else if (token.equals("(")) {
            Node node = new Node(name);
            while (!token.equals(")")) {
                node.add(parseNode(token));
                token = iter.next();
            }
            return node;
        } else {
            Leaf leaf = new Leaf(name);
            while (!token.equals(")")) {
                leaf.addValue(token);
                token = iter.next();
            }
            return leaf;
        }
    }

    private void match(String expected, Object actual) {
        if (!actual.toString().equals(expected)) {
            throw new RuntimeException("expected " + expected + " but got " + actual);
        }
    }
}
