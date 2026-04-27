#pragma once

#include "basepanel.h"

namespace jbead {

/*  Stats + bead-list summary. Layout follows the textile editor's
    redesign: a header block with pattern-wide counts, then a
    sequential strip of run-length swatches (count + colored
    rectangle + symbol), wrapping into columns when the panel runs
    out of vertical space. The legacy desktop layout is dropped
    intentionally — the textile redesign is what the user asked
    for.                                                          */
class ReportPanel : public BasePanel
{
    Q_OBJECT
public:
    ReportPanel(Model* model, Selection* selection, MainWindow* window, QWidget* parent = nullptr);

    QSize sizeHint() const override;
    QSize minimumSizeHint() const override;

    int xFor(BeadPoint pt) const override { return pt.x() * m_gridx; }
    int yFor(BeadPoint pt) const override { return pt.y() * m_gridy; }
    int offsetXFor(BeadPoint /*pt*/) const override { return 0; }

protected:
    void paintEvent(QPaintEvent* e) override;
};

} // namespace jbead
