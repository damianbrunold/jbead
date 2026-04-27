/*  JBead bead-pattern designer - http://www.brunoldsoftware.ch
    Copyright (C) 2009-2024  Damian Brunold (Java original)
    Copyright (C) 2026       Damian Brunold (Qt 6 port)

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.
*/

#include <QApplication>
#include <QIcon>
#include <QLibraryInfo>
#include <QLocale>
#include <QSettings>
#include <QTranslator>

#include "ui/mainwindow.h"

int main(int argc, char* argv[])
{
    Q_INIT_RESOURCE(icons);

    QApplication app(argc, argv);

    QApplication::setOrganizationName("Brunold Software");
    QApplication::setOrganizationDomain("brunoldsoftware.ch");
    QApplication::setApplicationName("JBead");
    QApplication::setApplicationVersion("0.1.0");

    {
        QIcon appIcon;
        appIcon.addFile(QStringLiteral(":/icons/jbead-16.png"), QSize(16, 16));
        appIcon.addFile(QStringLiteral(":/icons/jbead-32.png"), QSize(32, 32));
        QApplication::setWindowIcon(appIcon);
    }

    /*  Pick UI language. Saved preference under "Environment/Language"
        wins over the OS locale. "de" / "fr" / "en" are the three
        shipped translations; anything else falls back to English (the
        source language, no .qm needed).                              */
    QString lang;
    {
        QSettings settings;
        lang = settings.value(QStringLiteral("Environment/Language")).toString();
        if (lang.isEmpty()) {
            const QString tag = QLocale::system().name().toLower();
            if (tag.startsWith(QStringLiteral("de"))) lang = QStringLiteral("de");
            else if (tag.startsWith(QStringLiteral("fr"))) lang = QStringLiteral("fr");
            else lang = QStringLiteral("en");
        }
    }

    QTranslator qtTranslator;
    if (qtTranslator.load(QLocale(lang),
                          QStringLiteral("qt"), QStringLiteral("_"),
                          QLibraryInfo::path(QLibraryInfo::TranslationsPath))) {
        QCoreApplication::installTranslator(&qtTranslator);
    }

    QTranslator appTranslator;
    if (appTranslator.load(QStringLiteral(":/i18n/jbead_") + lang)) {
        QCoreApplication::installTranslator(&appTranslator);
    }

    MainWindow win;
    win.resize(1024, 768);
    win.show();

    return app.exec();
}
