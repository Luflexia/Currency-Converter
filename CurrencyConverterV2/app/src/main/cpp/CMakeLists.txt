cmake_minimum_required(VERSION 3.31.0)

add_library(user_validation SHARED
        user_validation.cpp)

find_library(log-lib
        log)

target_link_libraries(user_validation
        ${log-lib})