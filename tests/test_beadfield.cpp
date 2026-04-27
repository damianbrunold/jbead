#include "domain/beadfield.h"
#include "domain/beadpoint.h"
#include "domain/beadrect.h"
#include "domain/rectiterator.h"
#include "domain/segmentiterator.h"

#include <QTest>

using namespace jbead;

class TestBeadField : public QObject
{
    Q_OBJECT

private slots:
    void defaultDimensions()
    {
        BeadField f;
        QCOMPARE(f.width(),  BeadField::DEFAULT_WIDTH);
        QCOMPARE(f.height(), BeadField::DEFAULT_HEIGHT);
        QCOMPARE(int(f.get(0)), 0);
    }

    void rowMajorIndexing()
    {
        BeadField f;
        f.set(BeadPoint(3, 5), 7);
        QCOMPARE(int(f.get(BeadPoint(3, 5))), 7);
        QCOMPARE(f.indexOf(BeadPoint(3, 5)), 3 + f.width() * 5);
        QCOMPARE(f.pointAt(3 + f.width() * 5), BeadPoint(3, 5));
    }

    void resizePreservesTopLeft()
    {
        BeadField f;
        f.set(BeadPoint(0, 0), 1);
        f.set(BeadPoint(14, 0), 2);
        f.set(BeadPoint(0, 1), 3);
        f.setWidth(8);
        QCOMPARE(int(f.get(BeadPoint(0, 0))), 1);
        QCOMPARE(int(f.get(BeadPoint(0, 1))), 3);
        // column 14 was beyond new width=8; it must not bleed into the new layout
        QCOMPARE(int(f.get(BeadPoint(7, 0))), 0);
    }

    void mirrorHorizontalRoundtrip()
    {
        BeadField f;
        f.setWidth(4);
        f.setHeight(4);
        f.set(BeadPoint(0, 0), 1);
        f.set(BeadPoint(1, 0), 2);
        f.mirrorHorizontal(f.fullRect());
        QCOMPARE(int(f.get(BeadPoint(3, 0))), 1);
        QCOMPARE(int(f.get(BeadPoint(2, 0))), 2);
        f.mirrorHorizontal(f.fullRect());
        QCOMPARE(int(f.get(BeadPoint(0, 0))), 1);
        QCOMPARE(int(f.get(BeadPoint(1, 0))), 2);
    }

    void rotateOnlyForSquareRect()
    {
        BeadField f;
        f.setWidth(3);
        f.setHeight(3);
        f.set(BeadPoint(0, 0), 5);  // source: (left, bottom)
        f.rotate(f.fullRect());
        /*  Legacy mapping:  src (i,j)  ->  dst (j, h-1-i).
            For (0,0) in a 3x3 fullRect that's (0, 2) — the top-left
            corner. Verifies the rotation matches legacy faithfully. */
        QCOMPARE(int(f.get(BeadPoint(0, 2))), 5);
        QCOMPARE(int(f.get(BeadPoint(0, 0))), 0);
    }

    void rectIteratorRowMajor()
    {
        BeadRect r(BeadPoint(0, 0), BeadPoint(2, 1));
        QList<BeadPoint> seq;
        RectIterator it(r);
        while (it.hasNext()) seq.append(it.next());
        QCOMPARE(seq.size(), 6);
        QCOMPARE(seq.at(0), BeadPoint(0, 0));
        QCOMPARE(seq.at(2), BeadPoint(2, 0));
        QCOMPARE(seq.at(3), BeadPoint(0, 1));
        QCOMPARE(seq.at(5), BeadPoint(2, 1));
    }

    void rectIteratorEmpty()
    {
        RectIterator it(BeadRect::empty());
        QVERIFY(!it.hasNext());
    }

    void rectIteratorReversedNormalised()
    {
        BeadRect r(BeadPoint(2, 1), BeadPoint(0, 0));
        QList<BeadPoint> seq;
        RectIterator it(r);
        while (it.hasNext()) seq.append(it.next());
        QCOMPARE(seq.size(), 6);
        QCOMPARE(seq.at(0), BeadPoint(0, 0));
        QCOMPARE(seq.at(5), BeadPoint(2, 1));
    }

    void segmentIteratorHorizontal()
    {
        SegmentIterator it(BeadPoint(0, 0), BeadPoint(3, 0));
        QList<BeadPoint> seq;
        while (it.hasNext()) seq.append(it.next());
        QCOMPARE(seq.size(), 4);
        QCOMPARE(seq.at(0), BeadPoint(0, 0));
        QCOMPARE(seq.at(3), BeadPoint(3, 0));
    }

    void segmentIteratorDiagonalCovers()
    {
        SegmentIterator it(BeadPoint(0, 0), BeadPoint(3, 3));
        int n = 0;
        while (it.hasNext()) { it.next(); ++n; }
        QCOMPARE(n, 4);
    }

    void pointShifted()
    {
        QCOMPARE(BeadPoint(2, 0).shifted(0, 5), BeadPoint(2, 0));
        QCOMPARE(BeadPoint(2, 0).shifted(3, 5), BeadPoint(0, 1));
        QCOMPARE(BeadPoint(2, 0).shifted(8, 5), BeadPoint(0, 2));
    }
};

QTEST_APPLESS_MAIN(TestBeadField)
#include "test_beadfield.moc"
