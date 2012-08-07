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

public class BeadPainter {

    private CoordinateCalculator coord;
    private Model model;
    private boolean drawColors;
    private boolean drawSymbols;
    private boolean drawBorder = true;
    private Font symbolfont;

    public BeadPainter(CoordinateCalculator coord, Model model, boolean drawColors, boolean drawSymbols, Font symbolfont) {
        this.coord = coord;
        this.model = model;
        this.drawColors = drawColors;
        this.drawSymbols = drawSymbols;
        this.symbolfont = symbolfont;
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
        if (drawColors) {
            g.setColor(color);
            g.fillRect(x - dx, y, gridx, gridy);
        }
        if (drawSymbols) {
            setSymbolColor(g, color);
            g.drawString(BeadSymbols.get(c), x + (gridx - g.getFontMetrics().stringWidth(BeadSymbols.get(c))) / 2 - dx, y + symbolfont.getSize());
        }
        if (drawBorder) {
            g.setColor(Color.DARK_GRAY);
            g.drawRect(x - dx, y, gridx, gridy);
        }
    }

    private void setSymbolColor(Graphics g, Color color) {
        if (drawColors) {
            g.setColor(getContrastingColor(color));
        } else {
            g.setColor(Color.BLACK);
        }
    }

    private Color getContrastingColor(Color color) {
        if (getContrast(color, Color.WHITE) > getContrast(color, Color.BLACK)) {
            return Color.WHITE;
        } else {
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
