#pragma once

#include <QSettings>
#include <QString>

namespace jbead {

/*  Thin QSettings wrapper that mirrors the legacy java.util.prefs
    accessor surface. Values are scoped under a "category" group so
    keys collide neither with each other nor with Qt's own
    book-keeping. Underlying storage is whatever QSettings picks per
    platform (registry on Windows, plist on macOS, ini under
    XDG_CONFIG_HOME on Linux); the organisation/application keys are
    the ones set in main.cpp.                                       */
class Settings
{
public:
    Settings();

    void    setCategory(const QString& category);
    QString category() const { return m_category; }

    bool    hasSetting(const QString& name) const;

    int     loadInt(const QString& name, int defaultValue) const;
    qint64  loadLong(const QString& name, qint64 defaultValue) const;
    bool    loadBool(const QString& name, bool defaultValue) const;
    QString loadString(const QString& name, const QString& defaultValue) const;

    void    saveInt(const QString& name, int value);
    void    saveLong(const QString& name, qint64 value);
    void    saveBool(const QString& name, bool value);
    void    saveString(const QString& name, const QString& value);

    void    remove(const QString& name);
    void    flush();

private:
    QString fullKey(const QString& name) const { return m_category + QLatin1Char('/') + name; }

    mutable QSettings m_settings;
    QString           m_category;
};

} // namespace jbead
