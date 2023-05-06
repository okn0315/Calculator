package com.example.calculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calculator.ui.theme.CalculatorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CalculatorTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CalculatorAppearance()
                }
            }
        }
    }
}

enum class CalculatorState {
    NUMBER_ACCEPTING,
    OPERATION_ACCEPTING,
}
enum class ButtonName {
    ZERO,
    ONE,
    TWO,
    THREE,
    FOUR,
    FIVE,
    SIX,
    SEVEN,
    EIGHT,
    NINE,
    PLUS,
    MINUS,
    TIMES,
    DIVIDED,
    EQUAL,
    CLEAR,
}

enum class ErrorType {
    ZERO_DIVISION,
    UNEXPECTED_VALUE,
}

class Calculator(val callbackWhenError: (List<Any>) -> List<Any>) {
    private var displayNum: Int = 0
    private var displayOperation: ButtonName? = null
    private var memory: Int = 0
    private var state: CalculatorState = CalculatorState.OPERATION_ACCEPTING
    private var stackingNumber: Int = 0
    private var stackingOperation: ButtonName? = null

    fun buttonPress(button: ButtonName) : Pair<Int, ButtonName?> {
        when (state) {
            CalculatorState.NUMBER_ACCEPTING ->
                buttonPressedProcess(
                    button,
                    {
                        if (isNumberButton(button)) {
                            stackingNumber = stackingNumber * 10 + toNumber(button)!!
                            displayNum = stackingNumber
                        } else {
                            callbackWhenError(listOf(ErrorType.UNEXPECTED_VALUE))
                        }
                    },
                    {
                        if (isOperationButton(button)) {
                            if (stackingOperation == null) {
                                memory = stackingNumber
                                stackingOperation = button
                                state = CalculatorState.OPERATION_ACCEPTING
                            } else if (isOperationButton(stackingOperation!!)) {
                                memory = calculateTwoNumber(
                                    memory,
                                    stackingNumber,
                                    stackingOperation!!
                                )
                                displayNum = memory
                                stackingNumber = 0
                                stackingOperation = button
                                state = CalculatorState.OPERATION_ACCEPTING
                            } else {
                                callbackWhenError(listOf(ErrorType.UNEXPECTED_VALUE))
                            }
                            displayOperation = button
                        } else {
                            callbackWhenError(listOf(ErrorType.UNEXPECTED_VALUE))
                        }
                    },
                    {
                        if (button == ButtonName.EQUAL) {
                            if (stackingOperation != null) {
                                if (isOperationButton(stackingOperation!!)) {
                                    memory = calculateTwoNumber(
                                        memory,
                                        stackingNumber,
                                        stackingOperation!!
                                    )
                                } else {
                                    callbackWhenError(listOf(ErrorType.UNEXPECTED_VALUE))
                                }
                                displayNum = memory
                                stackingNumber = 0
                                stackingOperation = null
                                state = CalculatorState.OPERATION_ACCEPTING
                            }
                            displayOperation = null
                        } else {
                            callbackWhenError(listOf(ErrorType.UNEXPECTED_VALUE))
                        }
                    },
                    {
                        displayNum = 0
                        displayOperation = null
                        state = CalculatorState.OPERATION_ACCEPTING
                        memory = 0
                        stackingNumber = 0
                        stackingOperation = null
                    },
                )
            CalculatorState.OPERATION_ACCEPTING ->
                buttonPressedProcess(
                    button,
                    {
                        if (isNumberButton(button)) {
                            stackingNumber = toNumber(button)!!
                            displayNum = stackingNumber
                            state = CalculatorState.NUMBER_ACCEPTING
                        } else {
                            callbackWhenError(listOf(ErrorType.UNEXPECTED_VALUE))
                        }
                    },
                    {
                        if (isOperationButton(button)) {
                            stackingOperation = button
                            displayOperation = button
                        } else {
                            callbackWhenError(listOf(ErrorType.UNEXPECTED_VALUE))
                        }
                    },
                    {
                        stackingOperation = null
                        displayOperation = null
                    },
                    {
                        displayNum = 0
                        displayOperation = null
                        state = CalculatorState.OPERATION_ACCEPTING
                        memory = 0
                        stackingNumber = 0
                        stackingOperation = null
                    },
                )
        }

        return Pair(displayNum, displayOperation)
    }

    private fun calculateTwoNumber(
        firstNumber: Int,
        secondNumber: Int,
        operation: ButtonName
    ) : Int {
        return when (operation) {
            ButtonName.PLUS -> firstNumber + secondNumber
            ButtonName.MINUS -> firstNumber - secondNumber
            ButtonName.TIMES -> firstNumber * secondNumber
            ButtonName.DIVIDED -> {
                if (secondNumber != 0) {
                    firstNumber / secondNumber
                } else {
                    callbackWhenError(listOf(ErrorType.ZERO_DIVISION))
                    0
                }
            }
            else -> {
                callbackWhenError(listOf(ErrorType.UNEXPECTED_VALUE))
                0
            }
        }
    }

    private fun buttonPressedProcess(
        button: ButtonName,
        functionWhenPressedNumber: (ButtonName) -> Unit,
        functionWhenPressedOperation: (ButtonName) -> Unit,
        functionWhenPressedEqual: () -> Unit,
        functionWhenPressedClear: () -> Unit,
        functionWhenError : (List<Any>) -> List<Any> = callbackWhenError,
    ) {
        if (isNumberButton(button)) {
            functionWhenPressedNumber(button)
        } else if (isOperationButton(button)) {
            functionWhenPressedOperation(button)
        } else if (button == ButtonName.EQUAL) {
            functionWhenPressedEqual()
        } else if (button == ButtonName.CLEAR) {
            functionWhenPressedClear()
        } else {
            functionWhenError(listOf(ErrorType.UNEXPECTED_VALUE))
        }
    }

    private fun isNumberButton(button: ButtonName) : Boolean {
        return when (button) {
            ButtonName.ZERO,
            ButtonName.ONE,
            ButtonName.TWO,
            ButtonName.THREE,
            ButtonName.FOUR,
            ButtonName.FIVE,
            ButtonName.SIX,
            ButtonName.SEVEN,
            ButtonName.EIGHT,
            ButtonName.NINE, -> true
            else -> false
        }
    }

    private fun isOperationButton(button: ButtonName) : Boolean {
        return when (button) {
            ButtonName.PLUS,
            ButtonName.MINUS,
            ButtonName.TIMES,
            ButtonName.DIVIDED, -> true
            else -> false
        }
    }

    private fun toNumber(button: ButtonName) : Int? {
        return when (button) {
            ButtonName.ZERO -> 0
            ButtonName.ONE -> 1
            ButtonName.TWO -> 2
            ButtonName.THREE -> 3
            ButtonName.FOUR -> 4
            ButtonName.FIVE -> 5
            ButtonName.SIX -> 6
            ButtonName.SEVEN -> 7
            ButtonName.EIGHT -> 8
            ButtonName.NINE -> 9
            else -> null
        }
    }
}

@Composable
fun CalculatorAppearance(modifier: Modifier = Modifier) {
    var additionalMessage: Array<String> by remember { mutableStateOf(arrayOf()) }

    fun arrayAddAt(array: Array<String>, index: Int, string: String) : Array<String> {
        return if (index in 0..array.size) {
            (array.take(index) + string + array.takeLast(array.lastIndex - index + 1)).toTypedArray()
        } else {
            array
        }
    }

    fun arrayRemoveAt(array: Array<String>, index: Int) : Array<String> {
        return if (index in array.indices) {
            (array.take(index) + array.takeLast(array.lastIndex - index)).toTypedArray()
        } else {
            array
        }
    }


    fun callbackWhenError(description: List<Any>) : List<Any> {
        if (description.isEmpty()) {
            additionalMessage = arrayAddAt(
                additionalMessage, additionalMessage.size,
                "\"callbackWhenError()\" call error."
            )
        } else {
            when (description[0]) {
                ErrorType.UNEXPECTED_VALUE -> additionalMessage = arrayAddAt(
                    additionalMessage,
                    additionalMessage.size,
                    "System error. Please report."
                )
                ErrorType.ZERO_DIVISION -> additionalMessage = arrayAddAt(
                    additionalMessage,
                    additionalMessage.size,
                    "Can't be divided by 0."
                )
            }
        }
        return listOf()
    }


    val calculator by remember { mutableStateOf( Calculator( ::callbackWhenError ) ) }
    var result: Pair<Int, ButtonName?> by remember { mutableStateOf( Pair(0, null) ) }
    var number by remember { mutableStateOf(0) }
    var operation: ButtonName? by remember { mutableStateOf(null) }
    val maxAdditionalMessage = 4

    Column(
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .fillMaxSize()
            .background(color = Color.White)
            .padding(30.dp)
    ) {
        Column {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = modifier.fillMaxWidth()
            ) {
                number = result.component1()
                operation = result.component2()
                Text(
                    text = number.toString(),
                    fontSize = 40.sp
                )
                Text(
                    text = when (operation) {
                        ButtonName.PLUS -> "+"
                        ButtonName.MINUS -> "-"
                        ButtonName.TIMES -> "×"
                        ButtonName.DIVIDED -> "÷"
                        null -> "?"
                        else -> "error: Unknown operation"
                    },
                    fontSize = 40.sp,
                    textAlign = TextAlign.End
                )
            }
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp, bottom = 30.dp)
            ) {
                Column {
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = { result = calculator.buttonPress(ButtonName.SEVEN) }
                        ) {
                            Text("7")
                        }
                        Button(
                            onClick = { result = calculator.buttonPress(ButtonName.EIGHT) },
                            modifier = modifier.padding(start = 5.dp)
                        ) {
                            Text("8")
                        }
                        Button(
                            onClick = { result = calculator.buttonPress(ButtonName.NINE) },
                            modifier = modifier.padding(start = 5.dp)
                        ) {
                            Text("9")
                        }
                    }
                    Row {
                        Button(
                            onClick = { result = calculator.buttonPress(ButtonName.FOUR) }
                        ) {
                            Text("4")
                        }
                        Button(
                            onClick = { result = calculator.buttonPress(ButtonName.FIVE) },
                            modifier = modifier.padding(start = 5.dp)
                        ) {
                            Text("5")
                        }
                        Button(
                            onClick = { result = calculator.buttonPress(ButtonName.SIX) },
                            modifier = modifier.padding(start = 5.dp)
                        ) {
                            Text("6")
                        }
                    }
                    Row {
                        Button(
                            onClick = { result = calculator.buttonPress(ButtonName.ONE) }
                        ) {
                            Text("1")
                        }
                        Button(
                            onClick = { result = calculator.buttonPress(ButtonName.TWO) },
                            modifier = modifier.padding(start = 5.dp)
                        ) {
                            Text("2")
                        }
                        Button(
                            onClick = { result = calculator.buttonPress(ButtonName.THREE) },
                            modifier = modifier.padding(start = 5.dp)
                        ) {
                            Text("3")
                        }
                    }
                    Button(onClick = { result = calculator.buttonPress(ButtonName.ZERO) }) {
                        Text("0")
                    }
                }
                Column {
                    Button(onClick = { result = calculator.buttonPress(ButtonName.CLEAR) }) {
                        Text("C")
                    }
                    Row {
                        Button(
                            onClick = { result = calculator.buttonPress(ButtonName.PLUS) }
                        ) {
                            Text("+")
                        }
                        Button(
                            onClick = { result = calculator.buttonPress(ButtonName.MINUS) },
                            modifier = modifier.padding(start = 5.dp)
                        ) {
                            Text("-")
                        }
                    }
                    Row {
                        Button(
                            onClick = { result = calculator.buttonPress(ButtonName.TIMES) }
                        ) {
                            Text("×")
                        }
                        Button(
                            onClick = { result = calculator.buttonPress(ButtonName.DIVIDED) },
                            modifier = modifier.padding(start = 5.dp)
                        ) {
                            Text("÷")
                        }
                    }
                    Button(onClick = { result = calculator.buttonPress(ButtonName.EQUAL) }) {
                        Text("=")
                    }
                }
            }
            if (additionalMessage.isNotEmpty()) {
                for (
                index in additionalMessage.size - 1
                        downTo arrayOf(additionalMessage.size - maxAdditionalMessage, 0).max()
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp)
                    ) {
                        Text(
                            text = additionalMessage[index],
                            color = Color.Red
                        )
                        Button(
                            onClick = {
                                additionalMessage = arrayRemoveAt(
                                    additionalMessage,
                                    index
                                )
                            },
                            colors = buttonColors(
                                containerColor = Color.Red,
                                contentColor = Color.Yellow
                            ),
                            contentPadding = PaddingValues.Absolute(0.dp),
                            modifier = modifier
                                .height(35.dp)
                                .width(35.dp)
                        ) {
                            Text(
                                text = "OK",
                            )
                        }
                    }
                }
            }
        }
        Text(
            text = "ver0.1",
            modifier = Modifier
                .align(alignment = Alignment.End)
        )
    }
}

@Preview
@Composable
fun CalculatorAppearancePreview(modifier: Modifier = Modifier) {
    CalculatorTheme {
        CalculatorAppearance()
    }
}