package com.example.beetles.data

import androidx.room.Embedded
import androidx.room.Relation

data class GameRecordWithPlayer(
    @Embedded val gameRecord: GameRecord,
    @Embedded(prefix = "player_") val player: Player
)
