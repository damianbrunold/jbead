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
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;

import ch.jbead.ImageFactory;
import ch.jbead.Localization;

/**
 * 
 */
public class PatternWidthForm extends JDialog {
    private static final long serialVersionUID = 1L;

    private SpinnerModel widthModel = new SpinnerNumberModel(10, 5, 35, 1);
    private JSpinner patternwidth = new JSpinner(widthModel);

    private boolean isOK = false;

    public PatternWidthForm(Localization localization) {
        setTitle(localization.getString("patternwidthform.title"));
        getRootPane().setBorder(BorderFactory.createEmptyBorder(7, 7, 7, 7));
        setLayout(new BorderLayout());

        JPanel form = new JPanel();
        form.setLayout(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 2;
        form.add(new JLabel(localization.getString("patternwidthform.description")), constraints);

        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 1;
        form.add(new JLabel(localization.getString("patternwidthform.width")), constraints);

        constraints = new GridBagConstraints();
        constraints.gridx = 1;
        constraints.gridy = 1;
        form.add(patternwidth, constraints);

        add(form, BorderLayout.CENTER);

        JPanel buttons = new JPanel();
        buttons.setLayout(new FlowLayout());
        JButton ok = new JButton(localization.getString("ok"));
        buttons.add(ok);
        JButton cancel = new JButton(localization.getString("cancel"));
        buttons.add(cancel);
        add(buttons, BorderLayout.SOUTH);

        setIconImage(ImageFactory.getImage("jbead-16"));
        setModal(true);
        pack();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        ok.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                isOK = true;
                dispose();
            }
        });
        cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                isOK = false;
                dispose();
            }
        });
    }

    public boolean isOK() {
        return isOK;
    }

    public void setPatternWidth(int width) {
        patternwidth.setValue(width);
    }

    public int getPatternWidth() {
        return (Integer) patternwidth.getValue();
    }

}
