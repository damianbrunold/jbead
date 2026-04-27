#include "ui/actions.h"
#include "ui/mainwindow.h"

#include <QApplication>
#include <QCoreApplication>
#include <QResource>
#include <QTest>
#include <QTranslator>

class TestI18n : public QObject
{
    Q_OBJECT

private slots:
    void initTestCase()
    {
        qputenv("QT_QPA_PLATFORM", "offscreen");
    }

    void germanTranslatorLoadsAndAppliesActionLabels()
    {
        QTranslator translator;
        QVERIFY(translator.load(QStringLiteral(":/i18n/jbead_de")));
        QCoreApplication::installTranslator(&translator);

        // Action labels and descriptions translated from
        // legacy/src/jbead_de.properties.
        QCOMPARE(QCoreApplication::translate("jbead::Actions", "&New"),
                 QStringLiteral("&Neu"));
        QCOMPARE(QCoreApplication::translate("jbead::Actions", "Saves the pattern"),
                 QStringLiteral("Speichert das Muster"));
        QCOMPARE(QCoreApplication::translate("jbead::Actions", "Mirror &Horizontal"),
                 QStringLiteral("Spiegeln &horizontal"));

        // Manually-translated dialog strings from MANUAL_TRANSLATIONS.
        QCOMPARE(QCoreApplication::translate("jbead::ArrangeDialog", "Arrange"),
                 QStringLiteral("Anordnen"));

        QCoreApplication::removeTranslator(&translator);
    }

    void frenchTranslatorLoadsAndAppliesActionLabels()
    {
        QTranslator translator;
        QVERIFY(translator.load(QStringLiteral(":/i18n/jbead_fr")));
        QCoreApplication::installTranslator(&translator);

        QCOMPARE(QCoreApplication::translate("jbead::Actions", "&New"),
                 QStringLiteral("&Nouveau"));
        QCOMPARE(QCoreApplication::translate("jbead::Actions", "Opens a pattern"),
                 QStringLiteral("Ouvrir un nouveau patron"));

        QCoreApplication::removeTranslator(&translator);
    }

    void mainWindowMenuLabelsRespectActiveTranslator()
    {
        // Install German first, then construct the window — its
        // tr() calls should pick up the translated strings.
        QTranslator translator;
        QVERIFY(translator.load(QStringLiteral(":/i18n/jbead_de")));
        QCoreApplication::installTranslator(&translator);

        jbead::MainWindow w;
        QCOMPARE(w.actions()->action(jbead::Actions::Id::FileNew)->text(),
                 QStringLiteral("&Neu"));
        QCOMPARE(w.actions()->action(jbead::Actions::Id::FileSave)->text(),
                 QStringLiteral("&Speichern"));
        QCOMPARE(w.actions()->action(jbead::Actions::Id::ToolPencil)->text(),
                 QStringLiteral("&Bleistift"));

        QCoreApplication::removeTranslator(&translator);
    }
};

QTEST_MAIN(TestI18n)
#include "test_i18n.moc"
