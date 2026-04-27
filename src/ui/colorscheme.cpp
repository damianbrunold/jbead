#include "colorscheme.h"

#include <QApplication>
#include <QGuiApplication>
#include <QPalette>
#include <QStyleFactory>
#include <QStyleHints>

namespace jbead {

namespace {

QPalette lightPalette()
{
    QPalette p;
    p.setColor(QPalette::Window,          QColor(240, 240, 240));
    p.setColor(QPalette::WindowText,      Qt::black);
    p.setColor(QPalette::Base,            Qt::white);
    p.setColor(QPalette::AlternateBase,   QColor(245, 245, 245));
    p.setColor(QPalette::ToolTipBase,     QColor(255, 255, 220));
    p.setColor(QPalette::ToolTipText,     Qt::black);
    p.setColor(QPalette::Text,            Qt::black);
    p.setColor(QPalette::Button,          QColor(240, 240, 240));
    p.setColor(QPalette::ButtonText,      Qt::black);
    p.setColor(QPalette::BrightText,      Qt::red);
    p.setColor(QPalette::Link,            QColor(0, 0, 238));
    p.setColor(QPalette::Highlight,       QColor(0, 120, 215));
    p.setColor(QPalette::HighlightedText, Qt::white);
    p.setColor(QPalette::PlaceholderText, QColor(120, 120, 120));
    p.setColor(QPalette::Disabled, QPalette::WindowText, QColor(120, 120, 120));
    p.setColor(QPalette::Disabled, QPalette::Text,       QColor(120, 120, 120));
    p.setColor(QPalette::Disabled, QPalette::ButtonText, QColor(120, 120, 120));
    return p;
}

QPalette darkPalette()
{
    /*  Standard dark scheme — close to the GNOME / KDE
        "darcula"-ish defaults the textile editor uses for its
        dark mode (jbead.js settings.darcula).                    */
    QPalette p;
    p.setColor(QPalette::Window,          QColor(45, 45, 45));
    p.setColor(QPalette::WindowText,      QColor(220, 220, 220));
    p.setColor(QPalette::Base,            QColor(30, 30, 30));
    p.setColor(QPalette::AlternateBase,   QColor(45, 45, 45));
    p.setColor(QPalette::ToolTipBase,     QColor(60, 60, 60));
    p.setColor(QPalette::ToolTipText,     QColor(220, 220, 220));
    p.setColor(QPalette::Text,            QColor(220, 220, 220));
    p.setColor(QPalette::Button,          QColor(55, 55, 55));
    p.setColor(QPalette::ButtonText,      QColor(220, 220, 220));
    p.setColor(QPalette::BrightText,      Qt::red);
    p.setColor(QPalette::Link,            QColor(80, 160, 240));
    p.setColor(QPalette::Highlight,       QColor(60, 130, 200));
    p.setColor(QPalette::HighlightedText, Qt::white);
    p.setColor(QPalette::PlaceholderText, QColor(150, 150, 150));
    p.setColor(QPalette::Disabled, QPalette::WindowText, QColor(120, 120, 120));
    p.setColor(QPalette::Disabled, QPalette::Text,       QColor(120, 120, 120));
    p.setColor(QPalette::Disabled, QPalette::ButtonText, QColor(120, 120, 120));
    return p;
}

} // namespace

void applyColorScheme(const QString& scheme)
{
    if (scheme == QStringLiteral("light")) {
        QApplication::setStyle(QStyleFactory::create(QStringLiteral("Fusion")));
        QApplication::setPalette(lightPalette());
        QGuiApplication::styleHints()->setColorScheme(Qt::ColorScheme::Light);
    } else if (scheme == QStringLiteral("dark")) {
        QApplication::setStyle(QStyleFactory::create(QStringLiteral("Fusion")));
        QApplication::setPalette(darkPalette());
        QGuiApplication::styleHints()->setColorScheme(Qt::ColorScheme::Dark);
    } else {
        /*  "system" / unknown: clear our palette so the platform
            theme paints things and let styleHints follow the OS. */
        QApplication::setPalette(QPalette());
        QGuiApplication::styleHints()->setColorScheme(Qt::ColorScheme::Unknown);
    }
}

} // namespace jbead
