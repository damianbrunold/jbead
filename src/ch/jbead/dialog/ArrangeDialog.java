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

import ch.jbead.Localization;
import ch.jbead.Model;
import ch.jbead.Selection;

public class ArrangeDialog extends JDialog {
    private static final long serialVersionUID = 1L;

    private boolean isOK = false;

    private Selection selection;
    private Model model;

    private SpinnerModel horzModel = new SpinnerNumberModel(5, 0, 100, 1);
    private SpinnerModel vertModel = new SpinnerNumberModel(5, 0, 100, 1);
    private SpinnerModel copyModel = new SpinnerNumberModel(1, 0, 100, 1);

    private JSpinner horz = new JSpinner(horzModel);
    private JSpinner vert = new JSpinner(vertModel);
    private JSpinner Copies = new JSpinner(copyModel);
    private JButton bOK;
    private JButton bCancel;

    public ArrangeDialog(Localization localization, Selection selection, Model model) {
        this.selection = selection;
        this.model = model;
        setTitle(localization.getString("arrangedialog.title"));
        JPanel main = new JPanel();
        main.setBorder(BorderFactory.createEmptyBorder(7, 7, 7, 7));
        setContentPane(main);
        setLayout(new BorderLayout());

        JPanel form = new JPanel();
        form.setLayout(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        form.add(new JLabel(localization.getString("arrangedialog.horz")), constraints);

        constraints = new GridBagConstraints();
        constraints.gridx = 1;
        constraints.gridy = 0;
        form.add(horz, constraints);

        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 1;
        form.add(new JLabel(localization.getString("arrangedialog.vert")), constraints);

        constraints = new GridBagConstraints();
        constraints.gridx = 1;
        constraints.gridy = 1;
        form.add(vert, constraints);

        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 2;
        form.add(new JLabel(localization.getString("arrangedialog.copies")), constraints);

        constraints = new GridBagConstraints();
        constraints.gridx = 1;
        constraints.gridy = 2;
        form.add(Copies, constraints);

        add(form, BorderLayout.CENTER);

        JPanel buttons = new JPanel();
        buttons.setLayout(new FlowLayout());
        buttons.add(bOK = new JButton(localization.getString("ok")));
        buttons.add(bCancel  = new JButton(localization.getString("cancel")));
        add(buttons, BorderLayout.SOUTH);

        initDefaultValues();

        setModal(true);
        pack();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        bOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                isOK = true;
                dispose();
            }
        });
        bCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                isOK = false;
                dispose();
            }
        });
    }

    private void initDefaultValues() {
        horzModel.setValue(getDefaultHorzDisplacement());
        vertModel.setValue(getDefaultVertDisplacement());
    }

    private int getDefaultHorzDisplacement() {
        if (selection.width() == model.getWidth()) {
            return 0;
        } else {
            return selection.width();
        }
    }

    private int getDefaultVertDisplacement() {
        return selection.height();
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

    public int getOffset(int width) {
        return getVertOffset() * width + getHorzOffset();
    }

}
