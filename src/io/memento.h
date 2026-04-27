#pragma once

#include <QByteArray>
#include <QColor>
#include <QIODevice>
#include <QList>
#include <QString>

#include <cstdint>

namespace jbead {

/*  Format-agnostic snapshot exchanged between Model and the file
    format readers/writers. JBeadMemento (.jbb) and DbbMemento (.dbb)
    extend this with their own load() / save() implementations.  */
class Memento
{
public:
    virtual ~Memento() = default;

    virtual void save(QIODevice* out) const = 0;
    virtual void load(QIODevice* in)        = 0;

    /*  Format constraints. .jbb tolerates the full 32-color palette;
        .dbb caps at 10 and forces compactification.               */
    virtual int  maxSupportedColors() const { return 127; }
    virtual bool requiresCompactification() const { return false; }

    int  width()  const { return m_width; }
    int  height() const { return m_height; }
    void setWidth(int v)  { m_width = v; }
    void setHeight(int v) { m_height = v; }

    const QByteArray& data() const { return m_data; }
    void setData(const QByteArray& d) { m_data = d; }

    QList<QColor> colors() const { return m_colors; }
    void setColors(const QList<QColor>& c) { m_colors = c; }

    std::int8_t colorIndex() const { return m_colorIndex; }
    void setColorIndex(std::int8_t v) { m_colorIndex = v; }

    int zoomIndex() const { return m_zoomIndex; }
    void setZoomIndex(int v) { m_zoomIndex = v; }

    int scroll() const { return m_scroll; }
    void setScroll(int v) { m_scroll = v; }

    int shift() const { return m_shift; }
    void setShift(int v) { m_shift = v; }

    QString author() const { return m_author; }
    void setAuthor(const QString& v) { m_author = v; }

    QString organization() const { return m_organization; }
    void setOrganization(const QString& v) { m_organization = v; }

    QString notes() const { return m_notes; }
    void setNotes(const QString& v) { m_notes = v; }

    QString symbols() const { return m_symbols; }
    void setSymbols(const QString& v) { m_symbols = v; }

    QString selectedTool() const { return m_selectedTool; }
    void setSelectedTool(const QString& v) { m_selectedTool = v; }

    bool draftVisible() const      { return m_draftVisible; }
    bool correctedVisible() const  { return m_correctedVisible; }
    bool simulationVisible() const { return m_simulationVisible; }
    bool reportVisible() const     { return m_reportVisible; }
    bool drawColors() const        { return m_drawColors; }
    bool drawSymbols() const       { return m_drawSymbols; }

    void setDraftVisible(bool v)      { m_draftVisible = v; }
    void setCorrectedVisible(bool v)  { m_correctedVisible = v; }
    void setSimulationVisible(bool v) { m_simulationVisible = v; }
    void setReportVisible(bool v)     { m_reportVisible = v; }
    void setDrawColors(bool v)        { m_drawColors = v; }
    void setDrawSymbols(bool v)       { m_drawSymbols = v; }

protected:
    int           m_width  = 0;
    int           m_height = 0;
    QByteArray    m_data;
    QList<QColor> m_colors;
    std::int8_t   m_colorIndex = 1;
    int           m_zoomIndex  = 2;
    int           m_scroll     = 0;
    int           m_shift      = 0;
    QString       m_author;
    QString       m_organization;
    QString       m_notes;
    QString       m_symbols;
    QString       m_selectedTool = QStringLiteral("pencil");
    bool          m_draftVisible      = true;
    bool          m_correctedVisible  = true;
    bool          m_simulationVisible = true;
    bool          m_reportVisible     = true;
    bool          m_drawColors        = true;
    bool          m_drawSymbols       = false;
};

} // namespace jbead
