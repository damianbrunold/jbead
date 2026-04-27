#include <QString>
#include <QTest>

class TestSmoke : public QObject
{
    Q_OBJECT

private slots:
    void qstring_basic()
    {
        QString s = QStringLiteral("JBead");
        QCOMPARE(s.length(), 5);
        QVERIFY(s.startsWith(QLatin1String("JB")));
    }
};

QTEST_APPLESS_MAIN(TestSmoke)
#include "test_smoke.moc"
