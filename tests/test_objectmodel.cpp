#include "io/objectmodel.h"

#include <QTest>

using namespace jbead;

class TestObjectModel : public QObject
{
    Q_OBJECT

private slots:
    void parseFlatLeaves()
    {
        const QString src = QStringLiteral("(jbb (version 1) (author \"alice\") (notes \"hi\"))");
        ObjectModel om = ObjectModel::fromData(src, QStringLiteral("jbb"));
        QCOMPARE(om.intValue(QStringLiteral("version"), -1), 1);
        QCOMPARE(om.stringValue(QStringLiteral("author"), QString()), QStringLiteral("alice"));
        QCOMPARE(om.stringValue(QStringLiteral("notes"),  QString()), QStringLiteral("hi"));
    }

    void parseRepeatedLeaves()
    {
        const QString src =
            QStringLiteral("(jbb (colors (rgb 1 2 3) (rgb 4 5 6) (rgb 7 8 9)))");
        ObjectModel om = ObjectModel::fromData(src, QStringLiteral("jbb"));
        const auto rgb = om.getAll(QStringLiteral("colors/rgb"));
        QCOMPARE(rgb.size(), 3);
        Leaf* second = static_cast<Leaf*>(rgb.at(1));
        QCOMPARE(second->intValue(0), 4);
        QCOMPARE(second->intValue(1), 5);
        QCOMPARE(second->intValue(2), 6);
    }

    void parseBoolean()
    {
        ObjectModel om = ObjectModel::fromData(
            QStringLiteral("(jbb (view (draft-visible true) (corrected-visible false)))"),
            QStringLiteral("jbb"));
        QCOMPARE(om.boolValue(QStringLiteral("view/draft-visible"),     false), true);
        QCOMPARE(om.boolValue(QStringLiteral("view/corrected-visible"), true),  false);
    }

    void roundTripPretty()
    {
        ObjectModel om(QStringLiteral("jbb"));
        om.add(QStringLiteral("version"), {1});
        om.add(QStringLiteral("author"),  {QStringLiteral("a")});
        om.add(QStringLiteral("view/draft-visible"), {true});
        om.add(QStringLiteral("colors/rgb"), {255, 0, 0, 255});
        om.add(QStringLiteral("colors/rgb"), {0, 255, 0, 255});

        const QString text = om.toString();
        ObjectModel reparsed = ObjectModel::fromData(text, QStringLiteral("jbb"));
        QCOMPARE(reparsed.intValue(QStringLiteral("version"), -1), 1);
        QCOMPARE(reparsed.stringValue(QStringLiteral("author"), QString()),
                 QStringLiteral("a"));
        QCOMPARE(reparsed.boolValue(QStringLiteral("view/draft-visible"), false), true);
        QCOMPARE(reparsed.getAll(QStringLiteral("colors/rgb")).size(), 2);
    }

    void escapesInStrings()
    {
        ObjectModel om(QStringLiteral("jbb"));
        om.add(QStringLiteral("notes"), {QStringLiteral("hello \"world\"\\n")});
        const QString text = om.toString();
        ObjectModel re = ObjectModel::fromData(text, QStringLiteral("jbb"));
        QCOMPARE(re.stringValue(QStringLiteral("notes"), QString()),
                 QStringLiteral("hello \"world\"\\n"));
    }
};

QTEST_APPLESS_MAIN(TestObjectModel)
#include "test_objectmodel.moc"
