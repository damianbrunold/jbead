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

package ch.jbead.dialog;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import ch.jbead.ImageFactory;
import ch.jbead.Localization;
import ch.jbead.Version;

public class InfoAboutDialog extends JDialog {
    private static final long serialVersionUID = 1L;

    public InfoAboutDialog(Localization localization) {
        setTitle(localization.getString("infoaboutdialog.title"));
        setIconImage(ImageFactory.getImage("jbead-16"));
        JPanel main = new JPanel();
        main.setBorder(BorderFactory.createEmptyBorder(7, 7, 7, 7));
        setContentPane(main);
        setModal(true);
        setLayout(new BorderLayout());
        String version = Version.getInstance().getVersionString();
        add(new JLabel(localization.getString("infoaboutdialog.text").replace("VERSION", version)), BorderLayout.CENTER);
        JPanel buttons = new JPanel();
        JButton ok = new JButton(localization.getString("ok"));
        buttons.add(ok);
        add(buttons, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        ok.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                InfoAboutDialog.this.dispose();
            }
        });
    }


}
