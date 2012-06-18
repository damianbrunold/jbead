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

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Tokens implements Iterable<Object> {

    private List<Object> tokens = new ArrayList<Object>();

    public Tokens(String data) {
        parse(data);
    }

    @Override
    public Iterator<Object> iterator() {
        return tokens.iterator();
    }

    private void parse(String data) {
        try {
            int state = 0;
            StringBuilder token = new StringBuilder();
            Reader reader = new StringReader(data);
            int c = reader.read();
            while (c != -1) {
                char ch = (char) c;
                switch (state) {
                case 0:
                    if (Character.isWhitespace(ch)) {
                        break;
                    } else if (ch == '(' || ch == ')') {
                        addToken(Character.toString(ch));
                    } else if (ch == '"') {
                        token.append(ch);
                        state = 1;
                    } else {
                        token.append(ch);
                        state = 3;
                    }
                    break;

                case 1: // quoted string
                    if (ch == '\\') {
                        state = 2;
                    } else if (ch == '"') {
                        token.append(ch);
                        addToken(token.toString());
                        token = new StringBuilder();
                       state = 0;
                    } else {
                        token.append(ch);
                    }
                    break;

                case 2: // quoted string escape
                    token.append(ch);
                    state = 1;
                    break;

                case 3: // general token (int, date, boolean, identifier)
                    if (ch == ' ' || ch == '\n' || ch == '\r') {
                        addToken(token.toString());
                        token = new StringBuilder();
                        state = 0;
                    } else if (ch == ')' || ch == '(') {
                        addToken(token.toString());
                        addToken(Character.toString(ch));
                        token = new StringBuilder();
                        state = 0;
                    } else {
                        token.append(ch);
                    }
                }
                c = reader.read();
            }
            if (token.length() > 0) {
                addToken(token.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addToken(String token) {
        if (token.charAt(0) == '"') {
            tokens.add(token.substring(1, token.length() - 1));
        } else if (Character.isDigit(token.charAt(0))) {
            try {
                tokens.add(Integer.parseInt(token));
                return;
            } catch (NumberFormatException e) {
                // ignore
            }
            DateFormat format = new JBeadDateFormat();
            try {
                tokens.add(format.parse(token));
                return;
            } catch (ParseException e) {
                // ignore
            }
            tokens.add(token);
        } else if (token.equals("true")) {
            tokens.add(Boolean.TRUE);
        } else if (token.equals("false")) {
            tokens.add(Boolean.FALSE);
        } else {
            tokens.add(token);
        }
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        for (Object token : tokens) {
            result.append(token).append(",");
        }
        result.setLength(result.length() - 1);
        return result.toString();
    }
}
