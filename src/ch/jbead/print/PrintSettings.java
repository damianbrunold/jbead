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

package ch.jbead.print;

import java.awt.print.Paper;
import java.awt.print.PrinterJob;

import javax.print.PrintService;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.OrientationRequested;

public class PrintSettings {

    private PrintService service;
    private PrintRequestAttributeSet attributes;

    public PrintSettings() {
        service = null;
        attributes = new HashPrintRequestAttributeSet();
        initDefaultFormat();
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

    private void initDefaultFormat() {
        attributes.add(OrientationRequested.LANDSCAPE);
        attributes.add(new Copies(1));
        Paper paper = PrinterJob.getPrinterJob().defaultPage().getPaper();
        if (isLetter(paper)) {
            attributes.add(MediaSizeName.NA_LETTER);
        } else if (isLegal(paper)) {
            attributes.add(MediaSizeName.NA_LEGAL);
        } else {
            attributes.add(MediaSizeName.ISO_A4); // TODO make this default configurable
        }
    }

    private boolean isLetter(Paper paper) {
        return paper.getWidth() == 612.0 && paper.getHeight() == 792.0;
    }

    private boolean isLegal(Paper paper) {
        return paper.getWidth() == 612.0 && paper.getHeight() == 1008.0;
    }

}
