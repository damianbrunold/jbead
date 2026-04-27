#pragma once

#include <QByteArray>
#include <QColor>
#include <QIODevice>
#include <QString>

#include <cstdint>

namespace jbead {

/*  Low-level binary readers/writers used by the .dbb format and by
    the .jbb text reader (which reads the whole file as a single
    UTF-8 string). Faithful port of legacy JBeadInputStream /
    JBeadOutputStream:

      - readInt / writeInt are 4-byte little-endian.
      - readColor reads RGBA but returns alpha=255; writeColor
        always writes alpha=0 (matches legacy bug-compatibly).
      - readBackgroundColor maps the legacy DB-BEAD magic
        (R=15,G=0,B=0,A=128) to a light-grey UI background.        */

class JBeadInputStream
{
public:
    explicit JBeadInputStream(QIODevice* device) : m_device(device) {}

    quint8     readByte();
    qint32     readInt();
    bool       readBool();
    QByteArray read(qint64 length);
    QString    readString(qint64 length);
    QString    readAll();
    QColor     readColor();
    QColor     readBackgroundColor();

    QIODevice* device() const { return m_device; }

private:
    QIODevice* m_device;
};

class JBeadOutputStream
{
public:
    explicit JBeadOutputStream(QIODevice* device) : m_device(device) {}

    void write(quint8 value);
    void write(const QByteArray& bytes);
    void write(const QString& s);
    void writeInt(qint32 value);
    void writeBool(bool value);
    void writeColor(const QColor& color);

private:
    QIODevice* m_device;
};

} // namespace jbead
