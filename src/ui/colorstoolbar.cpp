#include "colorstoolbar.h"

#include "domain/model.h"

#include <QApplication>
#include <QColorDialog>
#include <QPainter>
#include <QPixmap>
#include <QToolButton>

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
        auto* btn = new QToolButton(this);
        btn->setAutoRaise(true);
        btn->setIcon(swatchIcon(m_model->color(i), i == m_model->selectedColor()));
        btn->setToolTip(QStringLiteral("Color %1").arg(i));
        connect(btn, &QToolButton::clicked, this, [this, i]() {
            m_model->setSelectedColor(static_cast<std::int8_t>(i));
            onSelectionChanged();
        });
        connect(btn, &QToolButton::pressed, this, [this, i, btn]() {
            // Double-click is approximated via a simple QColorDialog
            // launched on Shift+click — keeps the API minimal until a
            // proper preferences dialog lands.
            if (QApplication::keyboardModifiers().testFlag(Qt::ShiftModifier)) {
                const QColor picked = QColorDialog::getColor(
                    m_model->color(i), this, tr("Pick color %1").arg(i));
                if (picked.isValid()) m_model->setColor(i, picked);
            }
            (void) btn;
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
