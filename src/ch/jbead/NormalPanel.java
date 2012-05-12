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
public class NormalPanel extends JComponent {

	private static final long serialVersionUID = 1L;

	private BeadField field;
	private Color[] coltable;
	private int grid;
	private int scroll;
	private int normalleft;

	public NormalPanel(BeadField field, Color[] coltable, int grid, int scroll) {
		this.field = field;
		this.coltable = coltable;
		this.grid = grid;
		this.scroll = scroll;
	}

	public int getNormalleft() {
		return normalleft;
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		// Grid
		g.setColor(Color.DARK_GRAY);
		normalleft = getWidth() - 1 - (field.Width() + 1) * grid + grid / 2;
		int left = normalleft;
		if (left < 0) left = grid / 2;
		int maxj = Math.min(field.Height(), getHeight() / grid + 1);
		if (scroll % 2 == 0) {
			for (int i = 0; i < field.Width() + 1; i++) {
				for (int jj = 0; jj < maxj; jj += 2) {
					g.drawLine(left + i * grid, getHeight() - (jj + 1) * grid,
							   left + i * grid, getHeight() - jj * grid);
				}
			}
			for (int i = 0; i <= field.Width() + 1; i++) {
				for (int jj = 1; jj < maxj; jj += 2) {
					g.drawLine(left + i * grid - grid / 2, getHeight() - (jj + 1) * grid,
							   left + i * grid - grid / 2, getHeight() - jj * grid);
				}
			}
		} else {
			for (int i = 0; i <= field.Width() + 1; i++) {
				for (int jj = 0; jj < maxj; jj += 2) {
					g.drawLine(left + i * grid - grid / 2, getHeight() - (jj + 1) * grid,
							   left + i * grid - grid / 2, getHeight() - jj * grid);
				}
			}
			for (int i = 0; i < field.Width() + 1; i++) {
				for (int jj = 1; jj < maxj; jj += 2) {
					g.drawLine(left + i * grid, getHeight() - (jj + 1) * grid,
							   left + i * grid, getHeight() - jj * grid);
				}
			}
		}
		if (scroll % 2 == 0) {
			g.drawLine(left, getHeight() - 1, left + field.Width() * grid + 1, getHeight() - 1);
			for (int jj = 1; jj < maxj; jj++) {
				g.drawLine(left - grid / 2, getHeight() - 1 - jj * grid,
						   left + field.Width() * grid + grid / 2 + 1, getHeight() - 1 - jj * grid);
			}
		} else {
			for (int jj = 0; jj < maxj; jj++) {
				g.drawLine(left - grid / 2, getHeight() - 1 - jj * grid,
						   left + field.Width() * grid + grid / 2 + 1, getHeight() - 1 - jj * grid);
			}
		}

		// Daten
		for (int i = 0; i < field.Width(); i++) {
			for (int jj = 0; jj < maxj; jj++) {
				byte c = field.Get(i, jj + scroll);
				assert (c >= 0 && c <= 9);
				g.setColor(coltable[c]);
				int ii = i;
				int j1 = jj;
				ii = CorrectCoordinatesX(ii, j1);
				j1 = CorrectCoordinatesY(ii, j1);
				if (scroll % 2 == 0) {
					if (j1 % 2 == 0) {
						g.fillRect(left + ii * grid + 1, getHeight() - (j1 + 1) * grid, grid, grid);
					} else {
						g.fillRect(left - grid / 2 + ii * grid + 1, getHeight() - (j1 + 1) * grid, grid, grid);
					}
				} else {
					if (j1 % 2 == 1) {
						g.fillRect(left + ii * grid + 1, getHeight() - (j1 + 1) * grid, grid, grid);
					} else {
						g.fillRect(left - grid / 2 + ii * grid + 1, getHeight() - (j1 + 1) * grid, grid, grid);
					}
				}
			}
		}
	}

    int CorrectCoordinatesX (int _i, int _j)
    {
        int idx = _i + (_j+scroll)*field.Width();
        int m1 = field.Width();
        int m2 = field.Width()+1;
        int k = 0;
        int m = (k%2==0) ? m1 : m2;
        while (idx>=m) {
            idx -= m;
            k++;
            m = (k%2==0) ? m1 : m2;
        }
        _i = idx;
        _j = k-scroll;
        return _i;
    }

    int CorrectCoordinatesY (int _i, int _j)
    {
        int idx = _i + (_j+scroll)*field.Width();
        int m1 = field.Width();
        int m2 = field.Width()+1;
        int k = 0;
        int m = (k%2==0) ? m1 : m2;
        while (idx>=m) {
            idx -= m;
            k++;
            m = (k%2==0) ? m1 : m2;
        }
        _i = idx;
        _j = k-scroll;
        return _j;
    }

}
