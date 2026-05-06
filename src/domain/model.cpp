#include "model.h"

#include "beadsymbols.h"
#include "defaultcolors.h"
#include "rectiterator.h"
#include "segmentiterator.h"
#include "settings.h"
#include "io/memento.h"

#include <QSettings>

#include <QFileInfo>
#include <QStandardPaths>

namespace jbead {

Model::Model(QObject* parent)
    : QObject(parent),
      m_unnamed(tr("unnamed")),
      m_file(tr("unnamed")),
      m_symbols(BeadSymbols::symbols())
{
    m_field.clear();
    defaultColors();
    m_gridx = m_gridy = m_zoomTable[m_zoomIndex];
}

void Model::defaultColors()
{
    m_colors = DefaultColors::palette();
}

// ---- field --------------------------------------------------------

void Model::set(BeadPoint pt, std::int8_t value)
{
    m_field.set(pt, value);
    emit pointChanged(pt);
}

void Model::set(int idx, std::int8_t value)
{
    m_field.set(idx, value);
    emit pointChanged(m_field.pointAt(idx));
}

BeadRect Model::usedRect() const
{
    const int h = usedHeight();
    if (h == 0) return BeadRect::empty();
    return m_field.rect(0, h - 1);
}

int Model::usedHeight() const
{
    int used = 0;
    RectIterator it(m_field.fullRect());
    while (it.hasNext()) {
        const BeadPoint pt = it.next();
        if (m_field.get(pt) > 0) used = pt.y() + 1;
    }
    return used;
}

bool Model::equalRows(int j, int k) const
{
    for (int i = 0; i < width(); ++i) {
        if (get(BeadPoint(i, j)) != get(BeadPoint(i, k))) return false;
    }
    return true;
}

void Model::setWidth(int w)
{
    snapshot();
    m_field.setWidth(w);
    normalizeShift();
    setModified();
    setRepeatDirty();
    emit modelChanged();
}

void Model::setHeight(int h)
{
    m_field.setHeight(h);
    emit modelChanged();
}

// ---- colors -------------------------------------------------------

void Model::setColor(int index, const QColor& color)
{
    snapshot();
    m_colors[index] = color;
    setModified();
    emit colorChanged(index);
}

void Model::setSelectedColor(std::int8_t colorIndex)
{
    /*  No-op when nothing changes — keeps the toolbar /
        palette-editor / status-bar listeners from churning
        through identical "selection unchanged" repaints.         */
    if (m_colorIndex == colorIndex) return;
    m_colorIndex = colorIndex;
    emit selectedColorChanged(int(colorIndex));
}

QSet<std::int8_t> Model::usedColors() const
{
    QSet<std::int8_t> used;
    /*  Note: legacy iterates 0..lastIndex-1 (exclusive). Mirror that
        — covers all but the very last cell, which is fine for a
        15x800 default field. Bug-for-bug compatibility.            */
    for (int i = 0; i < m_field.lastIndex(); ++i) {
        used.insert(m_field.get(i));
    }
    return used;
}

std::int8_t Model::firstUsedAfter(const QList<bool>& inUse, std::int8_t index) const
{
    for (std::int8_t i = static_cast<std::int8_t>(index + 1); i < m_colors.size(); ++i) {
        if (inUse[i]) return i;
    }
    return -1;
}

void Model::moveColor(std::int8_t src, std::int8_t dest)
{
    m_field.replaceColor(src, dest);
    QColor tmp = m_colors[src];
    m_colors[src] = m_colors[dest];
    m_colors[dest] = tmp;
    emit colorsChanged();
}

void Model::compactifyColors()
{
    snapshot();
    const QSet<std::int8_t> used = usedColors();
    QList<bool> inUse(m_colors.size(), false);
    for (std::int8_t c : used) {
        if (c >= 0 && c < m_colors.size()) inUse[c] = true;
    }
    for (std::int8_t color = 0; color < m_colors.size(); ++color) {
        if (inUse[color]) continue;
        std::int8_t use = firstUsedAfter(inUse, color);
        if (use == -1) break;
        moveColor(use, color);
        inUse[color] = true;
        inUse[use]   = false;
    }
}

// ---- editing ------------------------------------------------------

void Model::insertRow()
{
    snapshot();
    m_field.insertRow();
    setRepeatDirty();
    setModified();
    emit modelChanged();
}

void Model::deleteRow()
{
    snapshot();
    m_field.deleteRow();
    setRepeatDirty();
    setModified();
    emit modelChanged();
}

void Model::drawLine(BeadPoint begin, BeadPoint end)
{
    snapshot();
    setModified();
    SegmentIterator it(begin.scrolled(m_scroll), end.scrolled(m_scroll));
    /*  Defensive bound check: clamp every cell to the field rect
        before writing. Prevents an out-of-bounds set() from a
        snap modifier or a buggy iterator from corrupting memory
        (the previous segfault report traced back to exactly this
        path before the SegmentIterator Bresenham fix).            */
    while (it.hasNext()) {
        const BeadPoint pt = it.next();
        if (pt.x() < 0 || pt.x() >= width()) continue;
        if (pt.y() < 0 || pt.y() >= height()) continue;
        set(pt, m_colorIndex);
    }
    setRepeatDirty();
}

void Model::fillLine(BeadPoint pt)
{
    snapshot();
    setModified();
    pt = pt.scrolled(m_scroll);
    const std::int8_t color      = m_colorIndex;
    const std::int8_t background = get(pt);
    const int startIndex = indexOf(pt);
    for (int index = startIndex; index >= 0; --index) {
        if (m_field.get(index) != background) break;
        set(index, color);
    }
    const int last = indexOf(BeadPoint(width() - 1, usedHeight() - 1));
    for (int index = startIndex + 1; index <= last; ++index) {
        if (m_field.get(index) != background) break;
        set(index, color);
    }
    setRepeatDirty();
}

void Model::setPoint(BeadPoint pt)
{
    snapshot();
    setModified();
    pt = pt.scrolled(m_scroll);
    if (m_field.get(pt) == m_colorIndex) {
        set(pt, 0);
    } else {
        set(pt, m_colorIndex);
    }
    setRepeatDirty();
}

void Model::arrangeSelection(const Selection& sel, int copies, int offset)
{
    snapshot();
    setModified();
    const BeadField buffer = copy();
    const BeadRect rect(sel.rect().begin(), sel.rect().end());
    RectIterator it(rect);
    while (it.hasNext()) {
        const BeadPoint pt = it.next();
        const std::int8_t c = buffer.get(pt.scrolled(m_scroll));
        if (c == 0) continue;
        int idx = m_field.indexOf(pt.scrolled(m_scroll));
        for (int k = 0; k < copies; ++k) {
            idx += offset;
            if (isValidIndex(idx)) set(idx, c);
        }
    }
    setRepeatDirty();
}

void Model::mirrorHorizontal(const BeadRect& r)
{
    snapshot();
    m_field.mirrorHorizontal(r.scrolled(m_scroll));
    setModified();
    setRepeatDirty();
    emit modelChanged();
}

void Model::mirrorVertical(const BeadRect& r)
{
    snapshot();
    m_field.mirrorVertical(r.scrolled(m_scroll));
    setModified();
    setRepeatDirty();
    emit modelChanged();
}

void Model::rotate(const BeadRect& r)
{
    if (!r.isSquare()) return;
    snapshot();
    m_field.rotate(r.scrolled(m_scroll));
    setModified();
    setRepeatDirty();
    emit modelChanged();
}

void Model::deleteRect(const BeadRect& r)
{
    snapshot();
    m_field.deleteRect(r.scrolled(m_scroll));
    setModified();
    setRepeatDirty();
    emit modelChanged();
}

BeadField Model::copy() const
{
    BeadField c;
    c.copyFrom(m_field);
    return c;
}

// ---- scroll / shift / zoom ---------------------------------------

void Model::setScroll(int scroll)
{
    m_scroll = scroll;
    emit scrollChanged(scroll);
}

void Model::shiftRight()
{
    ++m_shift;
    normalizeShift();
    emit shiftChanged(m_shift);
}

void Model::shiftLeft()
{
    --m_shift;
    normalizeShift();
    emit shiftChanged(m_shift);
}

void Model::normalizeShift()
{
    while (m_shift < 0)        m_shift += width();
    while (m_shift > width())  m_shift -= width();
}

void Model::updateZoom()
{
    if (m_zoomIndex >= int(m_zoomTable.size()) - 1) return;
    m_gridx = m_gridy = m_zoomTable[m_zoomIndex];
    emit zoomChanged(m_gridx, m_gridy);
}

void Model::zoomIn()
{
    if (m_zoomIndex >= int(m_zoomTable.size()) - 1) return;
    ++m_zoomIndex;
    m_gridx = m_gridy = m_zoomTable[m_zoomIndex];
    emit zoomChanged(m_gridx, m_gridy);
}

void Model::zoomOut()
{
    if (m_zoomIndex <= 0) return;
    --m_zoomIndex;
    m_gridx = m_gridy = m_zoomTable[m_zoomIndex];
    emit zoomChanged(m_gridx, m_gridy);
}

void Model::zoomNormal()
{
    if (isNormalZoom()) return;
    m_zoomIndex = ZOOM_NORMAL;
    m_gridx = m_gridy = m_zoomTable[m_zoomIndex];
    emit zoomChanged(m_gridx, m_gridy);
}

// ---- repeat -------------------------------------------------------

void Model::setRepeat(int r)
{
    m_repeatDirty = false;
    m_repeat = r;
    emit repeatChanged(r);
}

void Model::updateRepeat()
{
    const int h = usedHeight();
    if (h == 0) setRepeat(0);
    else        setRepeat(calcRepeat(h));
}

int Model::calcRepeat(int usedheight) const
{
    const int total = usedheight * m_field.width();
    for (int i = 1; i < total; ++i) {
        if (m_field.get(i) == m_field.get(0)) {
            bool ok = true;
            for (int k = i + 1; k < total; ++k) {
                if (m_field.get((k - i) % i) != m_field.get(k)) { ok = false; break; }
            }
            if (ok) return i;
        }
    }
    return total;
}

// ---- undo/redo ----------------------------------------------------

void Model::snapshot()
{
    m_undo.snapshot(m_field, m_colors, m_symbols, m_modified);
}
void Model::prepareSnapshot()
{
    m_undo.prepareSnapshot(m_field, m_colors, m_symbols, m_modified);
}

void Model::undo()
{
    m_undo.undo(m_field, m_colors, m_symbols);
    m_modified = m_undo.isModified();
    BeadSymbols::setSymbols(m_symbols);
    setRepeatDirty();
    emit colorsChanged();
    emit modelChanged();
}

void Model::redo()
{
    m_undo.redo(m_field, m_colors, m_symbols);
    m_modified = m_undo.isModified();
    BeadSymbols::setSymbols(m_symbols);
    setRepeatDirty();
    emit colorsChanged();
    emit modelChanged();
}

// ---- file state ---------------------------------------------------

void Model::setFilePath(const QString& path)
{
    m_file = path;
    emit modelChanged();
}

QString Model::currentDirectory() const
{
    if (m_saved) return QFileInfo(m_file).absolutePath();
    return QStandardPaths::writableLocation(QStandardPaths::DocumentsLocation);
}

void Model::clear()
{
    m_undo.clear();
    m_field.clear();
    m_repeat = 0;
    m_colorIndex = 1;
    defaultColors();
    /*  Sync the cached pixel dimensions to the new zoom level.
        Without this gridx/gridy keep the constructor's value
        (m_zoomTable[ZOOM_NORMAL] == 12) while zoomIndex says 2,
        and the next zoomIn() jumps to the same pixel size as
        before, breaking the canvas re-layout. The zoom index itself
        comes from the saved preference (View/ZoomIndex); loaded
        patterns override it via loadFrom.                          */
    {
        QSettings qs;
        const int saved = qs.value(QStringLiteral("View/ZoomIndex"), 2).toInt();
        m_zoomIndex = qBound(0, saved, int(m_zoomTable.size()) - 1);
    }
    m_gridx = m_gridy = m_zoomTable[m_zoomIndex];
    m_scroll = 0;
    m_shift = 0;
    Settings settings;
    settings.setCategory(QStringLiteral("user"));
    m_author       = settings.loadString(QStringLiteral("author"), QString());
    m_organization = settings.loadString(QStringLiteral("organization"), QString());
    /*  New patterns inherit the user's saved symbol palette (legacy
        initDefaultSymbols semantics). Falls back to DEFAULT_SYMBOLS
        when no preference has been saved yet.                       */
    {
        QSettings qs;
        m_symbols = qs.value(QStringLiteral("Environment/Symbols"),
                             BeadSymbols::DEFAULT_SYMBOLS).toString();
        if (m_symbols.isEmpty()) m_symbols = BeadSymbols::DEFAULT_SYMBOLS;
        BeadSymbols::setSymbols(m_symbols);
        /*  View toggles default to the user's last choice (View/Draw*
            keys persisted by MainWindow::doViewDrawModeChanged).
            Loaded files override these via loadFrom; this only
            affects new / cleared patterns.                          */
        m_drawColors  = qs.value(QStringLiteral("View/DrawColors"),  true).toBool();
        m_drawSymbols = qs.value(QStringLiteral("View/DrawSymbols"), false).toBool();
    }
    m_file = m_unnamed;
    m_saved = false;
    m_modified = false;
    emit modelChanged();
}

// ---- metadata -----------------------------------------------------

void Model::setAuthor(const QString& a)        { m_author = a;       emit modelChanged(); }
void Model::setOrganization(const QString& o)  { m_organization = o; emit modelChanged(); }

void Model::setSymbols(const QString& s)
{
    if (m_symbols == s) return;
    snapshot();
    m_symbols = s;
    /*  BeadPainter / StripPainter / ReportPanel etc. read symbols
        through the BeadSymbols singleton (no Model pointer at hand),
        so mirror the change there too.                              */
    BeadSymbols::setSymbols(s);
    setModified();
    emit modelChanged();
}

// ---- corrected (hexagonal) coordinates ---------------------------

BeadPoint Model::correct(BeadPoint pt) const
{
    int idx = pt.x() + (pt.y() + m_scroll) * width();
    const int m1 = width();
    const int m2 = m1 + 1;
    int k = 0;
    int m = m1;
    while (idx >= m) {
        idx -= m;
        ++k;
        m = (k % 2 == 0) ? m1 : m2;
    }
    return BeadPoint(idx, k - m_scroll);
}

int Model::correctedIndex(BeadPoint pt) const
{
    const int m1 = width();
    const int m2 = m1 + 1;
    const int j  = pt.y() + m_scroll;
    if (j % 2 == 0) {
        return (j / 2) * (m1 + m2) + pt.x();
    } else {
        return (j / 2 + 1) * m1 + (j / 2) * m2 + pt.x();
    }
}

// ---- memento bridge ----------------------------------------------

void Model::fillDefaultColorsUp()
{
    const QList<QColor> palette = DefaultColors::palette();
    while (m_colors.size() < DefaultColors::NUMBER_OF_COLORS) {
        m_colors.append(palette.at(m_colors.size()));
    }
}

void Model::saveTo(Memento& memento) const
{
    /*  Color-count enforcement and optional compactification both
        live in the calling code: compactifyColors() snapshots and
        emits, which is wrong to do from a const method. The .jbb
        file format permits 32 colors so this branch is normally
        a no-op anyway; .dbb's 10-color cap is enforced when the
        DbbFileFormat is the chosen writer (it calls
        compactifyColors() on the model before invoking saveTo()). */
    memento.setWidth(m_field.width());
    memento.setHeight(m_field.height());
    memento.setData(m_field.data());
    memento.setColors(m_colors);
    memento.setColorIndex(m_colorIndex);
    memento.setZoomIndex(m_zoomIndex);
    memento.setShift(m_shift);
    memento.setScroll(m_scroll);
    memento.setAuthor(m_author);
    memento.setOrganization(m_organization);
    memento.setNotes(m_notes);
    memento.setSymbols(m_symbols);
    memento.setDrawColors(m_drawColors);
    memento.setDrawSymbols(m_drawSymbols);
}

void Model::loadFrom(const Memento& memento)
{
    m_field.setData(memento.data(), memento.width(), memento.height());
    m_colors = memento.colors();
    fillDefaultColorsUp();
    m_colorIndex = memento.colorIndex();
    m_zoomIndex  = memento.zoomIndex();
    updateZoom();
    m_shift  = memento.shift();
    m_scroll = memento.scroll();
    m_author       = memento.author();
    m_organization = memento.organization();
    m_notes        = memento.notes();
    /*  Loaded files override the in-memory palette so saved patterns
        render with the symbols they were authored with. An empty
        memento value (e.g. .dbb files which carry no symbol field)
        keeps whatever the user already had configured.              */
    if (!memento.symbols().isEmpty()) {
        m_symbols = memento.symbols();
        BeadSymbols::setSymbols(m_symbols);
    }
    m_drawColors  = memento.drawColors();
    m_drawSymbols = memento.drawSymbols();
    /*  Force a repeat recalculation on the next render. clear()
        zeroed m_repeat before this load was kicked off, so without
        this flag the report panel would forever show "no repeat
        detected" until the user makes an edit that flips the dirty
        bit through some other path.                                */
    m_repeatDirty = true;
    emit modelChanged();
}

} // namespace jbead
