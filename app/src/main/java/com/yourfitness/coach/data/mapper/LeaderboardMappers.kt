package com.yourfitness.coach.data.mapper

import com.yourfitness.coach.data.entity.LeaderboardEntity
import com.yourfitness.coach.network.dto.Entries
import java.util.UUID

fun Entries.toEntity(boardId: Int, syncedAt: Long): LeaderboardEntity {
    return LeaderboardEntity(
        corporationId = corporationID ?: UUID.randomUUID().toString(),
        profileId = profileId ?: UUID.randomUUID().toString(),
        mediaId = mediaId ?: UUID.randomUUID().toString(),
        name = name.orEmpty(),
        surname = surname.orEmpty(),
        rank = rank ?: 0,
        score = score ?: 0,
        boardId = boardId,
        syncedAt = syncedAt
    )
}
