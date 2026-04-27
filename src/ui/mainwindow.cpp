#include "mainwindow.h"

#include <QLabel>
#include <QStatusBar>

MainWindow::MainWindow(QWidget* parent)
    : QMainWindow(parent)
{
    setWindowTitle(tr("JBead"));

    /*  Phase 1 placeholder. Phase 3 replaces this with the central
        splitter holding the four pattern views (Draft / Corrected /
        Simulation / Report) plus the redesigned bead-list panel.   */
    auto* placeholder = new QLabel(tr("JBead Qt 6 port — skeleton"), this);
    placeholder->setAlignment(Qt::AlignCenter);
    setCentralWidget(placeholder);

    statusBar()->showMessage(tr("Ready"));
}
