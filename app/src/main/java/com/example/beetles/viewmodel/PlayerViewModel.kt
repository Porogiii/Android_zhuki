package com.example.beetles.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.beetles.data.AppDatabase
import com.example.beetles.data.Player
import com.example.beetles.repository.PlayerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PlayerViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: PlayerRepository

    val allPlayers: StateFlow<List<Player>>
    val topPlayers: StateFlow<List<Player>>

    private val _selectedPlayer = MutableStateFlow<Player?>(null)
    val selectedPlayer: StateFlow<Player?> = _selectedPlayer.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        val playerDao = database.playerDao()
        repository = PlayerRepository(playerDao)

        val playersFlow = MutableStateFlow<List<Player>>(emptyList())
        val topPlayersFlow = MutableStateFlow<List<Player>>(emptyList())

        allPlayers = playersFlow.asStateFlow()
        topPlayers = topPlayersFlow.asStateFlow()

        viewModelScope.launch {
            repository.allPlayers.collect { players ->
                playersFlow.value = players
            }
        }

        viewModelScope.launch {
            repository.topPlayers.collect { players ->
                topPlayersFlow.value = players
            }
        }
    }

    fun insertPlayer(
        fullName: String,
        gender: String,
        course: String,
        difficulty: Int,
        birthDate: String,
        zodiacSign: String,
        onSuccess: (Long) -> Unit
    ) {
        viewModelScope.launch {
            val player = Player(
                fullName = fullName,
                gender = gender,
                course = course,
                difficulty = difficulty,
                birthDate = birthDate,
                zodiacSign = zodiacSign
            )
            val playerId = repository.insertPlayer(player)
            onSuccess(playerId)
        }
    }

    fun selectPlayer(player: Player) {
        _selectedPlayer.value = player
    }

    fun clearSelectedPlayer() {
        _selectedPlayer.value = null
    }

    fun updateGameResult(playerId: Int, score: Int) {
        viewModelScope.launch {
            repository.updateGameResult(playerId, score)
        }
    }

    fun deletePlayer(player: Player) {
        viewModelScope.launch {
            repository.deletePlayer(player)
        }
    }

    fun refreshSelectedPlayer() {
        val currentPlayer = _selectedPlayer.value
        if (currentPlayer != null) {
            viewModelScope.launch {
                val updated = repository.getPlayerById(currentPlayer.id)
                if (updated != null) {
                    _selectedPlayer.value = updated
                }
            }
        }
    }
}
