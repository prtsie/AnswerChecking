package com.example.answerchecking

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.random.Random

var isStarted: MutableState<Boolean>? = null
var first: MutableState<Int>? = null
var second: MutableState<Int>? = null
var correct: MutableState<Int>? = null
var incorrect: MutableState<Int>? = null
var operation: MutableState<Op>? = null
var answer: MutableState<Int>? = null
var input: MutableState<String>? = null
var parsedInput: MutableState<Int>? = null
var backColor: MutableState<Color>? = null
var startTime: MutableState<Long>? = null
var currentTime: MutableState<Long>? = null
var responseTimes: MutableState<List<Long>>? = null
var lastUserAnswerIsCorrect = false
var currentAnswerCorrect = false
var colorSaturation = 0.85f

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Greeting()
        }
    }
}

@Composable
fun Greeting() {
    isStarted = rememberSaveable { mutableStateOf(false) }
    first = rememberSaveable { mutableIntStateOf(0) }
    second = rememberSaveable { mutableIntStateOf(0) }
    correct = rememberSaveable { mutableIntStateOf(0) }
    incorrect = rememberSaveable { mutableIntStateOf(0) }
    parsedInput = rememberSaveable { mutableIntStateOf(0) }
    answer = rememberSaveable { mutableIntStateOf(0) }
    input = rememberSaveable { mutableStateOf("") }
    operation = rememberSaveable { mutableStateOf(Op.Add) }
    backColor = remember { mutableStateOf(Color.White) }
    startTime = rememberSaveable { mutableLongStateOf(0L) }
    currentTime = rememberSaveable { mutableLongStateOf(0L) }
    responseTimes = rememberSaveable { mutableStateOf(listOf<Long>()) }
    val coroutineScope = rememberCoroutineScope()

    fun generateEquation() {
        first!!.value = Random.nextInt(10, 89)
        operation!!.value = Op.entries[Random.nextInt(Op.entries.count())]
        when (operation!!.value) {
            Op.Add -> {
                second!!.value = Random.nextInt(10, 99 - first!!.value)
                answer!!.value = first!!.value + second!!.value
            }

            Op.Subtract -> {
                second!!.value = Random.nextInt(10, first!!.value + 1)
                answer!!.value = first!!.value - second!!.value
            }

            Op.Divide -> {
                second!!.value = Random.nextInt(1, 49)
                first!!.value = second!!.value * Random.nextInt(1, 99 / second!!.value)
                answer!!.value = first!!.value / second!!.value
            }

            Op.Multiple -> {
                second!!.value = Random.nextInt( 99 / first!!.value)
                answer!!.value = first!!.value * second!!.value
            }
        }

        currentAnswerCorrect = true
        if (Random.nextDouble() > 0.5) {
            answer!!.value = Random.nextInt(100)
            currentAnswerCorrect = false
        }
        startTime!!.value = System.currentTimeMillis()
    }

    fun startTimeUpdating() {
        coroutineScope.launch {
            while(true) {
                currentTime!!.value = System.currentTimeMillis() - startTime!!.value
                delay(10)
            }
        }
    }

    fun coloredOutput() {
        coroutineScope.launch {
            while (colorSaturation < 1.0f) {
                if (lastUserAnswerIsCorrect) {
                    backColor!!.value = Color.White.copy(red = colorSaturation, blue = colorSaturation)
                } else {
                    backColor!!.value = Color.White.copy(green = colorSaturation, blue = colorSaturation)
                }
                colorSaturation += 0.01f
                delay(20)
            }
            colorSaturation = 0.85f
        }
    }

    fun checkUserAnswer(answer: Boolean) {
        responseTimes!!.value += currentTime!!.value
        if (currentAnswerCorrect == answer) {
            lastUserAnswerIsCorrect = true
            correct!!.value++
        } else {
            lastUserAnswerIsCorrect = false
            incorrect!!.value++
        }
        coloredOutput()
        generateEquation()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backColor!!.value),
        verticalArrangement = Arrangement.SpaceAround,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Итого решено примеров", fontSize = TextUnit(4f, TextUnitType.Em))
            Text(
                (correct!!.value + incorrect!!.value).toString(),
                fontSize = TextUnit(4f, TextUnitType.Em)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Правильно", fontSize = TextUnit(4f, TextUnitType.Em))
                    Text(correct!!.value.toString(), fontSize = TextUnit(4f, TextUnitType.Em))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Неправильно", fontSize = TextUnit(4f, TextUnitType.Em))
                    Text(incorrect!!.value.toString(), fontSize = TextUnit(4f, TextUnitType.Em))
                }
            }
        }
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(formatTime(currentTime!!.value), fontSize = TextUnit(7f, TextUnitType.Em))
            Row(horizontalArrangement = Arrangement.SpaceAround, modifier = Modifier.fillMaxWidth()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Минимум", fontSize = TextUnit(4f, TextUnitType.Em))
                    Text(
                        if (responseTimes!!.value.isEmpty()) "0.00"
                        else formatTime(responseTimes!!.value.minOrNull() ?: 0L),
                        fontSize = TextUnit(4f, TextUnitType.Em)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Максимум", fontSize = TextUnit(4f, TextUnitType.Em))
                    Text(
                        if (responseTimes!!.value.isEmpty()) "0.00"
                        else formatTime(responseTimes!!.value.maxOrNull() ?: 0L),
                        fontSize = TextUnit(4f, TextUnitType.Em)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Среднее", fontSize = TextUnit(4f, TextUnitType.Em))
                    Text(
                        if (responseTimes!!.value.isEmpty()) "0.00"
                        else formatTime(responseTimes!!.value.average()),
                        fontSize = TextUnit(4f, TextUnitType.Em)
                    )
                }
            }
        }
        if (incorrect!!.value == 0) {
            Text("100%", fontSize = TextUnit(10f, TextUnitType.Em))
        } else {
            Text("${(correct!!.value.toDouble() / (correct!!.value + incorrect!!.value) * 10000).roundToInt() / 100.0}%", fontSize = TextUnit(10f, TextUnitType.Em))
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Text(first!!.value.toString(), fontSize = TextUnit(7f, TextUnitType.Em))
            Text(operation!!.value.toString(), fontSize = TextUnit(7f, TextUnitType.Em))
            Text(second!!.value.toString(), fontSize = TextUnit(7f, TextUnitType.Em))
            Text("=", fontSize = TextUnit(7f, TextUnitType.Em))
            Text(answer!!.value.toString(), fontSize = TextUnit(7f, TextUnitType.Em))
        }
        Column {
            Row(horizontalArrangement = Arrangement.SpaceBetween) {
                Button(
                    { checkUserAnswer(true) },
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 10.dp),
                    enabled = isStarted!!.value
                ) {
                    Text("ВЕРНО")
                }
                Button(
                    { checkUserAnswer(false) },
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 10.dp),
                    enabled = isStarted!!.value
                    ) {
                    Text("НЕВЕРНО")
                }
            }
            Button({
                generateEquation()
                isStarted!!.value = true
                startTimeUpdating()
            }, modifier = Modifier.fillMaxWidth(), enabled = !isStarted!!.value) {
                Text("СТАРТ")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Greeting()
}

fun formatTime(millis: Long): String {
    return "%.2f".format(millis / 1000.0)
}

fun formatTime(millis: Double): String {
    return "%.2f".format(millis / 1000.0)
}