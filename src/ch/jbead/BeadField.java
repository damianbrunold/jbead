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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * 
 */
public class BeadField {
	private static final int SIZE = 25 * 1000;
	private static final int SAVEZONE = 100;

	private byte[] field = new byte[SIZE + SAVEZONE];
	private int width;
	private int height;
	private int shift; // 0 - (width-1) Drehung nach rechts

	public BeadField() {
		SetWidth(15);
		Clear();
	}

	public int Width() {
		return width;
	}

	public int Height() {
		return height;
	}

	public int LastIndex() {
		return width * height - 1;
	}

	public boolean ValidIndex(int idx) {
		return idx >= 0 && idx <= LastIndex();
	}

	public void Clear() {
		for (int i = 0; i < SIZE; i++) {
			field[i] = 0;
		}
		if (Debug.ENABLED) {
			for (int i = SIZE; i < SIZE + SAVEZONE; i++) {
				field[i] = Byte.MAX_VALUE;
			}
		}
	}

	public void SetWidth(int _width) {
		assert (_width > 0 && _width < SIZE / 10);
		width = _width;
		height = SIZE / width;
		assert (width > 0 && height > 0);
	}

	public byte Get(int _x, int _y) {
		assert (width > 0 && height > 0);
		assert (_x < width);
		assert (_y < height);
		assert (_x + width * _y < SIZE);
		byte c = field[_x + width * _y];
		assert (c != Byte.MAX_VALUE);
		return c;
	}

	public byte Get(int _idx) {
		assert (width > 0);
		assert (_idx >= 0 && _idx < width * height);
		int i = _idx % width;
		int j = _idx / width;
		return Get(i, j);
	}

	public void Set(int _x, int _y, byte _data) {
		assert (width > 0 && height > 0);
		assert (_x < width);
		assert (_y < height);
		assert (_x + width * _y < SIZE);
		field[_x + width * _y] = _data;
		if (Debug.ENABLED) {
			for (int i = SIZE; i < SIZE + SAVEZONE; i++) {
				assert (field[i] == Byte.MAX_VALUE);
			}
		}
	}

	public void Set(int _idx, byte _data) {
		assert (width > 0);
		assert (_idx >= 0 && _idx < width * height);
		int i = _idx % width;
		int j = _idx / width;
		Set(i, j, _data);
	}

	public byte RawGet(int _idx) {
		assert (_idx < width * height);
		return field[_idx];
	}

	public void RawSet(int _idx, byte _data) {
		assert (_idx < width * height);
		field[_idx] = _data;
	}

	public void CopyFrom(BeadField _source) {
		SetWidth(_source.Width());
		for (int i = 0; i < width * height; i++) {
			RawSet(i, _source.RawGet(i));
		}
	}

	public void Save(ObjectOutputStream _f) throws IOException {
		// FIXME verify endianness issues
		_f.writeInt(width);
		_f.write(field, 0, SIZE);
	}

	public void Load(ObjectInputStream _f) throws IOException {
		// FIXME verify endianness issues
		width = _f.readInt();
		_f.read(field, 0, SIZE);
		SetWidth(width);
	}

	public void InsertLine() {
		for (int j = Height() - 1; j > 0; j--) {
			for (int i = 0; i < Width(); i++) {
				Set(i, j, Get(i, j - 1));
			}
		}
		for (int i = 0; i < Width(); i++) {
			Set(i, 0, (byte) 0);
		}
	}

	public void DeleteLine() {
		for (int j = 0; j < Height() - 1; j++) {
			for (int i = 0; i < Width(); i++) {
				Set(i, j, Get(i, j + 1));
			}
		}
		for (int i = 0; i < Width(); i++) {
			Set(i, Height() - 1, (byte) 0);
		}
	}

}
