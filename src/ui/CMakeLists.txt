qt_add_library(jbead_ui STATIC
    actions.h         actions.cpp
    basepanel.h       basepanel.cpp
    beadpainter.h     beadpainter.cpp
    colorstoolbar.h   colorstoolbar.cpp
    correctedpanel.h  correctedpanel.cpp
    dialogs.h         dialogs.cpp
    draftpanel.h      draftpanel.cpp
    imagefactory.h    imagefactory.cpp
    mainwindow.h      mainwindow.cpp
    reportpanel.h     reportpanel.cpp
    simulationpanel.h simulationpanel.cpp
    ${CMAKE_SOURCE_DIR}/resources/icons/icons.qrc
)

target_include_directories(jbead_ui PUBLIC ${CMAKE_SOURCE_DIR}/src)

target_link_libraries(jbead_ui
    PUBLIC
        Qt6::Core
        Qt6::Gui
        Qt6::Widgets
        Qt6::PrintSupport
        Qt6::Svg
        jbead_io
        jbead_domain
)
