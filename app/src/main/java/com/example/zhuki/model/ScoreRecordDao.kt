package com.example.zhuki.model

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ScoreRecordDao {
    @Insert
    suspend fun insert(scoreRecord: ScoreRecord)

    @Query("SELECT * FROM score_records ORDER BY score DESC, difficultyLevel DESC LIMIT 50")
    suspend fun getTopScores(): List<ScoreRecord>

    @Query("SELECT * FROM score_records WHERE playerName = :playerName ORDER BY date DESC")
    suspend fun getPlayerScores(playerName: String): List<ScoreRecord>

    @Query("SELECT * FROM score_records ORDER BY date DESC LIMIT 10")
    suspend fun getRecentScores(): List<ScoreRecord>
}