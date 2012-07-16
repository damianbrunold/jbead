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

import javax.swing.AbstractAction;
import javax.swing.Icon;

public abstract class BaseAction extends AbstractAction {

    private static final long serialVersionUID = 1L;

    protected JBeadFrame frame;
    protected Localization localization;
    protected Model model;
    protected Selection selection;

    public BaseAction(String name, Icon icon, JBeadFrame frame) {
        super(frame.getString("action." + name), icon);
        init(name, frame);
    }

    public BaseAction(String name, JBeadFrame frame) {
        super(frame.getString("action." + name));
        init(name, frame);
    }

    private void init(String name, JBeadFrame frame) {
        this.frame = frame;
        this.localization = frame;
        this.model = frame.getModel();
        this.selection = frame.getSelection();
        frame.registerAction(name, this);
    }

}
