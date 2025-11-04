package com.example.beetles.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.beetles.utils.calculateZodiacSign
import com.example.beetles.utils.getZodiacImageResource
import com.example.beetles.viewmodel.PlayerViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    playerViewModel: PlayerViewModel
) {
    val allPlayers by playerViewModel.allPlayers.collectAsState()
    val selectedPlayer by playerViewModel.selectedPlayer.collectAsState()

    var showNewPlayerForm by remember { mutableStateOf(true) }
    var showPlayerSelector by remember { mutableStateOf(false) }

    var fullName by remember { mutableStateOf("") }
    var selectedGender by remember { mutableStateOf("") }
    var selectedCourse by remember { mutableStateOf("") }
    var difficulty by remember { mutableFloatStateOf(1f) }
    var selectedDate by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var dateSelected by remember { mutableStateOf(false) }
    var showZodiacDialog by remember { mutableStateOf(false) }
    var calculatedZodiac by remember { mutableStateOf("") }

    val dateFormatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    val calendar = Calendar.getInstance().apply { timeInMillis = selectedDate }
    val isFormValid = fullName.isNotBlank() &&
            selectedGender.isNotBlank() &&
            selectedCourse.isNotBlank() &&
            dateSelected

    LaunchedEffect(allPlayers) {
        showNewPlayerForm = allPlayers.isEmpty()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Регистрация игрока",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineMedium
        )

        if (allPlayers.isNotEmpty() && !showNewPlayerForm) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Выберите игрока",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = { showPlayerSelector = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = selectedPlayer?.fullName ?: "Выберите из списка",
                            modifier = Modifier.weight(1f)
                        )
                        Icon(Icons.Default.ArrowDropDown, null)
                    }

                    if (selectedPlayer != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Курс: ${selectedPlayer!!.course}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Лучший результат: ${selectedPlayer!!.bestScore}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Игр сыграно: ${selectedPlayer!!.totalGames}",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { navController.navigate("game") },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Начать игру")
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        var showDeleteDialog by remember { mutableStateOf(false) }

                        OutlinedButton(
                            onClick = { showDeleteDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Удалить игрока")
                        }

                        if (showDeleteDialog) {
                            AlertDialog(
                                onDismissRequest = { showDeleteDialog = false },
                                title = { Text("Удалить игрока?") },
                                text = {
                                    Text("Вы уверены, что хотите удалить игрока ${selectedPlayer!!.fullName}? Все его рекорды будут удалены.")
                                },
                                confirmButton = {
                                    Button(
                                        onClick = {
                                            playerViewModel.deletePlayer(selectedPlayer!!)
                                            playerViewModel.clearSelectedPlayer()
                                            showDeleteDialog = false
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.error
                                        )
                                    ) {
                                        Text("Удалить")
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showDeleteDialog = false }) {
                                        Text("Отмена")
                                    }
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = { showNewPlayerForm = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Зарегистрировать нового игрока")
            }
        }

        if (showNewPlayerForm) {
            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("ФИО") },
                placeholder = { Text("Введите ваше ФИО") },
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = "Пол:",
                style = MaterialTheme.typography.titleMedium
            )

            val genderOptions = listOf("Мужской", "Женский")
            Column {
                genderOptions.forEach { gender ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = (gender == selectedGender),
                                onClick = { selectedGender = gender }
                            )
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (gender == selectedGender),
                            onClick = { selectedGender = gender }
                        )
                        Text(
                            text = gender,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }

            Text(
                text = "Курс:",
                style = MaterialTheme.typography.titleMedium
            )

            var expandedCourse by remember { mutableStateOf(false) }
            val courses = listOf("1 курс", "2 курс", "3 курс", "4 курс")

            ExposedDropdownMenuBox(
                expanded = expandedCourse,
                onExpandedChange = { expandedCourse = !expandedCourse }
            ) {
                OutlinedTextField(
                    value = selectedCourse.ifEmpty { "Не выбран" },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Выберите курс") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCourse) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expandedCourse,
                    onDismissRequest = { expandedCourse = false }
                ) {
                    courses.forEach { course ->
                        DropdownMenuItem(
                            text = { Text(course) },
                            onClick = {
                                selectedCourse = course
                                expandedCourse = false
                            }
                        )
                    }
                }
            }

            Text(
                text = "Уровень сложности: ${difficulty.toInt()}",
                style = MaterialTheme.typography.titleMedium
            )

            Slider(
                value = difficulty,
                onValueChange = { difficulty = it },
                valueRange = 1f..3f,
                steps = 1,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = "Дата рождения:",
                style = MaterialTheme.typography.titleMedium
            )

            Button(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    if (dateSelected) {
                        "Выбрать дату: ${dateFormatter.format(Date(selectedDate))}"
                    } else {
                        "Выбрать дату рождения"
                    }
                )
            }

            if (showDatePicker) {
                val datePickerState = rememberDatePickerState(
                    initialSelectedDateMillis = selectedDate
                )
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            datePickerState.selectedDateMillis?.let {
                                selectedDate = it
                                dateSelected = true
                            }
                            showDatePicker = false
                        }) {
                            Text("OK")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePicker = false }) {
                            Text("Отмена")
                        }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
            }

            Button(
                onClick = {
                    val day = calendar.get(Calendar.DAY_OF_MONTH)
                    val month = calendar.get(Calendar.MONTH) + 1
                    calculatedZodiac = calculateZodiacSign(day, month)

                    playerViewModel.insertPlayer(
                        fullName = fullName,
                        gender = selectedGender,
                        course = selectedCourse,
                        difficulty = difficulty.toInt(),
                        birthDate = dateFormatter.format(Date(selectedDate)),
                        zodiacSign = calculatedZodiac
                    ) { playerId ->
                        showZodiacDialog = true
                    }
                },
                enabled = isFormValid,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Зарегистрировать")
            }

            if (allPlayers.isNotEmpty()) {
                TextButton(
                    onClick = { showNewPlayerForm = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Отмена")
                }
            }
        }

        if (showZodiacDialog) {
            AlertDialog(
                onDismissRequest = { showZodiacDialog = false },
                title = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Ваш знак зодиака:",
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = calculatedZodiac,
                            style = MaterialTheme.typography.headlineLarge,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Image(
                            painter = painterResource(id = getZodiacImageResource(calculatedZodiac)),
                            contentDescription = "Знак зодиака $calculatedZodiac",
                            modifier = Modifier.size(128.dp),
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
                        )

                        Button(
                            onClick = {
                                showZodiacDialog = false
                                showNewPlayerForm = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Закрыть")
                        }
                    }
                },
                confirmButton = {}
            )
        }

        if (showPlayerSelector && allPlayers.isNotEmpty()) {
            AlertDialog(
                onDismissRequest = { showPlayerSelector = false },
                title = { Text("Выберите игрока") },
                text = {
                    Column {
                        allPlayers.forEach { player ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable {
                                        playerViewModel.selectPlayer(player)
                                        showPlayerSelector = false
                                    }
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = player.fullName,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        text = "Лучший результат: ${player.bestScore}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showPlayerSelector = false }) {
                        Text("Отмена")
                    }
                }
            )
        }
    }
}
