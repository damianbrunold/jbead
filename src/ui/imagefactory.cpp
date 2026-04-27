#include "imagefactory.h"

namespace jbead {

QString ImageFactory::resourcePath(const QString& name)
{
    return QStringLiteral(":/icons/") + name + QStringLiteral(".png");
}

QIcon ImageFactory::icon(const QString& name)
{
    return QIcon(resourcePath(name));
}

} // namespace jbead
