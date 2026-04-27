#include "domain/model.h"
#include "io/fileformat.h"
#include "print/printjob.h"
#include "print/printsettings.h"

#include <QCoreApplication>
#include <QDir>
#include <QFile>
#include <QFileInfo>
#include <QPrinter>
#include <QTemporaryDir>
#include <QTest>

using namespace jbead;

class TestPrintJob : public QObject
{
    Q_OBJECT

private slots:
    void initTestCase()
    {
        qputenv("QT_QPA_PLATFORM", "offscreen");
    }

    void exportsSampleToPdf()
    {
        Model model;
        model.clear();
        const QString sample = QDir(QStringLiteral(JBEAD_SAMPLES_DIR))
                                 .filePath(QStringLiteral("hearts.jbb"));
        FileFormat::load(sample, model);

        QTemporaryDir dir;
        QVERIFY(dir.isValid());
        const QString pdf = QDir(dir.path()).filePath(QStringLiteral("hearts.pdf"));

        PrintSettings s;
        QPrinter printer(QPrinter::HighResolution);
        s.apply(&printer);
        printer.setOutputFormat(QPrinter::PdfFormat);
        printer.setOutputFileName(pdf);

        PrintJob job(model, s);
        QVERIFY(job.run(&printer));
        QVERIFY(QFile::exists(pdf));
        const QFileInfo fi(pdf);
        QVERIFY(fi.size() > 1024); // every PDF page header alone is ~hundreds of bytes
    }

    void exportsSampleToPng()
    {
        Model model; model.clear();
        FileFormat::load(QDir(QStringLiteral(JBEAD_SAMPLES_DIR))
                           .filePath(QStringLiteral("hearts.jbb")),
                         model);

        QTemporaryDir dir;
        QVERIFY(dir.isValid());
        const QString path = QDir(dir.path()).filePath(QStringLiteral("hearts.png"));
        PrintSettings s;
        QVERIFY(PrintJob(model, s).exportImage(path, "PNG"));
        QVERIFY(QFileInfo(path).size() > 1024);
    }

    void exportsSampleToSvg()
    {
        Model model; model.clear();
        FileFormat::load(QDir(QStringLiteral(JBEAD_SAMPLES_DIR))
                           .filePath(QStringLiteral("hearts.jbb")),
                         model);

        QTemporaryDir dir;
        QVERIFY(dir.isValid());
        const QString path = QDir(dir.path()).filePath(QStringLiteral("hearts.svg"));
        PrintSettings s;
        QVERIFY(PrintJob(model, s).exportSvg(path));
        QVERIFY(QFileInfo(path).exists());
        // SVGs from Qt's QSvgGenerator are well over a kilobyte
        // even for trivial scenes (XML preamble + style block).
        QVERIFY(QFileInfo(path).size() > 256);
    }

    void settingsRoundTripThroughQSettings()
    {
        QCoreApplication::setOrganizationName("JBeadTest");
        QCoreApplication::setApplicationName("PrintJobTest");

        PrintSettings out;
        out.pageSize       = QPageSize::Letter;
        out.orientation    = QPageLayout::Landscape;
        out.marginLeftMm   = 7.5;
        out.printSimulation = false;
        out.fullPattern     = true;
        out.save();

        PrintSettings in;
        in.load();
        QCOMPARE(in.pageSize, QPageSize::Letter);
        QCOMPARE(int(in.orientation), int(QPageLayout::Landscape));
        QCOMPARE(in.marginLeftMm, 7.5);
        QCOMPARE(in.printSimulation, false);
        QCOMPARE(in.fullPattern, true);
    }
};

QTEST_MAIN(TestPrintJob)
#include "test_printjob.moc"
