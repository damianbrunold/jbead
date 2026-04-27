#include "swatchbutton.h"

#include <QMouseEvent>

namespace jbead {

SwatchButton::SwatchButton(QWidget* parent) : QToolButton(parent) {}

void SwatchButton::mouseDoubleClickEvent(QMouseEvent* e)
{
    if (e->button() == Qt::LeftButton) {
        emit doubleClicked();
        e->accept();
        return;
    }
    QToolButton::mouseDoubleClickEvent(e);
}

} // namespace jbead
