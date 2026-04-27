#include "domain/model.h"
#include "domain/selection.h"
#include "io/fileformat.h"
#include "ui/actions.h"
#include "ui/mainwindow.h"

#include <QApplication>
#include <QDir>
#include <QTest>

using namespace jbead;

class TestMainWindow : public QObject
{
    Q_OBJECT

private slots:
    void initTestCase()
    {
        qputenv("QT_QPA_PLATFORM", "offscreen");
    }

    void constructsAndExposesModel()
    {
        MainWindow w;
        QVERIFY(w.model() != nullptr);
        QVERIFY(w.actions() != nullptr);
        QVERIFY(w.selection() != nullptr);
        QCOMPARE(w.actions()->currentTool(), Actions::Id::ToolPencil);
    }

    void loadsSampleFile()
    {
        MainWindow w;
        const QString path = QDir(QStringLiteral(JBEAD_SAMPLES_DIR))
                              .filePath(QStringLiteral("hearts.jbb"));
        FileFormat::load(path, *w.model());
        QVERIFY(w.model()->width()  > 0);
        QVERIFY(w.model()->height() > 0);
        // Ensure the load propagated into the widgets without crashing.
        w.show();
        QTest::qWait(50);
    }

    void toolToggleIsExclusive()
    {
        MainWindow w;
        w.actions()->setCurrentTool(Actions::Id::ToolFill);
        QCOMPARE(w.actions()->currentTool(), Actions::Id::ToolFill);
        w.actions()->setCurrentTool(Actions::Id::ToolSelect);
        QCOMPARE(w.actions()->currentTool(), Actions::Id::ToolSelect);
    }

    void undoRollsBackEdit()
    {
        /*  Legacy BeadUndo only writes the *pre-state* on snapshot();
            redo therefore requires a follow-up prepareSnapshot()
            (driven by the idle timer in legacy JBeadFrame) to capture
            the post-state. We verify the simple undo path here — the
            redo path is exercised once Phase 6 ports the idle loop. */
        MainWindow w;
        w.model()->setSelectedColor(2);
        w.model()->setPoint(BeadPoint(1, 1));
        QCOMPARE(int(w.model()->get(BeadPoint(1, 1))), 2);
        QVERIFY(w.model()->canUndo());
        w.model()->undo();
        QCOMPARE(int(w.model()->get(BeadPoint(1, 1))), 0);
    }

    void selectionRectangle()
    {
        MainWindow w;
        w.selection()->init(BeadPoint(2, 3));
        w.selection()->update(BeadPoint(5, 7));
        QVERIFY(w.selection()->isActive());
        QCOMPARE(w.selection()->rect().width(), 4);
        QCOMPARE(w.selection()->rect().height(), 5);
        w.selection()->clear();
        QVERIFY(!w.selection()->isActive());
    }
};

QTEST_MAIN(TestMainWindow)
#include "test_mainwindow.moc"
