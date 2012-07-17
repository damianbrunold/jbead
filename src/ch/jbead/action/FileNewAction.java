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

package ch.jbead.action;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import ch.jbead.BaseAction;
import ch.jbead.ImageFactory;
import ch.jbead.JBeadFrame;

public class FileNewAction extends BaseAction {

    private static final long serialVersionUID = 1L;

    private static final String NAME = "file.new";

    public FileNewAction(JBeadFrame frame) {
        super(NAME, ImageFactory.getIcon(NAME), frame);
        putValue(SHORT_DESCRIPTION, localization.getString("action.file.new.description"));
        putValue(MNEMONIC_KEY, localization.getMnemonic("action.file.new.mnemonic"));
        putValue(ACCELERATOR_KEY, localization.getKeyStroke("action.file.new.keystroke"));
    }

    public void actionPerformed(ActionEvent e) {
        if (model.isModified()) {
            int answer = JOptionPane.showConfirmDialog(frame, localization.getString("savechanges"));
            if (answer == JOptionPane.CANCEL_OPTION) return;
            if (answer == JOptionPane.YES_OPTION) {
                frame.getAction("file.save").actionPerformed(e);
            }
        }
        selection.clear();
        model.clear();
        frame.selectDefaultColor();
        frame.updateScrollbar();
    }

}
