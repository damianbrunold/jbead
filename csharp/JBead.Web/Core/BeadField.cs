namespace JBead.Web.Core;

public class BeadField
{
    public const int DefaultWidth = 15;
    // Was 800 in the Java original to pre-allocate a large canvas. In the web version
    // rows translate to mounted BeadPoint components, so start smaller and let the user
    // grow via toolbar buttons.
    public const int DefaultHeight = 100;

    private byte[] cells = new byte[DefaultWidth * DefaultHeight];
    private int width;
    private int height;

    public BeadField()
    {
        width = DefaultWidth;
        height = DefaultHeight;
        Clear();
    }

    public int Width => width;
    public int Height => height;
    public int LastIndex => width * height - 1;
    public byte[] RawData => cells;

    public Rect FullRect => new(new Point(0, 0), new Point(width - 1, height - 1));
    public Rect GetRect(int startY, int endY) => new(new Point(0, startY), new Point(width - 1, endY));

    public bool IsValidIndex(int idx) => idx >= 0 && idx <= LastIndex;

    public void Clear() => Array.Clear(cells, 0, cells.Length);

    public void SetWidth(int newWidth)
    {
        byte[] next = new byte[newWidth * height];
        for (int j = 0; j < height; j++)
        {
            Array.Copy(cells, j * width, next, j * newWidth, Math.Min(newWidth, width));
        }
        cells = next;
        width = newWidth;
    }

    public void SetHeight(int newHeight)
    {
        if (height == newHeight) {
			return;
		}
		byte[] next = new byte[width * newHeight];
        Array.Copy(cells, 0, next, 0, width * Math.Min(height, newHeight));
        height = newHeight;
        cells = next;
    }

    public void CopyFrom(BeadField source)
    {
        SetWidth(source.Width);
        SetHeight(source.Height);
        Array.Copy(source.cells, cells, width * height);
    }

    public byte Get(Point p) => cells[GetIndex(p)];
    public byte Get(int index) => cells[index];
    public void Set(Point p, byte value) => cells[GetIndex(p)] = value;
    public void Set(int index, byte value) => cells[index] = value;

    public int GetIndex(Point p) => p.X + width * p.Y;
    public Point GetPoint(int index) => new(index % width, index / width);

    public void Swap(Point a, Point b)
    {
        byte t = Get(a);
        Set(a, Get(b));
        Set(b, t);
    }

    public void MirrorHorizontal(Rect rect)
    {
        for (int j = rect.Bottom; j <= rect.Top; j++)
        {
            for (int i = rect.Left; i <= (rect.Left + rect.Right) / 2; i++)
            {
                Swap(new Point(i, j), new Point(rect.Right - (i - rect.Left), j));
            }
        }
    }

    public void MirrorVertical(Rect rect)
    {
        for (int i = rect.Left; i <= rect.Right; i++)
        {
            for (int j = rect.Bottom; j <= (rect.Bottom + rect.Top) / 2; j++)
            {
                Swap(new Point(i, j), new Point(i, rect.Top - (j - rect.Bottom)));
            }
        }
    }

    public byte[] CopyOf(Rect rect)
    {
        byte[] data = new byte[rect.Size];
        for (int j = 0; j < rect.Height; j++)
        {
            for (int i = 0; i < rect.Width; i++)
            {
                data[j * rect.Width + i] = Get(new Point(rect.Left + i, rect.Bottom + j));
            }
        }
        return data;
    }

    public void Rotate(Rect rect)
    {
        if (!rect.IsSquare) {
			return;
		}
		byte[] buffer = CopyOf(rect);
        for (int j = 0; j < rect.Height; j++)
        {
            for (int i = 0; i < rect.Width; i++)
            {
                int x = j;
                int y = rect.Height - 1 - i;
                Set(new Point(rect.Left + x, rect.Bottom + y), buffer[j * rect.Width + i]);
            }
        }
    }

    public void Delete(Rect rect)
    {
        foreach (var p in rect) {
			Set(p, 0);
		}
	}

    public void InsertRow()
    {
        for (int j = height - 1; j > 0; j--)
        {
            for (int i = 0; i < width; i++)
            {
                var p = new Point(i, j);
                Set(p, Get(p.NextBelow()));
            }
        }
        for (int i = 0; i < width; i++) {
			Set(new Point(i, 0), 0);
		}
	}

    public void DeleteRow()
    {
        for (int j = 0; j < height - 1; j++)
        {
            for (int i = 0; i < width; i++)
            {
                var p = new Point(i, j);
                Set(p, Get(p.NextAbove()));
            }
        }
        for (int i = 0; i < width; i++) {
			Set(new Point(i, height - 1), 0);
		}
	}

    public void LoadRawData(int newWidth, int newHeight, byte[] data)
    {
        width = newWidth;
        height = newHeight;
        cells = data;
    }

    public void Replace(byte oldColor, byte newColor)
    {
        for (int i = 0; i < cells.Length; i++)
        {
            if (cells[i] == oldColor) {
				cells[i] = newColor;
			}
		}
    }
}
