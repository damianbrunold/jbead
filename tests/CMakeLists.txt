qt_add_executable(test_smoke test_smoke.cpp)
target_link_libraries(test_smoke PRIVATE Qt6::Core Qt6::Test)
add_test(NAME smoke COMMAND test_smoke)

qt_add_executable(test_beadfield test_beadfield.cpp)
target_link_libraries(test_beadfield PRIVATE Qt6::Core Qt6::Gui Qt6::Test jbead_domain)
add_test(NAME beadfield COMMAND test_beadfield)

qt_add_executable(test_objectmodel test_objectmodel.cpp)
target_link_libraries(test_objectmodel PRIVATE Qt6::Core Qt6::Gui Qt6::Test jbead_io)
add_test(NAME objectmodel COMMAND test_objectmodel)

qt_add_executable(test_jbeadstream test_jbeadstream.cpp)
target_link_libraries(test_jbeadstream PRIVATE Qt6::Core Qt6::Gui Qt6::Test jbead_io)
add_test(NAME jbeadstream COMMAND test_jbeadstream)

qt_add_executable(test_fileformat test_fileformat.cpp)
target_link_libraries(test_fileformat PRIVATE Qt6::Core Qt6::Gui Qt6::Test jbead_io jbead_domain)
target_compile_definitions(test_fileformat PRIVATE
    JBEAD_SAMPLES_DIR="${CMAKE_SOURCE_DIR}/samples")
add_test(NAME fileformat COMMAND test_fileformat)

qt_add_executable(test_mainwindow test_mainwindow.cpp)
target_link_libraries(test_mainwindow PRIVATE
    Qt6::Core Qt6::Gui Qt6::Widgets Qt6::PrintSupport Qt6::Test
    jbead_ui jbead_io jbead_domain)
target_compile_definitions(test_mainwindow PRIVATE
    JBEAD_SAMPLES_DIR="${CMAKE_SOURCE_DIR}/samples")
add_test(NAME mainwindow COMMAND test_mainwindow)
set_tests_properties(mainwindow PROPERTIES
    ENVIRONMENT "QT_QPA_PLATFORM=offscreen")
