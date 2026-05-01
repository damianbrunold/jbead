/*  JBead bead-pattern designer - http://www.brunoldsoftware.ch
    Copyright (C) 2009-2024  Damian Brunold (Java original)
    Copyright (C) 2026       Damian Brunold (Qt 6 port)

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.
*/

#include <QApplication>
#include <QCommandLineParser>
#include <QIcon>
#include <QLibraryInfo>
#include <QLocale>
#include <QSettings>
#include <QTranslator>

#include "ui/colorscheme.h"
#include "ui/mainwindow.h"
#include "version.h"

int main(int argc, char* argv[])
{
    Q_INIT_RESOURCE(icons);

    QApplication app(argc, argv);

    QApplication::setOrganizationName("Brunold Software");
    QApplication::setOrganizationDomain("brunoldsoftware.ch");
    QApplication::setApplicationName("JBead");
    QApplication::setApplicationVersion(QStringLiteral(JBEAD_VERSION_STRING));

    {
        QCommandLineParser parser;
        parser.setApplicationDescription(
            QStringLiteral("JBead bead-pattern designer"));
        parser.addHelpOption();
        parser.addVersionOption();
        parser.process(app);
    }

    {
        QIcon appIcon;
        appIcon.addFile(QStringLiteral(":/icons/jbead-16.png"), QSize(16, 16));
        appIcon.addFile(QStringLiteral(":/icons/jbead-32.png"), QSize(32, 32));
        QApplication::setWindowIcon(appIcon);
    }

    /*  Pick UI language. Saved preference under "Environment/Language"
        wins over the OS locale. Stored values: "system" (= OS locale),
        "en", "de", "fr". "en" / unmatched -> English source strings
        (no .qm needed).                                              */
    QString lang;
    {
        QSettings settings;
        lang = settings.value(QStringLiteral("Environment/Language"),
                              QStringLiteral("system")).toString();
        if (lang.isEmpty() || lang == QStringLiteral("system")) {
            const QString tag = QLocale::system().name().toLower();
            if (tag.startsWith(QStringLiteral("de"))) lang = QStringLiteral("de");
            else if (tag.startsWith(QStringLiteral("fr"))) lang = QStringLiteral("fr");
            else lang = QStringLiteral("en");
        }
    }

    /*  Apply the saved color scheme before any widget is built so
        dark-mode users don't see a flash of light on launch. The
        helper installs Fusion + a custom palette for "light" /
        "dark" — needed because the Linux platform theme plugin
        (Adwaita / Breeze) ignores QStyleHints::setColorScheme on
        its own.                                                   */
    {
        QSettings settings;
        jbead::applyColorScheme(
            settings.value(QStringLiteral("Environment/ColorScheme"),
                           QStringLiteral("system")).toString());
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

    /*  Geometry is restored inside MainWindow's constructor (or the
        constructor falls back to a default size on first run). Don't
        resize here — that would clobber the restored window state.  */
    jbead::MainWindow win;
    win.show();

    return app.exec();
}
