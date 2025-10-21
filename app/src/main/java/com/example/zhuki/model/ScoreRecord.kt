package com.example.zhuki.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "score_records")
data class ScoreRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val playerName: String,
    val score: Int,
    val difficultyLevel: Int,
    val date: Long = System.currentTimeMillis(),
    val course: String = "",
    val zodiacSign: String = ""
)