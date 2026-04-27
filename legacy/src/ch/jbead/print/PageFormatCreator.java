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

import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.PrinterJob;
import java.lang.reflect.Method;

import javax.print.PrintService;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Media;
import javax.print.attribute.standard.MediaPrintableArea;
import javax.print.attribute.standard.MediaSize;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.OrientationRequested;

/**
 * This is a copy of the sun jdk implementation of
 * PrinterJob.getPageFormat(PrintRequestAttributeSet attrs).
 *
 * The reason this copy is here is that the above method is only defined
 * starting in java 6. But jbead should be capable of running with java 5.
 * Therefore, I decided to copy the code into the project, so that it is
 * available even in java 5.
 *
 * In order to be able to profit from better implementations, e.g. in java
 * 7, the code first tries to call the getPageFormat method using
 * reflection. Only if this does not work is the copied code used. Thus, on
 * java 6 and 7 the original code is used, and on java 5 the copied one.
 */
public class PageFormatCreator {

    public static PageFormat create(PrinterJob job, PrintRequestAttributeSet attributes) {
        try {
            Method method = PrinterJob.class.getMethod("getPageFormat", PrintRequestAttributeSet.class);
            return (PageFormat) method.invoke(job, attributes);
        } catch (Exception e) {
            return getPageFormat(job, attributes);
        }
    }

    private static PageFormat getPageFormat(PrinterJob job, PrintRequestAttributeSet attributes) {
        PrintService service = job.getPrintService();
        PageFormat pf = job.defaultPage();

        if (service == null || attributes == null) {
            return pf;
        }

        Media media = (Media) attributes.get(Media.class);
        MediaPrintableArea mpa = (MediaPrintableArea) attributes.get(MediaPrintableArea.class);
        OrientationRequested orientReq = (OrientationRequested) attributes.get(OrientationRequested.class);

        if (media == null && mpa == null && orientReq == null) {
            return pf;
        }
        Paper paper = pf.getPaper();

        /*
         * If there's a media but no media printable area, we can try to
         * retrieve the default value for mpa and use that.
         */
        if (mpa == null && media != null && service.isAttributeCategorySupported(MediaPrintableArea.class)) {
            Object mpaVals = service.getSupportedAttributeValues(MediaPrintableArea.class, null, attributes);
            if (mpaVals instanceof MediaPrintableArea[] && ((MediaPrintableArea[]) mpaVals).length > 0) {
                mpa = ((MediaPrintableArea[]) mpaVals)[0];
            }
        }

        if (media != null && service.isAttributeValueSupported(media, null, attributes)) {
            if (media instanceof MediaSizeName) {
                MediaSizeName msn = (MediaSizeName) media;
                MediaSize msz = MediaSize.getMediaSizeForName(msn);
                if (msz != null) {
                    double inch = 72.0;
                    double paperWid = msz.getX(MediaSize.INCH) * inch;
                    double paperHgt = msz.getY(MediaSize.INCH) * inch;
                    paper.setSize(paperWid, paperHgt);
                    if (mpa == null) {
                        paper.setImageableArea(inch, inch, paperWid - 2 * inch, paperHgt - 2 * inch);
                    }
                }
            }
        }

        if (mpa != null && service.isAttributeValueSupported(mpa, null, attributes)) {
            float[] printableArea = mpa.getPrintableArea(MediaPrintableArea.INCH);
            for (int i = 0; i < printableArea.length; i++) {
                printableArea[i] = printableArea[i] * 72.0f;
            }
            paper.setImageableArea(printableArea[0], printableArea[1], printableArea[2], printableArea[3]);
        }

        if (orientReq != null && service.isAttributeValueSupported(orientReq, null, attributes)) {
            int orient;
            if (orientReq.equals(OrientationRequested.REVERSE_LANDSCAPE)) {
                orient = PageFormat.REVERSE_LANDSCAPE;
            } else if (orientReq.equals(OrientationRequested.LANDSCAPE)) {
                orient = PageFormat.LANDSCAPE;
            } else {
                orient = PageFormat.PORTRAIT;
            }
            pf.setOrientation(orient);
        }

        pf.setPaper(paper);
        pf = job.validatePage(pf);
        return pf;
    }
}
