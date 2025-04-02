package com.yourfitness.coach.data.dao

import androidx.room.*
import com.yourfitness.coach.data.entity.LeaderboardEntity
import com.yourfitness.coach.data.entity.LeaderboardEntity.Companion.LEADERBOARD_TABLE

@Dao
interface LeaderboardDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateEntries(entries: List<LeaderboardEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun writeEntry(entry: LeaderboardEntity)

    @Query("SELECT * FROM $LEADERBOARD_TABLE")
    suspend fun readAllData(): List<LeaderboardEntity>?

    @Query("SELECT * FROM $LEADERBOARD_TABLE WHERE boardId = :id")
    suspend fun readByBoardId(id: Int): List<LeaderboardEntity>?

    @Query("SELECT * FROM $LEADERBOARD_TABLE WHERE boardId = :id AND rank > :minRank AND rank <= :maxRank")
    suspend fun readByBoardIdPaging(id: Int, minRank: Int, maxRank: Int): List<LeaderboardEntity>?

    @Query("SELECT * FROM $LEADERBOARD_TABLE WHERE boardId = :id LIMIT 1")
    suspend fun getAnyByBoardId(id: Int): LeaderboardEntity?

    @Query("DELETE FROM $LEADERBOARD_TABLE WHERE boardId = :id")
    suspend fun deleteByBoardId(id: Int)
}
