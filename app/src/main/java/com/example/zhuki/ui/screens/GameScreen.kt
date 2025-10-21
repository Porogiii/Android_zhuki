package com.example.zhuki.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Alignment
import com.example.zhuki.R
import com.example.zhuki.model.GameSettings
import com.example.zhuki.model.Player
import com.example.zhuki.model.ScoreRecord
import com.example.zhuki.model.AppDatabase
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlin.random.Random

data class Bug(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val sizePx: Float = 144f
)

@Composable
fun GameScreen(
    modifier: Modifier = Modifier,
    settings: GameSettings,
    player: Player,
    onExit: () -> Unit = {}
) {
    val context = LocalContext.current
    val database = remember { AppDatabase.getInstance(context) }
    val coroutineScope = rememberCoroutineScope()

    var bugs by remember { mutableStateOf(listOf<Bug>()) }
    var score by remember { mutableStateOf(0) }
    var timeLeft by remember(settings.roundDuration) { mutableStateOf(settings.roundDuration) }
    var gameOver by remember { mutableStateOf(false) }
    var scoreSaved by remember { mutableStateOf(false) }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFe8f5e9))
    ) {
        val density = LocalDensity.current
        val areaWidthPx = with(density) { maxWidth.toPx() }
        val areaHeightPx = with(density) { maxHeight.toPx() }

        LaunchedEffect(areaWidthPx, areaHeightPx) {
            if (bugs.isEmpty() && areaWidthPx > 0f && areaHeightPx > 0f) {
                bugs = List(settings.maxCockroaches) { randomBug(areaWidthPx, areaHeightPx, settings.gameSpeed) }
            }
        }

        LaunchedEffect(settings) {
            timeLeft = timeLeft.coerceAtMost(settings.roundDuration)
            val diff = settings.maxCockroaches - bugs.size
            if (diff > 0 && areaWidthPx > 0f && areaHeightPx > 0f) {
                bugs = bugs + List(diff) { randomBug(areaWidthPx, areaHeightPx, settings.gameSpeed) }
            } else if (diff < 0) {
                bugs = bugs.take(settings.maxCockroaches)
            }
            bugs = bugs.map { it.copy(vx = randomVelocity(settings.gameSpeed), vy = randomVelocity(settings.gameSpeed)) }
        }

        LaunchedEffect(bugs, settings.gameSpeed, gameOver, areaWidthPx, areaHeightPx) {
            while (!gameOver) {
                val delayMs = maxOf(5L, (30L / settings.gameSpeed).toLong())
                delay(delayMs)
                bugs = bugs.map { bug ->
                    var newX = bug.x + bug.vx
                    var newY = bug.y + bug.vy
                    var newVx = bug.vx
                    var newVy = bug.vy
                    if (newX < 0 || newX > areaWidthPx - bug.sizePx) newVx = -bug.vx
                    if (newY < 0 || newY > areaHeightPx - bug.sizePx) newVy = -bug.vy
                    bug.copy(
                        x = newX.coerceIn(0f, areaWidthPx - bug.sizePx),
                        y = newY.coerceIn(0f, areaHeightPx - bug.sizePx),
                        vx = newVx,
                        vy = newVy
                    )
                }
            }
        }

        LaunchedEffect(timeLeft, gameOver, settings.roundDuration) {
            if (!gameOver) {
                if (timeLeft > 0) {
                    delay(1000L)
                    timeLeft--
                } else {
                    gameOver = true
                }
            }
        }

        LaunchedEffect(gameOver) {
            if (gameOver && !scoreSaved && player.fullName.isNotEmpty()) {
                val scoreRecord = ScoreRecord(
                    playerName = player.fullName,
                    score = score,
                    difficultyLevel = player.difficultyLevel,
                    course = player.course,
                    zodiacSign = player.zodiacSign
                )
                coroutineScope.launch {
                    try {
                        database.scoreRecordDao().insert(scoreRecord)
                        scoreSaved = true
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

        if (gameOver) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Игра окончена!", fontSize = 30.sp, color = Color.Black)
                Text("Ваш счёт: $score", fontSize = 24.sp, color = Color.DarkGray)

                if (player.fullName.isNotEmpty() && scoreSaved) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Результат сохранен!",
                        fontSize = 16.sp,
                        color = Color.Green
                    )
                } else if (player.fullName.isEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Заполните анкету для сохранения рекорда",
                        fontSize = 14.sp,
                        color = Color.Red
                    )
                }

                Spacer(Modifier.height(16.dp))
                Button(onClick = {
                    bugs = List(settings.maxCockroaches) { randomBug(areaWidthPx, areaHeightPx, settings.gameSpeed) }
                    score = 0
                    timeLeft = settings.roundDuration
                    gameOver = false
                    scoreSaved = false
                }) {
                    Text("Сыграть снова")
                }
                Spacer(Modifier.height(10.dp))
                Button(onClick = onExit) {
                    Text("Выход в меню")
                }
            }
        } else {
            val painter = painterResource(id = R.drawable.bug)
            val densityLocal = LocalDensity.current

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            val clickedBugIndex = bugs.indexOfFirst { bug ->
                                offset.x >= bug.x &&
                                        offset.x <= bug.x + bug.sizePx &&
                                        offset.y >= bug.y &&
                                        offset.y <= bug.y + bug.sizePx
                            }

                            if (clickedBugIndex != -1) {
                                score++
                                bugs = bugs.mapIndexed { i, b ->
                                    if (i == clickedBugIndex) randomBug(areaWidthPx, areaHeightPx, settings.gameSpeed)
                                    else b
                                }
                            } else {
                                score = maxOf(0, score - 1)
                            }
                        }
                    }
            ) {
                bugs.forEach { bug ->
                    val sizeDp = with(densityLocal) { bug.sizePx.toDp() }
                    Image(
                        painter = painter,
                        contentDescription = "Жук",
                        modifier = Modifier
                            .offset { IntOffset(bug.x.toInt(), bug.y.toInt()) }
                            .size(sizeDp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (player.fullName.isNotEmpty()) {
                    Text(
                        "Игрок: ${player.fullName}",
                        fontSize = 16.sp,
                        color = Color.Black
                    )
                    Spacer(Modifier.height(4.dp))
                }

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Очки: $score", fontSize = 20.sp, color = Color.Black)
                    Text("Время: $timeLeft", fontSize = 20.sp, color = Color.Black)
                }

                Text(
                    "Уровень: ${getDifficultyText(player.difficultyLevel)}",
                    fontSize = 14.sp,
                    color = Color.DarkGray
                )
            }
        }
    }
}

private fun randomBug(areaWidthPx: Float, areaHeightPx: Float, speed: Float): Bug {
    val size = 144f
    val x = Random.nextFloat() * (areaWidthPx - size)
    val y = Random.nextFloat() * (areaHeightPx - size)
    val vx = randomVelocity(speed)
    val vy = randomVelocity(speed)
    return Bug(x, y, vx, vy, size)
}

private fun randomVelocity(speed: Float): Float {
    return 5f * speed
}

private fun getDifficultyText(level: Int): String {
    return when (level) {
        1 -> "Легкий"
        2 -> "Средний"
        3 -> "Сложный"
        else -> "Неизвестно"
    }
}