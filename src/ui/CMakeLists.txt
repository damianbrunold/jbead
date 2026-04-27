qt_add_library(jbead_ui STATIC
    mainwindow.cpp
    mainwindow.h
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
