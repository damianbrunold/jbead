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

import java.awt.print.Book;
import java.awt.print.PageFormat;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.util.ArrayList;
import java.util.List;

import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.JobName;
import javax.swing.JOptionPane;

import ch.jbead.Localization;
import ch.jbead.Model;
import ch.jbead.View;


public class DesignPrinter {

    private Model model;
    private View view;
    private Localization localization;
    private PrintSettings settings;
    private boolean fullPattern = false;

    private List<PageLayout> pages = new ArrayList<PageLayout>();

    public DesignPrinter(Model model, View view, Localization localization, PrintSettings settings) {
        this.model = model;
        this.view = view;
        this.localization = localization;
        this.settings = settings;
    }

    public void setFullPattern(boolean fullPattern) {
        this.fullPattern = fullPattern;
    }

    private void layoutPages(PageFormat format) {
        int pageWidth = (int) format.getImageableWidth();
        int pageHeight = (int) format.getImageableHeight();
        PageLayout currentPage = new PageLayout(pageWidth);
        for (PartPrinter part : getPartPrinters()) {
            currentPage = layoutPart(part, currentPage, pageWidth, pageHeight);
        }
        addPartialPage(pageWidth, currentPage);
    }

    private PageLayout layoutPart(PartPrinter part, PageLayout currentPage, int pageWidth, int pageHeight) {
        List<Integer> columns = part.layoutColumns(pageWidth, pageHeight);
        for (int columnIndex = 0; columnIndex < columns.size(); columnIndex++) {
            int columnWidth = columns.get(columnIndex);
            if (pageWidth < columnWidth) throw new RuntimeException("Column is wider than page!");
            if (currentPage.getUnusedWidth() < columnWidth) {
                pages.add(currentPage);
                currentPage = new PageLayout(pageWidth);
            }
            currentPage.addPart(new PagePart(part, columnIndex, columnWidth));
        }
        return currentPage;
    }

    private void addPartialPage(int pageWidth, PageLayout currentPage) {
        if (currentPage.getUnusedWidth() < pageWidth) {
            pages.add(currentPage);
        }
    }

    private List<PartPrinter> getPartPrinters() {
        List<PartPrinter> printers = new ArrayList<PartPrinter>();
        addReportInfosPrinter(printers);
        addDraftPrinter(printers);
        addCorrectedPrinter(printers);
        addSimulationPrinter(printers);
        addBeadListPrinter(printers);
        return printers;
    }

    private void addBeadListPrinter(List<PartPrinter> printers) {
        if (view.isReportVisible()) printers.add(new BeadListPrinter(model, view, localization));
    }

    private void addSimulationPrinter(List<PartPrinter> printers) {
        if (view.isSimulationVisible()) printers.add(new SimulationPrinter(model, view, localization, fullPattern));
    }

    private void addCorrectedPrinter(List<PartPrinter> printers) {
        if (view.isCorrectedVisible()) printers.add(new CorrectedPrinter(model, view, localization, fullPattern));
    }

    private void addDraftPrinter(List<PartPrinter> printers) {
        if (view.isDraftVisible()) printers.add(new DraftPrinter(model, view, localization, fullPattern));
    }

    private void addReportInfosPrinter(List<PartPrinter> printers) {
        if (view.isReportVisible()) printers.add(new ReportInfosPrinter(model, view, localization));
    }

    public void print(boolean showDialog) {
        try {
            int scroll = model.getScroll();
            try {
                model.setScroll(0);
                PrinterJob printjob = getPrinterJob();
                PrintRequestAttributeSet attrs = getPrintAttributeSet();
                if (showDialog) {
                    if (!printjob.printDialog(attrs)) return;
                    settings.setService(printjob.getPrintService());
                }
                PageFormat pageformat = PageFormatCreator.create(printjob, attrs);
                layoutPages(pageformat);
                printjob.setPageable(createBook(pageformat));
                printjob.print(attrs);
            } finally {
                model.setScroll(scroll);
            }
        } catch (PrinterException e) {
            showPrintErrorMessage(e);
        }
    }

    private PrinterJob getPrinterJob() throws PrinterException {
        PrinterJob printjob = PrinterJob.getPrinterJob();
        if (settings.getService() != null) {
            printjob.setPrintService(settings.getService());
        }
        return printjob;
    }

    private PrintRequestAttributeSet getPrintAttributeSet() {
        PrintRequestAttributeSet attrs = new HashPrintRequestAttributeSet(settings.getAttributes());
        attrs.add(new JobName(getJobName(), null));
        return attrs;
    }

    private String getJobName() {
        return "jbead " + System.currentTimeMillis() + " " + normalize(model.getFile().getName());
    }

    private String normalize(String s) {
        return s.replace("ä", "ae").replace("ö", "oe").replace("ü", "ue");
    }

    private Book createBook(PageFormat pageformat) {
        Book book = new Book();
        for (PageLayout page : pages) {
            book.append(page, pageformat);
        }
        return book;
    }

    private void showPrintErrorMessage(PrinterException e) {
        String msg = e.getMessage();
        if (msg == null) msg = e.toString();
        JOptionPane.showMessageDialog(null, localization.getString("print.failure") + ": " + msg);
    }

}
