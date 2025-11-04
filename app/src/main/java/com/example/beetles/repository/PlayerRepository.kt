package com.example.beetles.repository

import com.example.beetles.data.Player
import com.example.beetles.data.PlayerDao
import kotlinx.coroutines.flow.Flow

class PlayerRepository(private val playerDao: PlayerDao) {

    val allPlayers: Flow<List<Player>> = playerDao.getAllPlayers()

    val topPlayers: Flow<List<Player>> = playerDao.getTopPlayers()

    suspend fun insertPlayer(player: Player): Long {
        return playerDao.insertPlayer(player)
    }

    suspend fun updatePlayer(player: Player) {
        playerDao.updatePlayer(player)
    }

    suspend fun deletePlayer(player: Player) {
        playerDao.deletePlayer(player)
    }

    suspend fun getPlayerById(playerId: Int): Player? {
        return playerDao.getPlayerById(playerId)
    }

    suspend fun updateGameResult(playerId: Int, score: Int) {
        val updated = playerDao.updateBestScoreIfHigher(playerId, score)
        if (updated == 0) {
            playerDao.incrementTotalGames(playerId)
        }
    }
}
