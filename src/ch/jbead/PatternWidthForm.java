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

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;

/**
 * 
 */
public class PatternWidthForm extends JDialog {
    private static final long serialVersionUID = 1L;

    private JLabel labelDescription = new JLabel();
    private JLabel labelWidth = new JLabel();
    private SpinnerModel widthModel = new SpinnerNumberModel(10, 5, 35, 1);
    private JSpinner Width = new JSpinner(widthModel);
    private JButton bOk = new JButton();
    private JButton bCancel = new JButton();

    private boolean isOK = false;

    public PatternWidthForm() {
        setLayout(new BorderLayout());

        JPanel form = new JPanel();
        form.setLayout(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 2;
        form.add(labelDescription, constraints);

        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 1;
        form.add(labelWidth, constraints);

        constraints = new GridBagConstraints();
        constraints.gridx = 1;
        constraints.gridy = 1;
        form.add(Width, constraints);

        add(form, BorderLayout.CENTER);

        JPanel buttons = new JPanel();
        buttons.setLayout(new FlowLayout());
        buttons.add(bOk);
        buttons.add(bCancel);
        add(buttons, BorderLayout.SOUTH);

        setModal(true);
        setSize(500, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        bOk.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                isOK = true;
                dispose();
            }
        });
        bCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                isOK = false;
                dispose();
            }
        });
    }

    public void reloadLanguage() {
        Language.C_H(this, Language.LANG.EN, "Width of pattern");
        Language.C_H(this, Language.LANG.GE, "Musterbreite");
        Language.C_H(labelDescription, Language.LANG.EN, "The width of pattern is equivalent to the circumference of the rope");
        Language.C_H(labelWidth, Language.LANG.GE, "Die Musterbreite entspricht dem Umfang der Kette");
        Language.C_H(labelWidth, Language.LANG.EN, "&Width of pattern:");
        Language.C_H(labelWidth, Language.LANG.GE, "&Musterbreite:");
        Language.C_H(bOk, Language.LANG.EN, "OK", "");
        Language.C_H(bOk, Language.LANG.GE, "OK", "");
        Language.C_H(bCancel, Language.LANG.EN, "Cancel", "");
        Language.C_H(bCancel, Language.LANG.GE, "Abbrechen", "");
    }

    public void FormShow() {
        reloadLanguage();
        setVisible(true);
    }

    public boolean isOK() {
        return isOK;
    }

    public void setWidth(int width) {
        Width.setValue(width);
    }

    public int getWidth() {
        return (Integer) Width.getValue();
    }

    public static void main(String[] args) {
        new PatternWidthForm().FormShow();
    }

}
