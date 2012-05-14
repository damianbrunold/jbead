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
import java.awt.Graphics;

import javax.swing.JComponent;

/**
 * 
 */
public class DraftPanel extends JComponent {

    private static final long serialVersionUID = 1L;

    private BeadField field;
    private Color[] coltable;
    private int grid;
    private int scroll;
    private int draftleft;

    public DraftPanel(BeadField field, Color[] coltable, int grid, int scroll) {
        this.field = field;
        this.coltable = coltable;
        this.grid = grid;
        this.scroll = scroll;
    }

    public int getDraftleft() {
        return draftleft;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Grid
        g.setColor(Color.DARK_GRAY);
        draftleft = getWidth() - field.Width() * grid - 1;
        int left = draftleft;
        if (left < 0) left = 0;
        int maxj = Math.min(field.Height(), getHeight() / grid + 1);
        for (int i = 0; i < field.Width() + 1; i++) {
            g.drawLine(left + i * grid, 0, left + i * grid, getHeight() - 1);
        }
        for (int j = 0; j < maxj; j++) {
            g.drawLine(left, getHeight() - 1 - j * grid, left + field.Width()
                    * grid, getHeight() - 1 - j * grid);
        }

        // Daten
        for (int i = 0; i < field.Width(); i++)
            for (int j = 0; j < maxj; j++) {
                byte c = field.Get(i, j + scroll);
                assert (c >= 0 && c <= 9);
                g.setColor(coltable[c]);
                g.fillRect(left + i * grid + 1, getHeight() - (j + 1) * grid,
                        grid, grid);
            }

        // Zehnermarkierungen
        g.setColor(Color.DARK_GRAY);
        for (int j = 0; j < maxj; j++) {
            if (((j + scroll) % 10) == 0) {
                g.drawLine(0, getHeight() - j * grid - 1, left - 6, getHeight()
                        - j * grid - 1);
                g.drawString(Integer.toString(j + scroll), 6, getHeight() - j
                        * grid + 1);
            }
        }

        // Auswahl
        // TODO
        // DraftSelectDraw();
    }

    public void updateBead(int _i, int _j) {
        if (!isVisible()) return;

        byte c = field.Get(_i, _j + scroll);
        assert (c >= 0 && c <= 9);

        Graphics g = getGraphics();
        g.setColor(coltable[c]);
        g.fillRect(draftleft + _i * grid + 1, getHeight() - (_j + 1) * grid,
                grid, grid);
        g.dispose();
    }

    boolean mouseToField(Point pt) {
        int _i = pt.getX();
        int _j = pt.getY();
        int i, jj;
        if (_i < draftleft || _i > draftleft + field.Width() * grid)
            return false;
        i = (_i - draftleft) / grid;
        if (i >= field.Width()) return false;
        jj = (getHeight() - _j) / grid;
        _i = i;
        _j = jj;
        return true;
    }

}
