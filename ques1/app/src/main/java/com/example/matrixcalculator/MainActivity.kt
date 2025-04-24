package com.example.matrixcalculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            MatrixCalculatorTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Use Accompanist insets to handle notch and system UI
                    SafeMatrixCalculatorApp()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SafeMatrixCalculatorApp() {
    // Create a padding modifier that respects system insets
    val modifier = Modifier
        .fillMaxSize()
        .systemBarsPadding() // This adds padding for status bar (notch) and navigation bar

    MatrixCalculatorApp(modifier)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatrixCalculatorApp(modifier: Modifier = Modifier) {
    val calculator = remember { MatrixCalculator() }
    val scrollState = rememberScrollState()

    var rowsA by remember { mutableStateOf("2") }
    var colsA by remember { mutableStateOf("2") }
    var rowsB by remember { mutableStateOf("2") }
    var colsB by remember { mutableStateOf("2") }

    var matrixAValues by remember { mutableStateOf(Array(2) { Array(2) { "" } }) }
    var matrixBValues by remember { mutableStateOf(Array(2) { Array(2) { "" } }) }

    var selectedOperation by remember { mutableStateOf(MatrixCalculator.OPERATION_ADD) }
    var resultMatrix by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .padding(16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Add top spacing for the status bar/notch area
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "Matrix Calculator",
            style = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Dimensions Input Section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Matrix Dimensions",
                    style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Matrix A", fontWeight = FontWeight.Bold)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TextField(
                                value = rowsA,
                                onValueChange = {
                                    rowsA = it
                                    updateMatrixDimensions(
                                        rowsA.toIntOrNull() ?: 2,
                                        colsA.toIntOrNull() ?: 2,
                                        rowsB.toIntOrNull() ?: 2,
                                        colsB.toIntOrNull() ?: 2,
                                        matrixAValues,
                                        matrixBValues,
                                        { matrixAValues = it },
                                        { matrixBValues = it }
                                    )
                                },
                                label = { Text("Rows") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.width(70.dp)
                            )
                            Text(" × ", fontSize = 24.sp)
                            TextField(
                                value = colsA,
                                onValueChange = {
                                    colsA = it
                                    updateMatrixDimensions(
                                        rowsA.toIntOrNull() ?: 2,
                                        colsA.toIntOrNull() ?: 2,
                                        rowsB.toIntOrNull() ?: 2,
                                        colsB.toIntOrNull() ?: 2,
                                        matrixAValues,
                                        matrixBValues,
                                        { matrixAValues = it },
                                        { matrixBValues = it }
                                    )
                                },
                                label = { Text("Cols") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.width(70.dp)
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Matrix B", fontWeight = FontWeight.Bold)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TextField(
                                value = rowsB,
                                onValueChange = {
                                    rowsB = it
                                    updateMatrixDimensions(
                                        rowsA.toIntOrNull() ?: 2,
                                        colsA.toIntOrNull() ?: 2,
                                        rowsB.toIntOrNull() ?: 2,
                                        colsB.toIntOrNull() ?: 2,
                                        matrixAValues,
                                        matrixBValues,
                                        { matrixAValues = it },
                                        { matrixBValues = it }
                                    )
                                },
                                label = { Text("Rows") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.width(70.dp)
                            )
                            Text(" × ", fontSize = 24.sp)
                            TextField(
                                value = colsB,
                                onValueChange = {
                                    colsB = it
                                    updateMatrixDimensions(
                                        rowsA.toIntOrNull() ?: 2,
                                        colsA.toIntOrNull() ?: 2,
                                        rowsB.toIntOrNull() ?: 2,
                                        colsB.toIntOrNull() ?: 2,
                                        matrixAValues,
                                        matrixBValues,
                                        { matrixAValues = it },
                                        { matrixBValues = it }
                                    )
                                },
                                label = { Text("Cols") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.width(70.dp)
                            )
                        }
                    }
                }
            }
        }

        // Matrix A Input
        MatrixInputCard(
            title = "Matrix A",
            rows = rowsA.toIntOrNull() ?: 2,
            cols = colsA.toIntOrNull() ?: 2,
            values = matrixAValues
        ) { row, col, value ->
            val newMatrix = matrixAValues.map { it.clone() }.toTypedArray()
            newMatrix[row][col] = value
            matrixAValues = newMatrix
        }

        // Operation Selection
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Operation",
                    style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    OperationButton(
                        text = "+",
                        selected = selectedOperation == MatrixCalculator.OPERATION_ADD,
                        onClick = { selectedOperation = MatrixCalculator.OPERATION_ADD }
                    )
                    OperationButton(
                        text = "-",
                        selected = selectedOperation == MatrixCalculator.OPERATION_SUBTRACT,
                        onClick = { selectedOperation = MatrixCalculator.OPERATION_SUBTRACT }
                    )
                    OperationButton(
                        text = "×",
                        selected = selectedOperation == MatrixCalculator.OPERATION_MULTIPLY,
                        onClick = { selectedOperation = MatrixCalculator.OPERATION_MULTIPLY }
                    )
                    OperationButton(
                        text = "÷",
                        selected = selectedOperation == MatrixCalculator.OPERATION_DIVIDE,
                        onClick = { selectedOperation = MatrixCalculator.OPERATION_DIVIDE }
                    )
                }
            }
        }

        // Matrix B Input
        MatrixInputCard(
            title = "Matrix B",
            rows = rowsB.toIntOrNull() ?: 2,
            cols = colsB.toIntOrNull() ?: 2,
            values = matrixBValues
        ) { row, col, value ->
            val newMatrix = matrixBValues.map { it.clone() }.toTypedArray()
            newMatrix[row][col] = value
            matrixBValues = newMatrix
        }

        // Calculate Button
        Button(
            onClick = {
                error = null
                try {
                    val rA = rowsA.toIntOrNull() ?: throw IllegalArgumentException("Invalid rows for Matrix A")
                    val cA = colsA.toIntOrNull() ?: throw IllegalArgumentException("Invalid columns for Matrix A")
                    val rB = rowsB.toIntOrNull() ?: throw IllegalArgumentException("Invalid rows for Matrix B")
                    val cB = colsB.toIntOrNull() ?: throw IllegalArgumentException("Invalid columns for Matrix B")

                    // Validate matrix elements
                    val matrixAString = StringBuilder()
                    for (i in 0 until rA) {
                        for (j in 0 until cA) {
                            val value = matrixAValues[i][j].toDoubleOrNull()
                                ?: throw IllegalArgumentException("Invalid value at Matrix A[${i+1}][${j+1}]")
                            matrixAString.append(value)
                            if (j < cA - 1) matrixAString.append(" ")
                        }
                        if (i < rA - 1) matrixAString.append(";")
                    }

                    val matrixBString = StringBuilder()
                    for (i in 0 until rB) {
                        for (j in 0 until cB) {
                            val value = matrixBValues[i][j].toDoubleOrNull()
                                ?: throw IllegalArgumentException("Invalid value at Matrix B[${i+1}][${j+1}]")
                            matrixBString.append(value)
                            if (j < cB - 1) matrixBString.append(" ")
                        }
                        if (i < rB - 1) matrixBString.append(";")
                    }

                    // Validate operation-specific constraints
                    when (selectedOperation) {
                        MatrixCalculator.OPERATION_ADD, MatrixCalculator.OPERATION_SUBTRACT -> {
                            if (rA != rB || cA != cB) {
                                throw IllegalArgumentException("Matrices must have the same dimensions for addition or subtraction")
                            }
                        }
                        MatrixCalculator.OPERATION_MULTIPLY -> {
                            if (cA != rB) {
                                throw IllegalArgumentException("Number of columns in Matrix A must equal number of rows in Matrix B")
                            }
                        }
                        MatrixCalculator.OPERATION_DIVIDE -> {
                            if (rB != cB) {
                                throw IllegalArgumentException("Matrix B must be square for division")
                            }
                        }
                    }

                    // Perform calculation
                    val result = calculator.performMatrixOperation(
                        matrixAString.toString(),
                        matrixBString.toString(),
                        rA, cA, rB, cB,
                        selectedOperation
                    )

                    if (result.startsWith("Error:")) {
                        error = result.substringAfter("Error: ")
                    } else {
                        resultMatrix = result
                    }
                } catch (e: Exception) {
                    error = e.message
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text("Calculate", fontSize = 18.sp)
        }

        // Result Section
        if (error != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFDEDED)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Error",
                        style = TextStyle(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFB00020)
                        ),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        error!!,
                        style = TextStyle(color = Color(0xFFB00020)),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else if (resultMatrix != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFE8F5E9)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Result Matrix",
                        style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32)),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    DisplayResultMatrix(resultMatrix!!)
                }
            }
        }

        // Add bottom spacing for navigation bar
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatrixInputCard(
    title: String,
    rows: Int,
    cols: Int,
    values: Array<Array<String>>,
    onValueChange: (row: Int, col: Int, value: String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                title,
                style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                for (i in 0 until rows) {
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (j in 0 until cols) {
                            TextField(
                                value = if (i < values.size && j < values[i].size) values[i][j] else "",
                                onValueChange = { onValueChange(i, j, it) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .width(70.dp),
                                colors = TextFieldDefaults.textFieldColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OperationButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.size(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Text(text, fontSize = 24.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun DisplayResultMatrix(resultString: String) {
    val rows = resultString.split(";")

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .border(2.dp, Color(0xFF2E7D32), RoundedCornerShape(8.dp))
            .padding(16.dp)
            .background(Color.Black, RoundedCornerShape(8.dp))
    ) {
        for (row in rows) {
            Row(
                modifier = Modifier.padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                val elements = row.trim().split(" ")
                for (element in elements) {
                    val value = element.toDoubleOrNull()
                    Text(
                        text = value?.let {
                            if (it == it.toInt().toDouble()) it.toInt().toString() else String.format("%.4f", it)
                        } ?: element,
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .width(80.dp),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
            }
        }
    }
}

fun updateMatrixDimensions(
    rowsA: Int,
    colsA: Int,
    rowsB: Int,
    colsB: Int,
    currentMatrixA: Array<Array<String>>,
    currentMatrixB: Array<Array<String>>,
    updateMatrixA: (Array<Array<String>>) -> Unit,
    updateMatrixB: (Array<Array<String>>) -> Unit
) {
    // Ensure valid dimensions (minimum 1x1)
    val safeRowsA = maxOf(1, rowsA)
    val safeColsA = maxOf(1, colsA)
    val safeRowsB = maxOf(1, rowsB)
    val safeColsB = maxOf(1, colsB)

    // Create new matrices with updated dimensions
    val newMatrixA = Array(safeRowsA) { i ->
        Array(safeColsA) { j ->
            if (i < currentMatrixA.size && j < currentMatrixA[0].size) currentMatrixA[i][j] else ""
        }
    }

    val newMatrixB = Array(safeRowsB) { i ->
        Array(safeColsB) { j ->
            if (i < currentMatrixB.size && j < currentMatrixB[0].size) currentMatrixB[i][j] else ""
        }
    }

    // Update the matrices
    updateMatrixA(newMatrixA)
    updateMatrixB(newMatrixB)
}

/**
 * Modifier extension to handle system bars and notch
 */
@Composable
fun Modifier.systemBarsPadding(): Modifier {
    return this.padding(
        top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding(),
        bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    )
}

@Composable
fun MatrixCalculatorTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color(0xFF6200EE),
            secondary = Color(0xFF03DAC5),
            background = Color(0xFF121212),
            surface = Color(0xFF1E1E1E),
            onPrimary = Color.White,
            onSecondary = Color.Black,
            onBackground = Color.White,
            onSurface = Color.White
        ),
        typography = Typography(),
        shapes = Shapes(),
        content = content
    )
}

private val Typography = Typography()
private val Shapes = Shapes()

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MatrixCalculatorTheme {
        SafeMatrixCalculatorApp()
    }
}