qt_add_library(jbead_ui STATIC
    actions.h         actions.cpp
    basepanel.h       basepanel.cpp
    beadpainter.h     beadpainter.cpp
    colorpickerdialog.h colorpickerdialog.cpp
    colorscheme.h     colorscheme.cpp
    colorstoolbar.h   colorstoolbar.cpp
    correctedpanel.h  correctedpanel.cpp
    dialogs.h         dialogs.cpp
    draftpanel.h      draftpanel.cpp
    imagefactory.h    imagefactory.cpp
    mainwindow.h      mainwindow.cpp
    mrumanager.h      mrumanager.cpp
    reportpanel.h     reportpanel.cpp
    simulationpanel.h simulationpanel.cpp
    swatchbutton.h    swatchbutton.cpp
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
        jbead_print
        jbead_io
        jbead_domain
)

# Translations: lupdate scans every tr() call across the project and
# produces i18n/jbead_<lang>.ts; lrelease compiles those into .qm and
# embeds them into the static library's qrc bundle (default prefix
# "/i18n", default basename "jbead_<lang>.qm"). The translator load
# path in main.cpp (":/i18n/jbead_<lang>") matches exactly. Attaching
# the translation bundle to jbead_ui (rather than to the jbead exe)
# means the test binaries — which link jbead_ui directly — also pick
# up the .qm resource for assertion.
set(JBEAD_TS_FILES
    ${CMAKE_SOURCE_DIR}/i18n/jbead_de.ts
    ${CMAKE_SOURCE_DIR}/i18n/jbead_fr.ts
)

qt_add_translations(jbead_ui
    TS_FILES ${JBEAD_TS_FILES}
)
