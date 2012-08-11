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

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.util.HashMap;
import java.util.Map;

public class BeadPainter {

    private CoordinateCalculator coord;
    private Model model;
    private View view;
    private boolean forceColors = false;
    private boolean drawBorder = true;
    private Font symbolfont;

    private static Map<Color, Color> contrastingColors = new HashMap<Color, Color>();

    public BeadPainter(CoordinateCalculator coord, Model model, View view, Font symbolfont) {
        this.coord = coord;
        this.model = model;
        this.view = view;
        this.symbolfont = symbolfont;
    }

    public void setForceColors() {
        this.forceColors = true;
    }

    public void setDrawBorder(boolean drawBorder) {
        this.drawBorder = drawBorder;
    }

    public void paint(Graphics g, Point pt, byte c) {
        Color color = model.getColor(c);
        int x = coord.x(pt);
        int y = coord.y(pt);
        int gridx = coord.getGridx();
        int gridy = coord.getGridy();
        int dx = coord.dx(pt);
        if (view.drawColors() || forceColors) {
            g.setColor(color);
            g.fillRect(x - dx, y, gridx, gridy);
        }
        if (view.drawSymbols()) {
            setSymbolColor(g, color);
            g.setFont(symbolfont);
            g.drawString(BeadSymbols.get(c), x + (gridx - g.getFontMetrics().stringWidth(BeadSymbols.get(c))) / 2 - dx, y + symbolfont.getSize());
        }
        if (drawBorder) {
            g.setColor(Color.DARK_GRAY);
            g.drawRect(x - dx, y, gridx, gridy);
        }
    }

    private void setSymbolColor(Graphics g, Color color) {
        if (view.drawColors() || forceColors) {
            g.setColor(getContrastingColor(color));
        } else {
            g.setColor(Color.BLACK);
        }
    }

    private Color getContrastingColor(Color color) {
        // use memoization to reduce recalculation of contrasting colors
        if (contrastingColors.containsKey(color)) return contrastingColors.get(color);
        if (getContrast(color, Color.WHITE) > getContrast(color, Color.BLACK)) {
            contrastingColors.put(color, Color.WHITE);
            return Color.WHITE;
        } else {
            contrastingColors.put(color, Color.BLACK);
            return Color.BLACK;
        }
    }

    private int getContrast(Color a, Color b) {
        int red_diff = a.getRed() - b.getRed();
        int green_diff = a.getGreen() - b.getGreen();
        int blue_diff = a.getBlue() - b.getBlue();
        return (int) Math.sqrt(red_diff * red_diff + green_diff * green_diff + blue_diff * blue_diff);
    }

}
