package com.example.matrixcalculator

class MatrixCalculator {
    companion object {
        // Constants for operation type
        const val OPERATION_ADD = 0
        const val OPERATION_SUBTRACT = 1
        const val OPERATION_MULTIPLY = 2
        const val OPERATION_DIVIDE = 3

        init {
            System.loadLibrary("matrix-calculator")
        }
    }

    // Native method to perform matrix operations
    external fun performMatrixOperation(
        matrixA: String,
        matrixB: String,
        rowsA: Int,
        colsA: Int,
        rowsB: Int,
        colsB: Int,
        operation: Int
    ): String
}