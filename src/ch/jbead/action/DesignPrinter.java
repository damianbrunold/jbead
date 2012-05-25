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

package ch.jbead.action;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

import ch.jbead.Localization;
import ch.jbead.Model;
import ch.jbead.Point;

public class DesignPrinter {

    private Model model;
    private Localization localization;
    private PageFormat pageFormat;
    private boolean withDraft;
    private boolean withNormal;
    private boolean withSimulation;
    private boolean withReport;
    
    public DesignPrinter(Model model,
            Localization localization,
            PageFormat pageFormat,
            boolean withDraft, 
            boolean withNormal, 
            boolean withSimulation, 
            boolean withReport) {
        this.model = model;
        this.localization = localization;
        this.pageFormat = pageFormat;
        this.withDraft = withDraft;
        this.withNormal = withNormal;
        this.withSimulation = withSimulation;
        this.withReport = withReport;
    }
    
    public void print(boolean showDialog) {
        try {
            if (showDialog) {
                PrinterJob pj = PrinterJob.getPrinterJob();
                if (pj.printDialog()) {
                    pj.setPrintable(new Printable() {
                        @Override
                        public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
                            if (pageIndex == 0) {
                                printAll(graphics, pageFormat, pageIndex);
                                return PAGE_EXISTS;
                            } else {
                                return NO_SUCH_PAGE;
                            }
                        }
                    }, pageFormat);
                    pj.print();
                }
            } else {
                PrinterJob pj = PrinterJob.getPrinterJob();
                pj.setPrintable(new Printable() {
                    @Override
                    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
                        if (pageIndex == 0) {
                            printAll(graphics, pageFormat, pageIndex);
                            return PAGE_EXISTS;
                        } else {
                            return NO_SUCH_PAGE;
                        }
                    }
                }, pageFormat);
                pj.print();
            }
        } catch (PrinterException e) {
            // TODO show error dialog
        }
    }

    private void printAll(Graphics g, PageFormat pageFormat, int pageIndex) {
//      String title = "jbead"; // APP_TITLE;
//      title += " - " + savedialog.getSelectedFile().getName();
      // TODO print headers and footers?

      int sx = 72; // 72 dpi
      int sy = 72; // 72 dpi

      int zoomIndex = model.getZoomIndex();
      int gx = (15 + zoomIndex * 5) * sx / 254;
      int gy = (15 + zoomIndex * 5) * sy / 254;

      int draftleft = 0;
      int normalleft = 0;
      int simulationleft = 0;
      int reportleft = 0;
      int reportcols = 0;

      int m = mm2px(10, sx);
      if (withDraft) {
          draftleft = m;
          m += mm2px(13, sx) + model.getWidth() * gx + mm2px(7, sx);
      }

      if (withNormal) {
          normalleft = m;
          m += mm2px(7, sx) + (model.getWidth() + 1) * gx;
      }

      if (withSimulation) {
          simulationleft = m;
          m += mm2px(7, sx) + (model.getWidth() / 2 + 1) * gx;
      }

      if (withReport) {
          reportleft = m;
          reportcols = ((int) pageFormat.getWidth() - m - 10) / (mm2px(5, sx) + mm2px(8, sx));
      }

      int h = (int) pageFormat.getHeight() - mm2py(10, sy);

      // //////////////////////////////////////
      //
      // Draft
      //
      // //////////////////////////////////////

      // Grid
      g.setColor(Color.BLACK);
      int left = draftleft + mm2px(13, sx);
      if (left < 0) left = 0;
      int maxj = Math.min(model.getHeight(), (h - mm2py(10, sy)) / gy);
      for (int i = 0; i < model.getWidth() + 1; i++) {
          g.drawLine(left + i * gx, h - (maxj) * gy, left + i * gx, h - 1);
      }
      for (int j = 0; j <= maxj; j++) {
          g.drawLine(left, h - 1 - j * gy, left + model.getWidth() * gx, h - 1 - j * gy);
      }

      // Daten
      for (int i = 0; i < model.getWidth(); i++) {
          for (int j = 0; j < maxj; j++) {
              byte c = model.get(new Point(i, j));
              if (c > 0) {
                  g.setColor(model.getColor(c));
                  g.fillRect(left + i * gx + 1, h - (j + 1) * gy, gx, gy);
              }
          }
      }

      // Zehnermarkierungen
      g.setColor(Color.BLACK);
      for (int j = 0; j < maxj; j++) {
          if ((j % 10) == 0) {
              g.drawLine(draftleft, h - j * gy - 1, left - mm2px(3, sx), h - j * gy - 1);
              g.drawString(Integer.toString(j), draftleft, h - j * gy + mm2py(1, sy));
          }
      }

      // //////////////////////////////////////
      //
      // Korrigiert (normal)
      //
      // //////////////////////////////////////

      // Grid
      g.setColor(Color.BLACK);
      left = normalleft + gx / 2;
      if (left < 0) left = gx / 2;
      maxj = Math.min(model.getHeight(), (h - mm2py(10, sy)) / gy);
      for (int i = 0; i < model.getWidth() + 1; i++) {
          for (int jj = 0; jj < maxj; jj += 2) {
              g.drawLine(left + i * gx, h - (jj + 1) * gy, left + i * gx, h - jj * gy);
          }
      }
      for (int i = 0; i <= model.getWidth() + 1; i++) {
          for (int jj = 1; jj < maxj; jj += 2) {
              g.drawLine(left + i * gx - gx / 2, h - (jj + 1) * gy, left + i * gx - gx / 2, h - jj * gy);
          }
      }
      g.drawLine(left, h - 1, left + model.getWidth() * gx + 1, h - 1);
      for (int jj = 1; jj <= maxj; jj++) {
          g.drawLine(left - gx / 2, h - 1 - jj * gy, left + model.getWidth() * gx + gx / 2 + 1, h - 1 - jj * gy);
      }

      // Daten
      for (int i = 0; i < model.getWidth(); i++) {
          for (int jj = 0; jj < maxj; jj++) {
              byte c = model.get(new Point(i, jj));
              if (c == 0) continue;
              g.setColor(model.getColor(c));
              int ii = i;
              int j1 = jj;
              ii = correctCoordinatesX(ii, j1);
              j1 = correctCoordinatesY(ii, j1);
              if (j1 % 2 == 0) {
                  g.fillRect(left + ii * gx + 1, h - (j1 + 1) * gy, gx, gy);
              } else {
                  g.fillRect(left - gx / 2 + ii * gx + 1, h - (j1 + 1) * gy, gx, gy);
              }
          }
      }

      // //////////////////////////////////////
      //
      // Simulation
      //
      // //////////////////////////////////////

      // Grid
      g.setColor(Color.BLACK);
      left = simulationleft + gx / 2;
      if (left < 0) left = gx / 2;
      maxj = Math.min(model.getHeight(), (h - mm2py(10, sy)) / gy);
      int w = model.getWidth() / 2;
      for (int j = 0; j < maxj; j += 2) {
          for (int i = 0; i < w + 1; i++) {
              g.drawLine(left + i * gx, h - (j + 1) * gy, left + i * gx, h - j * gy);
          }
          if (j > 0) {
              g.drawLine(left - gx / 2, h - (j + 1) * gy, left - gx / 2, h - j * gy);
          }
      }
      for (int j = 1; j < maxj; j += 2) {
          for (int i = 0; i < w + 1; i++) {
              g.drawLine(left + i * gx - gx / 2, h - (j + 1) * gy, left + i * gx - gx / 2, h - j * gy);
          }
          g.drawLine(left + w * gx, h - (j + 1) * gy, left + w * gx, h - j * gy);
      }
      g.drawLine(left, h - 1, left + w * gx + 1, h - 1);
      for (int j = 1; j <= maxj; j++) {
          g.drawLine(left - gx / 2, h - 1 - j * gy, left + w * gx + 1, h - 1 - j * gy);
      }

      // Daten
      for (int i = 0; i < model.getWidth(); i++) {
          for (int j = 0; j < maxj; j++) {
              byte c = model.get(new Point(i, j));
              if (c == 0) continue;
              g.setColor(model.getColor(c));
              int ii = i;
              int jj = j;
              ii = correctCoordinatesX(ii, jj);
              jj = correctCoordinatesY(ii, jj);
              if (ii > w && ii != model.getWidth()) continue;
              if (jj % 2 == 0) {
                  if (ii == w) continue;
                  g.fillRect(left + ii * gx + 1, h - (jj + 1) * gy, gx, gy);
              } else {
                  if (ii != model.getWidth() && ii != w) {
                      g.fillRect(left - gx / 2 + ii * gx + 1, h - (jj + 1) * gy, gx, gy);
                  } else if (ii == w) {
                      g.fillRect(left - gx / 2 + ii * gx + 1, h - (jj + 1) * gy, gx / 2, gy);
                  } else {
                      g.fillRect(left - gx / 2 + 1, h - (jj + 2) * gy, gx / 2, gy);
                  }
              }
          }
      }

      // //////////////////////////////////////
      //
      // Auswertung
      //
      // //////////////////////////////////////

      int x1 = reportleft;
      int x2 = reportleft + mm2px(30, sx);
      int y = mm2py(10, sy);
      int dy = mm2py(5, sy);
      int dx = mm2px(5, sx);

      // Mustername
      g.setColor(Color.BLACK);
      g.drawString(localization.getString("report.pattern"), x1, y);
      g.drawString(model.getFile().getName(), x2, y);
      y += dy;
      // Umfang
      g.drawString(localization.getString("report.circumference"), x1, y);
      g.drawString(Integer.toString(model.getWidth()), x2, y);
      y += dy;
      // Farbrapport
      g.drawString(localization.getString("report.colorrepeat"), x1, y);
      g.drawString(Integer.toString(model.getColorRepeat()) + " " + localization.getString("report.beads"), x2, y);
      y += dy;
      int colorRepeat = model.getColorRepeat();
      // Faedelliste...
      if (colorRepeat > 0) {
          int page = 1;
          int column = 0;
          g.drawString(localization.getString("report.listofbeads"), x1, y);
          y += dy;
          int ystart = y;
          byte col = model.get(colorRepeat - 1);
          int count = 1;
          for (int i = colorRepeat - 2; i >= 0; i--) {
              if (model.get(i) == col) {
                  count++;
              } else {
                  if (col != 0) {
                      g.setColor(model.getColor(col));
                      g.fillRect(x1, y, dx - mm2px(1, sx), dy - mm2py(1, sy));
                      g.setColor(Color.WHITE);
                      g.drawRect(x1, y, dx - mm2px(1, sx), dy - mm2py(1, sy));
                  } else {
                      g.setColor(Color.WHITE);
                      g.fillRect(x1, y, dx - mm2px(1, sx), dy - mm2py(1, sy));
                      g.setColor(Color.BLACK);
                      g.drawRect(x1, y, dx - mm2px(1, sx), dy - mm2py(1, sy));
                  }
                  g.setColor(Color.BLACK);
                  g.drawString(Integer.toString(count), x1 + dx + 3, y);
                  y += dy;
                  col = model.get(i);
                  count = 1;
              }
              if (y >= (int) pageFormat.getHeight() - mm2py(10, sy)) {
                  x1 += dx + mm2px(8, sx);
                  y = ystart;
                  column++;
                  if (column >= reportcols) { // neue Seite und weiter...
                      // TODO handle multipage output, sigh...
                      break;
                      // Printer().NewPage();
                      // x1 = draftleft;
                      // x2 = draftleft + MM2PRx(30, sx);
                      // y = MM2PRy(10, sy);
                      // reportcols = (Printer().PageWidth - draftleft - 10) /
                      // (MM2PRx(5, sx) + MM2PRx(8, sx));
                      // column = 0;
                      // page++;
                      // canvas.Pen.Color = clBlack;
                      // canvas.TextOut (x1, y,
                      // String(Language.STR("Pattern ",
                      // "Muster "))+savedialog.getSelectedFile().getName() +
                      // " - " + Language.STR("page ", "Seite ") +
                      // IntToStr(page));
                      // y += dy;
                      // ystart = y;
                  }
              }
          }
          if (y < (int) pageFormat.getHeight() - mm2py(10, sy)) {
              if (col != 0) {
                  g.setColor(model.getColor(col));
                  g.fillRect(x1, y, dx - mm2px(1, sx), dy - mm2py(1, sy));
                  g.setColor(Color.WHITE);
                  g.drawRect(x1, y, dx - mm2px(1, sx), dy - mm2py(1, sy));
              } else {
                  g.setColor(Color.WHITE);
                  g.fillRect(x1, y, dx - mm2px(1, sx), dy - mm2py(1, sy));
                  g.setColor(Color.BLACK);
                  g.drawRect(x1, y, dx - mm2px(1, sx), dy - mm2py(1, sy));
              }
              g.setColor(Color.BLACK);
              g.drawString(Integer.toString(count), x1 + dx + 3, y);
          }
      }
    }
    
    private int correctCoordinatesX(int i, int j) {
        int idx = i + (j + model.getScroll()) * model.getWidth();
        int m1 = model.getWidth();
        int m2 = model.getWidth() + 1;
        int k = 0;
        int m = (k % 2 == 0) ? m1 : m2;
        while (idx >= m) {
            idx -= m;
            k++;
            m = (k % 2 == 0) ? m1 : m2;
        }
        i = idx;
        j = k - model.getScroll();
        return i;
    }

    private int correctCoordinatesY(int i, int j) {
        int idx = i + (j + model.getScroll()) * model.getWidth();
        int m1 = model.getWidth();
        int m2 = model.getWidth() + 1;
        int k = 0;
        int m = (k % 2 == 0) ? m1 : m2;
        while (idx >= m) {
            idx -= m;
            k++;
            m = (k % 2 == 0) ? m1 : m2;
        }
        i = idx;
        j = k - model.getScroll();
        return j;
    }

    private int mm2px(int x, int sx) {
        return x * sx / 254;
    }

    private int mm2py(int y, int sy) {
        return y * sy / 254;
    }

}
