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
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JColorChooser;
import javax.swing.JComponent;

public class ColorPalette extends JComponent {

    private static final long serialVersionUID = 1L;

    private static final int d = 14;

    private static final Dimension preferredSize = new Dimension(16 * d, 2 * d);

    private Model model;
    private Localization localization;

    public ColorPalette(Model model, Localization localization) {
        this.model = model;
        this.localization = localization;
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    chooseColor(e.getX(), e.getY());
                } else {
                    selectColor(e.getX(), e.getY());
                }
            }
        });
    }

    private void chooseColor(int x, int y) {
        byte c = getColorIndex(x, y);
        Color color = JColorChooser.showDialog(null, localization.getString("colorchooser.title"), model.getColor(c));
        if (color == null) return;
        model.setColor(c, color);
        repaint();
    }

    private void selectColor(int x, int y) {
        model.setSelectedColor(getColorIndex(x, y));
        repaint();
    }

    private byte getColorIndex(int x, int y) {
        byte idx = (byte) (x / d);
        if (y > d) idx += 16;
        return idx;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        for (byte i = 0; i < 16; i++) {
            g.setColor(model.getColor(i));
            g.fillRect(i * d, 0, d, d);
            if (i == model.getSelectedColor()) {
                g.setColor(Color.BLACK);
                g.drawRect(i * d, 0, d - 1, d - 1);
                g.setColor(Color.WHITE);
                g.drawRect(i * d + 1, 1, d - 3, d - 3);
            }
        }
        for (byte i = 16; i < 32; i++) {
            g.setColor(model.getColor(i));
            g.fillRect((i - 16) * d, d, d, d);
            if (i == model.getSelectedColor()) {
                g.setColor(Color.BLACK);
                g.drawRect((i - 16) * d, d, d - 1, d - 1);
                g.setColor(Color.WHITE);
                g.drawRect((i - 16) * d + 1, d + 1, d - 3, d - 3);
            }
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return preferredSize;
    }


}
