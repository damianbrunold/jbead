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

package ch.jbead;

public class BeadSymbols {

    public static final String MIDDLE_DOT = "\u00b7";
    public static final String DEFAULT_SYMBOLS = MIDDLE_DOT + "abcdefghijklmnopqrstuvwxyz+-/\\*";
    public static String SYMBOLS = DEFAULT_SYMBOLS;

    static {
        reloadSymbols();
    }

    public static void restoreDefaults() {
        SYMBOLS = DEFAULT_SYMBOLS;
    }

    public static void reloadSymbols() {
        Settings settings = new Settings();
        settings.setCategory("view");
        SYMBOLS = settings.loadString("symbols", DEFAULT_SYMBOLS);
    }

    public static String get(byte index) {
        if (index >= SYMBOLS.length()) return " ";
        return SYMBOLS.substring(index, index + 1);
    }

}
