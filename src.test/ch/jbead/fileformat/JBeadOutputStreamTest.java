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
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import junit.framework.TestCase;

public class JBeadOutputStreamTest extends TestCase {

    public void testWriteInt() throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        JBeadOutputStream stream = new JBeadOutputStream(result);
        stream.writeInt(0x0102f3f4);
        byte[] bytes = result.toByteArray();
        assertEquals(4, bytes.length);
        assertEquals((byte) 0xf4, bytes[0]);
        assertEquals((byte) 0xf3, bytes[1]);
        assertEquals((byte) 0x02, bytes[2]);
        assertEquals((byte) 0x01, bytes[3]);
    }

    public void testWriteByte() throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        JBeadOutputStream stream = new JBeadOutputStream(result);
        stream.write(0x12);
        byte[] bytes = result.toByteArray();
        assertEquals(1, bytes.length);
        assertEquals((byte) 0x12, bytes[0]);
    }

    public void testWriteStr() throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        JBeadOutputStream stream = new JBeadOutputStream(result);
        stream.write("abc\u00cd");
        byte[] bytes = result.toByteArray();
        assertEquals(5, bytes.length);
        assertEquals((byte) 'a', bytes[0]);
        assertEquals((byte) 'b', bytes[1]);
        assertEquals((byte) 'c', bytes[2]);
        // must be utf-8 encoded
        assertEquals((byte) -61, bytes[3]);
        assertEquals((byte) -115, bytes[4]);
    }

    public void testWriteTrue() throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        JBeadOutputStream stream = new JBeadOutputStream(result);
        stream.writeBool(true);
        byte[] bytes = result.toByteArray();
        assertEquals(1, bytes.length);
        assertEquals((byte) 0x1, bytes[0]);
    }

    public void testWriteFalse() throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        JBeadOutputStream stream = new JBeadOutputStream(result);
        stream.writeBool(false);
        byte[] bytes = result.toByteArray();
        assertEquals(1, bytes.length);
        assertEquals((byte) 0x0, bytes[0]);
    }

    public void testWriteColor() throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        JBeadOutputStream stream = new JBeadOutputStream(result);
        stream.writeColor(new Color(1, 2, 255, 128));
        byte[] bytes = result.toByteArray();
        assertEquals(4, bytes.length);
        assertEquals((byte) 1, bytes[0]);
        assertEquals((byte) 2, bytes[1]);
        assertEquals((byte) 255, bytes[2]);
        assertEquals((byte) 0, bytes[3]);
    }

    public void testWriteBytes() throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        JBeadOutputStream stream = new JBeadOutputStream(result);
        stream.write(new byte[] {1, 2, 3});
        byte[] bytes = result.toByteArray();
        assertEquals(3, bytes.length);
        assertEquals((byte) 1, bytes[0]);
        assertEquals((byte) 2, bytes[1]);
        assertEquals((byte) 3, bytes[2]);
    }

    public void testWriteBytesOffsetCount() throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        JBeadOutputStream stream = new JBeadOutputStream(result);
        stream.write(new byte[] {1, 2, 3}, 1, 1);
        byte[] bytes = result.toByteArray();
        assertEquals(1, bytes.length);
        assertEquals((byte) 2, bytes[0]);
    }

    public static class FakeOutputStream extends ByteArrayOutputStream {
        private boolean isClosed = false;

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
        FakeOutputStream result = new FakeOutputStream();
        JBeadOutputStream stream = new JBeadOutputStream(result);
        assertFalse(result.isClosed());
        stream.close();
        assertTrue(result.isClosed());
    }

}
