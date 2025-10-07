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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Alignment
import com.example.zhuki.R
import com.example.zhuki.model.GameSettings
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
    onExit: () -> Unit = {}
) {
    var bugs by remember { mutableStateOf(listOf<Bug>()) }
    var score by remember { mutableStateOf(0) }
    var timeLeft by remember(settings.roundDuration) { mutableStateOf(settings.roundDuration) }
    var gameOver by remember { mutableStateOf(false) }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFe8f5e9))
    ) {
        val density = LocalDensity.current
        val areaWidthPx = with(density) { maxWidth.toPx() }
        val areaHeightPx = with(density) { maxHeight.toPx() }

        // Инициализация жуков
        LaunchedEffect(areaWidthPx, areaHeightPx) {
            if (bugs.isEmpty() && areaWidthPx > 0f && areaHeightPx > 0f) {
                bugs = List(settings.maxCockroaches) { randomBug(areaWidthPx, areaHeightPx, settings.gameSpeed) }
            }
        }

        // Обновление настроек
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

        // Движение жуков
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

        // Таймер
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

        if (gameOver) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Игра окончена!", fontSize = 30.sp, color = Color.Black)
                Text("Ваш счёт: $score", fontSize = 24.sp, color = Color.DarkGray)
                Spacer(Modifier.height(10.dp))
                Button(onClick = {
                    bugs = List(settings.maxCockroaches) { randomBug(areaWidthPx, areaHeightPx, settings.gameSpeed) }
                    score = 0
                    timeLeft = settings.roundDuration
                    gameOver = false
                }) {
                    Text("Сыграть снова")
                }
                Spacer(Modifier.height(10.dp))
//                Button(onClick = onExit) {
//                    Text("Выход в меню")
//                }
            }
        } else {
            val painter = painterResource(id = R.drawable.bug)
            val densityLocal = LocalDensity.current

            // Один pointerInput на весь экран
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
                                // Попал по жуку
                                score++
                                bugs = bugs.mapIndexed { i, b ->
                                    if (i == clickedBugIndex) randomBug(areaWidthPx, areaHeightPx, settings.gameSpeed)
                                    else b
                                }
                            } else {
                                // Промах по пустому месту
                                score = maxOf(0, score - 1)
                            }
                        }
                    }
            ) {
                // Отрисовка жуков поверх
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

            // UI сверху
            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Очки: $score", fontSize = 20.sp, color = Color.Black)
                Spacer(Modifier.width(40.dp))
                Text("Время: $timeLeft", fontSize = 20.sp, color = Color.Black)
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