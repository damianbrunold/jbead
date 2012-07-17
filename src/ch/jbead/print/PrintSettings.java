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

package ch.jbead.print;

import java.awt.print.Paper;
import java.awt.print.PrinterJob;

import javax.print.PrintService;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.OrientationRequested;

import ch.jbead.Settings;

public class PrintSettings {

    private PrintService service;
    private PrintRequestAttributeSet attributes;

    public PrintSettings(Settings settings) {
        service = null;
        attributes = new HashPrintRequestAttributeSet();
        initDefaultFormat(settings);
    }

    public PrintService getService() {
        return service;
    }

    public PrintRequestAttributeSet getAttributes() {
        return attributes;
    }

    public void setService(PrintService service) {
        this.service = service;
    }

    public void setAttributes(PrintRequestAttributeSet attributes) {
        this.attributes = attributes;
    }

    private void initDefaultFormat(Settings settings) {
        settings.setCategory("print");
        String paperorient = settings.loadString("orientation");
        if (paperorient.equalsIgnoreCase("portrait")) {
            attributes.add(OrientationRequested.PORTRAIT);
        } else if (paperorient.equalsIgnoreCase("reverse_portrait")) {
            attributes.add(OrientationRequested.REVERSE_PORTRAIT);
        } else if (paperorient.equalsIgnoreCase("reverse_landscape")) {
            attributes.add(OrientationRequested.REVERSE_LANDSCAPE);
        } else {
            attributes.add(OrientationRequested.LANDSCAPE);
        }
        attributes.add(new Copies(1));
        String papername = settings.loadString("paper");
        if (papername.length() > 0) {
            attributes.add(getMedia(papername));
        } else {
            Paper paper = PrinterJob.getPrinterJob().defaultPage().getPaper();
            if (isLetter(paper)) {
                attributes.add(MediaSizeName.NA_LETTER);
            } else if (isLegal(paper)) {
                attributes.add(MediaSizeName.NA_LEGAL);
            } else {
                attributes.add(getMedia("a4"));
            }
        }
    }

    private MediaSizeName getMedia(String paper) {
        if (paper.equalsIgnoreCase("a0")) {
            return MediaSizeName.ISO_A0;
        } else if (paper.equalsIgnoreCase("a1")) {
            return MediaSizeName.ISO_A1;
        } else if (paper.equalsIgnoreCase("a2")) {
            return MediaSizeName.ISO_A2;
        } else if (paper.equalsIgnoreCase("a3")) {
            return MediaSizeName.ISO_A3;
        } else if (paper.equalsIgnoreCase("a4")) {
            return MediaSizeName.ISO_A4;
        } else if (paper.equalsIgnoreCase("a5")) {
            return MediaSizeName.ISO_A5;
        } else if (paper.equalsIgnoreCase("a6")) {
            return MediaSizeName.ISO_A6;
        } else if (paper.equalsIgnoreCase("a7")) {
            return MediaSizeName.ISO_A7;
        } else if (paper.equalsIgnoreCase("a8")) {
            return MediaSizeName.ISO_A8;
        } else if (paper.equalsIgnoreCase("a9")) {
            return MediaSizeName.ISO_A9;
        } else if (paper.equalsIgnoreCase("a10")) {
            return MediaSizeName.ISO_A10;
        } else if (paper.equalsIgnoreCase("letter")) {
            return MediaSizeName.NA_LETTER;
        } else if (paper.equalsIgnoreCase("legal")) {
            return MediaSizeName.NA_LEGAL;
        } else if (paper.equalsIgnoreCase("executive")) {
            return MediaSizeName.EXECUTIVE;
        } else if (paper.equalsIgnoreCase("ledger")) {
            return MediaSizeName.LEDGER;
        } else if (paper.equalsIgnoreCase("tabloid")) {
            return MediaSizeName.TABLOID;
        } else if (paper.equalsIgnoreCase("invoice")) {
            return MediaSizeName.INVOICE;
        } else if (paper.equalsIgnoreCase("folio")) {
            return MediaSizeName.FOLIO;
        } else if (paper.equalsIgnoreCase("quarto")) {
            return MediaSizeName.QUARTO;
        } else {
            return MediaSizeName.ISO_A4;
        }
    }

    private boolean isLetter(Paper paper) {
        return paper.getWidth() == 612.0 && paper.getHeight() == 792.0;
    }

    private boolean isLegal(Paper paper) {
        return paper.getWidth() == 612.0 && paper.getHeight() == 1008.0;
    }

}
