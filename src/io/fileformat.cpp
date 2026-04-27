#include "fileformat.h"

#include "dbbmemento.h"
#include "jbeadmemento.h"
#include "domain/model.h"

#include <QFile>
#include <QFileInfo>
#include <QObject>

#include <stdexcept>

namespace jbead {

namespace {
constexpr char DBB_MAGIC[] = "DB-BEAD/01:\r\n";
constexpr int  DBB_MAGIC_LEN = sizeof(DBB_MAGIC) - 1;
} // namespace

FileFormat::Kind FileFormat::detectFromPath(const QString& path)
{
    return path.endsWith(QStringLiteral(".dbb"), Qt::CaseInsensitive)
        ? Kind::Dbb : Kind::JBead;
}

void FileFormat::save(const QString& path, const Model& model)
{
    QFile out(path);
    if (!out.open(QIODevice::WriteOnly | QIODevice::Truncate))
        throw std::runtime_error(out.errorString().toStdString());

    if (detectFromPath(path) == Kind::Dbb) {
        out.write(DBB_MAGIC, DBB_MAGIC_LEN);
        DbbMemento m;
        model.saveTo(m);
        m.save(&out);
    } else {
        JBeadMemento m;
        model.saveTo(m);
        m.save(&out);
    }
}

void FileFormat::load(const QString& path, Model& model)
{
    QFile in(path);
    if (!in.open(QIODevice::ReadOnly))
        throw std::runtime_error(in.errorString().toStdString());

    if (detectFromPath(path) == Kind::Dbb) {
        const QByteArray magic = in.read(DBB_MAGIC_LEN);
        if (magic != QByteArray(DBB_MAGIC, DBB_MAGIC_LEN))
            throw std::runtime_error("invalid .dbb header");
        DbbMemento m;
        m.load(&in);
        model.loadFrom(m);
    } else {
        JBeadMemento m;
        m.load(&in);
        model.loadFrom(m);
    }

    model.setFilePath(path);
    model.setSaved();
    model.setModified(false);
}

QString FileFormat::jbeadNameFilter()
{
    return QObject::tr("JBead files (*.jbb)");
}

QString FileFormat::dbbNameFilter()
{
    return QObject::tr("DB-BEAD files (*.dbb)");
}

QString FileFormat::combinedNameFilter()
{
    return QObject::tr("JBead and DB-BEAD files (*.jbb *.dbb)");
}

QStringList FileFormat::allNameFilters()
{
    return { combinedNameFilter(), jbeadNameFilter(), dbbNameFilter() };
}

} // namespace jbead
