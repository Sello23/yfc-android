package com.yourfitness.common.data.mappers

import com.yourfitness.common.data.entity.RegionSettingsEntity
import com.yourfitness.common.domain.date.today
import com.yourfitness.common.network.dto.SettingsRegion

fun SettingsRegion.toEntity(): RegionSettingsEntity {
    return RegionSettingsEntity(
        packages = packages,
        subscriptionCost = subscriptionCost,
        currency = currency,
        coinsToVoucherOwner = coinsToVoucherOwner,
        coinValue = coinValue,
        timeZoneOffset = timeZoneOffset ?: 0,
        lastUpdateTime = today().time
    )
}
