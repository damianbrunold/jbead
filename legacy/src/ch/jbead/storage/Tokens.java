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

    private LexerState state;
    private StringBuilder token;

    public Tokens(String data) {
        parse(data);
    }

    public Iterator<Object> iterator() {
        return tokens.iterator();
    }

    private void parse(String data) {
        try {
            state = LexerState.START;
            token = new StringBuilder();
            Reader reader = new StringReader(data);
            int c = reader.read();
            while (c != -1) {
                char ch = (char) c;
                processChar(ch);
                c = reader.read();
            }
            if (token.length() > 0) {
                addToken(token.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processChar(char ch) {
        switch (state) {
        case START:
            startState(ch);
            break;

        case QUOTED_STRING:
            quotedStringState(ch);
            break;

        case QUOTED_STRING_ESCAPE:
            quotedStringEscapeState(ch);
            break;

        case GENERAL_TOKEN: //(int, date, boolean, identifier)
            generalTokenState(ch);
            break;

        case COMMENT:
            commentState(ch);
            break;
        }
    }

    private void startState(char ch) {
        if (Character.isWhitespace(ch)) {
            return;
        } else if (ch == '(' || ch == ')') {
            addToken(Character.toString(ch));
        } else if (ch == '"') {
            quotedStringEscapeState(ch);
        } else if (ch == '#') {
            state = LexerState.COMMENT;
        } else {
            token.append(ch);
            state = LexerState.GENERAL_TOKEN;
        }
    }

    private void quotedStringState(char ch) {
        if (ch == '\\') {
            state = LexerState.QUOTED_STRING_ESCAPE;
        } else if (ch == '"') {
            token.append(ch);
            addToken(token.toString());
            token = new StringBuilder();
           state = LexerState.START;
        } else {
            token.append(ch);
        }
    }

    private void quotedStringEscapeState(char ch) {
        token.append(ch);
        state = LexerState.QUOTED_STRING;
    }

    private void generalTokenState(char ch) {
        if (ch == ' ' || ch == '\n' || ch == '\r') {
            addToken(token.toString());
            token = new StringBuilder();
            state = LexerState.START;
        } else if (ch == ')' || ch == '(') {
            addToken(token.toString());
            addToken(Character.toString(ch));
            token = new StringBuilder();
            state = LexerState.START;
        } else {
            token.append(ch);
        }
    }

    private void commentState(char ch) {
        if (ch == '\n') {
            state = LexerState.START;
        }
    }

    private void addToken(String token) {
        if (token.charAt(0) == '"') {
            addStringToken(token);
        } else if (Character.isDigit(token.charAt(0))) {
            if (addIntegerToken(token)) {
                return;
            } else if (addDateToken(token)) {
                return;
            } else {
                addIdentifierToken(token);
            }
        } else if (token.equals("true")) {
            addTrueToken();
        } else if (token.equals("false")) {
            addFalseToken();
        } else {
            addIdentifierToken(token);
        }
    }

    private boolean addIntegerToken(String token) {
        try {
            tokens.add(Integer.parseInt(token));
            return true;
        } catch (NumberFormatException e) {
            // ignore
        }
        return false;
    }

    private boolean addDateToken(String token) {
        DateFormat format = new JBeadDateFormat();
        try {
            tokens.add(format.parse(token));
            return true;
        } catch (ParseException e) {
            // ignore
        }
        return false;
    }

    private void addIdentifierToken(String token) {
        tokens.add(token);
    }

    private void addFalseToken() {
        tokens.add(Boolean.FALSE);
    }

    private void addTrueToken() {
        tokens.add(Boolean.TRUE);
    }

    private void addStringToken(String token) {
        tokens.add(token.substring(1, token.length() - 1));
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
