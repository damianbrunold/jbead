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
import java.awt.event.KeyEvent;

import javax.swing.JFileChooser;
import javax.swing.KeyStroke;

import ch.jbead.BaseAction;
import ch.jbead.JBeadFrame;
import ch.jbead.ImageFactory;

public class FileOpenAction extends BaseAction {

    private static final long serialVersionUID = 1L;

    private static final String NAME = "file.open";

    public FileOpenAction(JBeadFrame frame) {
        super(NAME, ImageFactory.getIcon(NAME), frame);
        putValue(SHORT_DESCRIPTION, localization.getString("action.file.open.description"));
        putValue(MNEMONIC_KEY, KeyEvent.VK_O);
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("control O"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JFileChooser dialog = new JFileChooser();
        dialog.setCurrentDirectory(model.getCurrentDirectory());
        dialog.setMultiSelectionEnabled(false);
        frame.setOpenFileFilters(dialog);
        if (dialog.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
            frame.updateFileFormat(dialog.getFileFilter(), dialog.getSelectedFile());
            frame.loadFile(dialog.getSelectedFile(), true);
        }
    }

}
