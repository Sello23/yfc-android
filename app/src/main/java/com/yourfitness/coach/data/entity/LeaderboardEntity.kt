package com.yourfitness.coach.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.yourfitness.coach.data.entity.LeaderboardEntity.Companion.LEADERBOARD_TABLE
import com.yourfitness.coach.network.dto.Entries

@Entity(tableName = LEADERBOARD_TABLE)
data class LeaderboardEntity(
    @PrimaryKey(autoGenerate = true)
    val _id: Int = 0,
    val corporationId: String,
    val profileId: String,
    val mediaId: String,
    val name: String,
    val surname: String,
    val rank: Int,
    val score: Int,
    val boardId: Int,
    val syncedAt: Long
) {
    companion object {
        const val LEADERBOARD_TABLE = "leaderboard_table"

        val empty
            get() = LeaderboardEntity(0, "", "", "", "", "", 0, 0, 0, 0L)
    }
}

val LeaderboardEntity.fullName get() = listOfNotNull(name, surname).joinToString(" ").trim()
