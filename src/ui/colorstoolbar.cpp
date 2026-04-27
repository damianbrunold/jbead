#include "colorstoolbar.h"

#include "colorpickerdialog.h"
#include "domain/model.h"
#include "swatchbutton.h"

#include <QPainter>
#include <QPixmap>

namespace jbead {

namespace {

QPixmap swatchIcon(const QColor& color, bool selected, int size = 18)
{
    QPixmap pm(size, size);
    pm.fill(Qt::transparent);
    QPainter p(&pm);
    p.fillRect(1, 1, size - 2, size - 2, color);
    p.setPen(selected ? QPen(Qt::red, 2) : QPen(Qt::black, 1));
    p.drawRect(1, 1, size - 2, size - 2);
    return pm;
}

} // namespace

ColorsToolbar::ColorsToolbar(Model* model, QWidget* parent)
    : QToolBar(tr("Colors"), parent), m_model(model)
{
    setMovable(false);
    setIconSize(QSize(20, 20));
    rebuild();
    connect(m_model, &Model::colorsChanged, this, &ColorsToolbar::rebuild);
    connect(m_model, &Model::colorChanged,  this, &ColorsToolbar::onColorChanged);
    connect(m_model, &Model::modelChanged,  this, &ColorsToolbar::rebuild);
}

void ColorsToolbar::rebuild()
{
    clear();
    for (int i = 0; i < m_model->colorCount(); ++i) {
        auto* btn = new SwatchButton(this);
        btn->setAutoRaise(true);
        btn->setIcon(swatchIcon(m_model->color(i), i == m_model->selectedColor()));
        btn->setToolTip(tr("Color %1 — double-click to edit").arg(i));
        connect(btn, &QToolButton::clicked, this, [this, i]() {
            m_model->setSelectedColor(static_cast<std::int8_t>(i));
            onSelectionChanged();
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

void ColorsToolbar::onSelectionChanged()
{
    rebuild();
}

void ColorsToolbar::onColorChanged(int /*idx*/)
{
    rebuild();
}

} // namespace jbead
