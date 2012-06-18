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

import java.awt.Color;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

public class JBeadInputStream {

    private InputStream in;

    public JBeadInputStream(InputStream in) {
        this.in = in;
    }

    public String readAll() throws IOException {
        StringBuilder result = new StringBuilder();
        byte[] buffer = new byte[1024];
        int read = in.read(buffer);
        while (read != -1) {
            result.append(new String(buffer, 0, read, "UTF-8"));
            read = in.read(buffer);
        }
        return result.toString();
    }

    public byte read() throws IOException {
        int b = in.read();
        if (b == -1) throw new EOFException();
        return (byte) b;
    }

    public int readInt() throws IOException {
        int b1 = in.read();
        int b2 = in.read();
        int b3 = in.read();
        int b4 = in.read();
        return (b4 << 24) | (b3 << 16) | (b2 << 8) | b1;
    }

    public void read(byte[] bytes, int off, int length) throws IOException {
        in.read(bytes, off, length);
    }

    public void read(byte[] bytes) throws IOException {
        in.read(bytes);
    }

    public String read(int length) throws IOException {
        byte[] buffer = new byte[length];
        in.read(buffer);
        return new String(buffer, "UTF-8");
    }

    public Color readColor() throws IOException {
        int red = in.read();
        int green = in.read();
        int blue = in.read();
        @SuppressWarnings("unused")
        int alpha = in.read();
        return new Color(red, green, blue);
    }

    /**
     * For backwards compatibility with db-bead, we treat the first
     * color, the background color, differently.
     */
    public Color readBackgroundColor() throws IOException {
        int red = in.read();
        int green = in.read();
        int blue = in.read();
        int alpha = in.read();
        if (red == 15 && green == 0 && blue == 0 && alpha == 128) {
            return new Color(240, 240, 240);
        } else {
            return new Color(red, green, blue);
        }
    }

    public boolean readBool() throws IOException {
        return in.read() != 0;
    }

    public void close() throws IOException {
        in.close();
    }

}
