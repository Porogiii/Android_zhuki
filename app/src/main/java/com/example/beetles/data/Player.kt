package com.example.beetles.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "players")
data class Player(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val fullName: String,
    val gender: String,
    val course: String,
    val difficulty: Int,
    val birthDate: String,
    val zodiacSign: String,
    val bestScore: Int = 0,
    val totalGames: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
