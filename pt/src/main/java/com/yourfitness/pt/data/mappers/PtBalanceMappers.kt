package com.yourfitness.pt.data.mappers

import com.yourfitness.pt.data.entity.PtBalanceEntity
import com.yourfitness.pt.network.dto.PtBalanceDto

fun PtBalanceDto.toEntity(): PtBalanceEntity? {
    if (ptId == null) return null
    return PtBalanceEntity(
        ptId = ptId,
        amount = amount ?: 0,
        profileId = profileId.orEmpty()
    )
}
