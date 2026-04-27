#pragma once

#include "basepanel.h"

namespace jbead {

/*  Editable left canvas. Owns mouse + keyboard input for the
    pencil / select / fill / pipette tools and renders row-number
    markers along the left edge.

    Straight-line drawing (textile redesign): when the user drags
    with Shift held, the preview snaps to the closer of the eight
    cardinal/diagonal directions and on release commits a Bresenham
    line in that direction.                                        */
class DraftPanel : public BasePanel
{
    Q_OBJECT
public:
    DraftPanel(Model* model, Selection* selection, MainWindow* window, QWidget* parent = nullptr);

    QSize sizeHint() const override;
    QSize minimumSizeHint() const override;

    int xFor(BeadPoint pt)        const override;
    int yFor(BeadPoint pt)        const override;
    int offsetXFor(BeadPoint /*pt*/) const override { return 0; }

protected:
    void paintEvent(QPaintEvent* e) override;
    void mousePressEvent(QMouseEvent* e) override;
    void mouseMoveEvent(QMouseEvent* e) override;
    void mouseReleaseEvent(QMouseEvent* e) override;

private:
    static constexpr int GAP = 6;
    static constexpr int MARKER_WIDTH = 30;

    int  offsetX() const;
    int  maxJ() const;

    /*  Pixel -> field cell. Returns std::optional via the bool out
        parameter rather than a sentinel point because (-1,-1) is a
        valid cell after iterator normalisation.                  */
    bool mouseToField(QPoint pixel, BeadPoint* out) const;

    void paintBeads(QPainter& p);
    void paintMarkers(QPainter& p);
    void paintSelection(QPainter& p);

    bool m_dragging = false;
    bool m_shiftHeld = false;
    BeadPoint m_dragOrigin;
    BeadPoint m_dragLast;
};

} // namespace jbead
