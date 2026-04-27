#include "basepanel.h"

#include "domain/model.h"
#include "domain/selection.h"
#include "mainwindow.h"

namespace jbead {

BasePanel::BasePanel(Model* model, Selection* selection, MainWindow* window, QWidget* parent)
    : QWidget(parent), m_model(model), m_selection(selection), m_window(window)
{
    m_gridx  = m_model->gridx();
    m_gridy  = m_model->gridy();
    m_scroll = m_model->scroll();

    connect(m_model, &Model::modelChanged,   this, &BasePanel::onModelChanged);
    connect(m_model, &Model::pointChanged,   this, &BasePanel::onPointChanged);
    connect(m_model, &Model::scrollChanged,  this, &BasePanel::onScrollChanged);
    connect(m_model, &Model::zoomChanged,    this, &BasePanel::onZoomChanged);
    connect(m_model, &Model::shiftChanged,   this, &BasePanel::onShiftChanged);
    connect(m_model, &Model::repeatChanged,  this, &BasePanel::onRepeatChanged);
    connect(m_model, &Model::colorsChanged,  this, &BasePanel::onColorsChanged);
    connect(m_model, &Model::colorChanged,   this, &BasePanel::onColorChanged);

    setBackgroundRole(QPalette::Light);
    setAutoFillBackground(true);
}

void BasePanel::onModelChanged()
{
    m_gridx  = m_model->gridx();
    m_gridy  = m_model->gridy();
    m_scroll = m_model->scroll();
    update();
}

void BasePanel::onPointChanged(BeadPoint /*pt*/) { update(); }
void BasePanel::onScrollChanged(int s)           { m_scroll = s; update(); }
void BasePanel::onZoomChanged(int gx, int gy)    { m_gridx = gx; m_gridy = gy; update(); }
void BasePanel::onShiftChanged(int)              { update(); }
void BasePanel::onRepeatChanged(int)             { update(); }
void BasePanel::onColorsChanged()                { update(); }
void BasePanel::onColorChanged(int)              { update(); }

QFont BasePanel::symbolFont() const
{
    QFont f;
    f.setPixelSize(qMax(8, m_gridy - 2));
    return f;
}

} // namespace jbead
