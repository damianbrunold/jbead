#pragma once

#include "basepanel.h"

#include <cstdint>

namespace jbead {

/*  Half-circumference tube preview. Shows model.width/2 columns and
    applies both Model::correct() and Model::shift() so the user can
    rotate the simulated tube without disturbing the data. Direct
    port of legacy SimulationPanel; the four-case painter logic for
    even/odd scroll x even/odd row is preserved literally because
    the bead-edge wrapping at the seam is sensitive to it.        */
class SimulationPanel : public BasePanel
{
    Q_OBJECT
public:
    SimulationPanel(Model* model, Selection* selection, MainWindow* window, QWidget* parent = nullptr);

    QSize sizeHint() const override;
    QSize minimumSizeHint() const override;

    /*  CoordinateCalculator hooks are present for completeness but
        SimulationPanel does its own painting (no BeadPainter
        dispatch — the seam-edge cells are half-width).            */
    int xFor(BeadPoint pt)        const override;
    int yFor(BeadPoint pt)        const override;
    int offsetXFor(BeadPoint pt)  const override;

protected:
    void paintEvent(QPaintEvent* e) override;
    void mouseReleaseEvent(QMouseEvent* e) override;

private:
    int  panelOffsetX() const;
    int  panelLeft() const;
    int  visibleWidth() const;
    bool mouseToField(QPoint pixel, BeadPoint* out) const;
    void paintBead(QPainter& p, int x, int y, int w, int h,
                   const QColor& color, std::int8_t colorIndex) const;
};

} // namespace jbead
