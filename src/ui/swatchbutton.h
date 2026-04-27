#pragma once

#include <QToolButton>

namespace jbead {

/*  QToolButton variant that emits a doubleClicked signal — Qt's
    base QAbstractButton turns the second click in a double-click
    into a regular click() (after a short delay), so a custom
    signal is the cleanest way to detect "user double-clicked this
    swatch to edit it".                                             */
class SwatchButton : public QToolButton
{
    Q_OBJECT
public:
    explicit SwatchButton(QWidget* parent = nullptr);

signals:
    void doubleClicked();

protected:
    void mouseDoubleClickEvent(QMouseEvent* e) override;
};

} // namespace jbead
