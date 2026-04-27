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

/*  Same contrast heuristic the on-screen BeadPainter uses, copied
    here to keep src/print/ free of UI dependencies.               */
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

void drawBead(QPainter& p, qreal x, qreal y, qreal w, qreal h,
              const QColor& color, const QString& symbol)
{
    p.fillRect(QRectF(x, y, w, h), color);
    p.setPen(Qt::black);
    p.drawRect(QRectF(x, y, w, h));
    if (!symbol.isEmpty()) {
        p.setPen(contrastFor(color));
        QFont f = p.font(); f.setPointSizeF(qMax(5.0, h * 0.55));
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
        QFont f = p.font(); f.setPointSizeF(FONT_SIZE);
        p.setFont(f);
        p.drawText(QRectF(x, y, w, h), Qt::AlignCenter, label);
    }
}

// -----------------------------------------------------------------
// Report-info part: filename, author, organisation, counts,
// per-colour totals.
// -----------------------------------------------------------------

class ReportInfoPart : public PartPainter
{
public:
    explicit ReportInfoPart(const Model& m) : m_model(m) { build(); }

    qreal width(qreal /*paintHeight*/) const override
    {
        return std::max<qreal>(infoBlockWidth() + 2 * BORDER, 80.0);
    }

    void paint(QPainter& p, qreal x, qreal y, qreal paintHeight) const override
    {
        p.setPen(Qt::black);
        QFont f = p.font(); f.setPointSizeF(FONT_SIZE);
        p.setFont(f);

        const QFontMetricsF fm(f);
        const qreal x0 = x + BORDER;
        qreal yy = y + fm.ascent();
        qreal labelW = 0;
        for (const auto& [lbl, val] : m_infos) {
            labelW = std::max(labelW, fm.horizontalAdvance(lbl));
        }
        for (const auto& [lbl, val] : m_infos) {
            p.drawText(QPointF(x0, yy), lbl);
            p.drawText(QPointF(x0 + labelW + 4, yy), val);
            yy += LINE_H;
        }

        // Per-colour totals grid below the info block.
        if (!m_counts.isEmpty()) {
            yy += LINE_H * 0.5;
            const qreal pillH = FONT_SIZE + 8;
            const qreal pillW = pillWidth();
            const qreal cellW = pillW + 4;
            const qreal cellH = pillH + 3;
            const int perRow = std::max(1, int((width(paintHeight) - 2 * BORDER) / cellW));
            int col = 0;
            for (const auto& [idx, cnt] : m_counts) {
                if (yy + pillH > y + paintHeight) break;
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

// -----------------------------------------------------------------
// Draft part — full grid with a row-number margin on the left.
// -----------------------------------------------------------------

class DraftPart : public PartPainter
{
public:
    explicit DraftPart(const Model& m) : m_model(m) {}

    qreal width(qreal /*paintHeight*/) const override
    {
        return m_model.width() * GX + MARKER_W + 2 * BORDER;
    }

    void paint(QPainter& p, qreal x, qreal y, qreal paintHeight) const override
    {
        const int rpc = std::max(1, int(paintHeight / GY));
        const int rows = std::min(rpc, m_model.usedHeight());
        const qreal xGrid = x + BORDER + MARKER_W;
        for (int j = 0; j < rows; ++j) {
            const qreal yj = y + (rpc - j - 1) * GY;
            for (int i = 0; i < m_model.width(); ++i) {
                const std::int8_t c = m_model.get(BeadPoint(i, j));
                drawBead(p, xGrid + i * GX, yj, GX, GY,
                         m_model.color(c), BeadSymbols::glyph(c));
            }
        }

        // Row-number labels every 10 rows.
        QFont f = p.font(); f.setPointSizeF(FONT_SIZE - 1);
        p.setFont(f);
        p.setPen(Qt::black);
        const QFontMetricsF fm(f);
        for (int j = 0; j < rows; ++j) {
            if ((j + 1) % 10 != 0) continue;
            const qreal yj = y + (rpc - j - 1) * GY;
            p.drawLine(QPointF(x + BORDER, yj),
                       QPointF(xGrid - GX / 2, yj));
            p.drawText(QPointF(x + BORDER,
                               yj - 1),
                       QString::number(j + 1));
        }
    }

private:
    const Model& m_model;
};

// -----------------------------------------------------------------
// Corrected part — hex-stagger grid.
// -----------------------------------------------------------------

class CorrectedPart : public PartPainter
{
public:
    explicit CorrectedPart(const Model& m) : m_model(m) {}

    qreal width(qreal /*paintHeight*/) const override
    {
        return (m_model.width() + 1) * GX + 2 * BORDER;
    }

    void paint(QPainter& p, qreal x, qreal y, qreal paintHeight) const override
    {
        const int rpc = std::max(1, int(paintHeight / GY));
        const int n   = m_model.usedHeight() * m_model.width();
        const qreal xGrid = x + BORDER + GX / 2;

        for (int idx = 0; idx < n; ++idx) {
            const QPoint h = correctYForIndex(m_model.width(), idx);
            if (h.y() >= rpc) break;
            const int dataX = idx % m_model.width();
            const int dataY = idx / m_model.width();
            const std::int8_t c = m_model.get(BeadPoint(dataX, dataY));
            const qreal xoff = (h.y() % 2 == 0) ? 0 : -GX / 2;
            const qreal xx = xGrid + h.x() * GX + xoff;
            const qreal yy = y + (rpc - h.y() - 1) * GY;
            drawBead(p, xx, yy, GX, GY, m_model.color(c),
                     BeadSymbols::glyph(c));
        }
    }

private:
    const Model& m_model;
};

// -----------------------------------------------------------------
// Simulation part — half-circumference tube preview.
// -----------------------------------------------------------------

class SimulationPart : public PartPainter
{
public:
    explicit SimulationPart(const Model& m) : m_model(m) {}

    qreal width(qreal /*paintHeight*/) const override
    {
        return m_model.width() * GX / 2.0 + 2 * BORDER;
    }

    void paint(QPainter& p, qreal x, qreal y, qreal paintHeight) const override
    {
        const int rpc = std::max(1, int(paintHeight / GY));
        const int n   = m_model.usedHeight() * m_model.width();
        const int vw  = m_model.width() / 2;
        const qreal xGrid = x + BORDER;

        p.setRenderHint(QPainter::Antialiasing, true);
        for (int idx = 0; idx < n; ++idx) {
            const QPoint h = correctYForIndex(m_model.width(), idx);
            if (h.y() >= rpc) break;
            if (h.y() % 2 == 0 && h.x() >= vw) continue;
            if (h.y() % 2 == 1 && h.x() >  vw) continue;
            const int dataX = idx % m_model.width();
            const int dataY = idx / m_model.width();
            const std::int8_t c = m_model.get(BeadPoint(dataX, dataY));

            qreal xx, w;
            if (h.y() % 2 == 0) { xx = xGrid + h.x() * GX;             w = GX; }
            else if (h.x() == 0)         { xx = xGrid;                  w = GX / 2; }
            else if (h.x() == vw)        { xx = xGrid + (h.x() - 1) * GX + GX / 2; w = GX / 2; }
            else                         { xx = xGrid + (h.x() - 1) * GX + GX / 2; w = GX; }
            const qreal yy = y + (rpc - h.y() - 1) * GY;

            // Round bead — same look as the on-screen simulation.
            p.setBrush(m_model.color(c));
            p.setPen(QPen(Qt::black, 0.5));
            p.drawEllipse(QRectF(xx, yy, w, GY));
        }
    }

private:
    const Model& m_model;
};

// -----------------------------------------------------------------
// Bead list part — run-length pills with a leading direction arrow.
// -----------------------------------------------------------------

class BeadListPart : public PartPainter
{
public:
    explicit BeadListPart(const Model& m) : m_model(m), m_list(m) {}

    qreal width(qreal /*paintHeight*/) const override
    {
        return ARROW_W + pillWidth() + 2 * BORDER;
    }

    void paint(QPainter& p, qreal x, qreal y, qreal paintHeight) const override
    {
        if (m_list.runs().isEmpty()) return;

        const qreal pillW = pillWidth();
        const qreal pillH = FONT_SIZE + 8;
        const qreal rowH  = pillH + 3;
        qreal x0 = x + BORDER;

        // Direction arrow on the very first column.
        p.setPen(QPen(Qt::black, 1));
        const qreal ax = x0 + ARROW_W / 2;
        const qreal ay0 = y;
        const qreal ay1 = y + ARROW_LEN;
        const qreal head = ARROW_W / 2;
        p.drawLine(QPointF(ax, ay0), QPointF(ax, ay1));
        p.drawLine(QPointF(ax, ay1), QPointF(ax - head, ay1 - head));
        p.drawLine(QPointF(ax, ay1), QPointF(ax + head, ay1 - head));
        x0 += ARROW_W;

        qreal yy = y;
        for (const BeadRun& run : m_list.runs()) {
            if (yy + pillH > y + paintHeight) break;
            drawPill(p, x0, yy, pillW, pillH,
                     m_model.color(run.color()),
                     QString::number(run.count()));
            yy += rowH;
        }
    }

private:
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

    static constexpr qreal ARROW_W   = 12.0;
    static constexpr qreal ARROW_LEN = 30.0;

    const Model& m_model;
    BeadList     m_list;
};

} // namespace

// -----------------------------------------------------------------
// StripLayout
// -----------------------------------------------------------------

StripLayout::StripLayout(const Model& model, const PrintSettings& settings)
    : m_model(model), m_settings(settings)
{
    buildParts();
}

StripLayout::~StripLayout() = default;

void StripLayout::buildParts()
{
    /*  Layout order matches the textile editor: ReportInfo, then the
        three pattern views (each a single column), then BeadList.
        Skips parts the user toggled off in PrintSettings.            */
    if (m_settings.printReport)     m_parts.append(std::make_shared<ReportInfoPart>(m_model));
    if (m_settings.printDraft)      m_parts.append(std::make_shared<DraftPart>(m_model));
    if (m_settings.printCorrected)  m_parts.append(std::make_shared<CorrectedPart>(m_model));
    if (m_settings.printSimulation) m_parts.append(std::make_shared<SimulationPart>(m_model));
    if (m_settings.printBeadList)   m_parts.append(std::make_shared<BeadListPart>(m_model));
}

int StripLayout::columnCount() const { return m_parts.size(); }

QSizeF StripLayout::naturalSize(qreal paintHeight) const
{
    qreal total = 0;
    for (const auto& part : m_parts) total += part->width(paintHeight);
    return QSizeF(total, paintHeight);
}

qreal StripLayout::paintNatural(QPainter& p, qreal offsetX, qreal offsetY,
                                qreal paintHeight) const
{
    qreal cursor = offsetX;
    for (const auto& part : m_parts) {
        const qreal w = part->width(paintHeight);
        part->paint(p, cursor, offsetY, paintHeight);
        cursor += w;
    }
    return cursor - offsetX;
}

void StripLayout::paintFitted(QPainter& p, qreal x, qreal y,
                              qreal w, qreal h) const
{
    /*  Lay out at native paint height, then uniformly scale the
        whole strip down (never up — small patterns sit at native
        size on the page). Centre horizontally if narrower than w. */
    if (m_parts.isEmpty() || w <= 0 || h <= 0) return;

    const QSizeF natural = naturalSize(h);
    const qreal scaleX = (natural.width()  > 0) ? w / natural.width()  : 1.0;
    const qreal scaleY = (natural.height() > 0) ? h / natural.height() : 1.0;
    const qreal scale  = std::min<qreal>(1.0, std::min(scaleX, scaleY));
    const qreal usedW  = natural.width() * scale;
    const qreal cx     = x + (w - usedW) / 2.0;

    p.save();
    p.translate(cx, y);
    p.scale(scale, scale);
    paintNatural(p, 0, 0, h);
    p.restore();
}

} // namespace jbead
