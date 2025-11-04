package com.example.beetles.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GameRecordDao {

    @Insert
    suspend fun insertGameRecord(gameRecord: GameRecord): Long

    @Query("SELECT * FROM game_records ORDER BY playedAt DESC")
    fun getAllGameRecords(): Flow<List<GameRecord>>

    @Query("SELECT * FROM game_records WHERE playerId = :playerId ORDER BY playedAt DESC")
    fun getPlayerRecords(playerId: Int): Flow<List<GameRecord>>

    @Query("SELECT * FROM game_records ORDER BY score DESC LIMIT 10")
    fun getTopRecords(): Flow<List<GameRecord>>

    @Query("DELETE FROM game_records WHERE playerId = :playerId")
    suspend fun deletePlayerRecords(playerId: Int)
}
