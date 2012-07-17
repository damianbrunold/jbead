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

package ch.jbead;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.font.FontRenderContext;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class ReportInfos implements Iterable<String> {

    private Model model;
    private Localization localization;
    private Map<String, String> infos = new LinkedHashMap<String, String>();

    public ReportInfos(Model model, Localization localization) {
        this.model = model;
        this.localization = localization;
        addInfos();
    }

    private void addInfos() {
        addInfo("report.pattern", model.getFile().getName());
        if (model.getAuthor().length() > 0) {
            addInfo("report.author", model.getAuthor());
        }
        addInfo("report.circumference", model.getWidth());
        addInfo("report.colorrepeat", model.getRepeat() + " " + localization.getString("report.beads"));
        if (model.getRepeat() % model.getWidth() == 0) {
            addInfo("report.rowsperrepeat", model.getRepeat() / model.getWidth());
        } else {
            int rows = model.getRepeat() / model.getWidth();
            int beads = model.getRepeat() % model.getWidth();
            addInfo("report.rowsperrepeat",
                    rows + " " + getRowsLabel(rows) + " " +
                    beads + " " + getBeadsLabel(beads));
        }
        addInfo("report.numberofrows", model.getUsedHeight());
        addInfo("report.numberofbeads", Integer.toString(model.getUsedHeight() * model.getWidth()) + " " + localization.getString("report.beads"));
    }

    private String getRowsLabel(int rows) {
        if (rows == 1) {
            return localization.getString("report.row");
        } else {
            return localization.getString("report.rows");
        }
    }

    private String getBeadsLabel(int beads) {
        if (beads == 1) {
            return localization.getString("report.bead");
        } else {
            return localization.getString("report.beads");
        }
    }

    private void addInfo(String labelKey, Object info) {
        infos.put(localization.getString(labelKey), info.toString());
    }

    public Iterator<String> iterator() {
        return infos.keySet().iterator();
    }

    public String getInfo(String label) {
        return infos.get(label);
    }

    public int getLineCount() {
        return infos.size();
    }

    public int getMaxLabelWidth(FontMetrics metrics) {
        int maxwidth = 0;
        for (String label : infos.keySet()) {
            maxwidth = Math.max(maxwidth, metrics.stringWidth(label));
        }
        return maxwidth;
    }

    public int getMaxInfoWidth(FontMetrics metrics) {
        int maxwidth = 0;
        for (String label : infos.keySet()) {
            maxwidth = Math.max(maxwidth, metrics.stringWidth(infos.get(label)));
        }
        return maxwidth;
    }

    public int getWidth(FontMetrics metrics) {
        return getMaxLabelWidth(metrics) + metrics.stringWidth(" ") + getMaxInfoWidth(metrics);
    }

    public int getMaxLabelWidth(Font font, FontRenderContext context) {
        int maxwidth = 0;
        for (String label : infos.keySet()) {
            maxwidth = Math.max(maxwidth, (int) font.getStringBounds(label, context).getWidth());
        }
        return maxwidth;
    }

    public int getMaxInfoWidth(Font font, FontRenderContext context) {
        int maxwidth = 0;
        for (String label : infos.keySet()) {
            maxwidth = Math.max(maxwidth, (int) font.getStringBounds(infos.get(label), context).getWidth());
        }
        return maxwidth;
    }

    public int getWidth(Font font, FontRenderContext context) {
        return getMaxLabelWidth(font, context) + (int) font.getStringBounds(" ", context).getWidth() + getMaxInfoWidth(font, context);
    }

    public int getHeight(int fontSize) {
        return infos.size() * fontSize;
    }

}
