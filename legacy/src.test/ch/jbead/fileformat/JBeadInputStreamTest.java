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

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;

import junit.framework.TestCase;

public class JBeadInputStreamTest extends TestCase {

    public void testReadInt() throws IOException {
        ByteArrayInputStream data = new ByteArrayInputStream(new byte[] {(byte) 0xf4, (byte) 0xf3, 2, 1});
        JBeadInputStream stream = new JBeadInputStream(data);
        assertEquals(0x0102f3f4, stream.readInt());
    }

    public void testReadByte() throws IOException {
        ByteArrayInputStream data = new ByteArrayInputStream(new byte[] {1});
        JBeadInputStream stream = new JBeadInputStream(data);
        assertEquals(1, stream.read());
    }

    public void testReadByteEndOfFile() throws IOException {
        ByteArrayInputStream data = new ByteArrayInputStream(new byte[] {});
        JBeadInputStream stream = new JBeadInputStream(data);
        try {
            stream.read();
            fail();
        } catch (EOFException e) {
            // expected
        }
    }

    public void testReadBytes() throws IOException {
        ByteArrayInputStream data = new ByteArrayInputStream(new byte[] {1, 2, 3, 4});
        JBeadInputStream stream = new JBeadInputStream(data);
        byte[] result = new byte[3];
        stream.read(result);
        assertEquals(1, result[0]);
        assertEquals(2, result[1]);
        assertEquals(3, result[2]);
    }

    public void testReadBytesOffsetAndSize() throws IOException {
        ByteArrayInputStream data = new ByteArrayInputStream(new byte[] {1});
        JBeadInputStream stream = new JBeadInputStream(data);
        byte[] result = new byte[3];
        stream.read(result, 1, 1);
        assertEquals(0, result[0]);
        assertEquals(1, result[1]);
        assertEquals(0, result[2]);
    }

    public void testReadString() throws IOException {
        ByteArrayInputStream data = new ByteArrayInputStream(new byte[] {'h', 'a', 'l', 'l', 'o'});
        JBeadInputStream stream = new JBeadInputStream(data);
        assertEquals("hallo", stream.read(5));
    }

    public void testReadStringUTF8() throws IOException {
        // tests the decoding of utf-8 encoded data
        ByteArrayInputStream data = new ByteArrayInputStream(new byte[] {-61, -115});
        JBeadInputStream stream = new JBeadInputStream(data);
        assertEquals("\u00cd", stream.read(2));
    }

    public void testReadColor() throws IOException {
        ByteArrayInputStream data = new ByteArrayInputStream(new byte[] {1, 2, 3, 4});
        JBeadInputStream stream = new JBeadInputStream(data);
        Color color = stream.readColor();
        assertEquals(1, color.getRed());
        assertEquals(2, color.getGreen());
        assertEquals(3, color.getBlue());
        assertEquals(255, color.getAlpha());
    }

    public void testReadBackgroundColor() throws IOException {
        ByteArrayInputStream data = new ByteArrayInputStream(new byte[] {15, 0, 0, (byte) 128});
        JBeadInputStream stream = new JBeadInputStream(data);
        Color color = stream.readBackgroundColor();
        assertEquals(240, color.getRed());
        assertEquals(240, color.getGreen());
        assertEquals(240, color.getBlue());
    }

    public void testReadBackgroundColorNomatch() throws IOException {
        ByteArrayInputStream data = new ByteArrayInputStream(new byte[] {1, 2, 3, 4});
        JBeadInputStream stream = new JBeadInputStream(data);
        Color color = stream.readBackgroundColor();
        assertEquals(1, color.getRed());
        assertEquals(2, color.getGreen());
        assertEquals(3, color.getBlue());
    }

    public void testReadBool() throws IOException {
        ByteArrayInputStream data = new ByteArrayInputStream(new byte[] {1, 0});
        JBeadInputStream stream = new JBeadInputStream(data);
        assertTrue(stream.readBool());
        assertFalse(stream.readBool());
    }

    public void testReadAll() throws IOException {
        ByteArrayInputStream data = new ByteArrayInputStream(new byte[] {'h', 'e', 'l', 'l', 'o', '\n', 'w', 'o', 'r', 'l', 'd'});
        JBeadInputStream stream = new JBeadInputStream(data);
        assertEquals("hello\nworld", stream.readAll());
    }

    public static class FakeInputStream extends ByteArrayInputStream {
        private boolean isClosed = false;

        public FakeInputStream(byte[] buf) {
            super(buf);
        }

        @Override
        public void close() throws IOException {
            super.close();
            isClosed = true;
        }

        public boolean isClosed() {
            return isClosed;
        }
    }

    public void testClose() throws IOException {
        FakeInputStream data = new FakeInputStream(new byte[] {1});
        JBeadInputStream stream = new JBeadInputStream(data);
        assertFalse(data.isClosed());
        stream.close();
        assertTrue(data.isClosed());
    }

}
