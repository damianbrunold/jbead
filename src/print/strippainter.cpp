#include "strippainter.h"

#include "domain/beadcounts.h"
#include "domain/beadlist.h"
#include "domain/beadsymbols.h"
#include "domain/model.h"

#include <QFileInfo>
#include <QFontMetricsF>
#include <QPainter>

#include <algorithm>
#include <cmath>

namespace jbead {

namespace {

/*  Layout constants in points (1/72 inch). Match the textile
    editor's defaults so the printed output looks the same regardless
    of which platform the user sees the same pattern on.            */
constexpr qreal GX        = 12.0;        // bead cell width
constexpr qreal GY        = 12.0;        // bead cell height
constexpr qreal MARKER_W  = 22.0;        // row-number column on draft
constexpr qreal BORDER    = 4.0;         // padding around each part
constexpr qreal FONT_SIZE = 9.0;
constexpr qreal LINE_H    = FONT_SIZE + 4.0;

QColor contrastFor(const QColor& c)
{
    const auto dist = [&](const QColor& a, const QColor& b) {
        return std::sqrt(double((a.red()-b.red())*(a.red()-b.red())
                              + (a.green()-b.green())*(a.green()-b.green())
                              + (a.blue()-b.blue())*(a.blue()-b.blue())));
    };
    return dist(c, Qt::white) > dist(c, Qt::black) ? Qt::white : Qt::black;
}

/*  Hexagonal layout helper — same formula Model::correctedIndex
    uses, rewritten in (xi, yj) terms so we can iterate the bead
    string and lay it out as the corrected / simulation views do. */
QPoint correctYForIndex(int width, int idx)
{
    const int m1 = width;
    const int m2 = m1 + 1;
    int y = 0;
    int m = m1;
    while (idx >= m) {
        idx -= m;
        ++y;
        m = (y % 2 == 0) ? m1 : m2;
    }
    return QPoint(idx, y);
}

/*  Font sizing rule for both helpers and the part bodies below:
    use setPixelSize, NOT setPointSize. Two reasons:

      - On a HighResolution QPrinter we apply painter.scale(s, s) to
        map points into device pixels (s = printer.resolution() /
        72). setPointSize converts via logicalDpi (= printer res),
        producing a glyph already in printer-pixels — which the
        painter transform then scales AGAIN, leaving text 17x too
        large at 1200 dpi. setPixelSize sizes in painter coords
        directly so the transform applies once like everything else.
      - On the raster path (no scale, image dpi=72) the result is
        the same as setPointSize, so this fix is invisible there. */
void setBodyFont(QPainter& p, qreal sizePt = FONT_SIZE)
{
    QFont f = p.font();
    f.setPixelSize(qMax<int>(4, int(std::round(sizePt))));
    p.setFont(f);
}

void drawBead(QPainter& p, qreal x, qreal y, qreal w, qreal h,
              const QColor& color, const QString& symbol,
              bool drawColors, bool drawSymbols)
{
    /*  drawRect fills with the painter's *current brush* on top of
        stroking with the pen. The previous fillRect(color) +
        drawRect(rect) idiom inherited a stale brush from earlier
        calls (e.g. drawPill in the report block left the brush
        set to the last colour swatch — black for stripes.jbb),
        which then overwrote every subsequent bead with that
        colour. Set both brush and pen explicitly here.            */
    p.setBrush(drawColors ? QBrush(color) : QBrush(Qt::NoBrush));
    p.setPen(Qt::black);
    p.drawRect(QRectF(x, y, w, h));
    if (drawSymbols && !symbol.isEmpty()) {
        const QColor fill = drawColors ? color : QColor(Qt::white);
        p.setPen(contrastFor(fill));
        QFont f = p.font();
        f.setPixelSize(qMax<int>(4, int(std::round(h * 0.55))));
        p.setFont(f);
        p.drawText(QRectF(x, y, w, h), Qt::AlignCenter, symbol);
    }
}

void drawPill(QPainter& p, qreal x, qreal y, qreal w, qreal h,
              const QColor& fill, const QString& label)
{
    p.setRenderHint(QPainter::Antialiasing, true);
    p.setBrush(fill);
    p.setPen(QPen(Qt::black, 0.6));
    p.drawEllipse(QRectF(x, y, w, h));
    if (!label.isEmpty()) {
        p.setPen(contrastFor(fill));
        QFont f = p.font();
        f.setPixelSize(qMax<int>(5, int(std::round(h * 0.55))));
        p.setFont(f);
        p.drawText(QRectF(x, y, w, h), Qt::AlignCenter, label);
    }
}

// =================================================================
// ReportInfoPart — file metadata + per-colour totals. Single column.
// =================================================================

class ReportInfoPart : public PartPainter
{
public:
    explicit ReportInfoPart(const Model& m) : m_model(m) { build(); }

    QList<qreal> columnWidths(qreal /*h*/) const override
    {
        return { std::max<qreal>(infoBlockWidth() + 2 * BORDER, 100.0) };
    }

    void paintColumn(QPainter& p, qreal x, qreal y, qreal h, int) const override
    {
        p.setPen(Qt::black);
        setBodyFont(p);
        const QFontMetricsF fm(p.font());
        const qreal x0 = x + BORDER;
        qreal yy = y + fm.ascent();
        qreal labelW = 0;
        for (const auto& [lbl, val] : m_infos) {
            labelW = std::max(labelW, fm.horizontalAdvance(lbl));
        }
        for (const auto& [lbl, val] : m_infos) {
            p.drawText(QPointF(x0, yy), lbl);
            p.drawText(QPointF(x0 + labelW + 6, yy), val);
            yy += LINE_H;
        }

        // Per-colour totals grid below the info block.
        if (!m_counts.isEmpty()) {
            yy += LINE_H * 0.5;
            const qreal pillH = FONT_SIZE + 8;
            const qreal pillW = pillWidth();
            const qreal cellW = pillW + 4;
            const qreal cellH = pillH + 3;
            const qreal colW  = columnWidths(h).first() - 2 * BORDER;
            const int   perRow = std::max(1, int(colW / cellW));
            int col = 0;
            for (const auto& [idx, cnt] : m_counts) {
                if (yy + pillH > y + h) break;
                const qreal cx = x0 + col * cellW;
                drawPill(p, cx, yy, pillW, pillH, m_model.color(idx),
                         QString::number(cnt));
                ++col;
                if (col >= perRow) { col = 0; yy += cellH; }
            }
        }
    }

private:
    void build()
    {
        if (m_model.isRepeatDirty()) const_cast<Model&>(m_model).updateRepeat();

        QString file = QFileInfo(m_model.filePath()).fileName();
        if (file.isEmpty()) file = QStringLiteral("unnamed");
        m_infos.append({QStringLiteral("Pattern:"),       file});
        if (!m_model.author().isEmpty())
            m_infos.append({QStringLiteral("Author:"),    m_model.author()});
        if (!m_model.organization().isEmpty())
            m_infos.append({QStringLiteral("Organization:"), m_model.organization()});
        m_infos.append({QStringLiteral("Circumference:"),
                        QString::number(m_model.width())});
        m_infos.append({QStringLiteral("Rows:"),
                        QString::number(m_model.usedHeight())});
        if (m_model.repeat() > 0) {
            m_infos.append({QStringLiteral("Repeat (beads):"),
                            QString::number(m_model.repeat())});
        }
        m_infos.append({QStringLiteral("Total beads:"),
                        QString::number(m_model.usedHeight() * m_model.width())});

        BeadCounts counts(m_model);
        for (int c = 1; c < m_model.colorCount(); ++c) {
            const int n = counts.count(static_cast<std::int8_t>(c));
            if (n > 0) m_counts.append({c, n});
        }
    }

    qreal pillWidth() const
    {
        const qreal pillH = FONT_SIZE + 8;
        if (m_counts.isEmpty()) return pillH * 1.6;
        int maxCount = 0;
        for (const auto& [idx, cnt] : m_counts) maxCount = std::max(maxCount, cnt);
        const qreal textW = QString::number(maxCount).size() * FONT_SIZE * 0.65;
        return std::max(pillH * 1.6, textW + 14);
    }

    qreal infoBlockWidth() const
    {
        if (m_infos.isEmpty()) return 0.0;
        const qreal charW = FONT_SIZE * 0.65;
        qreal maxLbl = 0, maxVal = 0;
        for (const auto& [lbl, val] : m_infos) {
            maxLbl = std::max(maxLbl, qreal(lbl.size()) * charW);
            maxVal = std::max(maxVal, qreal(val.size()) * charW);
        }
        return maxLbl + charW + maxVal;
    }

    const Model& m_model;
    QList<QPair<QString, QString>> m_infos;
    QList<QPair<int, int>>          m_counts;
};

// =================================================================
// _GridPart — common base for Draft / Corrected / Simulation. Each
// emits one column per `rowsPerColumn` rows of the pattern (or
// exactly one column when settings.singleColumnGrids is set, used
// by the export sketch).
// =================================================================

class _GridPart : public PartPainter
{
public:
    _GridPart(const Model& m, const PrintSettings& s)
        : m_model(m), m_settings(s) {}

protected:
    int rowsPerColumn(qreal h) const
    {
        return std::max(1, int(h / GY));
    }

    int printableRows(qreal h) const
    {
        const int rpc  = rowsPerColumn(h);
        const int used = m_model.usedHeight();
        if (used <= rpc || m_settings.fullPattern) return used;
        if (m_model.repeat() > 0) {
            const int repeatRows =
                (m_model.repeat() + m_model.width() - 1) / m_model.width();
            const int rounded = ((repeatRows + rpc - 1) / rpc) * rpc;
            return std::min(rounded, used);
        }
        return used;
    }

    int gridColumnCount(qreal h) const
    {
        const int rows = printableRows(h);
        const int rpc  = rowsPerColumn(h);
        int cols = std::max(1, (rows + rpc - 1) / rpc);
        if (m_settings.singleColumnGrids) cols = 1;
        return cols;
    }

    const Model&         m_model;
    const PrintSettings& m_settings;
};

// =================================================================
// DraftPart — rectangular grid + 10-row markers on the left.
// =================================================================

class DraftPart : public _GridPart
{
public:
    using _GridPart::_GridPart;

    QList<qreal> columnWidths(qreal h) const override
    {
        const qreal w = m_model.width() * GX + MARKER_W + 2 * BORDER;
        QList<qreal> out;
        for (int i = 0, n = gridColumnCount(h); i < n; ++i) out.append(w);
        return out;
    }

    void paintColumn(QPainter& p, qreal x, qreal y, qreal h, int col) const override
    {
        const int rpc = rowsPerColumn(h);
        const int rowsTotal = printableRows(h);
        const int start = col * rpc;
        const int rowsHere = std::min(rpc, rowsTotal - start);
        if (rowsHere <= 0) return;
        const qreal xGrid = x + BORDER + MARKER_W;

        for (int j = 0; j < rowsHere; ++j) {
            const int row = start + j;
            const qreal yj = y + (rpc - j - 1) * GY;
            for (int i = 0; i < m_model.width(); ++i) {
                const std::int8_t c = m_model.get(BeadPoint(i, row));
                drawBead(p, xGrid + i * GX, yj, GX, GY,
                         m_model.color(c), BeadSymbols::glyph(c),
                         m_settings.drawColors, m_settings.drawSymbols);
            }
        }

        // Row-number labels every 10 rows.
        QFont f = p.font(); f.setPixelSize(qMax<int>(4, int(FONT_SIZE - 1)));
        p.setFont(f);
        p.setPen(Qt::black);
        const QFontMetricsF fm(f);
        for (int j = 0; j < rowsHere; ++j) {
            const int row = start + j;
            if ((row + 1) % 10 != 0) continue;
            const qreal yj = y + (rpc - j - 1) * GY;
            p.drawLine(QPointF(x + BORDER, yj),
                       QPointF(xGrid - GX / 2, yj));
            p.drawText(QPointF(x + BORDER, yj - 1),
                       QString::number(row + 1));
        }
    }
};

// =================================================================
// CorrectedPart — hex-stagger grid.
// =================================================================

class CorrectedPart : public _GridPart
{
public:
    using _GridPart::_GridPart;

    QList<qreal> columnWidths(qreal h) const override
    {
        const qreal w = (m_model.width() + 1) * GX + 2 * BORDER;
        QList<qreal> out;
        for (int i = 0, n = gridColumnCount(h); i < n; ++i) out.append(w);
        return out;
    }

    void paintColumn(QPainter& p, qreal x, qreal y, qreal h, int col) const override
    {
        const int rpc = rowsPerColumn(h);
        const int start = col * rpc;
        const int end   = start + rpc;
        const int n = printableRows(h) * m_model.width();
        const qreal xGrid = x + BORDER + GX / 2;

        for (int idx = 0; idx < n; ++idx) {
            const QPoint hex = correctYForIndex(m_model.width(), idx);
            if (hex.y() < start || hex.y() >= end) continue;
            const int dataX = idx % m_model.width();
            const int dataY = idx / m_model.width();
            const std::int8_t c = m_model.get(BeadPoint(dataX, dataY));
            const qreal xoff = (hex.y() % 2 == 0) ? 0 : -GX / 2;
            const qreal xx = xGrid + hex.x() * GX + xoff;
            const qreal yy = y + (rpc - (hex.y() - start) - 1) * GY;
            drawBead(p, xx, yy, GX, GY, m_model.color(c),
                     BeadSymbols::glyph(c),
                     m_settings.drawColors, m_settings.drawSymbols);
        }
    }
};

// =================================================================
// SimulationPart — half-circumference tube preview, round beads.
// =================================================================

class SimulationPart : public _GridPart
{
public:
    using _GridPart::_GridPart;

    QList<qreal> columnWidths(qreal h) const override
    {
        const qreal w = m_model.width() * GX / 2.0 + 2 * BORDER;
        QList<qreal> out;
        for (int i = 0, n = gridColumnCount(h); i < n; ++i) out.append(w);
        return out;
    }

    void paintColumn(QPainter& p, qreal x, qreal y, qreal h, int col) const override
    {
        p.setRenderHint(QPainter::Antialiasing, true);
        const int rpc   = rowsPerColumn(h);
        const int start = col * rpc;
        const int end   = start + rpc;
        const int n     = printableRows(h) * m_model.width();
        const int vw    = m_model.width() / 2;
        const qreal xGrid = x + BORDER;

        for (int idx = 0; idx < n; ++idx) {
            const QPoint hex = correctYForIndex(m_model.width(), idx);
            if (hex.y() < start || hex.y() >= end) continue;
            if (hex.y() % 2 == 0 && hex.x() >= vw) continue;
            if (hex.y() % 2 == 1 && hex.x() >  vw) continue;
            const int dataX = idx % m_model.width();
            const int dataY = idx / m_model.width();
            const std::int8_t c = m_model.get(BeadPoint(dataX, dataY));

            qreal xx, w;
            if (hex.y() % 2 == 0)        { xx = xGrid + hex.x() * GX;             w = GX; }
            else if (hex.x() == 0)       { xx = xGrid;                            w = GX / 2; }
            else if (hex.x() == vw)      { xx = xGrid + (hex.x() - 1) * GX + GX / 2; w = GX / 2; }
            else                         { xx = xGrid + (hex.x() - 1) * GX + GX / 2; w = GX; }
            const qreal yy = y + (rpc - (hex.y() - start) - 1) * GY;

            // Round bead — same look as the on-screen simulation.
            p.setBrush(m_model.color(c));
            p.setPen(QPen(Qt::black, 0.5));
            p.drawEllipse(QRectF(xx, yy, w, GY));
        }
    }
};

// =================================================================
// BeadListPart — run-length pills with leading direction arrow.
// Wraps into multiple columns when there are more runs than fit
// vertically in `paintHeight`.
// =================================================================

class BeadListPart : public PartPainter
{
public:
    explicit BeadListPart(const Model& m) : m_model(m), m_list(m) {}

    QList<qreal> columnWidths(qreal h) const override
    {
        const int n = m_list.runs().size();
        if (n <= 0) return {};
        const int bpc = beadsPerColumn(h);
        const int cols = std::max(1, (n + bpc - 1) / bpc);
        QList<qreal> out;
        for (int i = 0; i < cols; ++i) {
            out.append(columnPixelWidth(/*first=*/i == 0));
        }
        return out;
    }

    void paintColumn(QPainter& p, qreal x, qreal y, qreal h, int col) const override
    {
        const int bpc   = beadsPerColumn(h);
        const int start = col * bpc;
        if (start >= m_list.runs().size()) return;
        const qreal pillW = pillWidth();
        const qreal pillH = FONT_SIZE + 8;
        const qreal rowH  = pillH + 3;

        qreal x0 = x + BORDER;
        if (col == 0) {
            // Direction arrow drawn alongside the very first column.
            p.setPen(QPen(Qt::black, 1));
            const qreal ax = x0 + ARROW_W / 2;
            const qreal ay0 = y;
            const qreal ay1 = y + ARROW_LEN;
            const qreal head = ARROW_W / 2;
            p.drawLine(QPointF(ax, ay0), QPointF(ax, ay1));
            p.drawLine(QPointF(ax, ay1), QPointF(ax - head, ay1 - head));
            p.drawLine(QPointF(ax, ay1), QPointF(ax + head, ay1 - head));
            x0 += ARROW_W;
        }

        for (int i = 0; i < bpc; ++i) {
            const int idx = start + i;
            if (idx >= m_list.runs().size()) break;
            const BeadRun& run = m_list.runs().at(idx);
            const qreal yy = y + i * rowH;
            drawPill(p, x0, yy, pillW, pillH,
                     m_model.color(run.color()),
                     QString::number(run.count()));
        }
    }

private:
    int beadsPerColumn(qreal h) const
    {
        const qreal pillH = FONT_SIZE + 8;
        const qreal rowH  = pillH + 3;
        return std::max(1, int(h / rowH));
    }

    qreal pillWidth() const
    {
        const qreal pillH = FONT_SIZE + 8;
        if (m_list.runs().isEmpty()) return pillH * 1.6;
        int maxCount = 0;
        for (const BeadRun& run : m_list.runs())
            maxCount = std::max(maxCount, run.count());
        const qreal textW = QString::number(maxCount).size() * FONT_SIZE * 0.65;
        return std::max(pillH * 1.6, textW + 14);
    }

    qreal columnPixelWidth(bool first) const
    {
        return pillWidth() + 4 + (first ? ARROW_W : 0) + 2 * BORDER;
    }

    static constexpr qreal ARROW_W   = 12.0;
    static constexpr qreal ARROW_LEN = 30.0;

    const Model& m_model;
    BeadList     m_list;
};

// -----------------------------------------------------------------
// Part-list construction shared by both layouts.
// -----------------------------------------------------------------

void buildPartList(const Model& m, const PrintSettings& s,
                   QList<std::shared_ptr<PartPainter>>& out)
{
    if (s.printReport)     out.append(std::make_shared<ReportInfoPart>(m));
    if (s.printDraft)      out.append(std::make_shared<DraftPart>(m, s));
    if (s.printCorrected)  out.append(std::make_shared<CorrectedPart>(m, s));
    if (s.printSimulation) out.append(std::make_shared<SimulationPart>(m, s));
    if (s.printBeadList)   out.append(std::make_shared<BeadListPart>(m));
}

} // namespace

// -----------------------------------------------------------------
// StripLayout — single-page strip used by Export.
// -----------------------------------------------------------------

StripLayout::StripLayout(const Model& model, const PrintSettings& settings)
    : m_model(model), m_settings(settings)
{
    buildParts();
}

StripLayout::~StripLayout() = default;

void StripLayout::buildParts() { buildPartList(m_model, m_settings, m_parts); }

int StripLayout::columnCount(qreal h) const
{
    int n = 0;
    for (const auto& part : m_parts) n += part->columnWidths(h).size();
    return n;
}

QSizeF StripLayout::naturalSize(qreal paintHeight) const
{
    qreal total = 0;
    for (const auto& part : m_parts) {
        for (qreal w : part->columnWidths(paintHeight)) total += w;
    }
    return QSizeF(total, paintHeight);
}

qreal StripLayout::paintNatural(QPainter& p, qreal offsetX, qreal offsetY,
                                qreal paintHeight) const
{
    qreal cursor = offsetX;
    for (const auto& part : m_parts) {
        const QList<qreal> widths = part->columnWidths(paintHeight);
        for (int ci = 0; ci < widths.size(); ++ci) {
            part->paintColumn(p, cursor, offsetY, paintHeight, ci);
            cursor += widths[ci];
        }
    }
    return cursor - offsetX;
}

void StripLayout::paintFitted(QPainter& p, qreal x, qreal y,
                              qreal w, qreal h) const
{
    if (m_parts.isEmpty() || w <= 0 || h <= 0) return;
    const QSizeF natural = naturalSize(h);
    const qreal scaleX = (natural.width()  > 0) ? w / natural.width()  : 1.0;
    const qreal scale  = std::min<qreal>(1.0, scaleX);
    const qreal usedW  = natural.width() * scale;
    const qreal cx     = x + (w - usedW) / 2.0;

    p.save();
    p.translate(cx, y);
    p.scale(scale, scale);
    paintNatural(p, 0, 0, h);
    p.restore();
}

// -----------------------------------------------------------------
// MultiPageLayout — multi-page packer used by Print.
// -----------------------------------------------------------------

MultiPageLayout::MultiPageLayout(const Model& model, const PrintSettings& settings)
    : m_model(model), m_settings(settings)
{
    buildParts();
}

MultiPageLayout::~MultiPageLayout() = default;

void MultiPageLayout::buildParts() { buildPartList(m_model, m_settings, m_parts); }

int MultiPageLayout::pageCount() const { return m_pages.size(); }

void MultiPageLayout::layout(const QSizeF& pageSize)
{
    m_pages.clear();
    m_pageH = pageSize.height();
    if (m_pageH <= 0 || pageSize.width() <= 0) return;

    Page current;
    for (const auto& part : m_parts) {
        const QList<qreal> widths = part->columnWidths(m_pageH);
        for (int ci = 0; ci < widths.size(); ++ci) {
            const qreal w = widths[ci];
            /*  Start a new page when the next column would push us
                past the page width — same packing strategy the
                textile editor's _layout_pages uses. We always keep
                at least one column per page, even if it overflows
                horizontally, so a too-wide single column degrades
                gracefully rather than emitting an empty page.    */
            if (current.totalWidth + w > pageSize.width()
                && !current.columns.isEmpty()) {
                m_pages.append(current);
                current = Page{};
            }
            current.columns.append({part, ci, w});
            current.totalWidth += w;
        }
    }
    if (!current.columns.isEmpty()) m_pages.append(current);
}

void MultiPageLayout::paintPage(QPainter& p, int index,
                                qreal x, qreal y, qreal w, qreal h) const
{
    if (index < 0 || index >= m_pages.size()) return;
    const Page& page = m_pages.at(index);

    /*  Centre the row of columns horizontally if they don't fill
        the page width. Single-column pages then sit nicely
        rather than hugging the left margin.                     */
    const qreal cx = x + std::max<qreal>(0, (w - page.totalWidth) / 2.0);
    qreal cursor = cx;
    for (const PageColumn& pc : page.columns) {
        pc.part->paintColumn(p, cursor, y, h, pc.columnIndex);
        cursor += pc.width;
    }
    Q_UNUSED(w);
}

} // namespace jbead
