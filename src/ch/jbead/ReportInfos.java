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

import java.awt.FontMetrics;
import java.awt.Graphics;
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
        addInfo("report.circumference", model.getWidth());
        addInfo("report.colorrepeat", model.getRepeat() + " " + localization.getString("report.beads"));
        if (model.getRepeat() % model.getWidth() == 0) {
            addInfo("report.rowsperrepeat", model.getRepeat() / model.getWidth());
        } else {
            addInfo("report.rowsperrepeat",
                    Integer.toString(model.getRepeat() / model.getWidth()) + " " +
                    localization.getString("report.remainder") + " " +
                    Integer.toString(model.getRepeat() % model.getWidth()) + " " +
                    localization.getString("report.beads"));
        }
        addInfo("report.numberofrows", model.getUsedHeight());
        addInfo("report.numberofbeads", Integer.toString(model.getUsedHeight() * model.getWidth()) + " " + localization.getString("report.beads"));
    }

    private void addInfo(String labelKey, Object info) {
        infos.put(localization.getString(labelKey), info.toString());
    }

    @Override
    public Iterator<String> iterator() {
        return infos.keySet().iterator();
    }

    public String getInfo(String label) {
        return infos.get(label);
    }

    public int getMaxLabelWidth(Graphics g) {
        FontMetrics metrics = g.getFontMetrics();
        int maxwidth = 0;
        for (String label : infos.keySet()) {
            maxwidth = Math.max(maxwidth, metrics.stringWidth(label));
        }
        return maxwidth;
    }

    public int getMaxInfoWidth(Graphics g) {
        FontMetrics metrics = g.getFontMetrics();
        int maxwidth = 0;
        for (String label : infos.keySet()) {
            maxwidth = Math.max(maxwidth, metrics.stringWidth(infos.get(label)));
        }
        return maxwidth;
    }

    public int getWidth(Graphics g) {
        FontMetrics metrics = g.getFontMetrics();
        return getMaxLabelWidth(g) + metrics.stringWidth(" ") + getMaxInfoWidth(g);
    }

    public int getHeight(Graphics g) {
        FontMetrics metrics = g.getFontMetrics();
        return infos.size() * metrics.getHeight();
    }

}
