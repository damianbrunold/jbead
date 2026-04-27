#include "domain/model.h"
#include "domain/selection.h"

#include <QSignalSpy>
#include <QTest>

using namespace jbead;

class TestModel : public QObject
{
    Q_OBJECT

private slots:
    void newModelHasDefaultPaletteAndCleanState()
    {
        Model m;
        m.clear();
        QCOMPARE(m.colorCount(), 32);
        QCOMPARE(int(m.selectedColor()), 1);
        QCOMPARE(m.scroll(), 0);
        QCOMPARE(m.shift(),  0);
        QCOMPARE(m.repeat(), 0);
        QVERIFY(!m.isModified());
        QVERIFY(!m.canUndo());
        QVERIFY(!m.canRedo());
        QVERIFY(!m.usedRect().width()
                 || m.usedRect().begin().x() < 0);  // empty()
    }

    void setPointTogglesAndFiresSignal()
    {
        Model m; m.clear();
        QSignalSpy points(&m, &Model::pointChanged);
        m.setSelectedColor(3);
        m.setPoint(BeadPoint(2, 4));
        QCOMPARE(int(m.get(BeadPoint(2, 4))), 3);
        QCOMPARE(points.count(), 1);
        QVERIFY(m.isModified());

        // Setting the same colour to a cell already containing it
        // erases (toggles to background).
        m.setPoint(BeadPoint(2, 4));
        QCOMPARE(int(m.get(BeadPoint(2, 4))), 0);
    }

    void drawLineBresenhamAndScroll()
    {
        Model m; m.clear();
        m.setSelectedColor(5);
        // Vertical line from (3,0) to (3,4) — five cells.
        m.drawLine(BeadPoint(3, 0), BeadPoint(3, 4));
        for (int j = 0; j <= 4; ++j) {
            QCOMPARE(int(m.get(BeadPoint(3, j))), 5);
        }
        QVERIFY(m.isModified());

        // Scroll offset is applied to the endpoints, so after
        // setScroll(2) drawing (0,0)-(0,0) lands at row 2.
        m.setScroll(2);
        m.setSelectedColor(7);
        m.drawLine(BeadPoint(0, 0), BeadPoint(0, 0));
        QCOMPARE(int(m.get(BeadPoint(0, 2))), 7);
    }

    void fillLineFloodsContiguousBackground()
    {
        Model m; m.clear();
        m.setSelectedColor(2);
        m.set(BeadPoint(5, 0), 9);   // sentinel: stops the fill
        m.fillLine(BeadPoint(0, 0));
        // Fill walks linearly from index 0 forward until it hits a
        // non-background cell, then stops — covering 0..4.
        for (int i = 0; i <= 4; ++i) {
            QCOMPARE(int(m.get(BeadPoint(i, 0))), 2);
        }
        QCOMPARE(int(m.get(BeadPoint(5, 0))), 9);
    }

    void mirrorHorizontalSwapsColumns()
    {
        Model m; m.clear();
        m.setSelectedColor(1);
        m.set(BeadPoint(0, 0), 4);
        m.set(BeadPoint(1, 0), 5);
        m.mirrorHorizontal(BeadRect(BeadPoint(0, 0),
                                    BeadPoint(m.width() - 1, 0)));
        QCOMPARE(int(m.get(BeadPoint(m.width() - 1, 0))), 4);
        QCOMPARE(int(m.get(BeadPoint(m.width() - 2, 0))), 5);
        QCOMPARE(int(m.get(BeadPoint(0, 0))), 0);
    }

    void mirrorVerticalSwapsRows()
    {
        Model m; m.clear();
        m.set(BeadPoint(0, 0), 1);
        m.set(BeadPoint(0, 1), 2);
        m.set(BeadPoint(0, 2), 3);
        m.mirrorVertical(BeadRect(BeadPoint(0, 0), BeadPoint(0, 2)));
        QCOMPARE(int(m.get(BeadPoint(0, 0))), 3);
        QCOMPARE(int(m.get(BeadPoint(0, 1))), 2);
        QCOMPARE(int(m.get(BeadPoint(0, 2))), 1);
    }

    void rotateOnlyForSquareRect()
    {
        Model m; m.clear();
        m.setWidth(3);
        m.setHeight(5);
        m.set(BeadPoint(0, 0), 4);
        // Non-square rect: rotate is a no-op.
        m.rotate(BeadRect(BeadPoint(0, 0), BeadPoint(2, 1)));
        QCOMPARE(int(m.get(BeadPoint(0, 0))), 4);
        // Square rect: cell at src (i=0,j=0) maps to dst (j=0, h-1-i=2).
        m.rotate(BeadRect(BeadPoint(0, 0), BeadPoint(2, 2)));
        QCOMPARE(int(m.get(BeadPoint(0, 2))), 4);
    }

    void deleteRectClearsRegion()
    {
        Model m; m.clear();
        for (int i = 0; i < 5; ++i) m.set(BeadPoint(i, 0), 7);
        m.deleteRect(BeadRect(BeadPoint(1, 0), BeadPoint(3, 0)));
        QCOMPARE(int(m.get(BeadPoint(0, 0))), 7);
        QCOMPARE(int(m.get(BeadPoint(1, 0))), 0);
        QCOMPARE(int(m.get(BeadPoint(3, 0))), 0);
        QCOMPARE(int(m.get(BeadPoint(4, 0))), 7);
    }

    void arrangeSelectionStampsCopiesAlongOffset()
    {
        Model m; m.clear();
        m.setSelectedColor(6);
        m.set(BeadPoint(0, 0), 6);
        Selection sel;
        sel.init(BeadPoint(0, 0));
        sel.update(BeadPoint(0, 0));
        // Three copies, +1 cell each (i.e. index +1).
        m.arrangeSelection(sel, 3, 1);
        QCOMPARE(int(m.get(BeadPoint(1, 0))), 6);
        QCOMPARE(int(m.get(BeadPoint(2, 0))), 6);
        QCOMPARE(int(m.get(BeadPoint(3, 0))), 6);
    }

    void undoRollsBackEditPrepareSnapshotEnablesRedo()
    {
        Model m; m.clear();
        m.setSelectedColor(4);
        m.setPoint(BeadPoint(2, 2));
        QCOMPARE(int(m.get(BeadPoint(2, 2))), 4);
        m.prepareSnapshot();           // capture post-state
        QVERIFY(m.canUndo());
        m.undo();
        QCOMPARE(int(m.get(BeadPoint(2, 2))), 0);
        QVERIFY(m.canRedo());
        m.redo();
        QCOMPARE(int(m.get(BeadPoint(2, 2))), 4);
    }

    void zoomCyclesThroughTable()
    {
        Model m; m.clear();
        const int normal = m.gridx();
        m.zoomIn();  QVERIFY(m.gridx() > normal);
        m.zoomOut(); QCOMPARE(m.gridx(), normal);
        m.zoomNormal();
        QCOMPARE(m.zoomIndex(), Model::ZOOM_NORMAL);
        QVERIFY(m.isNormalZoom());
    }

    void shiftWrapsHorizontally()
    {
        Model m; m.clear();
        const int W = m.width();
        m.shiftRight();
        QCOMPARE(m.shift(), 1);
        m.shiftLeft();
        m.shiftLeft();
        QCOMPARE(m.shift(), W - 1);     // wraps modulo width
    }

    void repeatDetectsTwoRowPattern()
    {
        Model m; m.clear();
        m.setWidth(4);
        // Two-row stripe:  row0 = 1 1 1 1, row1 = 2 2 2 2, repeated.
        for (int j = 0; j < 6; ++j) {
            const std::int8_t c = (j % 2 == 0) ? 1 : 2;
            for (int i = 0; i < 4; ++i) m.set(BeadPoint(i, j), c);
        }
        m.updateRepeat();
        // Repeat unit is the smallest period in the linear bead
        // sequence — here a row-pair of 4+4 = 8 beads.
        QCOMPARE(m.repeat(), 8);
    }

    void selectionLineDestinationSnapsTo45()
    {
        /*  lineDestination snaps the cursor to a 45-degree line
            from begin, constrained to the *shorter* axis. So for
            (0,0)->(5,2) the major axis is x, but the snapped point
            sits |dy|=2 cells away along x: (2,2). For axis-aligned
            selections lineDestination() is a no-op.               */
        Selection s;
        s.init(BeadPoint(0, 0));
        s.update(BeadPoint(5, 2));
        QCOMPARE(s.lineDestination(), BeadPoint(2, 2));

        s.init(BeadPoint(0, 0));
        s.update(BeadPoint(2, 5));
        QCOMPARE(s.lineDestination(), BeadPoint(2, 2));

        s.init(BeadPoint(0, 0));
        s.update(BeadPoint(5, 0));
        QCOMPARE(s.lineDestination(), BeadPoint(5, 0));
    }

    void clearResetsState()
    {
        Model m; m.clear();
        m.setSelectedColor(8);
        m.setPoint(BeadPoint(1, 1));
        QVERIFY(m.isModified());
        m.clear();
        QVERIFY(!m.isModified());
        QCOMPARE(int(m.get(BeadPoint(1, 1))), 0);
        QCOMPARE(int(m.selectedColor()), 1);
    }
};

QTEST_APPLESS_MAIN(TestModel)
#include "test_model.moc"
