package com.example.zhuki.model

import java.util.Calendar

data class Player(
    val fullName: String = "",
    val gender: String = "",
    val course: String = "",
    val difficultyLevel: Int = 1,
    val birthDate: Calendar = Calendar.getInstance(),
    val zodiacSign: String = ""
)