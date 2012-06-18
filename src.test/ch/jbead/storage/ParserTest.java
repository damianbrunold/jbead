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

import junit.framework.TestCase;

public class ParserTest extends TestCase {

    public void testParser() {
        Tokens tokens = new Tokens("(jbb (author \"Damian Brunold\") (colors (rgb 1 2 3) (rgb  4 5 6)))");
        Parser parser = new Parser(tokens);
        Node result = parser.parse();
        assertEquals("jbb", result.getName());
        assertEquals("Damian Brunold", result.get("author").asLeaf().getValue());
        assertEquals("[1, 2, 3]", result.get("colors").getAll("rgb").get(0).asLeaf().getValues().toString());
        assertEquals("[4, 5, 6]", result.get("colors").getAll("rgb").get(1).asLeaf().getValues().toString());
    }

}
