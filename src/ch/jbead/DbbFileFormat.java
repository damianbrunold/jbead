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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class DbbFileFormat implements FileFormat {

    private static final String MAGIC_FILE_HEADER = "DB-BEAD/01:\r\n";

    @Override
    public String getName() {
        return "DB-BEAD";
    }

    @Override
    public void save(Model model, BeadForm form, File destfile) throws IOException {
        JBeadOutputStream out = new JBeadOutputStream(new FileOutputStream(model.getFile()));
        try {
            out.write(MAGIC_FILE_HEADER);
            model.save(out);
            out.writeBool(form.isDraftVisible());
            out.writeBool(form.isCorrectedVisible());
            out.writeBool(form.isSimulationVisible());
            // report flag is not saved?!
            model.setModified(false);
        } finally {
            out.close();
        }
    }

    @Override
    public void load(Model model, BeadForm form, File srcfile) throws IOException {
        JBeadInputStream in = new JBeadInputStream(new FileInputStream(srcfile));
        try {
            String strid = in.read(13);
            if (!strid.equals(MAGIC_FILE_HEADER)) {
                throw new IOException(form.getString("invalidformat"));
            }
            form.clearSelection();
            model.clear();
            model.load(in, true);
            form.setDraftVisible(in.readBool());
            form.setCorrectedVisible(in.readBool());
            form.setSimulationVisible(in.readBool());
            form.setReportVisible(true);
        } finally {
            in.close();
        }
    }

}
