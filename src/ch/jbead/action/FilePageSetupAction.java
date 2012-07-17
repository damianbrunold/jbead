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
import java.awt.print.PrinterJob;

import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Media;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.OrientationRequested;

import ch.jbead.BaseAction;
import ch.jbead.JBeadFrame;
import ch.jbead.Settings;
import ch.jbead.print.PrintSettings;

public class FilePageSetupAction extends BaseAction {

    private static final long serialVersionUID = 1L;

    private static final String NAME = "file.pagesetup";

    public FilePageSetupAction(JBeadFrame frame) {
        super(NAME, frame);
        putValue(SHORT_DESCRIPTION, localization.getString("action.file.pagesetup.description"));
        putValue(MNEMONIC_KEY, localization.getMnemonic("action.file.pagesetup.mnemonic"));
    }

    public void actionPerformed(ActionEvent e) {
        PrinterJob job = PrinterJob.getPrinterJob();
        PrintSettings settings = frame.getPrintSettings();
        if (job.pageDialog(settings.getAttributes()) != null) {
            saveConfig(settings.getAttributes());
        }
    }

    private void saveConfig(PrintRequestAttributeSet attributes) {
        Settings settings = new Settings();
        settings.setCategory("print");
        Media media = (Media) attributes.get(Media.class);
        if (media == MediaSizeName.ISO_A0) {
            settings.saveString("paper", "A0");
        } else if (media == MediaSizeName.ISO_A1) {
            settings.saveString("paper", "A1");
        } else if (media == MediaSizeName.ISO_A2) {
            settings.saveString("paper", "A2");
        } else if (media == MediaSizeName.ISO_A3) {
            settings.saveString("paper", "A3");
        } else if (media == MediaSizeName.ISO_A4) {
            settings.saveString("paper", "A4");
        } else if (media == MediaSizeName.ISO_A5) {
            settings.saveString("paper", "A5");
        } else if (media == MediaSizeName.ISO_A6) {
            settings.saveString("paper", "A6");
        } else if (media == MediaSizeName.ISO_A7) {
            settings.saveString("paper", "A7");
        } else if (media == MediaSizeName.ISO_A8) {
            settings.saveString("paper", "A8");
        } else if (media == MediaSizeName.ISO_A9) {
            settings.saveString("paper", "A9");
        } else if (media == MediaSizeName.ISO_A10) {
            settings.saveString("paper", "A10");
        } else if (media == MediaSizeName.NA_LETTER) {
            settings.saveString("paper", "Letter");
        } else if (media == MediaSizeName.NA_LEGAL) {
            settings.saveString("paper", "Legal");
        } else if (media == MediaSizeName.EXECUTIVE) {
            settings.saveString("paper", "Executive");
        } else if (media == MediaSizeName.LEDGER) {
            settings.saveString("paper", "Ledger");
        } else if (media == MediaSizeName.TABLOID) {
            settings.saveString("paper", "Tabloid");
        } else if (media == MediaSizeName.INVOICE) {
            settings.saveString("paper", "Invoice");
        } else if (media == MediaSizeName.FOLIO) {
            settings.saveString("paper", "Folio");
        } else if (media == MediaSizeName.QUARTO) {
            settings.saveString("paper", "Quarto");
        }
        OrientationRequested orientation = (OrientationRequested) attributes.get(OrientationRequested.class);
        if (orientation == OrientationRequested.LANDSCAPE) {
            settings.saveString("orientation", "Landscape");
        } else if (orientation == OrientationRequested.PORTRAIT) {
            settings.saveString("orientation", "Portrait");
        } else if (orientation == OrientationRequested.REVERSE_LANDSCAPE) {
            settings.saveString("orientation", "Reverse_Landscape");
        } else if (orientation == OrientationRequested.REVERSE_PORTRAIT) {
            settings.saveString("orientation", "Reverse_Portrait");
        }
    }

}
