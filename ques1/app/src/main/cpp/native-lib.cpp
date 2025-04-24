#include <jni.h>
#include <string>
#include <sstream>
#include <stdexcept>
#include <iostream>
#include "Eigen/Dense" // Include Eigen library

class Matrix {
private:
    Eigen::MatrixXd data; // Use Eigen's matrix type

public:
    Matrix(int rows, int cols) : data(rows, cols) {
        data.setZero(); // Initialize with zeros
    }

    // Constructor from Eigen matrix
    explicit Matrix(const Eigen::MatrixXd& mat) : data(mat) {}

    void setElement(int i, int j, double value) {
        if (i >= 0 && i < data.rows() && j >= 0 && j < data.cols()) {
            data(i, j) = value;
        } else {
            throw std::out_of_range("Matrix indices out of range");
        }
    }

    double getElement(int i, int j) const {
        if (i >= 0 && i < data.rows() && j >= 0 && j < data.cols()) {
            return data(i, j);
        } else {
            throw std::out_of_range("Matrix indices out of range");
        }
    }

    int getRows() const { return data.rows(); }
    int getCols() const { return data.cols(); }

    // Matrix addition
    Matrix add(const Matrix& other) const {
        if (data.rows() != other.data.rows() || data.cols() != other.data.cols()) {
            throw std::invalid_argument("Matrix dimensions don't match for addition");
        }
        return Matrix(data + other.data); // Use Eigen's operator+
    }

    // Matrix subtraction
    Matrix subtract(const Matrix& other) const {
        if (data.rows() != other.data.rows() || data.cols() != other.data.cols()) {
            throw std::invalid_argument("Matrix dimensions don't match for subtraction");
        }
        return Matrix(data - other.data); // Use Eigen's operator-
    }

    // Matrix multiplication
    Matrix multiply(const Matrix& other) const {
        if (data.cols() != other.data.rows()) {
            throw std::invalid_argument("Matrix dimensions don't match for multiplication");
        }
        return Matrix(data * other.data); // Use Eigen's operator*
    }

    // Matrix division (multiplies by inverse)
    Matrix divide(const Matrix& other) const {
        if (other.data.rows() != other.data.cols()) {
            throw std::invalid_argument("Divisor matrix must be square for division");
        }

        // Check if matrix is invertible
        Eigen::FullPivLU<Eigen::MatrixXd> lu(other.data);
        if (!lu.isInvertible()) {
            throw std::invalid_argument("Matrix is singular, cannot be inverted");
        }

        return Matrix(data * other.data.inverse()); // Use Eigen's inverse()
    }

    std::string toString() const {
        std::stringstream ss;
        for (int i = 0; i < data.rows(); i++) {
            for (int j = 0; j < data.cols(); j++) {
                ss << data(i, j);
                if (j < data.cols() - 1) ss << " ";
            }
            if (i < data.rows() - 1) ss << ";";
        }
        return ss.str();
    }
};

// Helper function to parse matrix from string
Matrix parseMatrix(const char* matrixStr, int rows, int cols) {
    Matrix matrix(rows, cols);
    std::istringstream ss(matrixStr);
    std::string rowStr;
    int rowIdx = 0;

    while (std::getline(ss, rowStr, ';') && rowIdx < rows) {
        std::istringstream rowSS(rowStr);
        double value;
        int colIdx = 0;

        while (rowSS >> value && colIdx < cols) {
            matrix.setElement(rowIdx, colIdx, value);
            colIdx++;
        }

        rowIdx++;
    }

    return matrix;
}

extern "C" {
JNIEXPORT jstring JNICALL
Java_com_example_matrixcalculator_MatrixCalculator_performMatrixOperation(
        JNIEnv* env,
        jobject /* this */,
        jstring matrixA,
        jstring matrixB,
        jint rowsA,
        jint colsA,
        jint rowsB,
        jint colsB,
        jint operation) {

    std::string resultStr;
    const char* matrixAStr = nullptr;
    const char* matrixBStr = nullptr;
    std::cout << "performMatrixOperation JNI function invoked" << std::endl;

    try {
        // Get string data from Java
        matrixAStr = env->GetStringUTFChars(matrixA, nullptr);
        if (!matrixAStr) {
            throw std::runtime_error("Failed to get matrix A string");
        }

        matrixBStr = env->GetStringUTFChars(matrixB, nullptr);
        if (!matrixBStr) {
            throw std::runtime_error("Failed to get matrix B string");
        }

        // Parse matrices
        Matrix a = parseMatrix(matrixAStr, rowsA, colsA);
        Matrix b = parseMatrix(matrixBStr, rowsB, colsB);

        Matrix result(1, 1);

        // Perform operation
        switch (operation) {
            case 0: // Addition
                result = a.add(b);
                break;
            case 1: // Subtraction
                result = a.subtract(b);
                break;
            case 2: // Multiplication
                result = a.multiply(b);
                break;
            case 3: // Division
                result = a.divide(b);
                break;
            default:
                throw std::invalid_argument("Invalid operation code");
        }

        resultStr = result.toString();
    } catch (const std::exception& e) {
        resultStr = "Error: " + std::string(e.what());
    }

    // Clean up resources
    if (matrixAStr) {
        env->ReleaseStringUTFChars(matrixA, matrixAStr);
    }
    if (matrixBStr) {
        env->ReleaseStringUTFChars(matrixB, matrixBStr);
    }

    return env->NewStringUTF(resultStr.c_str());
}
}