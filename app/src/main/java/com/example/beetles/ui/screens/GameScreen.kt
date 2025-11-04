package com.example.beetles.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.beetles.R
import com.example.beetles.viewmodel.GameViewModel

@Composable
fun GameScreen(
    viewModel: GameViewModel,
    onBackClick: () -> Unit
) {
    val density = LocalDensity.current
    val gameState by viewModel.gameState.collectAsState()
    var boxWidth by remember { mutableFloatStateOf(0f) }
    var boxHeight by remember { mutableFloatStateOf(0f) }
    var isInitialized by remember { mutableStateOf(false) }

    val beetlePainter = painterResource(id = R.drawable.cockroach)
    val tintColor = MaterialTheme.colorScheme.onSurface

    var frameCount by remember { mutableLongStateOf(0L) }

    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(16)
            frameCount++
        }
    }

    LaunchedEffect(boxWidth, boxHeight) {
        if (boxWidth > 0 && boxHeight > 0) {
            viewModel.updateScreenSize(boxWidth, boxHeight)
            if (!isInitialized) {
                viewModel.initGame(boxWidth, boxHeight)
                isInitialized = true
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned { coordinates ->
                    with(density) {
                        boxWidth = coordinates.size.width.toDp().value
                        boxHeight = coordinates.size.height.toDp().value
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val clickX = offset.x / density.density
                        val clickY = offset.y / density.density
                        var hitSomething = false

                        if (gameState.showBonus) {
                            val bonusLeft = gameState.bonusX
                            val bonusRight = gameState.bonusX + 60
                            val bonusTop = gameState.bonusY
                            val bonusBottom = gameState.bonusY + 60

                            if (clickX in bonusLeft..bonusRight &&
                                clickY in bonusTop..bonusBottom) {
                                viewModel.onBonusClicked()
                                hitSomething = true
                            }
                        }

                        if (!hitSomething) {
                            viewModel.beetles.forEach { beetle ->
                                if (beetle.isAlive) {
                                    val beetleLeft = beetle.x
                                    val beetleRight = beetle.x + 80
                                    val beetleTop = beetle.y
                                    val beetleBottom = beetle.y + 80

                                    if (clickX in beetleLeft..beetleRight &&
                                        clickY in beetleTop..beetleBottom) {
                                        viewModel.onBeetleClicked(beetle.id)
                                        hitSomething = true
                                    }
                                }
                            }
                        }

                        if (!hitSomething) {
                            viewModel.onMissClick()
                        }
                    }
                }
        ) {
            frameCount

            viewModel.beetles.forEach { beetle ->
                if (beetle.isAlive) {
                    val sizePx = 80.dp.toPx()
                    val xPx = beetle.x.dp.toPx()
                    val yPx = beetle.y.dp.toPx()

                    translate(left = xPx, top = yPx) {
                        rotate(
                            degrees = beetle.rotation,
                            pivot = Offset(sizePx / 2, sizePx / 2)
                        ) {
                            with(beetlePainter) {
                                draw(
                                    size = Size(sizePx, sizePx),
                                    colorFilter = ColorFilter.tint(tintColor)
                                )
                            }
                        }
                    }
                }
            }

            if (gameState.showBonus) {
                val bonusSizePx = 60.dp.toPx()
                val bonusXPx = gameState.bonusX.dp.toPx()
                val bonusYPx = gameState.bonusY.dp.toPx()

                drawCircle(
                    color = Color(0xFFFFD700),
                    radius = bonusSizePx / 2,
                    center = Offset(bonusXPx + bonusSizePx / 2, bonusYPx + bonusSizePx / 2)
                )
                drawCircle(
                    color = Color(0xFFFFA500),
                    radius = bonusSizePx / 2 - 3.dp.toPx(),
                    center = Offset(bonusXPx + bonusSizePx / 2, bonusYPx + bonusSizePx / 2)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Очки: ${gameState.score}",
                    style = MaterialTheme.typography.titleLarge,
                    color = if (gameState.score >= 0) Color.Green else Color.Red
                )
                Text(
                    text = "Время: ${gameState.timeLeft}с",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            if (gameState.isBonusActive) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFFD700).copy(alpha = 0.3f)
                        )
                    ) {
                        Text(
                            text = "ГРАВИТАЦИЯ АКТИВНА: ${gameState.bonusTimeLeft}с",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF6B00)
                        )
                    }
                }
            }
        }

        Button(
            onClick = onBackClick,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            Text("Назад в меню")
        }

        if (gameState.isGameOver) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.8f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.padding(32.dp),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Игра окончена!",
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Text(
                            text = "Ваш результат: ${gameState.score} очков",
                            style = MaterialTheme.typography.titleLarge,
                            color = if (gameState.score >= 0) Color.Green else Color.Red
                        )
                        Button(onClick = onBackClick) {
                            Text("Вернуться в меню")
                        }
                    }
                }
            }
        }

        if (gameState.countdown > 0 && !gameState.isGameStarted) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${gameState.countdown}",
                    fontSize = 120.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
