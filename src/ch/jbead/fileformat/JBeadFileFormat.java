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

package ch.jbead.fileformat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.filechooser.FileFilter;

import ch.jbead.JBeadFrame;
import ch.jbead.Model;

public class JBeadFileFormat implements FileFormat {

    public static final String EXTENSION = ".jbb";

    public String getName() {
        return "jbead";
    }

    public void save(Model model, JBeadFrame form, File destfile) throws IOException {
        JBeadOutputStream out = new JBeadOutputStream(new FileOutputStream(destfile));
        try {
            Memento memento = new JBeadMemento();
            model.saveTo(memento);
            form.saveTo(memento);
            memento.save(out);
            model.setModified(false);
        } finally {
            out.close();
        }
    }

    public void load(Model model, JBeadFrame form, File srcfile) throws IOException {
        JBeadInputStream in = new JBeadInputStream(new FileInputStream(srcfile));
        try {
            Memento memento = new JBeadMemento();
            memento.load(in);
            form.clearSelection();
            model.clear();
            model.loadFrom(memento);
            form.loadFrom(memento);
        } finally {
            in.close();
        }
    }

    public FileFilter getFileFilter() {
        return new JbbFileFilter();
    }

    public String getExtension() {
        return EXTENSION;
    }

}
