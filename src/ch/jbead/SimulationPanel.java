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
public class SimulationPanel extends JComponent {

	private static final long serialVersionUID = 1L;

	private BeadField field;
	private Color[] coltable;
	private int grid;
	private int scroll;
	private int shift;
	private int simulationleft;

	public SimulationPanel(BeadField field, Color[] coltable, int grid, int scroll, int shift) {
		this.field = field;
		this.coltable = coltable;
		this.grid = grid;
		this.scroll = scroll;
	}
	
	public void setShift(int shift) {
		this.shift = shift;
	}

	public int getSimulationleft() {
		return simulationleft;
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

        // Grid
		g.setColor(Color.DARK_GRAY);
        simulationleft = getWidth()-1-(field.Width()+1)*grid/2 + grid/2;
        int left = simulationleft;
        if (left<0) left=grid/2;
        int maxj = Math.min(field.Height(), getHeight()/grid+1);
        int w = field.Width()/2;
        if (scroll%2==0) {
            for (int j=0; j<maxj; j+=2) {
                for (int i=0; i<w+1; i++) {
                    if (j==0 && scroll==0 && i<shift) continue;
                    g.drawLine(left+i*grid, getHeight()-(j+1)*grid, left+i*grid, getHeight()-j*grid);
                }
                if (j>0 || scroll>0) {
                    g.drawLine(left-grid/2, getHeight()-(j+1)*grid, left-grid/2, getHeight()-j*grid);
                }
            }
            for (int j=1; j<maxj; j+=2) {
                for (int i=0; i<w+1; i++) {
                    g.drawLine(left+i*grid-grid/2, getHeight()-(j+1)*grid, left+i*grid-grid/2, getHeight()-j*grid);
                }
                g.drawLine(left+field.Width()*grid, getHeight()-(j+1)*grid, left+field.Width()*grid, getHeight()-j*grid);
            }
        } else {
            for (int j=0; j<maxj; j+=2) {
                for (int i=0; i<w+1; i++) {
                    g.drawLine(left+i*grid-grid/2, getHeight()-(j+1)*grid, left+i*grid-grid/2, getHeight()-j*grid);
                }
                g.drawLine(left+field.Width()*grid, getHeight()-(j+1)*grid, left+field.Width()*grid, getHeight()-j*grid);
            }
            for (int j=1; j<maxj; j+=2) {
                for (int i=0; i<w+1; i++) {
                    g.drawLine(left+i*grid, getHeight()-(j+1)*grid, left+i*grid, getHeight()-j*grid);
                }
                g.drawLine(left-grid/2, getHeight()-(j+1)*grid, left-grid/2, getHeight()-j*grid);
            }
        }
        if (scroll%2==0) {
            if (scroll==0) {
                g.drawLine(left+shift*grid, getHeight()-1, left+w*grid+1, getHeight()-1);
                for (int j=1; j<maxj; j++) {
                    g.drawLine(left-grid/2, getHeight()-1-j*grid, left+w*grid+1, getHeight()-1-j*grid);
                }
                g.drawLine(left+w*grid, 0, left+w*grid, getHeight()-1-grid);
            } else {
                for (int j=0; j<maxj; j++) {
                    g.drawLine(left-grid/2, getHeight()-1-j*grid, left+w*grid+1, getHeight()-1-j*grid);
                }
                g.drawLine(left+w*grid, 0, left+w*grid, getHeight()-1-grid);
            }
        } else {
            for (int j=0; j<maxj; j++) {
                g.drawLine(left-grid/2, getHeight()-1-j*grid, left+w*grid+1, getHeight()-1-j*grid);
            }
            g.drawLine(left+w*grid, 0, left+w*grid, getHeight()-1);
        }

        // Daten
        for (int i=0; i<field.Width(); i++) {
            for (int j=0; j<maxj; j++) {
                byte c = field.Get (i, j+scroll);
                assert(c>=0 && c<=9);
                g.setColor(coltable[c]);
                int idx = i+field.Width()*j + shift;
                int ii = idx % field.Width();
                int jj = idx / field.Width();
                ii = CorrectCoordinatesX(ii, jj);
                jj = CorrectCoordinatesY(ii, jj);
                if (ii>w && ii!=field.Width()) continue;
                if (scroll%2==0) {
                    if (jj%2==0) {
                        if (ii==w) continue;
                        g.fillRect(left+ii*grid+1, getHeight()-(jj+1)*grid, grid, grid);
                    } else {
                        if (ii!=field.Width() && ii!=w) {
                            g.fillRect(left-grid/2+ii*grid+1, getHeight()-(jj+1)*grid, grid, grid);
                        } else if (ii==field.Width()) {
                            g.fillRect(left-grid/2+1, getHeight()-(jj+2)*grid, grid/2, grid);
                        } else {
                            g.fillRect(left-grid/2+ii*grid+1, getHeight()-(jj+1)*grid, grid/2, grid);
                        }
                    }
                } else {
                    if (jj%2==1) {
                        if (ii==w) continue;
                        g.fillRect(left+ii*grid+1, getHeight()-(jj+1)*grid, grid, grid);
                    } else {
                        if (ii!=field.Width() && ii!=w) {
                            g.fillRect(left-grid/2+ii*grid+1, getHeight()-(jj+1)*grid, grid, grid);
                        } else if (ii==field.Width()) {
                            g.fillRect(left-grid/2+1, getHeight()-(jj+2)*grid, grid/2, grid);
                        } else {
                            g.fillRect(left-grid/2+ii*grid+1, getHeight()-(jj+1)*grid, grid/2, grid);
                        }
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
