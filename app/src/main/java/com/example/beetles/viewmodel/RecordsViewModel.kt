package com.example.beetles.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.beetles.data.AppDatabase
import com.example.beetles.data.GameRecord
import com.example.beetles.data.Player
import com.example.beetles.repository.GameRecordRepository
import com.example.beetles.repository.PlayerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RecordWithPlayerInfo(
    val id: Int,
    val playerName: String,
    val score: Int,
    val difficulty: Int,
    val gameSpeed: Float,
    val playedAt: Long
)

class RecordsViewModel(application: Application) : AndroidViewModel(application) {
    private val gameRecordRepository: GameRecordRepository
    private val playerRepository: PlayerRepository

    private val _topRecords = MutableStateFlow<List<RecordWithPlayerInfo>>(emptyList())
    val topRecords: StateFlow<List<RecordWithPlayerInfo>> = _topRecords.asStateFlow()

    private val _topPlayers = MutableStateFlow<List<Player>>(emptyList())
    val topPlayers: StateFlow<List<Player>> = _topPlayers.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        gameRecordRepository = GameRecordRepository(database.gameRecordDao())
        playerRepository = PlayerRepository(database.playerDao())

        loadTopRecords()
        loadTopPlayers()
    }

    private fun loadTopRecords() {
        viewModelScope.launch {
            gameRecordRepository.topRecords.collect { records ->
                val recordsWithInfo = mutableListOf<RecordWithPlayerInfo>()

                records.forEach { record ->
                    val player = playerRepository.getPlayerById(record.playerId)
                    if (player != null) {
                        recordsWithInfo.add(
                            RecordWithPlayerInfo(
                                id = record.id,
                                playerName = player.fullName,
                                score = record.score,
                                difficulty = record.difficulty,
                                gameSpeed = record.gameSpeed,
                                playedAt = record.playedAt
                            )
                        )
                    }
                }

                _topRecords.value = recordsWithInfo
            }
        }
    }

    private fun loadTopPlayers() {
        viewModelScope.launch {
            playerRepository.topPlayers.collect { players ->
                _topPlayers.value = players
            }
        }
    }
}
