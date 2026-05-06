#include "colorstoolbar.h"

#include "beadpainter.h"
#include "colorpickerdialog.h"
#include "domain/beadsymbols.h"
#include "domain/model.h"
#include "mainwindow.h"
#include "swatchbutton.h"

#include <QGuiApplication>
#include <QPainter>
#include <QPalette>
#include <QPixmap>

namespace jbead {

namespace {

QPixmap swatchIcon(const QColor& color, int colorIndex, bool selected,
                   bool drawColors, bool drawSymbols, int size = 18)
{
    QPixmap pm(size, size);
    pm.fill(Qt::transparent);
    QPainter p(&pm);
    p.setRenderHint(QPainter::Antialiasing, true);

    /*  Outline + symbol use the active palette so colour-blind mode
        stays legible in dark themes. Selection still gets a red
        border regardless — that's the marker the user clicks for. */
    const QColor outline = drawColors
        ? QColor(Qt::black)
        : QGuiApplication::palette().color(QPalette::WindowText);

    if (drawColors) p.fillRect(1, 1, size - 2, size - 2, color);

    if (drawSymbols) {
        const QString sym = BeadSymbols::glyph(colorIndex);
        if (!sym.trimmed().isEmpty()) {
            p.setPen(drawColors ? BeadPainter::contrastingColor(color) : outline);
            QFont f = p.font();
            f.setPixelSize(qMax(8, int(size * 0.7)));
            f.setBold(true);
            p.setFont(f);
            p.drawText(QRect(0, 0, size, size), Qt::AlignCenter, sym);
        }
    }

    p.setPen(selected ? QPen(Qt::red, 2) : QPen(outline, 1));
    p.drawRect(1, 1, size - 2, size - 2);
    return pm;
}

} // namespace

ColorsToolbar::ColorsToolbar(Model* model, MainWindow* window, QWidget* parent)
    : QToolBar(tr("Colors"), parent), m_model(model), m_window(window)
{
    setMovable(false);
    setIconSize(QSize(20, 20));
    rebuild();
    connect(m_model, &Model::colorsChanged, this, &ColorsToolbar::rebuild);
    connect(m_model, &Model::colorChanged,  this, &ColorsToolbar::onColorChanged);
    connect(m_model, &Model::modelChanged,  this, &ColorsToolbar::rebuild);
    /*  Selection from any source (clicking a swatch here, the
        palette editor dialog, the pipette tool) flips the model's
        active colour. Rebuild so the red selection border moves
        onto the new swatch.                                       */
    connect(m_model, &Model::selectedColorChanged,
            this, [this](int) { rebuild(); });
}

void ColorsToolbar::rebuild()
{
    clear();
    const bool drawColors  = m_window->drawColors();
    const bool drawSymbols = m_window->drawSymbols();
    for (int i = 0; i < m_model->colorCount(); ++i) {
        auto* btn = new SwatchButton(this);
        btn->setAutoRaise(true);
        btn->setIcon(swatchIcon(m_model->color(i), i,
                                i == m_model->selectedColor(),
                                drawColors, drawSymbols));
        btn->setToolTip(tr("Color %1 — double-click to edit").arg(i));
        connect(btn, &QToolButton::clicked, this, [this, i]() {
            /*  Just push the change through the model — it emits
                selectedColorChanged which we listen to and rebuild
                from. No need to call rebuild() ourselves here.    */
            m_model->setSelectedColor(static_cast<std::int8_t>(i));
        });
        /*  Double-click opens a single-color picker for this swatch
            (mirrors the textile editor's _onDoubleClick on the
            palette area). Live: model.setColor emits colorChanged
            which all panels listen to, so the change shows
            immediately. The model snapshots before the change so
            it's undoable.                                          */
        connect(btn, &SwatchButton::doubleClicked, this, [this, i]() {
            const QColor picked = ColorPickerDialog::getColor(
                m_model->color(i), this, tr("Pick color %1").arg(i));
            if (picked.isValid()) m_model->setColor(i, picked);
        });
        addWidget(btn);
    }
}

void ColorsToolbar::onColorChanged(int /*idx*/)
{
    rebuild();
}

} // namespace jbead
