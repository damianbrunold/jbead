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

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

/**
 * 
 */
public class FilePrintAction extends BaseAction {

    private static final long serialVersionUID = 1L;

    public FilePrintAction(BeadForm form) {
        super("file.print", ImageFactory.getIcon("file.print"), form);
        putValue(SHORT_DESCRIPTION, "Prints the pattern");
        putValue(MNEMONIC_KEY, KeyEvent.VK_P);
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("control P"));
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        form.filePrintClick(e.getSource() instanceof JMenuItem);
    }

}
