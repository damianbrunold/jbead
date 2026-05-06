#pragma once

#include <QToolBar>

namespace jbead {

class MainWindow;
class Model;

/*  Horizontal palette strip living in the chrome below the main
    toolbar. One QToolButton per palette entry; clicking selects
    that color (Model::setSelectedColor); double-clicking opens a
    QColorDialog that re-binds the entry. The currently-selected
    swatch is highlighted with a heavy border. Honours the View ->
    Draw Colors / Draw Symbols toggles: with colors off the swatch
    is drawn outline-only, with symbols on the per-slot glyph is
    overlaid (so colour-blind mode is still navigable).            */
class ColorsToolbar : public QToolBar
{
    Q_OBJECT
public:
    ColorsToolbar(Model* model, MainWindow* window, QWidget* parent = nullptr);

public slots:
    void rebuild();

private slots:
    void onColorChanged(int idx);

private:
    Model*      m_model;
    MainWindow* m_window;
};

} // namespace jbead
