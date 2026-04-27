qt_add_library(jbead_print STATIC
    printsettings.h  printsettings.cpp
    strippainter.h   strippainter.cpp
    printjob.h       printjob.cpp
)

target_include_directories(jbead_print PUBLIC ${CMAKE_SOURCE_DIR}/src)

target_link_libraries(jbead_print
    PUBLIC
        Qt6::Core
        Qt6::Gui
        Qt6::PrintSupport
        Qt6::Svg
        jbead_domain
)
