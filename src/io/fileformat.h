#pragma once

#include <QString>
#include <QStringList>

namespace jbead {

class Model;
class Memento;

/*  High-level open / save dispatcher. Picks .jbb or .dbb based on
    file extension. Throws std::runtime_error / FileFormatException
    on I/O or parse errors. The Model is mutated in place; on a
    successful load() the caller should also reset its own per-view
    UI state (legacy JBeadFrame.loadFrom / clearSelection) — that
    bridge will be added in Phase 3 once MainWindow exists.        */
class FileFormat
{
public:
    enum class Kind { JBead, Dbb };

    static Kind detectFromPath(const QString& path);

    /*  Save model to path; format inferred from extension. Sets
        model.setSaved() and clears the modified flag on success.  */
    static void save(const QString& path, const Model& model);

    /*  Load path into the (already-cleared) model. Caller is
        responsible for calling Model::clear() first if it wants a
        full reset; load() just populates from the on-disk memento. */
    static void load(const QString& path, Model& model);

    /*  Qt-style name filters, suitable for QFileDialog.          */
    static QString jbeadNameFilter();
    static QString dbbNameFilter();
    static QString combinedNameFilter();
    static QStringList allNameFilters();
};

} // namespace jbead
