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
import java.io.IOException;
import java.io.InputStream;

/**
 * 
 */
public class JBeadInputStream {

    private InputStream in;
    
    public JBeadInputStream(InputStream in) {
        this.in = in;
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
        // TODO verify the order of the components
        int alpha = in.read();
        int red = in.read();
        int green = in.read();
        int blue = in.read();
        return new Color(red, green, blue, alpha);
    }
    
    public boolean readBool() throws IOException {
        return in.read() == 1;
    }
}
