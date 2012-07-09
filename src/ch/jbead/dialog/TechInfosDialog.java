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
import javax.swing.JPanel;
import javax.swing.JTextArea;

import ch.jbead.ImageFactory;
import ch.jbead.JBeadMemento;
import ch.jbead.Localization;
import ch.jbead.Version;

public class TechInfosDialog extends JDialog {
    private static final long serialVersionUID = 1L;

    public TechInfosDialog(Localization localization) {
        setTitle(localization.getString("techinfos.title"));
        setIconImage(ImageFactory.getImage("jbead-16"));
        getRootPane().setBorder(BorderFactory.createEmptyBorder(7, 7, 7, 7));
        setModal(true);
        setLayout(new BorderLayout());
        JTextArea text = new JTextArea();
        text.setText(getTechInfos(localization));
        text.setFocusable(true);
        text.setEditable(false);
        text.setTabSize(15);
        add(text, BorderLayout.CENTER);
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
                TechInfosDialog.this.dispose();
            }
        });
    }

    private String getTechInfos(Localization localization) {
        StringBuilder infos = new StringBuilder();
        addInfo(infos, localization.getString("techinfos.version"), Version.getInstance().getVersionString());
        addInfo(infos, localization.getString("techinfos.fileformat"), JBeadMemento.VERSION);
        addInfo(infos, localization.getString("techinfos.javaversion"), System.getProperty("java.version"));
        addInfo(infos, localization.getString("techinfos.javavendor"), System.getProperty("java.vendor"));
        addInfo(infos, localization.getString("techinfos.javahome"), System.getProperty("java.home"));
        addInfo(infos, localization.getString("techinfos.osname"), System.getProperty("os.name"));
        addInfo(infos, localization.getString("techinfos.osarch"), System.getProperty("os.arch"));
        addInfo(infos, localization.getString("techinfos.osversion"), System.getProperty("os.version"));
        return infos.toString().trim();
    }

    private void addInfo(StringBuilder infos, String label, Object data) {
        infos.append(label).append(":\t").append(data.toString()).append("\n");
    }
}
