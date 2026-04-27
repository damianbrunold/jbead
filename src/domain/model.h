#pragma once

#include "beadfield.h"
#include "beadundo.h"
#include "selection.h"

#include <QColor>
#include <QList>
#include <QObject>
#include <QSet>
#include <QString>

#include <array>
#include <cstdint>

namespace jbead {

class Memento;

/*  Central document model. Faithful port of legacy ch.jbead.Model;
    the eight ModelListener callbacks become Qt signals (pointChanged,
    modelChanged, colorChanged, colorsChanged, scrollChanged,
    shiftChanged, zoomChanged, repeatChanged).

    Notes vs. legacy:
      - Color storage is QColor / QList<QColor>.
      - File path is QString rather than java.io.File.
      - getCurrentDirectory()'s Swing JFileChooser fallback is
        replaced by QStandardPaths::DocumentsLocation.
      - clear() reads "user/author" + "user/organization" from
        Settings; identical category and key names as legacy.       */
class Model : public QObject
{
    Q_OBJECT
public:
    static constexpr int ZOOM_NORMAL = 3;

    explicit Model(QObject* parent = nullptr);

    // ---- field accessors ----------------------------------------
    int width()  const { return m_field.width(); }
    int height() const { return m_field.height(); }

    std::int8_t get(BeadPoint p) const { return m_field.get(p); }
    std::int8_t get(int idx)     const { return m_field.get(idx); }
    void set(BeadPoint p, std::int8_t v);
    void set(int idx, std::int8_t v);
    bool isValidIndex(int idx) const { return m_field.isValidIndex(idx); }

    int  indexOf(BeadPoint p) const { return m_field.indexOf(p); }
    BeadPoint pointAt(int idx) const { return m_field.pointAt(idx); }

    BeadRect fullRect() const { return m_field.fullRect(); }
    BeadRect rect(int starty, int endy) const { return m_field.rect(starty, endy); }
    BeadRect usedRect() const;
    int      usedHeight() const;
    bool     equalRows(int j, int k) const;

    void setWidth(int width);
    void setHeight(int height);

    // ---- color palette ------------------------------------------
    QColor      color(int index) const { return m_colors.at(index); }
    int         colorCount() const     { return m_colors.size(); }
    void        setColor(int index, const QColor& color);
    std::int8_t selectedColor() const  { return m_colorIndex; }
    void        setSelectedColor(std::int8_t colorIndex);
    QList<QColor> colors() const       { return m_colors; }

    QSet<std::int8_t> usedColors() const;
    void              compactifyColors();

    // ---- editing ------------------------------------------------
    void insertRow();
    void deleteRow();
    void drawLine(BeadPoint begin, BeadPoint end);
    void fillLine(BeadPoint pt);
    void setPoint(BeadPoint pt);
    void arrangeSelection(const Selection& sel, int copies, int offset);
    void mirrorHorizontal(const BeadRect& rect);
    void mirrorVertical(const BeadRect& rect);
    void rotate(const BeadRect& rect);
    void deleteRect(const BeadRect& rect);

    BeadField copy() const;

    // ---- scroll / shift / zoom ----------------------------------
    int  scroll() const { return m_scroll; }
    void setScroll(int scroll);

    int  shift() const { return m_shift; }
    void shiftRight();
    void shiftLeft();

    int  gridx() const { return m_gridx; }
    int  gridy() const { return m_gridy; }
    int  zoomIndex() const { return m_zoomIndex; }
    void zoomIn();
    void zoomOut();
    void zoomNormal();
    bool isNormalZoom() const { return m_zoomIndex == ZOOM_NORMAL; }

    // ---- repeat -------------------------------------------------
    int  repeat() const { return m_repeat; }
    bool isRepeatDirty() const { return m_repeatDirty; }
    void setRepeatDirty() { m_repeatDirty = true; }
    void updateRepeat();

    // ---- undo/redo ----------------------------------------------
    void snapshot();
    void prepareSnapshot();
    void undo();
    void redo();
    bool canUndo() const { return m_undo.canUndo(); }
    bool canRedo() const { return m_undo.canRedo(); }

    // ---- file state ---------------------------------------------
    QString filePath() const { return m_file; }
    void    setFilePath(const QString& path);
    QString currentDirectory() const;
    bool    isSaved() const { return m_saved; }
    void    setSaved() { m_saved = true; }
    bool    isModified() const { return m_modified; }
    void    setModified(bool modified = true) { m_modified = modified; }
    void    clear();

    // ---- metadata -----------------------------------------------
    QString author() const { return m_author; }
    void    setAuthor(const QString& author);
    QString organization() const { return m_organization; }
    void    setOrganization(const QString& organization);
    QString notes() const { return m_notes; }
    void    setNotes(const QString& notes) { m_notes = notes; }

    // ---- corrected (hexagonal-offset) coordinates ---------------
    BeadPoint correct(BeadPoint pt) const;
    int       correctedIndex(BeadPoint pt) const;

    // ---- file I/O bridge ---------------------------------------
    void saveTo(Memento& memento) const;
    void loadFrom(const Memento& memento);

signals:
    void pointChanged(BeadPoint pt);
    void modelChanged();
    void colorChanged(int colorIndex);
    void colorsChanged();
    /*  Fired when the selected palette index changes (i.e. the
        active drawing colour). Distinct from colorChanged, which
        means a slot's RGB value was edited.                      */
    void selectedColorChanged(int colorIndex);
    void scrollChanged(int scroll);
    void shiftChanged(int shift);
    void zoomChanged(int gridx, int gridy);
    void repeatChanged(int repeat);

private:
    void defaultColors();
    void normalizeShift();
    void fillDefaultColorsUp();
    void updateZoom();
    void setRepeat(int r);
    int  calcRepeat(int usedheight) const;
    std::int8_t firstUsedAfter(const QList<bool>& inUse, std::int8_t index) const;
    void moveColor(std::int8_t src, std::int8_t dest);

    BeadUndo            m_undo;
    BeadField           m_field;
    QList<QColor>       m_colors;
    std::int8_t         m_colorIndex = 1;
    int                 m_gridx = 0;
    int                 m_gridy = 0;
    std::array<int, 8>  m_zoomTable{6, 8, 10, 12, 14, 16, 18, 20};
    int                 m_zoomIndex = ZOOM_NORMAL;
    int                 m_scroll = 0;
    int                 m_shift = 0;
    QString             m_file;
    QString             m_unnamed;
    bool                m_repeatDirty = false;
    int                 m_repeat = 0;
    bool                m_saved = false;
    bool                m_modified = false;
    QString             m_author;
    QString             m_organization;
    QString             m_notes;
};

} // namespace jbead
