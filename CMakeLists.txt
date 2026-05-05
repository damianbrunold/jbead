cmake_minimum_required(VERSION 3.21)
project(jbead
    VERSION 2.0.1
    DESCRIPTION "JBead bead-pattern designer (Qt 6 port)"
    LANGUAGES CXX
)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS OFF)

set(CMAKE_AUTOMOC ON)
set(CMAKE_AUTOUIC ON)
set(CMAKE_AUTORCC ON)

option(JBEAD_BUILD_TESTS "Build unit tests" ON)

# Qt 6.5 LTS minimum. LinguistTools drives lupdate / lrelease for the
# .ts -> .qm pipeline used by the in-app translator (jbead_de.ts /
# jbead_fr.ts under i18n/).
find_package(Qt6 6.5 REQUIRED COMPONENTS Core Gui Widgets PrintSupport Svg LinguistTools)
if(JBEAD_BUILD_TESTS)
    find_package(Qt6 6.5 REQUIRED COMPONENTS Test)
    enable_testing()
endif()

qt_standard_project_setup()

add_subdirectory(src)

if(JBEAD_BUILD_TESTS)
    add_subdirectory(tests)
endif()

# ---------------------------------------------------------------------------
# Install rules + per-platform bundle glue used by the packaging scripts
# under packaging/. Running `cmake --install build --prefix <dir>` lays out
# a relocatable tree that linuxdeploy / windeployqt / macdeployqt can
# consume.
# ---------------------------------------------------------------------------
include(GNUInstallDirs)

if(APPLE)
    set_target_properties(jbead PROPERTIES
        MACOSX_BUNDLE_INFO_PLIST       "${CMAKE_SOURCE_DIR}/resources/macos/Info.plist.in"
        MACOSX_BUNDLE_BUNDLE_NAME      "JBead"
        MACOSX_BUNDLE_GUI_IDENTIFIER   "ch.brunoldsoftware.jbead"
        MACOSX_BUNDLE_BUNDLE_VERSION   "${PROJECT_VERSION}"
        MACOSX_BUNDLE_SHORT_VERSION_STRING "${PROJECT_VERSION}"
        MACOSX_BUNDLE_ICON_FILE        "jbead.icns"
    )
    install(TARGETS jbead BUNDLE DESTINATION .)
    # Install the bundle icon directly into Contents/Resources. Doing it as
    # an install(FILES) rule (rather than target_sources +
    # MACOSX_PACKAGE_LOCATION) is what reliably lands it inside the .app
    # for the Ninja generator.
    if(EXISTS "${CMAKE_SOURCE_DIR}/resources/macos/jbead.icns")
        install(FILES "${CMAKE_SOURCE_DIR}/resources/macos/jbead.icns"
                DESTINATION "jbead.app/Contents/Resources")
    endif()
elseif(WIN32)
    install(TARGETS jbead RUNTIME DESTINATION .)
else()
    install(TARGETS jbead RUNTIME DESTINATION ${CMAKE_INSTALL_BINDIR})
    install(FILES resources/linux/jbead.desktop
            DESTINATION ${CMAKE_INSTALL_DATADIR}/applications)
    install(FILES resources/icons/jbead-32.png
            DESTINATION ${CMAKE_INSTALL_DATADIR}/icons/hicolor/32x32/apps
            RENAME jbead.png)
    install(FILES resources/icons/jbead-16.png
            DESTINATION ${CMAKE_INSTALL_DATADIR}/icons/hicolor/16x16/apps
            RENAME jbead.png)
    install(FILES resources/jbead.svg
            DESTINATION ${CMAKE_INSTALL_DATADIR}/icons/hicolor/scalable/apps)
endif()

install(FILES LICENSE.txt README.txt
        DESTINATION ${CMAKE_INSTALL_DOCDIR} OPTIONAL)
