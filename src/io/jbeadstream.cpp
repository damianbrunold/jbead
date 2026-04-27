#include "jbeadstream.h"

#include <stdexcept>

namespace jbead {

quint8 JBeadInputStream::readByte()
{
    char ch;
    if (m_device->getChar(&ch)) return static_cast<quint8>(ch);
    throw std::runtime_error("unexpected end of file");
}

qint32 JBeadInputStream::readInt()
{
    const quint8 b1 = readByte();
    const quint8 b2 = readByte();
    const quint8 b3 = readByte();
    const quint8 b4 = readByte();
    return static_cast<qint32>(
        (quint32(b4) << 24) | (quint32(b3) << 16) |
        (quint32(b2) << 8)  |  quint32(b1));
}

bool JBeadInputStream::readBool() { return readByte() != 0; }

QByteArray JBeadInputStream::read(qint64 length)
{
    QByteArray out;
    out.resize(int(length));
    qint64 got = 0;
    while (got < length) {
        const qint64 n = m_device->read(out.data() + got, length - got);
        if (n <= 0) throw std::runtime_error("unexpected end of file");
        got += n;
    }
    return out;
}

QString JBeadInputStream::readString(qint64 length)
{
    return QString::fromUtf8(read(length));
}

QString JBeadInputStream::readAll()
{
    return QString::fromUtf8(m_device->readAll());
}

QColor JBeadInputStream::readColor()
{
    const quint8 r = readByte();
    const quint8 g = readByte();
    const quint8 b = readByte();
    /*  alpha byte is read and discarded — legacy returns
        Color(r,g,b) which yields opaque alpha 255.                */
    (void) readByte();
    return QColor(r, g, b);
}

QColor JBeadInputStream::readBackgroundColor()
{
    const quint8 r = readByte();
    const quint8 g = readByte();
    const quint8 b = readByte();
    const quint8 a = readByte();
    /*  Legacy DB-BEAD magic for "default white background": (15, 0,
        0, 128). Mapped to neutral light-grey so the rendered field
        doesn't show the sentinel color. Anything else passes
        through as the literal RGB triplet (alpha discarded).      */
    if (r == 15 && g == 0 && b == 0 && a == 128) return QColor(240, 240, 240);
    return QColor(r, g, b);
}

// ---- output -------------------------------------------------------

void JBeadOutputStream::write(quint8 value)
{
    const char ch = static_cast<char>(value);
    if (m_device->write(&ch, 1) != 1) throw std::runtime_error("write failed");
}

void JBeadOutputStream::write(const QByteArray& bytes)
{
    if (m_device->write(bytes) != bytes.size()) throw std::runtime_error("write failed");
}

void JBeadOutputStream::write(const QString& s)
{
    write(s.toUtf8());
}

void JBeadOutputStream::writeInt(qint32 value)
{
    write(quint8( value        & 0xff));
    write(quint8((value >>  8) & 0xff));
    write(quint8((value >> 16) & 0xff));
    write(quint8((value >> 24) & 0xff));
}

void JBeadOutputStream::writeBool(bool value)
{
    write(quint8(value ? 1 : 0));
}

void JBeadOutputStream::writeColor(const QColor& color)
{
    write(quint8(color.red()));
    write(quint8(color.green()));
    write(quint8(color.blue()));
    /*  alpha always written as 0. Matches legacy
        JBeadOutputStream.writeColor — historical quirk; readers
        ignore the alpha byte anyway.                              */
    write(quint8(0));
}

} // namespace jbead
