#pragma once

#include "beadpainter.h"
#include "domain/beadpoint.h"

#include <QFont>
#include <QWidget>

namespace jbead {

class MainWindow;
class Model;
class Selection;

/*  Common base for the four pattern-view widgets. Holds references
    to the shared Model + Selection + MainWindow (used to read tool
    state and view-toggle state), keeps the cached zoom and scroll
    used by paintEvent, and wires the model signals that every view
    cares about.

    Concrete subclasses implement paintEvent and the
    CoordinateCalculator interface (since each view has its own
    cell layout).                                                  */
class BasePanel : public QWidget, public CoordinateCalculator
{
    Q_OBJECT
public:
    BasePanel(Model* model, Selection* selection, MainWindow* window, QWidget* parent = nullptr);

    int gridX() const override { return m_gridx; }
    int gridY() const override { return m_gridy; }

protected slots:
    virtual void onModelChanged();
    virtual void onPointChanged(BeadPoint pt);
    virtual void onScrollChanged(int scroll);
    virtual void onZoomChanged(int gridx, int gridy);
    virtual void onShiftChanged(int shift);
    virtual void onRepeatChanged(int repeat);
    virtual void onColorsChanged();
    virtual void onColorChanged(int colorIndex);

protected:
    QFont symbolFont() const;

    Model*      m_model;
    Selection*  m_selection;
    MainWindow* m_window;
    int         m_gridx = 12;
    int         m_gridy = 12;
    int         m_scroll = 0;
};

} // namespace jbead
