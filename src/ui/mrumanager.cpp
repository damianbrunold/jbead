#include "mrumanager.h"

#include <QAction>
#include <QFileInfo>
#include <QMenu>
#include <QSettings>

namespace jbead {

MruManager::MruManager(QMenu* submenu, QObject* parent)
    : QObject(parent), m_menu(submenu)
{
    load();
}

void MruManager::load()
{
    QSettings s;
    m_paths = s.value(QStringLiteral("Files/recentFiles")).toStringList();
    prune();
    rebuildMenu();
}

void MruManager::save() const
{
    QSettings s;
    s.setValue(QStringLiteral("Files/recentFiles"), m_paths);
}

void MruManager::addPath(const QString& path)
{
    if (path.isEmpty()) return;
    /*  Canonicalise so the same file referenced by two different
        spellings (relative vs absolute, symlink, ./foo vs foo)
        deduplicates correctly.                                    */
    const QString canonical = QFileInfo(path).canonicalFilePath();
    const QString stored    = canonical.isEmpty() ? path : canonical;
    m_paths.removeAll(stored);
    m_paths.prepend(stored);
    prune();
    save();
    rebuildMenu();
}

void MruManager::prune()
{
    /*  Drop entries that no longer exist on disk so the user
        doesn't see stale paths point to deleted files.            */
    m_paths.erase(
        std::remove_if(m_paths.begin(), m_paths.end(),
                       [](const QString& p) { return !QFileInfo(p).exists(); }),
        m_paths.end());
    while (m_paths.size() > MAX_ENTRIES) m_paths.removeLast();
}

void MruManager::rebuildMenu()
{
    m_menu->clear();
    if (m_paths.isEmpty()) {
        QAction* placeholder = m_menu->addAction(tr("(empty)"));
        placeholder->setEnabled(false);
        return;
    }
    int idx = 0;
    for (const QString& path : std::as_const(m_paths)) {
        const QString label = QStringLiteral("&%1  %2")
                                .arg(++idx)
                                .arg(QFileInfo(path).fileName());
        QAction* a = m_menu->addAction(label);
        a->setToolTip(path);
        connect(a, &QAction::triggered, this, [this, path]() {
            emit openRequested(path);
        });
    }
}

} // namespace jbead
