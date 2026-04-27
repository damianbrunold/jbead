qt_add_library(jbead_domain STATIC
    beadpoint.h
    beadrect.h
    beadrun.h
    rectiterator.h
    segmentiterator.h
    defaultcolors.h     defaultcolors.cpp
    beadsymbols.h       beadsymbols.cpp
    beadfield.h         beadfield.cpp
    beadundo.h          beadundo.cpp
    beadcounts.h        beadcounts.cpp
    beadlist.h          beadlist.cpp
    selection.h         selection.cpp
    settings.h          settings.cpp
    model.h             model.cpp
)

target_include_directories(jbead_domain PUBLIC ${CMAKE_SOURCE_DIR}/src)

target_link_libraries(jbead_domain
    PUBLIC
        Qt6::Core
        Qt6::Gui
)
