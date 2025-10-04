package com.example.zhuki.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.example.zhuki.model.GameSettings
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    gameSettings: GameSettings,
    onSettingsUpdate: (GameSettings) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Настройки игры",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                },
                scrollBehavior = scrollBehavior
            )
        },
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Скорость игры
            SettingsCard(title = "Скорость игры: ${"%.1f".format(gameSettings.gameSpeed)}x") {
                Slider(
                    value = gameSettings.gameSpeed,
                    onValueChange = { onSettingsUpdate(gameSettings.copy(gameSpeed = it)) },
                    valueRange = 0.5f..3.0f,
                    steps = 4,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Максимальное количество тараканов (5-50 с шагом 5)
            SettingsCard(title = "Макс. тараканов: ${gameSettings.maxCockroaches}") {
                val cockroachValues = listOf(5, 10, 15, 20, 25, 30, 35, 40, 45, 50)
                val cockroachPosition = cockroachValues.indexOf(gameSettings.maxCockroaches).coerceAtLeast(0).toFloat()

                Slider(
                    value = cockroachPosition,
                    onValueChange = { position ->
                        val index = position.roundToInt().coerceIn(0, cockroachValues.size - 1)
                        onSettingsUpdate(gameSettings.copy(maxCockroaches = cockroachValues[index]))
                    },
                    valueRange = 0f..(cockroachValues.size - 1).toFloat(),
                    steps = cockroachValues.size - 2,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "Диапазон: 5 - 50 (шаг: 5)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Интервал появления бонусов (5-40 с шагом 5)
            SettingsCard(title = "Интервал бонусов: ${gameSettings.bonusInterval} сек") {
                val bonusValues = listOf(5, 10, 15, 20, 25, 30, 35, 40)
                val bonusPosition = bonusValues.indexOf(gameSettings.bonusInterval).coerceAtLeast(0).toFloat()

                Slider(
                    value = bonusPosition,
                    onValueChange = { position ->
                        val index = position.roundToInt().coerceIn(0, bonusValues.size - 1)
                        onSettingsUpdate(gameSettings.copy(bonusInterval = bonusValues[index]))
                    },
                    valueRange = 0f..(bonusValues.size - 1).toFloat(),
                    steps = bonusValues.size - 2,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "Диапазон: 5 - 40 сек (шаг: 5)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Длительность раунда (30-240 с шагом 30)
            SettingsCard(title = "Длительность раунда: ${gameSettings.roundDuration} сек") {
                val durationValues = listOf(30, 60, 90, 120, 150, 180, 210, 240)
                val durationPosition = durationValues.indexOf(gameSettings.roundDuration).coerceAtLeast(0).toFloat()

                Slider(
                    value = durationPosition,
                    onValueChange = { position ->
                        val index = position.roundToInt().coerceIn(0, durationValues.size - 1)
                        onSettingsUpdate(gameSettings.copy(roundDuration = durationValues[index]))
                    },
                    valueRange = 0f..(durationValues.size - 1).toFloat(),
                    steps = durationValues.size - 2,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "Диапазон: 30 - 240 сек (шаг: 30)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Кнопки предустановок сложности
            DifficultyPresets(
                onDifficultySelected = { difficulty ->
                    onSettingsUpdate(GameSettings.getByDifficulty(difficulty))
                }
            )
        }
    }
}

@Composable
fun SettingsCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
fun DifficultyPresets(onDifficultySelected: (Int) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Предустановки сложности", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onDifficultySelected(1) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Легко")
                }
                Button(
                    onClick = { onDifficultySelected(2) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Средне")
                }
                Button(
                    onClick = { onDifficultySelected(3) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Сложно")
                }
            }

            // Описание предустановок
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "• Легко: 10 жуков, бонусы каждые 15 сек, 2 мин раунд\n" +
                        "• Средне: 20 жуков, бонусы каждые 25 сек, 1.5 мин раунд\n" +
                        "• Сложно: 35 жуков, бонусы каждые 35 сек, 1 мин раунд",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}