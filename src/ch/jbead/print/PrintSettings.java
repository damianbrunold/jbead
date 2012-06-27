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

import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.PrinterJob;

import javax.print.PrintService;

public class PrintSettings {

    private PrintService service;
    private PageFormat format;

    public PrintSettings() {
        service = null;
        format = getDefaultFormat();
    }

    public PrintService getService() {
        return service;
    }

    public PageFormat getFormat() {
        return format;
    }

    public void setService(PrintService service) {
        this.service = service;
    }

    public void setFormat(PageFormat format) {
        this.format = format;
    }

    private PageFormat getDefaultFormat() {
        PageFormat format = PrinterJob.getPrinterJob().defaultPage();
        format.setOrientation(PageFormat.LANDSCAPE);
        // Set fix A4 paper, maybe make customizable
        Paper paper = new Paper();
        paper.setSize(Convert.mm2pt(210), Convert.mm2pt(297));
        paper.setImageableArea(Convert.mm2pt(15), Convert.mm2pt(15), Convert.mm2pt(210 - 2 * 15), Convert.mm2pt(297 - 2 * 15));
        format.setPaper(paper);
        return format;
    }

}
