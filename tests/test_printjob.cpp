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

    void respectsSectionToggles()
    {
        Model model;
        model.clear();

        QPrinter scratch(QPrinter::HighResolution);
        scratch.setOutputFormat(QPrinter::PdfFormat);
        QTemporaryDir dir;
        QVERIFY(dir.isValid());
        scratch.setOutputFileName(QDir(dir.path()).filePath(QStringLiteral("a.pdf")));

        // Disable everything: no pages, no output.
        PrintSettings empty;
        empty.printDraft       = false;
        empty.printCorrected   = false;
        empty.printSimulation  = false;
        empty.printReport      = false;
        empty.printBeadList    = false;
        PrintJob noPages(model, empty);
        QVERIFY(!noPages.run(&scratch));
        QCOMPARE(noPages.pageCount(), 0);

        // Enable just the report -> exactly one page.
        PrintSettings only;
        only.printDraft = only.printCorrected = only.printSimulation = false;
        only.printReport = true;
        only.printBeadList = false;
        PrintJob justReport(model, only);
        QPrinter printer2(QPrinter::HighResolution);
        printer2.setOutputFormat(QPrinter::PdfFormat);
        printer2.setOutputFileName(QDir(dir.path()).filePath(QStringLiteral("b.pdf")));
        only.apply(&printer2);
        QVERIFY(justReport.run(&printer2));
        QCOMPARE(justReport.pageCount(), 1);
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
