#pragma once

#include <QObject>
#include <QStringList>

class QAction;
class QMenu;

namespace jbead {

/*  Most-recently-used file list, persisted across runs in
    QSettings ("Files/recentFiles") and surfaced as a "Recent Files"
    submenu in the File menu. Capacity is fixed at 6, matching the
    legacy JBead app's six action.file.mru0..5 slots. Selecting an
    entry emits openRequested(path); MainWindow listens and routes
    the path through its existing maybeSave/loadFrom flow.        */
class MruManager : public QObject
{
    Q_OBJECT
public:
    static constexpr int MAX_ENTRIES = 6;

    /*  `submenu` is owned by the caller — MruManager only
        populates / clears its actions in response to addPath /
        load.                                                      */
    MruManager(QMenu* submenu, QObject* parent = nullptr);

    void load();
    void save() const;

    /*  Push a freshly-opened or freshly-saved file to the top of
        the list. Existing entries with the same canonical path
        are removed before insertion (move-to-front semantics).  */
    void addPath(const QString& path);

    QStringList paths() const { return m_paths; }

signals:
    void openRequested(const QString& path);

private:
    void rebuildMenu();
    void prune();

    QMenu*       m_menu;
    QStringList  m_paths;
};

} // namespace jbead
