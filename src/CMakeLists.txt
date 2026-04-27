qt_add_executable(jbead
    main.cpp
)

target_link_libraries(jbead
    PRIVATE
        Qt6::Core
        Qt6::Gui
        Qt6::Widgets
        Qt6::PrintSupport
        jbead_ui
        jbead_print
        jbead_io
        jbead_domain
)

set_target_properties(jbead PROPERTIES
    WIN32_EXECUTABLE ON
    MACOSX_BUNDLE    ON
)

add_subdirectory(io)
add_subdirectory(domain)
add_subdirectory(print)
add_subdirectory(ui)
