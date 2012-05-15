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
public class CopyForm extends JDialog {
    private static final long serialVersionUID = 1L;

    private boolean isOK = false;

    private SpinnerModel horzModel = new SpinnerNumberModel(5, 0, 100, 1);
    private SpinnerModel vertModel = new SpinnerNumberModel(5, 0, 100, 1);
    private SpinnerModel copyModel = new SpinnerNumberModel(1, 0, 100, 1);

    private JLabel lHorz = new JLabel();
    private JSpinner horz = new JSpinner(horzModel);
    private JLabel lVert = new JLabel();
    private JSpinner vert = new JSpinner(vertModel);
    private JLabel lCopies = new JLabel();
    private JSpinner Copies = new JSpinner(copyModel);
    private JButton bOK = new JButton();
    private JButton bCancel = new JButton();

    public CopyForm() {
        setLayout(new BorderLayout());

        JPanel form = new JPanel();
        form.setLayout(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        form.add(lHorz, constraints);

        constraints = new GridBagConstraints();
        constraints.gridx = 1;
        constraints.gridy = 0;
        form.add(horz, constraints);

        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 1;
        form.add(lVert, constraints);

        constraints = new GridBagConstraints();
        constraints.gridx = 1;
        constraints.gridy = 1;
        form.add(vert, constraints);

        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 2;
        form.add(lCopies, constraints);

        constraints = new GridBagConstraints();
        constraints.gridx = 1;
        constraints.gridy = 2;
        form.add(Copies, constraints);

        add(form, BorderLayout.CENTER);

        JPanel buttons = new JPanel();
        buttons.setLayout(new FlowLayout());
        buttons.add(bOK);
        buttons.add(bCancel);
        add(buttons, BorderLayout.SOUTH);

        setModal(true);
        setSize(500, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        bOK.addActionListener(new ActionListener() {
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

    public boolean isOK() {
        return isOK;
    }

    public int getCopies() {
        return (Integer) Copies.getValue();
    }

    public int getVertOffset() {
        return (Integer) vert.getValue();
    }

    public int getHorzOffset() {
        return (Integer) horz.getValue();
    }

    public void reloadLanguage() {
        Texts.update(this, Language.EN, "Arrangement");
        Texts.update(this, Language.GE, "Anordnen");
        Texts.update(lCopies, Language.EN, "&Number of copies:");
        Texts.update(lCopies, Language.GE, "&Anzahl Kopien:");
        Texts.update(lHorz, Language.EN, "&Horizontal displacement:");
        Texts.update(lHorz, Language.GE, "&Horizontaler Versatz:");
        Texts.update(lVert, Language.EN, "&Vertical displacement:");
        Texts.update(lVert, Language.GE, "&Vertikaler Versatz:");
        Texts.update(bOK, Language.EN, "OK", "");
        Texts.update(bOK, Language.GE, "OK", "");
        Texts.update(bCancel, Language.EN, "Cancel", "");
        Texts.update(bCancel, Language.GE, "Abbrechen", "");
    }

    public void formShow() {
        reloadLanguage();
        setVisible(true);
    }

}
