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

public class TokensTest extends TestCase {

    public void testTokens() {
        assertEquals("(,jbb,(,version,1,),(,author,Damian Brunold,),)", new Tokens("(jbb\n    (version 1)\n    (author \"Damian Brunold\"))\n").toString());
    }

    public void testQuotedString() {
        assertEquals("hello \\ \" world", new Tokens("\"hello \\\\ \\\" world\"").toString());
    }

    public void testInteger() {
        assertEquals(Integer.valueOf(1), new Tokens("1").iterator().next());
    }

    public void testTrue() {
        assertEquals(Boolean.TRUE, new Tokens("true").iterator().next());
    }

    public void testFalse() {
        assertEquals(Boolean.FALSE, new Tokens("false").iterator().next());
    }

    public void testComment() {
        assertEquals("(,jbb,(,version,1,),(,author,Damian Brunold,),)", new Tokens("(jbb\n# just some stuff\n    (version 1) # this is the version\n    (author \"Damian Brunold\"))\n").toString());
    }

}
