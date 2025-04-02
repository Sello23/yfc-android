package com.yourfitness.community.data.mappers

import com.yourfitness.common.network.dto.ProgressLevel
import com.yourfitness.community.data.entity.FriendsEntity
import com.yourfitness.community.network.dto.FriendsDto
import java.util.UUID

fun FriendsDto.toEntity(
    levels: List<ProgressLevel>,
    isFriend: Boolean? = null,
    requestIn: Boolean? = null,
    requestOut: Boolean? = null,
): FriendsEntity {
    return FriendsEntity(
        id = id ?: UUID.randomUUID().toString(),
        mediaId = mediaId.orEmpty(),
        name = name.orEmpty(),
        progressLevelId = progressLevelId.orEmpty(),
        progressLevelMediaId = progressLevelMediaId.orEmpty(),
        progressLevelName = progressLevelName.orEmpty(),
        surname = surname.orEmpty(),
        isFriend = isFriend ?: this.isFriend ?: false,
        requestIn = requestIn ?: this.requestIn ?: false,
        requestOut = requestOut ?: this.requestOut ?: false,
        levelNumber = levels.indexOfFirst { it.id == progressLevelId } + 1
    )
}
