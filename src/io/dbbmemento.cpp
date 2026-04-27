#include "dbbmemento.h"

#include "jbeadstream.h"
#include "domain/beadsymbols.h"

#include <stdexcept>

namespace jbead {

void DbbMemento::save(QIODevice* out) const
{
    JBeadOutputStream s(out);
    s.writeInt(m_width);

    /*  25000 bytes of pattern data, truncated or zero-padded.    */
    const int copy = qMin(DBB_FIELD_SIZE, int(m_data.size()));
    s.write(m_data.left(copy));
    if (copy < DBB_FIELD_SIZE) {
        s.write(QByteArray(DBB_FIELD_SIZE - copy, '\0'));
    }

    /*  Exactly 10 palette entries (40 bytes). Missing slots filled
        with opaque white, matching legacy.                        */
    for (int i = 0; i < 10; ++i) {
        s.writeColor(i < m_colors.size() ? m_colors.at(i) : QColor(255, 255, 255));
    }

    s.write(quint8(m_colorIndex));
    s.writeInt(m_zoomIndex);
    s.writeInt(m_shift);
    s.writeInt(m_scroll);
    s.writeBool(m_draftVisible);
    s.writeBool(m_correctedVisible);
    s.writeBool(m_simulationVisible);
    /*  reportVisible / selectedTool / drawColors / drawSymbols /
        symbols are intentionally not persisted by .dbb.           */
}

void DbbMemento::load(QIODevice* in)
{
    JBeadInputStream s(in);
    m_width = s.readInt();
    if (m_width <= 0)
        throw std::runtime_error("file format error: width was non-positive");

    m_height = (DBB_FIELD_SIZE + m_width - 1) / m_width;
    m_data = QByteArray(m_width * m_height, '\0');
    /*  Field is exactly 25000 bytes on disk; the trailing
        (m_width*m_height - DBB_FIELD_SIZE) cells stay zero-initialised. */
    const QByteArray payload = s.read(DBB_FIELD_SIZE);
    std::memcpy(m_data.data(), payload.constData(), DBB_FIELD_SIZE);

    m_colors.clear();
    m_colors.append(s.readBackgroundColor());
    for (int i = 1; i < 10; ++i) {
        m_colors.append(s.readColor());
    }

    m_colorIndex = static_cast<std::int8_t>(s.readByte());
    m_zoomIndex  = s.readInt();
    m_shift      = s.readInt();
    m_scroll     = s.readInt();
    if (m_zoomIndex < 0 || m_shift < 0 || m_scroll < 0)
        throw std::runtime_error("file format error: negative int field");

    m_draftVisible      = s.readBool();
    m_correctedVisible  = s.readBool();
    m_simulationVisible = s.readBool();

    /*  Defaults for fields not stored in the .dbb wire format.   */
    m_reportVisible = true;
    m_drawColors    = true;
    m_drawSymbols   = false;
    m_symbols       = BeadSymbols::DEFAULT_SYMBOLS;
    m_selectedTool  = QStringLiteral("pencil");
    m_author.clear();
    m_organization.clear();
}

} // namespace jbead
