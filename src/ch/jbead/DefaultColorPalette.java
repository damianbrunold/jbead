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

package ch.jbead;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;

public class DefaultColorPalette extends ArrayList<Color> {

    private static final long serialVersionUID = 1L;

    private static final Color[] COLORS = new Color[] {
        new Color(255, 255, 255),
        new Color(128, 0, 0),
        new Color(254, 15, 15),
        new Color(254, 76, 38),
        new Color(246, 40, 3),
        new Color(255, 231, 22),
        new Color(245, 249, 6),
        new Color(255, 255, 0),
        new Color(251, 139, 11),
        new Color(163, 27, 41),
        new Color(179, 94, 3),
        new Color(42, 18, 156),
        new Color(64, 154, 230),
        new Color(90, 42, 252),
        new Color(49, 166, 255),
        new Color(105, 198, 177),
        new Color(0, 176, 92),
        new Color(142, 228, 119),
        new Color(76, 139, 86),
        new Color(153, 206, 176),
        new Color(64, 172, 185),
        new Color(63, 223, 29),
        new Color(223, 87, 187),
        new Color(255, 96, 212),
        new Color(200, 181, 255),
        new Color(176, 136, 160),
        new Color(226, 237, 239),
        new Color(219, 220, 222),
        new Color(143, 147, 159),
        new Color(58, 70, 86),
        new Color(38, 52, 55),
        new Color(0, 0, 0),
    };

    public static final int NUMBER_OF_COLORS = COLORS.length;

    public DefaultColorPalette() {
        super();
        Collections.addAll(this, COLORS);
    }

}
