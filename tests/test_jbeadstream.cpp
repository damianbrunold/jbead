#include "io/jbeadstream.h"

#include <QBuffer>
#include <QTest>

using namespace jbead;

class TestJBeadStream : public QObject
{
    Q_OBJECT

private slots:
    void littleEndianInt()
    {
        QBuffer buf;
        buf.open(QIODevice::ReadWrite);
        JBeadOutputStream out(&buf);
        out.writeInt(0x01020304);

        const QByteArray bytes = buf.data();
        QCOMPARE(int(bytes.size()), 4);
        QCOMPARE(quint8(bytes.at(0)), quint8(0x04));
        QCOMPARE(quint8(bytes.at(1)), quint8(0x03));
        QCOMPARE(quint8(bytes.at(2)), quint8(0x02));
        QCOMPARE(quint8(bytes.at(3)), quint8(0x01));

        buf.seek(0);
        JBeadInputStream in(&buf);
        QCOMPARE(in.readInt(), qint32(0x01020304));
    }

    void colorRoundTripDropsAlpha()
    {
        QBuffer buf;
        buf.open(QIODevice::ReadWrite);
        JBeadOutputStream out(&buf);
        out.writeColor(QColor(10, 20, 30, 200));
        // Writer always emits alpha=0; reader ignores alpha and
        // returns opaque QColor.
        QCOMPARE(quint8(buf.data().at(3)), quint8(0));
        buf.seek(0);
        JBeadInputStream in(&buf);
        const QColor c = in.readColor();
        QCOMPARE(c.red(),   10);
        QCOMPARE(c.green(), 20);
        QCOMPARE(c.blue(),  30);
        QCOMPARE(c.alpha(), 255);
    }

    void backgroundColorMagic()
    {
        QBuffer buf;
        buf.open(QIODevice::ReadWrite);
        const char magic[] = {15, 0, 0, char(128)};
        buf.write(magic, 4);
        buf.seek(0);
        JBeadInputStream in(&buf);
        const QColor c = in.readBackgroundColor();
        QCOMPARE(c, QColor(240, 240, 240));
    }

    void boolEncoding()
    {
        QBuffer buf;
        buf.open(QIODevice::ReadWrite);
        JBeadOutputStream out(&buf);
        out.writeBool(true);
        out.writeBool(false);
        QCOMPARE(quint8(buf.data().at(0)), quint8(1));
        QCOMPARE(quint8(buf.data().at(1)), quint8(0));
        buf.seek(0);
        JBeadInputStream in(&buf);
        QVERIFY(in.readBool());
        QVERIFY(!in.readBool());
    }
};

QTEST_APPLESS_MAIN(TestJBeadStream)
#include "test_jbeadstream.moc"
