set(_jbead_platform_sources "")
if(WIN32)
    # Embed the application icon into jbead.exe so Explorer, the
    # taskbar and the Start menu pick it up. Both MSVC's rc.exe and
    # MinGW's windres compile .rc files automatically when listed
    # as a target source.
    list(APPEND _jbead_platform_sources
        "${CMAKE_SOURCE_DIR}/resources/windows/jbead.rc")
endif()

configure_file(
    "${CMAKE_CURRENT_SOURCE_DIR}/version.h.in"
    "${CMAKE_CURRENT_BINARY_DIR}/version.h"
    @ONLY
)

qt_add_executable(jbead
    main.cpp
    ${_jbead_platform_sources}
)

target_include_directories(jbead PRIVATE "${CMAKE_CURRENT_BINARY_DIR}")

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
