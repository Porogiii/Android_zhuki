package com.example.zhuki.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.example.zhuki.model.ScoreRecord
import com.example.zhuki.model.AppDatabase
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordsScreen() {
    val context = LocalContext.current
    val database = remember { AppDatabase.getInstance(context) }
    val scoreRecordDao = database.scoreRecordDao()

    var scores by remember { mutableStateOf<List<ScoreRecord>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedTab by remember { mutableStateOf(0) }

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(selectedTab) {
        isLoading = true
        coroutineScope.launch {
            try {
                scores = when (selectedTab) {
                    0 -> scoreRecordDao.getTopScores()
                    1 -> scoreRecordDao.getRecentScores()
                    else -> emptyList()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                scores = emptyList()
            }
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Рекорды") })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // Табы
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Топ рекорды") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Последние игры") }
                )
            }

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (scores.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Нет записей о рекордах", fontSize = 18.sp)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(scores) { score ->
                        ScoreRecordItem(scoreRecord = score)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ScoreRecordItem(scoreRecord: ScoreRecord) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = scoreRecord.playerName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = "${scoreRecord.score} очков",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Уровень: ${getDifficultyText(scoreRecord.difficultyLevel)}")
                Text("Курс: ${scoreRecord.course}")
            }

            Spacer(modifier = Modifier.height(4.dp))

            if (scoreRecord.zodiacSign.isNotEmpty()) {
                Text("Знак зодиака: ${scoreRecord.zodiacSign}")
                Spacer(modifier = Modifier.height(4.dp))
            }

            Text(
                text = "Дата: ${formatDate(scoreRecord.date)}",
                fontSize = 12.sp,
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

private fun formatDate(timestamp: Long): String {
    val date = Date(timestamp)
    val formatter = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    return formatter.format(date)
}