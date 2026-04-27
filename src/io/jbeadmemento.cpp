#include "jbeadmemento.h"

#include "jbeadstream.h"
#include "objectmodel.h"
#include "domain/beadsymbols.h"

namespace jbead {

void JBeadMemento::save(QIODevice* out) const
{
    /*  File layout (S-expression):

          (jbb
            (version 1)
            (author "...")
            (organization "...")
            (notes "...")
            (colors
              (rgb r g b a)
              ... one per palette entry ...)
            (view
              (draft-visible bool) (corrected-visible bool)
              (simulation-visible bool) (report-visible bool)
              (selected-tool "pencil"|"select"|...)
              (selected-color int) (zoom int)
              (scroll int) (shift int)
              (draw-colors bool) (draw-symbols bool)
              (symbols "..."))
            (model
              (row b0 b1 ... b_{width-1})
              ... height rows total ...))                          */

    ObjectModel om(QStringLiteral("jbb"));

    om.add(QStringLiteral("version"),      {VERSION});
    om.add(QStringLiteral("author"),       {m_author});
    om.add(QStringLiteral("organization"), {m_organization});
    om.add(QStringLiteral("notes"),        {m_notes});

    for (const QColor& c : m_colors) {
        om.add(QStringLiteral("colors/rgb"),
               {c.red(), c.green(), c.blue(), c.alpha()});
    }

    om.add(QStringLiteral("view/draft-visible"),      {m_draftVisible});
    om.add(QStringLiteral("view/corrected-visible"),  {m_correctedVisible});
    om.add(QStringLiteral("view/simulation-visible"), {m_simulationVisible});
    om.add(QStringLiteral("view/report-visible"),     {m_reportVisible});
    om.add(QStringLiteral("view/selected-tool"),      {m_selectedTool});
    om.add(QStringLiteral("view/selected-color"),     {int(m_colorIndex)});
    om.add(QStringLiteral("view/zoom"),               {m_zoomIndex});
    om.add(QStringLiteral("view/scroll"),             {m_scroll});
    om.add(QStringLiteral("view/shift"),              {m_shift});
    om.add(QStringLiteral("view/draw-colors"),        {m_drawColors});
    om.add(QStringLiteral("view/draw-symbols"),       {m_drawSymbols});
    om.add(QStringLiteral("view/symbols"),            {m_symbols});

    for (int j = 0; j < m_height; ++j) {
        QList<QVariant> row;
        row.reserve(m_width);
        for (int i = 0; i < m_width; ++i) {
            row.append(int(static_cast<std::int8_t>(m_data.at(j * m_width + i))));
        }
        om.add(QStringLiteral("model/row"), row);
    }

    JBeadOutputStream s(out);
    s.write(om.toString());
}

void JBeadMemento::load(QIODevice* in)
{
    JBeadInputStream s(in);
    const ObjectModel om = ObjectModel::fromData(s.readAll(), QStringLiteral("jbb"));

    /*  version is currently always 1; the legacy upgrade() path is
        a no-op stub. Honour the same intent: read the field and
        ignore the version number for now.                         */
    (void) om.intValue(QStringLiteral("version"), 1);
    m_author       = om.stringValue(QStringLiteral("author"), QString());
    m_organization = om.stringValue(QStringLiteral("organization"), QString());
    m_notes        = om.stringValue(QStringLiteral("notes"), QString());

    m_colors.clear();
    for (Node* n : om.getAll(QStringLiteral("colors/rgb"))) {
        if (!n->isLeaf()) continue;
        Leaf* leaf = static_cast<Leaf*>(n);
        const int r = leaf->intValue(0);
        const int g = leaf->intValue(1);
        const int b = leaf->intValue(2);
        const int a = leaf->valueCount() == 4 ? leaf->intValue(3) : 255;
        m_colors.append(QColor(r, g, b, a));
    }

    m_draftVisible      = om.boolValue(QStringLiteral("view/draft-visible"),      true);
    m_correctedVisible  = om.boolValue(QStringLiteral("view/corrected-visible"),  true);
    m_simulationVisible = om.boolValue(QStringLiteral("view/simulation-visible"), true);
    m_reportVisible     = om.boolValue(QStringLiteral("view/report-visible"),     true);
    m_colorIndex   = static_cast<std::int8_t>(om.intValue(QStringLiteral("view/selected-color"), 1));
    m_selectedTool = om.stringValue(QStringLiteral("view/selected-tool"), QStringLiteral("pencil"));
    m_zoomIndex    = om.intValue(QStringLiteral("view/zoom"),   2);
    m_scroll       = om.intValue(QStringLiteral("view/scroll"), 0);
    m_shift        = om.intValue(QStringLiteral("view/shift"),  0);
    m_drawColors   = om.boolValue(QStringLiteral("view/draw-colors"),  true);
    m_drawSymbols  = om.boolValue(QStringLiteral("view/draw-symbols"), false);
    m_symbols      = om.stringValue(QStringLiteral("view/symbols"), BeadSymbols::DEFAULT_SYMBOLS);

    const QList<Node*> rows = om.getAll(QStringLiteral("model/row"));
    m_height = rows.size();
    m_width  = rows.isEmpty() ? 0 : static_cast<Leaf*>(rows.first())->valueCount();
    m_data.resize(m_width * m_height);
    int idx = 0;
    for (Node* row : rows) {
        Leaf* leaf = static_cast<Leaf*>(row);
        for (int i = 0; i < m_width; ++i) {
            m_data[idx++] = static_cast<char>(leaf->intValue(i) & 0xff);
        }
    }
}

} // namespace jbead
