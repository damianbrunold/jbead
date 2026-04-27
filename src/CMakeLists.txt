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
        jbead_io
        jbead_domain
)

set_target_properties(jbead PROPERTIES
    WIN32_EXECUTABLE ON
    MACOSX_BUNDLE    ON
)

# Translations: lupdate scans the listed sources and produces
# i18n/jbead_<lang>.ts; lrelease compiles those into .qm and embeds
# them into the executable via a generated qrc. The .qm prefix matches
# the translator load path in main.cpp (":/i18n/").
set(JBEAD_TS_FILES
    ${CMAKE_SOURCE_DIR}/i18n/jbead_de.ts
    ${CMAKE_SOURCE_DIR}/i18n/jbead_fr.ts
)

qt_add_translations(jbead
    TS_FILES ${JBEAD_TS_FILES}
    QM_FILES_OUTPUT_VARIABLE JBEAD_QM_FILES
)

add_subdirectory(compat)
add_subdirectory(io)
add_subdirectory(domain)
add_subdirectory(print)
add_subdirectory(ui)
