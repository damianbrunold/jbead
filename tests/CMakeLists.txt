qt_add_executable(test_smoke
    test_smoke.cpp
)

target_link_libraries(test_smoke
    PRIVATE
        Qt6::Core
        Qt6::Test
)

add_test(NAME smoke COMMAND test_smoke)
