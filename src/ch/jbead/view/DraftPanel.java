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

package ch.jbead.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import ch.jbead.BeadPainter;
import ch.jbead.CoordinateCalculator;
import ch.jbead.JBeadFrame;
import ch.jbead.Model;
import ch.jbead.Point;
import ch.jbead.Selection;
import ch.jbead.SelectionListener;
import ch.jbead.ui.SymbolFont;

public class DraftPanel extends BasePanel implements SelectionListener, CoordinateCalculator {

    private static final long serialVersionUID = 1L;

    private static final int GAP = 6;
    private static final int MARKER_WIDTH = 30;

    private int offsetx;
    private int maxj;
    private Font defaultfont;
    private Font symbolfont;

    public DraftPanel(Model model, Selection selection, final JBeadFrame frame) {
        super(model, frame, selection);
        setBackground(Color.LIGHT_GRAY);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                handleMouseDown(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                handleMouseUp(e);
            }
        });
        addMouseMotionListener(new MouseMotionListener() {
            public void mouseDragged(MouseEvent e) {
                handleMouseMove(e);
            }
            public void mouseMoved(MouseEvent e) {
                // empty
            }
        });
    }

    private void draftLinePreview() {
        if (!view.getSelectedTool().equals("pencil")) return;
        if (!selection.isActive()) return;
        linePreview(selection.getOrigin(), selection.getLineDest());
    }

    private void drawPrepress() {
        if (view.getSelectedTool().equals("pencil")) {
            drawPrepress(selection.getOrigin());
        }
    }

    public void handleMouseDown(MouseEvent event) {
        if (view.isDragging()) return;
        Point pt = new Point(event.getX(), event.getY());
        if (event.getButton() == MouseEvent.BUTTON1) {
            pt = mouseToField(pt);
            if (pt == null) return;
            view.setDragging(true);
            selection.init(pt);
            drawPrepress();
            draftLinePreview();
        }
    }

    public void handleMouseMove(MouseEvent event) {
        Point pt = new Point(event.getX(), event.getY());
        if (view.isDragging()) {
            pt = mouseToField(pt);
            if (pt == null) return;
            draftLinePreview();
            selection.update(pt);
            draftLinePreview();
        }
    }

    private void handleMouseUp(MouseEvent event) {
        Point pt = new Point(event.getX(), event.getY());
        if (view.isDragging()) {
            pt = mouseToField(pt);
            if (pt == null) return;
            draftLinePreview();
            selection.update(pt);
            view.setDragging(false);
            String tool = view.getSelectedTool();
            if (tool.equals("pencil")) {
                if (!selection.isActive()) {
                    setPoint(selection.getOrigin());
                } else {
                    drawLine(selection.getOrigin(), selection.getLineDest());
                }
            } else if (tool.equals("fill")) {
                fillLine(selection.getOrigin());
            } else if (tool.equals("pipette")) {
                selectColorFrom(selection.getOrigin());
            } else if (tool.equals("select")) {
                if (!selection.isActive()) {
                    setPoint(selection.getOrigin());
                }
            }
        }
    }

    private void selectColorFrom(Point pt) {
        byte colorIndex = model.get(pt.scrolled(model.getScroll()));
        view.selectColor(colorIndex);
    }

    private void drawLine(Point begin, Point end) {
        model.drawLine(begin, end);
    }

    private void fillLine(Point pt) {
        model.fillLine(pt);
    }

    private void setPoint(Point pt) {
        model.setPoint(pt);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        setHints(g);
        defaultfont = g.getFont();
        symbolfont = SymbolFont.get(gridy);
        offsetx = getOffsetX();
        maxj = getMaxJ();
        paintBeads(g);
        paintMarkers(g);
        if (selection.isNormal()) {
            paintSelection(g, Color.RED, selection);
        }
    }

    private void setHints(Graphics g) {
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(model.getWidth() * gridx + MARKER_WIDTH + GAP, 3 * gridy);
    }

    @Override
    public Dimension getPreferredSize() {
        return getMinimumSize();
    }

    @Override
    public Dimension getMaximumSize() {
        return getMinimumSize();
    }

    private int getOffsetX() {
        return Math.max(3 + MARKER_WIDTH + GAP, (getWidth() - model.getWidth() * gridx - 1) / 2);
    }

    private int getMaxJ() {
        return Math.min(model.getHeight() - scroll, getHeight() / gridy + 1);
    }

    public int x(Point pt) {
        return offsetx + pt.getX() * gridx;
    }

    public int y(Point pt) {
        return getHeight() - 1 - (pt.getY() + 1) * gridy;
    }

    private int y(int j) {
        return y(new Point(0, j));
    }

    private void paintBeads(Graphics g) {
        BeadPainter painter = new BeadPainter(this, model, view, symbolfont);
        for (int j = 0; j < maxj; j++) {
            for (int i = 0; i < model.getWidth(); i++) {
                byte c = model.get(new Point(i, j).scrolled(scroll));
                painter.paint(g, new Point(i, j), c);
            }
        }
    }

    private void paintMarkers(Graphics g) {
        g.setFont(defaultfont);
        g.setColor(Color.DARK_GRAY);
        setHints(g);
        int fontHeight = g.getFontMetrics().getAscent();
        for (int j = 0; j < maxj; j++) {
            if (((j + scroll) % 10) == 0) {
                g.drawLine(offsetx - GAP - MARKER_WIDTH, y(j) + gridy, offsetx - GAP, y(j) + gridy);
                String label = Integer.toString(j + scroll);
                int labelWidth = g.getFontMetrics().stringWidth(label);
                g.drawString(label, offsetx - GAP - MARKER_WIDTH + (MARKER_WIDTH - labelWidth) / 2, y(j) + gridy + fontHeight + 1);
            }
        }
    }

    public void drawSelection(Selection sel) {
        if (!sel.isNormal()) return;
        Graphics g = getGraphics();
        paintSelection(g, Color.RED, sel);
        g.dispose();
    }

    public void clearSelection(Selection sel) {
        if (!sel.isNormal()) return;
        Graphics g = getGraphics();
        paintSelection(g, Color.DARK_GRAY, sel);
        g.dispose();
    }

    private void paintSelection(Graphics g, Color color, Selection sel) {
        g.setColor(color);
        g.drawRect(x(sel.getBegin()), y(sel.getEnd()), sel.width() * gridx, sel.height() * gridy);
    }

    public void redraw(int i, int j) {
        if (!isVisible()) return;
        byte c = model.get(new Point(i, j).scrolled(model.getScroll()));
        BeadPainter painter = new BeadPainter(this, model, view, symbolfont);
        Graphics g = getGraphics();
        setHints(g);
        painter.paint(g, new Point(i, j), c);
        g.dispose();
    }

    @Override
    public void redraw(Point pt) {
        redraw(pt.getX(), pt.getY() - scroll);
    }

    public void selectPreview(boolean draw, Point pt1, Point pt2) {
        Graphics g = getGraphics();
        g.setColor(draw ? Color.RED : Color.DARK_GRAY);
        g.drawRect(x(pt1), y(pt2), (pt2.getX() - pt1.getX() + 1) * gridx, (pt2.getY() - pt1.getY() + 1) * gridy);
        g.dispose();
    }

    public void linePreview(Point pt1, Point pt2) {
        Graphics g = getGraphics();
        g.setColor(Color.WHITE);
        g.setXORMode(Color.BLACK);
        g.drawLine(x(pt1) + gridx / 2, y(pt1.getY()) + gridy / 2, x(pt2) + gridx / 2, y(pt2.getY()) + gridy / 2);
        g.dispose();
    }

    public void drawPrepress(Point pt) {
        Graphics g = getGraphics();
        int x0 = x(pt);
        int y0 = y(pt);
        g.setColor(Color.BLACK);
        g.drawLine(x0 + 1, y0 + gridy - 1, x0 + 1, y0 + 1);
        g.drawLine(x0 + 1, y0 + 1, x0 + gridx - 1, y0 + 1);
        g.setColor(Color.WHITE);
        g.drawLine(x0 + 1 + 1, y0 + gridy - 1, x0 + gridx - 1, y0 + gridy - 1);
        g.drawLine(x0 + gridx - 1, y0 + gridy - 1, x0 + gridx - 1, y0 + 1);
        g.dispose();
    }

    public Point mouseToField(Point pt) {
        int _i = pt.getX();
        int _j = pt.getY();
        int i, jj;
        if (_i < offsetx || _i > offsetx + model.getWidth() * gridx) return null;
        i = (_i - offsetx) / gridy;
        if (i >= model.getWidth()) return null;
        jj = (getHeight() - _j) / gridy;
        return new Point(i, jj);
    }

    public void selectionUpdated(Selection before, Selection current) {
        clearSelection(before);
        drawSelection(current);
    }

    public void selectionDeleted(Selection sel) {
        clearSelection(sel);
    }

}
