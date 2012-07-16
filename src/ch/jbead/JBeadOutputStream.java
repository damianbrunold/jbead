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

import java.awt.Color;
import java.io.IOException;
import java.io.OutputStream;

/**
 * 
 */
public class JBeadOutputStream {

    private OutputStream out;

    public JBeadOutputStream(OutputStream out) {
        this.out = out;
    }

    public void writeInt(int value) throws IOException {
        byte b1 = (byte) (value & 0xff);
        byte b2 = (byte) ((value >> 8) & 0xff);
        byte b3 = (byte) ((value >> 16) & 0xff);
        byte b4 = (byte) ((value >> 24) & 0xff);
        out.write(b1);
        out.write(b2);
        out.write(b3);
        out.write(b4);
    }
    
    public void write(int value) throws IOException {
        out.write(value);
    }

    public void write(byte[] bytes, int off, int length) throws IOException {
        out.write(bytes, off, length);
    }

    public void write(byte[] bytes) throws IOException {
        out.write(bytes);
    }

    public void write(String str) throws IOException {
        out.write(str.getBytes("UTF-8"));
    }

    public void writeColor(Color color) throws IOException {
        out.write(color.getRed());
        out.write(color.getGreen());
        out.write(color.getBlue());
        out.write(0);
    }
    
    public void writeBool(boolean value) throws IOException {
        out.write(value ? 1 : 0);
    }

    public void close() throws IOException {
        out.close();
    }

}
