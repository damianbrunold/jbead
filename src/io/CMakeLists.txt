qt_add_library(jbead_io STATIC
    jbeadstream.h     jbeadstream.cpp
    objectmodel.h     objectmodel.cpp
    memento.h
    jbeadmemento.h    jbeadmemento.cpp
    dbbmemento.h      dbbmemento.cpp
    fileformat.h      fileformat.cpp
)

target_include_directories(jbead_io PUBLIC ${CMAKE_SOURCE_DIR}/src)

target_link_libraries(jbead_io
    PUBLIC
        Qt6::Core
        Qt6::Gui
        jbead_domain
)
