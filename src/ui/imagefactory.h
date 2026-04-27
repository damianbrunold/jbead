#pragma once

#include <QIcon>
#include <QString>

namespace jbead {

/*  Icon-resource lookup, modelled on legacy ImageFactory. The PNGs
    were copied verbatim into resources/icons/ during Phase 1 and
    registered via icons.qrc; they live under the ":/icons/" prefix
    with their original "file.new.png" / "view.zoomin.png" /
    "tool.pencil.png" naming. Callers pass the bare name without
    extension, matching the legacy API.                           */
class ImageFactory
{
public:
    static QIcon icon(const QString& name);
    static QString resourcePath(const QString& name);
};

} // namespace jbead
