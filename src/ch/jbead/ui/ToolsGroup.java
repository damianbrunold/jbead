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

import java.util.HashMap;
import java.util.Map;

import javax.swing.ButtonGroup;

public class ToolsGroup {

    private ButtonGroup menugroup = new ButtonGroup();
    private ButtonGroup toolbargroup = new ButtonGroup();

    private Map<String, ToolButton> buttons = new HashMap<String, ToolButton>();
    private Map<String, ToolMenuItem> items = new HashMap<String, ToolMenuItem>();

    public ToolButton addTool(String tool, ToolButton button) {
        buttons.put(tool, button);
        toolbargroup.add(button);
        return button;
    }

    public ToolMenuItem addTool(String tool, ToolMenuItem item) {
        items.put(tool, item);
        menugroup.add(item);
        return item;
    }

    public void selectTool(String tool) {
        if (!buttons.containsKey(tool)) tool = "pencil";
        buttons.get(tool).setSelected(true);
        items.get(tool).setSelected(true);
    }

    public boolean isSelected(String tool) {
        return buttons.get(tool).isSelected();
    }

    public String getSelectedTool() {
        for (Map.Entry<String, ToolButton> entry : buttons.entrySet()) {
            if (entry.getValue().isSelected()) return entry.getKey();
        }
        return "pencil";
    }

}
