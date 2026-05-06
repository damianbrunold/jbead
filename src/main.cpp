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
#include <QEvent>
#include <QFileOpenEvent>
#include <QIcon>
#include <QLibraryInfo>
#include <QLocale>
#include <QPointer>
#include <QSettings>
#include <QTranslator>

#include "domain/beadsymbols.h"
#include "ui/colorscheme.h"
#include "ui/mainwindow.h"
#include "version.h"

namespace {

/*  Catches macOS QFileOpenEvent (Finder double-click on a .jbb /
    .dbb file, drop on the Dock icon, "Open With" from another app)
    and routes the path to the main window. Buffers until the window
    is wired up via setMainWindow(), so a path that arrives during
    QApplication construction (cold launch) isn't lost.            */
class JBeadApp : public QApplication
{
public:
    using QApplication::QApplication;

    void setMainWindow(jbead::MainWindow* w)
    {
        m_win = w;
        if (w && !m_pendingPath.isEmpty()) {
            w->openExternalFile(m_pendingPath);
            m_pendingPath.clear();
        }
    }

    bool event(QEvent* e) override
    {
        if (e->type() == QEvent::FileOpen) {
            const QString path =
                static_cast<QFileOpenEvent*>(e)->file();
            if (m_win)
                m_win->openExternalFile(path);
            else
                m_pendingPath = path;
            return true;
        }
        return QApplication::event(e);
    }

private:
    QPointer<jbead::MainWindow> m_win;
    QString m_pendingPath;
};

} // namespace

int main(int argc, char* argv[])
{
    Q_INIT_RESOURCE(icons);

    JBeadApp app(argc, argv);

    QApplication::setOrganizationName("Brunold Software");
    QApplication::setOrganizationDomain("brunoldsoftware.ch");
    QApplication::setApplicationName("JBead");
    QApplication::setApplicationVersion(QStringLiteral(JBEAD_VERSION_STRING));

    QString cliFile;
    {
        QCommandLineParser parser;
        parser.setApplicationDescription(
            QStringLiteral("JBead bead-pattern designer"));
        parser.addHelpOption();
        parser.addVersionOption();
        parser.addPositionalArgument(QStringLiteral("file"),
            QObject::tr("Pattern file to open (.jbb / .dbb)."),
            QStringLiteral("[file]"));
        parser.process(app);
        const QStringList pos = parser.positionalArguments();
        if (!pos.isEmpty())
            cliFile = pos.first();
    }

    /*  Skip on macOS: setWindowIcon() there calls
        [NSApp setApplicationIconImage:], which would override the
        bundle's CFBundleIconFile (the squircle-clipped .icns) for
        the Dock while the app is running. macOS window titlebars
        don't show app icons, so there's nothing to gain.          */
#ifndef Q_OS_MACOS
    {
        QIcon appIcon;
        appIcon.addFile(QStringLiteral(":/icons/jbead-16.png"), QSize(16, 16));
        appIcon.addFile(QStringLiteral(":/icons/jbead-32.png"), QSize(32, 32));
        QApplication::setWindowIcon(appIcon);
    }
#endif

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

        /*  Restore the user's symbol palette (legacy "view/symbols"
            setting). BeadPainter consults BeadSymbols::glyph on every
            paint, so applying this before any widget is built avoids
            a flicker between the default and saved palettes.        */
        const QString syms = settings.value(QStringLiteral("Environment/Symbols"),
                                            jbead::BeadSymbols::DEFAULT_SYMBOLS)
                                     .toString();
        if (!syms.isEmpty())
            jbead::BeadSymbols::setSymbols(syms);
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
    app.setMainWindow(&win);

    /*  Argv path is for terminal launches and `open -a JBead foo.jbb`
        invocations; Finder double-clicks arrive as QFileOpenEvent
        during exec() instead. The two are mutually exclusive in
        practice -- Finder doesn't pass file paths in argv.         */
    if (!cliFile.isEmpty())
        win.openExternalFile(cliFile);

    win.show();

    return app.exec();
}
