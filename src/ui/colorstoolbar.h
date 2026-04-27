#pragma once

#include <QToolBar>

namespace jbead {

class Model;

/*  Horizontal palette strip living in the chrome below the main
    toolbar. One QToolButton per palette entry; clicking selects
    that color (Model::setSelectedColor); double-clicking opens a
    QColorDialog that re-binds the entry. The currently-selected
    swatch is highlighted with a heavy border.                    */
class ColorsToolbar : public QToolBar
{
    Q_OBJECT
public:
    explicit ColorsToolbar(Model* model, QWidget* parent = nullptr);

private slots:
    void rebuild();
    void onSelectionChanged();
    void onColorChanged(int idx);

private:
    Model* m_model;
};

} // namespace jbead
