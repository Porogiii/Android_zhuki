package com.example.beetles.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayer(player: Player): Long

    @Update
    suspend fun updatePlayer(player: Player)

    @Delete
    suspend fun deletePlayer(player: Player)

    @Query("SELECT * FROM players ORDER BY id DESC")
    fun getAllPlayers(): Flow<List<Player>>

    @Query("SELECT * FROM players WHERE id = :playerId")
    suspend fun getPlayerById(playerId: Int): Player?

    @Query("SELECT * FROM players ORDER BY bestScore DESC LIMIT 10")
    fun getTopPlayers(): Flow<List<Player>>

    @Query("UPDATE players SET bestScore = :score, totalGames = totalGames + 1 WHERE id = :playerId AND bestScore < :score")
    suspend fun updateBestScoreIfHigher(playerId: Int, score: Int): Int

    @Query("UPDATE players SET totalGames = totalGames + 1 WHERE id = :playerId")
    suspend fun incrementTotalGames(playerId: Int)
}
