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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * 
 */
public class AboutBox extends JDialog {
    private static final long serialVersionUID = 1L;

    private JLabel text = new JLabel();
    private JButton bOK = new JButton("OK");

    public AboutBox() {
        setModal(true);
        setLayout(new BorderLayout());
        add(text, BorderLayout.CENTER);
        JPanel buttons = new JPanel();
        buttons.add(bOK);
        add(buttons, BorderLayout.SOUTH);
        reloadLanguage();
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        bOK.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                AboutBox.this.dispose();
            }
        });
    }

    public void FormShow() {
        reloadLanguage();
        setVisible(true);
    }

    private void setEnglishText() {
        String t = "<html><h1>jbead</h1>" + "<p>This is <b>jbead</b>, a program designed to help you design crochet bead ropes. "
                + "The creation of such ropes is described in e.g. the book "
                + "'Gehäkelte Glasperlenketten' written by Lotti Gygax. It is hard work to "
                + "create such a rope, but the result is very beautiful." + "<p>&nbsp;</p>"
                + "<p>With <b>jbead</b> you simulate before you start working how your design will "
                + "look like as a finished rope. You can make changes directly on the screen. " + "<p>&nbsp;</p>"
                + "<p>After finishing the design, you can print out all relevant data "
                + "including a 'list of beads', which is very useful for correctly arranging " + "the beads onto the thread. " + "<br/> "
                + "<p><b>jbead</b> was written by Damian Brunold. It is freely available and licensed "
                + "under the GPL v3. This means, you can use and copy it freely and you can "
                + "create derivative works (if you are a programmer). Damian Brunold cannot "
                + "assume any liability for bugs and damage caused by using the program. "
                + "You have to decide for yourself whether the program is useful for you or not. " + "<p>&nbsp;</p> "
                + "<p>More information is available at http://www.brunoldsoftware.ch or by sending "
                + "e-mail to info@brunoldsoftware.ch. This also is the address to direct bug " + "reports or feature requests to. "
                + "<p>&nbsp;</p> " + "<p>Have fun using <b>jbead</b> " + "<p>Damian Brunold ";
        text.setText(t);
    }

    private void setGermanText() {
        String t = "<html><h1>jbead</h1>" + "<p>Dies ist <b>jbead</b>, ein Programm, das Ihnen beim Entwurf von gehäkelten "
                + "Perlenketten helfen soll. Die Erstellung solcher Ketten wird beispielsweise "
                + "im Buch 'Gehäkelte Glasperlenketten' von Lotti Gygax beschrieben. Die Arbeit ist aufwändig und "
                + "langwierig. Das Resultat entschädigt aber für die erlittene Mühsal. " + "<p>&nbsp;</p> "
                + "<p>Mit <b>jbead</b> können Sie schon vor Beginn der Arbeit simulieren, wie Ihr "
                + "Entwurf als Kette dann aussehen wird. Direkt am Bildschirm können Sie " + "Änderungen vornehmen.\\par " + "<p>&nbsp;</p> "
                + "<p>Wenn Sie zufrieden mit dem Entwurf sind, können Sie alle notwendigen "
                + "Daten ausdrucken lassen, inklusive einer 'Fädelliste', die hilfreich "
                + "für das Auffädeln der Perlen auf das Häkelgarn ist.\\par " + "<p>&nbsp;</p> "
                + "<p><b>jbead</b> wurde von Damian Brunold geschrieben. Es steht unter der Lizenz "
                + "GPL v3, was bedeutet, dass Sie es kostenlos verwenden, kopieren und ändern "
                + "dürfen. Dafür übernimmt Damian Brunold absolut keine " + "Haftung für Fehler und Schäden durch Benutzung des Programmes. "
                + "Sie müssen selber entscheiden, ob das Programm für Sie nützlich " + "ist oder nicht.\\par " + "<p>&nbsp;</p> "
                + "<p>Weitere Informationen erhalten Sie unter http://www.brunoldsoftware.ch "
                + "oder per E-Mail an info@brunoldsoftware.ch. An diese Adresse können "
                + "Sie auch Fehler oder Verbesserungsvorschläge melden.\\par " + "<p>&nbsp;</p> " + "<p>Viel Spass mit dem Programm</p>"
                + "<p>Damian Brunold</p>";
        text.setText(t);
    }

    public void reloadLanguage() {
        Language.C_H(this, Language.LANG.EN, "About DB-BEAD");
        Language.C_H(this, Language.LANG.GE, "Über DB-BEAD");
        if (Language.active_language == Language.LANG.EN) {
            setEnglishText();
        } else {
            setGermanText();
        }
        Language.C_H(bOK, Language.LANG.EN, "OK", "");
        Language.C_H(bOK, Language.LANG.GE, "OK", "");
    }

    public static void main(String[] args) {
        new AboutBox().FormShow();
    }
}
