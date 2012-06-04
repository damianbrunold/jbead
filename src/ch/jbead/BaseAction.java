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

import javax.swing.AbstractAction;
import javax.swing.Icon;

/**
 *
 */
public abstract class BaseAction extends AbstractAction {

    private static final long serialVersionUID = 1L;

    protected BeadForm form;

    public BaseAction(String name, Icon icon, BeadForm form) {
        super(form.getBundle().getString("action." + name), icon);
        init(name, form);
    }

    public BaseAction(String name, BeadForm form) {
        super(form.getBundle().getString("action." + name));
        init(name, form);
    }

    private void init(String name, BeadForm form) {
        this.form = form;
        form.registerAction(name, this);
    }

}
