# Matrix Calculator Android App 🧮

## Overview ✨
Matrix Calculator is an Android application built using Jetpack Compose and JNI (Java Native Interface) that allows users to perform common matrix operations. The app leverages C++ and the Eigen library for efficient matrix computations while providing a modern, intuitive user interface.

## Features 🚀
- ➕ Matrix addition
- ➖ Matrix subtraction
- ✖️ Matrix multiplication
- ➗ Matrix division
- 🔄 Dynamic matrix size adjustment
- ✅ Real-time input validation
- ⚠️ Error handling with descriptive messages
- 🎨 Clean, modern UI built with Jetpack Compose
- ⚡ Efficient calculations using native C++ code

## Screenshots 📱

### Main Interface
<img width="354" alt="Screenshot 2025-04-24 at 10 37 54 AM" src="https://github.com/user-attachments/assets/c73927d0-5e34-4945-9ddc-7fb893bd2279" />

### Calculation Results
<img width="326" alt="Screenshot 2025-04-24 at 10 38 12 AM" src="https://github.com/user-attachments/assets/bbff870e-a050-4ccb-a1cd-5ffb5e9f6b31" />


## Technical Stack 🛠️
- **UI Framework**: Jetpack Compose 🖌️
- **Native Code**: C++ with JNI 🔌
- **Matrix Library**: Eigen (C++) 📊
- **Build System**: Gradle with CMake for native code 🏗️

## Project Structure 📁

### Android App (Kotlin) 📱
- `MainActivity.kt`: Entry point for the app with UI components
- `MatrixCalculator.kt`: JNI wrapper class for native operations

### Native Code (C++) ⚙️
- `native-lib.cpp`: Implementation of matrix operations using Eigen library
- Matrix class that encapsulates Eigen's MatrixXd with operations:
  - Addition
  - Subtraction
  - Multiplication
  - Division (using matrix inverse)

### CMake Configuration 🔧
```cmake
cmake_minimum_required(VERSION 3.10.2)
project(matrix-calculator)
add_library(
        matrix-calculator
        SHARED
        native-lib.cpp
)
include_directories(${CMAKE_SOURCE_DIR}/include)
find_library(
        log-lib
        log
)
target_link_libraries(
        matrix-calculator
        ${log-lib}
)
```

## Setup Instructions 📝

### Prerequisites
- Android Studio Arctic Fox (2020.3.1) or newer 🦊
- CMake 3.10.2 or higher
- Android NDK 21.0.6113669 or newer
- Eigen library (included in the project)

### Build & Run 🚀
1. Clone the repository
2. Open the project in Android Studio
3. Sync Gradle files
4. Build the project (this will compile both Kotlin and C++ code)
5. Run on an emulator or physical device

## How It Works 🔍

### User Interface Flow
1. Set dimensions for Matrix A and Matrix B ⚙️
2. Enter values for each matrix 🔢
3. Select an operation (addition, subtraction, multiplication, or division) 🔣
4. Press "Calculate" to perform the operation ✨
5. View the result or error message 📊

### Native Code Execution
1. Matrix data is collected from UI and passed to the native layer 📤
2. C++ code parses the matrices from string representations 🔄
3. Eigen library performs the requested operation 🧮
4. Result is formatted and returned to Kotlin/Java layer 📥
5. UI displays the formatted result 📱

## Limitations ⚠️
- Matrix dimensions are limited by device memory
- Division requires Matrix B to be invertible (non-singular)
- For multiplication, Matrix A columns must equal Matrix B rows

## Future Improvements 💡
- Additional matrix operations (determinant, transpose, etc.)
- Save/load matrices 💾
- Matrix templates for common scenarios 📋
- Performance optimizations for large matrices 🚀
- Dark/light theme support 🌓

## License 📄
[Insert your license here]

## Acknowledgements 🙏
- Eigen library for matrix operations
- Jetpack Compose for modern Android UI
