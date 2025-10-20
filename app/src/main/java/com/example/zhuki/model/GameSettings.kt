package com.example.zhuki.model

data class GameSettings(
    val gameSpeed: Float = 1.0f,
    val maxCockroaches: Int = 5,
    val bonusInterval: Int = 30,
    val roundDuration: Int = 60
) {
    companion object {
        fun getByDifficulty(difficulty: Int): GameSettings {
            return when (difficulty) {
                1 -> GameSettings( // Легкий
                    gameSpeed = 1f,
                    maxCockroaches = 10,
                    bonusInterval = 15,
                    roundDuration = 120
                )
                2 -> GameSettings( // Средний
                    gameSpeed = 1.5f,
                    maxCockroaches = 20,
                    bonusInterval = 25,
                    roundDuration = 90
                )
                3 -> GameSettings( // Сложный
                    gameSpeed = 2f,
                    maxCockroaches = 35,
                    bonusInterval = 35,
                    roundDuration = 60
                )
                else -> GameSettings()
            }
        }
    }
}