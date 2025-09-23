package com.example.zhuki

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
import androidx.compose.material3.Slider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.zhuki.model.Player
import com.example.zhuki.utils.ZodiacUtils
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerFormScreen() {
    var player by remember { mutableStateOf(Player()) }
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
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ФИО
            OutlinedTextField(
                value = player.fullName,
                onValueChange = { player = player.copy(fullName = it) },
                label = { Text("ФИО") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Пол
            Text("Пол:", modifier = Modifier.align(Alignment.Start))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Row(
                    modifier = Modifier
                        .selectable(
                            selected = player.gender == "Мужской",
                            onClick = { player = player.copy(gender = "Мужской") }
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = player.gender == "Мужской",
                        onClick = { player = player.copy(gender = "Мужской") }
                    )
                    Text("Мужской")
                }

                Row(
                    modifier = Modifier
                        .selectable(
                            selected = player.gender == "Женский",
                            onClick = { player = player.copy(gender = "Женский") }
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = player.gender == "Женский",
                        onClick = { player = player.copy(gender = "Женский") }
                    )
                    Text("Женский")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Курс (выпадающий список)
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
                                player = player.copy(course = course)
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Уровень сложности
            Text("Уровень сложности: ${player.difficultyLevel}", modifier = Modifier.align(Alignment.Start))
            Slider(
                value = player.difficultyLevel.toFloat(),
                onValueChange = { player = player.copy(difficultyLevel = it.toInt()) },
                valueRange = 1f..3f,
                steps = 1,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Текущая выбранная дата
            if (player.zodiacSign.isNotEmpty()) {
                Text(
                    text = "Выбранная дата: ${selectedDate.get(Calendar.DAY_OF_MONTH)}." +
                            "${selectedDate.get(Calendar.MONTH) + 1}." +
                            "${selectedDate.get(Calendar.YEAR)}",
                    modifier = Modifier.align(Alignment.Start)
                )
            }

            // Кнопка выбора даты
            Button(
                onClick = { showCalendarDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Выбрать дату рождения")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Знак зодиака
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

                        // Использование Box для точного центрирования
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

            // Кнопка отправки
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

            // Отображение результатов
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
                        Text("Уровень сложности: ${player.difficultyLevel}")
                        Text("Дата рождения: ${player.birthDate.get(Calendar.DAY_OF_MONTH)}." +
                                "${player.birthDate.get(Calendar.MONTH) + 1}." +
                                "${player.birthDate.get(Calendar.YEAR)}")
                        Text("Знак зодиака: ${player.zodiacSign}")
                    }
                }
            }

            // Диалог выбора даты
            if (showCalendarDialog) {
                AlertDialog(
                    onDismissRequest = { showCalendarDialog = false },
                    title = { Text("Выберите дату рождения") },
                    text = {
                        Column {
                            // CalendarView с возможностью выбора года
                            AndroidView(
                                factory = { context ->
                                    CalendarView(context).apply {
                                        // Устанавливаем минимальную и максимальную даты
                                        val minCalendar = Calendar.getInstance()
                                        minCalendar.set(1900, 0, 1) // 1 января 1900
                                        minDate = minCalendar.timeInMillis

                                        val maxCalendar = Calendar.getInstance()
                                        maxCalendar.set(2100, 11, 31) // 31 декабря 2100
                                        maxDate = maxCalendar.timeInMillis

                                        // Настраиваем отображение (некоторые настройки могут не работать на всех версиях Android)
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

                            player = player.copy(
                                birthDate = selectedDate,
                                zodiacSign = ZodiacUtils.getZodiacSign(day, month)
                            )
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