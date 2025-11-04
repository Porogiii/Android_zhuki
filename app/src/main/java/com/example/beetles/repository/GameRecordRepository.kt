package com.example.beetles.repository

import com.example.beetles.data.GameRecord
import com.example.beetles.data.GameRecordDao
import kotlinx.coroutines.flow.Flow

class GameRecordRepository(private val gameRecordDao: GameRecordDao) {

    val allRecords: Flow<List<GameRecord>> = gameRecordDao.getAllGameRecords()

    val topRecords: Flow<List<GameRecord>> = gameRecordDao.getTopRecords()

    suspend fun insertGameRecord(gameRecord: GameRecord): Long {
        return gameRecordDao.insertGameRecord(gameRecord)
    }

    fun getPlayerRecords(playerId: Int): Flow<List<GameRecord>> {
        return gameRecordDao.getPlayerRecords(playerId)
    }

    suspend fun deletePlayerRecords(playerId: Int) {
        gameRecordDao.deletePlayerRecords(playerId)
    }
}
