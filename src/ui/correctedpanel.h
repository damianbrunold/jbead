#pragma once

#include "basepanel.h"

namespace jbead {

/*  Hexagonal-offset preview. Read-only-ish: clicks toggle / fill,
    but no rectangular selection or pencil-line drawing — that is
    the DraftPanel's job. The dx() override is what produces the
    every-other-row half-cell stagger.                           */
class CorrectedPanel : public BasePanel
{
    Q_OBJECT
public:
    CorrectedPanel(Model* model, Selection* selection, MainWindow* window, QWidget* parent = nullptr);

    QSize sizeHint() const override;
    QSize minimumSizeHint() const override;

    int xFor(BeadPoint pt) const override;
    int yFor(BeadPoint pt) const override;
    int offsetXFor(BeadPoint pt) const override;

protected:
    void paintEvent(QPaintEvent* e) override;
    void mouseReleaseEvent(QMouseEvent* e) override;

private:
    int  panelOffsetX() const;
    int  panelLeft() const;
    bool mouseToField(QPoint pixel, BeadPoint* out) const;
};

} // namespace jbead
