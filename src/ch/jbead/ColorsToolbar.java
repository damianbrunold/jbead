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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JColorChooser;
import javax.swing.JToolBar;

public class ColorsToolbar extends JToolBar {

    private static final long serialVersionUID = 1L;

    private Localization localization;
    private Model model;
    private ButtonGroup colorsGroup = new ButtonGroup();
    private List<ColorButton> colors = new ArrayList<ColorButton>();

    ActionListener colorActionListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            colorClick(e);
        }
    };

    MouseAdapter colorMouseAdapter = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
            super.mouseClicked(e);
            if (e.getClickCount() == 2) {
                colorDblClick(e.getSource());
            }
        }

    };

    public ColorsToolbar(Localization localization, Model model) {
        this.localization = localization;
        this.model = model;
        addColorButtons();
    }

    public void selectDefaultColor() {
        colors.get(1).setSelected(true);
    }

    public void selectColor(byte colorindex) {
        colors.get(colorindex).setSelected(true);
    }

    public void updateColorIcons() {
        for (byte i = 0; i < model.getColorCount(); i++) {
            updateColorIcon(i);
        }
    }

    public void updateColorIcon(byte colorindex) {
        colors.get(colorindex).setIcon(new ColorIcon(model, colorindex));
    }

    public void updateAll() {
        colors.clear();
        removeAll();
        colorsGroup = new ButtonGroup();
        addColorButtons();
    }

    private void addColorButtons() {
        for (int i = 0; i < model.getColorCount(); i++) {
            add(createColorButton(i));
        }
        selectColor(model.getColorIndex());
    }

    private ColorButton createColorButton(int index) {
        ColorButton button = new ColorButton(new ColorIcon(model, (byte) index));
        button.addActionListener(colorActionListener);
        button.addMouseListener(colorMouseAdapter);
        colorsGroup.add(button);
        colors.add(button);
        return button;
    }

    private void colorClick(ActionEvent event) {
        ColorButton sender = (ColorButton) event.getSource();
        model.setColorIndex(sender.getColorIndex());
    }

    private void colorDblClick(Object sender) {
        ColorButton colorButton = (ColorButton) sender;
        byte c = colorButton.getColorIndex();
        Color color = JColorChooser.showDialog(this, localization.getString("colorchooser.title"), model.getColor(c));
        if (color == null) return;
        model.setColor(c, color);
    }

}
