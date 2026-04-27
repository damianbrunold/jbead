#include "domain/model.h"
#include "io/dbbmemento.h"
#include "io/fileformat.h"
#include "io/jbeadmemento.h"

#include <QBuffer>
#include <QDir>
#include <QFile>
#include <QFileInfo>
#include <QTemporaryDir>
#include <QTest>

using namespace jbead;

namespace {

/*  All four files in samples/ at repo root are real .jbb patterns
    written by legacy JBead. Source-of-truth for round-trip tests. */
QString sampleDir()
{
    QDir d(QStringLiteral(JBEAD_SAMPLES_DIR));
    return d.absolutePath();
}

QStringList samples()
{
    return {
        QStringLiteral("stripes.jbb"),
        QStringLiteral("hearts.jbb"),
        QStringLiteral("small_hearts.jbb"),
        QStringLiteral("green_yellow_diagonal.jbb"),
    };
}

} // namespace

class TestFileFormat : public QObject
{
    Q_OBJECT

private slots:
    void loadSamples_data()
    {
        QTest::addColumn<QString>("path");
        for (const QString& s : samples())
            QTest::newRow(qPrintable(s)) << QDir(sampleDir()).filePath(s);
    }

    void loadSamples()
    {
        QFETCH(QString, path);
        Model model;
        model.clear();
        FileFormat::load(path, model);
        QVERIFY(model.width()  > 0);
        QVERIFY(model.height() > 0);
        QVERIFY(model.colorCount() >= 10);  // .jbb writes the full palette; padded to 32
        QCOMPARE(model.filePath(), path);
        QVERIFY(model.isSaved());
        QVERIFY(!model.isModified());
    }

    void roundTripJbeadInMemory()
    {
        // Set up a small known field
        Model writer;
        writer.clear();
        writer.setWidth(4);
        writer.setHeight(6);
        writer.setSelectedColor(3);
        writer.setAuthor(QStringLiteral("test"));
        writer.setOrganization(QStringLiteral("co"));
        writer.set(BeadPoint(0, 0), 1);
        writer.set(BeadPoint(1, 0), 2);
        writer.set(BeadPoint(2, 0), 3);
        writer.set(BeadPoint(3, 0), 1);
        writer.set(BeadPoint(0, 1), 4);

        QBuffer buf;
        buf.open(QIODevice::ReadWrite);
        JBeadMemento out;
        writer.saveTo(out);
        out.save(&buf);

        buf.seek(0);
        JBeadMemento in;
        in.load(&buf);

        Model reader;
        reader.clear();
        reader.loadFrom(in);

        QCOMPARE(reader.width(),  writer.width());
        QCOMPARE(reader.height(), writer.height());
        QCOMPARE(reader.author(), writer.author());
        QCOMPARE(reader.organization(), writer.organization());
        QCOMPARE(reader.selectedColor(), writer.selectedColor());
        for (int j = 0; j < writer.height(); ++j) {
            for (int i = 0; i < writer.width(); ++i) {
                QCOMPARE(reader.get(BeadPoint(i, j)),
                         writer.get(BeadPoint(i, j)));
            }
        }
    }

    void saveLoadJbeadOnDisk()
    {
        QTemporaryDir dir;
        QVERIFY(dir.isValid());
        const QString path = QDir(dir.path()).filePath(QStringLiteral("rt.jbb"));

        Model writer;
        writer.clear();
        writer.set(BeadPoint(0, 0), 5);
        writer.set(BeadPoint(2, 3), 7);
        writer.setNotes(QStringLiteral("memo"));
        FileFormat::save(path, writer);

        Model reader;
        reader.clear();
        FileFormat::load(path, reader);
        QCOMPARE(int(reader.get(BeadPoint(0, 0))), 5);
        QCOMPARE(int(reader.get(BeadPoint(2, 3))), 7);
        QCOMPARE(reader.notes(), QStringLiteral("memo"));
    }

    void sampleSemanticRoundTrip()
    {
        // Load a real sample, save it to a temp file, reload, and
        // verify the field data + key metadata survive the trip.
        // (Not byte-equality: the legacy writer's exact whitespace
        // and field-ordering are not contractual.)
        QTemporaryDir dir;
        QVERIFY(dir.isValid());
        for (const QString& sample : samples()) {
            const QString src = QDir(sampleDir()).filePath(sample);
            const QString dst = QDir(dir.path()).filePath(sample);

            Model a, b;
            a.clear();
            FileFormat::load(src, a);
            FileFormat::save(dst, a);
            b.clear();
            FileFormat::load(dst, b);

            QCOMPARE(b.width(),  a.width());
            QCOMPARE(b.height(), a.height());
            QCOMPARE(b.author(), a.author());
            QCOMPARE(b.notes(),  a.notes());
            for (int i = 0; i < a.width() * a.height(); ++i) {
                QCOMPARE(b.get(i), a.get(i));
            }
        }
    }

    void dbbBinaryRoundTripInMemory()
    {
        // Small field that fits within 25000-byte fixed payload.
        Model writer;
        writer.clear();
        writer.setWidth(15);
        writer.set(BeadPoint(0, 0), 1);
        writer.set(BeadPoint(7, 9), 2);
        writer.setSelectedColor(2);

        QBuffer buf;
        buf.open(QIODevice::ReadWrite);
        DbbMemento out;
        writer.saveTo(out);
        out.save(&buf);
        QCOMPARE(int(buf.data().size()),
                 4 /*width*/ + 25000 /*field*/ + 40 /*palette*/
                   + 1 /*colorIndex*/ + 12 /*3 ints*/ + 3 /*3 bools*/);

        buf.seek(0);
        DbbMemento in;
        in.load(&buf);

        Model reader;
        reader.clear();
        reader.loadFrom(in);
        QCOMPARE(reader.width(), 15);
        QCOMPARE(int(reader.get(BeadPoint(0, 0))), 1);
        QCOMPARE(int(reader.get(BeadPoint(7, 9))), 2);
        QCOMPARE(reader.selectedColor(), std::int8_t(2));
    }
};

QTEST_APPLESS_MAIN(TestFileFormat)
#include "test_fileformat.moc"
