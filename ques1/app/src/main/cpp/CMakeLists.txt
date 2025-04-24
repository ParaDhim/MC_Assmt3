cmake_minimum_required(VERSION 3.10.2)

# Set the project name
project(matrix-calculator)

# Add a library with the source files
add_library(
        matrix-calculator
        SHARED
        native-lib.cpp
)

# Include directories
include_directories(${CMAKE_SOURCE_DIR}/include)

# Find the Android NDK
find_library(
        log-lib
        log
)

# Link against the Android NDK libraries
target_link_libraries(
        matrix-calculator
        ${log-lib}
)