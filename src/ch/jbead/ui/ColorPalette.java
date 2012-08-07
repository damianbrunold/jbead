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

package ch.jbead.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;

import ch.jbead.BeadPainter;
import ch.jbead.CoordinateCalculator;
import ch.jbead.JBeadFrame;
import ch.jbead.Localization;
import ch.jbead.Model;
import ch.jbead.Point;
import ch.jbead.ViewListener;

public class ColorPalette extends JComponent implements ViewListener, CoordinateCalculator {

    private static final long serialVersionUID = 1L;

    private static final int d = 14;

    private static final Font symbolfont = new Font("SansSerif", Font.PLAIN, d - 2);

    private static final Dimension preferredSize = new Dimension(16 * d, 2 * d);

    private Model model;
    private Localization localization;

    private boolean drawColors = true;
    private boolean drawSymbols = false;

    public ColorPalette(Model model, JBeadFrame frame) {
        this.model = model;
        this.localization = frame;
        frame.addListener(this);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(final MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showPopupMenu(e.getX(), e.getY());
                }
            }
            @Override
            public void mouseReleased(final MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showPopupMenu(e.getX(), e.getY());
                }
            }
            @Override
            public void mouseClicked(final MouseEvent e) {
                if (e.isPopupTrigger()) return;
                if (e.getClickCount() == 2) {
                    chooseColor(e.getX(), e.getY());
                } else {
                    selectColor(e.getX(), e.getY());
                }
            }

        });
    }

    private void showPopupMenu(final int x, final int y) {
        JPopupMenu menu = new JPopupMenu();
        menu.add(new AbstractAction(localization.getString("colorpalette.popup.select")) {
            private static final long serialVersionUID = 1L;
            public void actionPerformed(ActionEvent e) {
                selectColor(x, y);
            }
        });
        menu.add(new AbstractAction(localization.getString("colorpalette.popup.edit")) {
            private static final long serialVersionUID = 1L;
            public void actionPerformed(ActionEvent e2) {
                chooseColor(x, y);
            }
        });
        menu.add(new AbstractAction(localization.getString("colorpalette.popup.asbackground")) {
            private static final long serialVersionUID = 1L;
            public void actionPerformed(ActionEvent e2) {
                asBackground(x, y);
            }
        });
        menu.show(ColorPalette.this, x - 20, y - 10);
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

    private void asBackground(int x, int y) {
        byte index = getColorIndex(x, y);
        Color background = model.getColor((byte) 0);
        Color color = model.getColor(index);
        model.setColor((byte) 0, color);
        model.setColor(index, background);
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
        g.setFont(symbolfont);
        BeadPainter painter = new BeadPainter(this, model, drawColors, drawSymbols, symbolfont);
        painter.setWidthBorder(false);
        for (byte i = 0; i < 32; i++) {
            Point pt = new Point(i % 16, i / 16);
            g.setColor(model.getColor(i));
            painter.paint(g, pt, i);
            if (i == model.getSelectedColor()) {
                g.setColor(Color.BLACK);
                g.drawRect(x(pt), y(pt), d - 1, d - 1);
                g.setColor(Color.WHITE);
                g.drawRect(x(pt) + 1, y(pt) + 1, d - 3, d - 3);
            }
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return preferredSize;
    }

    public int getGridx() {
        return d;
    }

    public int getGridy() {
        return d;
    }

    public int x(Point pt) {
        return pt.getX() * d;
    }

    public int y(Point pt) {
        return pt.getY() * d;
    }

    public int dx(Point pt) {
        return 0;
    }

    public int dx(int j) {
        return 0;
    }

    public int w(Point pt) {
        return d;
    }

    public void drawColorsChanged(boolean drawColors) {
        this.drawColors = drawColors;
    }

    public void drawSymbolsChanged(boolean drawSymbols) {
        this.drawSymbols = drawSymbols;
    }


}
