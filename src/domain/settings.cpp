#include "settings.h"

namespace jbead {

Settings::Settings()
    : m_settings(), m_category(QStringLiteral("general"))
{
}

void Settings::setCategory(const QString& category) { m_category = category; }

bool    Settings::hasSetting(const QString& name) const             { return m_settings.contains(fullKey(name)); }
int     Settings::loadInt(const QString& name, int v) const         { return m_settings.value(fullKey(name), v).toInt(); }
qint64  Settings::loadLong(const QString& name, qint64 v) const     { return m_settings.value(fullKey(name), v).toLongLong(); }
bool    Settings::loadBool(const QString& name, bool v) const       { return m_settings.value(fullKey(name), v).toBool(); }
QString Settings::loadString(const QString& name, const QString& v) const { return m_settings.value(fullKey(name), v).toString(); }

void    Settings::saveInt(const QString& name, int v)               { m_settings.setValue(fullKey(name), v); }
void    Settings::saveLong(const QString& name, qint64 v)           { m_settings.setValue(fullKey(name), v); }
void    Settings::saveBool(const QString& name, bool v)             { m_settings.setValue(fullKey(name), v); }
void    Settings::saveString(const QString& name, const QString& v) { m_settings.setValue(fullKey(name), v); }

void    Settings::remove(const QString& name) { m_settings.remove(fullKey(name)); }
void    Settings::flush()                     { m_settings.sync(); }

} // namespace jbead
