package com.example.zhuki.ui.screens

import android.widget.CalendarView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.zhuki.model.Player
import com.example.zhuki.utils.ZodiacUtils
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerFormScreen(
    player: Player,
    onPlayerUpdate: (Player) -> Unit,
    modifier: Modifier = Modifier
) {
    var showResults by remember { mutableStateOf(false) }
    var showCalendarDialog by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf(Calendar.getInstance()) }

    val courses = listOf("1 курс", "2 курс", "3 курс", "4 курс")
    var expanded by remember { mutableStateOf(false) }

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Форма игрока") },
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = player.fullName,
                onValueChange = { onPlayerUpdate(player.copy(fullName = it)) },
                label = { Text("ФИО") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("Пол:", modifier = Modifier.align(Alignment.Start))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Row(
                    modifier = Modifier
                        .selectable(
                            selected = player.gender == "Мужской",
                            onClick = { onPlayerUpdate(player.copy(gender = "Мужской")) }
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = player.gender == "Мужской",
                        onClick = { onPlayerUpdate(player.copy(gender = "Мужской")) }
                    )
                    Text("Мужской")
                }

                Row(
                    modifier = Modifier
                        .selectable(
                            selected = player.gender == "Женский",
                            onClick = { onPlayerUpdate(player.copy(gender = "Женский")) }
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = player.gender == "Женский",
                        onClick = { onPlayerUpdate(player.copy(gender = "Женский")) }
                    )
                    Text("Женский")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = player.course,
                    onValueChange = {},
                    label = { Text("Курс") },
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    courses.forEach { course ->
                        DropdownMenuItem(
                            text = { Text(course) },
                            onClick = {
                                onPlayerUpdate(player.copy(course = course))
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            DifficultySection(
                currentDifficulty = player.difficultyLevel,
                onDifficultySelected = { difficulty ->
                    onPlayerUpdate(player.copy(difficultyLevel = difficulty))
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (player.zodiacSign.isNotEmpty()) {
                Text(
                    text = "Выбранная дата: ${selectedDate.get(Calendar.DAY_OF_MONTH)}." +
                            "${selectedDate.get(Calendar.MONTH) + 1}." +
                            "${selectedDate.get(Calendar.YEAR)}",
                    modifier = Modifier.align(Alignment.Start)
                )
            }

            Button(
                onClick = { showCalendarDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Выбрать дату рождения")
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (player.zodiacSign.isNotEmpty()) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Знак зодиака: ${player.zodiacSign}",
                            fontSize = 18.sp,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = when (player.zodiacSign) {
                                    "Овен" -> "♈"
                                    "Телец" -> "♉"
                                    "Близнецы" -> "♊"
                                    "Рак" -> "♋"
                                    "Лев" -> "♌"
                                    "Дева" -> "♍"
                                    "Весы" -> "♎"
                                    "Скорпион" -> "♏"
                                    "Стрелец" -> "♐"
                                    "Козерог" -> "♑"
                                    "Водолей" -> "♒"
                                    "Рыбы" -> "♓"
                                    else -> "🌙"
                                },
                                fontSize = 40.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { showResults = true },
                modifier = Modifier.fillMaxWidth(),
                enabled = player.fullName.isNotEmpty() &&
                        player.gender.isNotEmpty() &&
                        player.course.isNotEmpty() &&
                        player.zodiacSign.isNotEmpty()
            ) {
                Text("Сохранить данные")
            }

            if (showResults) {
                Spacer(modifier = Modifier.height(24.dp))
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text("Данные игрока:", style = MaterialTheme.typography.headlineSmall)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("ФИО: ${player.fullName}")
                        Text("Пол: ${player.gender}")
                        Text("Курс: ${player.course}")
                        Text("Уровень сложности: ${getDifficultyText(player.difficultyLevel)}")
                        Text("Дата рождения: ${player.birthDate.get(Calendar.DAY_OF_MONTH)}." +
                                "${player.birthDate.get(Calendar.MONTH) + 1}." +
                                "${player.birthDate.get(Calendar.YEAR)}")
                        Text("Знак зодиака: ${player.zodiacSign}")
                    }
                }
            }

            if (showCalendarDialog) {
                AlertDialog(
                    onDismissRequest = { showCalendarDialog = false },
                    title = { Text("Выберите дату рождения") },
                    text = {
                        Column {
                            AndroidView(
                                factory = { context ->
                                    CalendarView(context).apply {
                                        val minCalendar = Calendar.getInstance()
                                        minCalendar.set(1900, 0, 1)
                                        minDate = minCalendar.timeInMillis

                                        val maxCalendar = Calendar.getInstance()
                                        maxCalendar.set(2100, 11, 31)
                                        maxDate = maxCalendar.timeInMillis

                                        date = selectedDate.timeInMillis

                                        setOnDateChangeListener { _, year, month, dayOfMonth ->
                                            selectedDate.set(year, month, dayOfMonth)
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(400.dp)
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    },
                    confirmButton = {
                        Button(onClick = {
                            val day = selectedDate.get(Calendar.DAY_OF_MONTH)
                            val month = selectedDate.get(Calendar.MONTH)

                            onPlayerUpdate(player.copy(
                                birthDate = selectedDate,
                                zodiacSign = ZodiacUtils.getZodiacSign(day, month)
                            ))
                            showCalendarDialog = false
                        }) {
                            Text("OK")
                        }
                    },
                    dismissButton = {
                        Button(onClick = { showCalendarDialog = false }) {
                            Text("Отмена")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun DifficultySection(
    currentDifficulty: Int,
    onDifficultySelected: (Int) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Уровень сложности: ${getDifficultyText(currentDifficulty)}",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onDifficultySelected(1) },
                    modifier = Modifier.weight(1f),
                    enabled = currentDifficulty != 1
                ) {
                    Text("Легко")
                }
                Button(
                    onClick = { onDifficultySelected(2) },
                    modifier = Modifier.weight(1f),
                    enabled = currentDifficulty != 2
                ) {
                    Text("Средне")
                }
                Button(
                    onClick = { onDifficultySelected(3) },
                    modifier = Modifier.weight(1f),
                    enabled = currentDifficulty != 3
                ) {
                    Text("Сложно")
                }
            }

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

private fun getDifficultyText(level: Int): String {
    return when (level) {
        1 -> "Легкий"
        2 -> "Средний"
        3 -> "Сложный"
        else -> "Неизвестно"
    }
}