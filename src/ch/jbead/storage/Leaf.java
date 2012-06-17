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

import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Leaf extends Node {

    private static DateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", DateFormatSymbols.getInstance(Locale.ENGLISH));

    private List<Object> values = new ArrayList<Object>();

    public Leaf(String name, Object... values) {
        super(name);
        Collections.addAll(this.values, values);
    }

    public Leaf addValue(Object value) {
        values.add(value);
        return this;
    }

    public String format(String indent) {
        StringBuilder result = new StringBuilder();
        result.append(indent).append("(").append(name);
        for (Object value : values) {
            result.append(" ").append(formatValue(value));
        }
        result.append(")\n");
        return result.toString();
    }

    private String formatValue(Object value) {
        if (value instanceof String) {
            return "\"" + value.toString().replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
        } else if (value instanceof Date) {
            return dateformat.format((Date) value);
        } else if (value instanceof Calendar) {
            return dateformat.format(((Calendar) value).getTime());
        } else {
            return String.valueOf(value);
        }
    }

}
