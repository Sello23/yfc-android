package com.yourfitness.common.data.mappers

import com.yourfitness.common.data.entity.ProfileEntity
import com.yourfitness.common.network.dto.ProfileResponse

fun ProfileResponse.toEntity(): ProfileEntity {
    return ProfileEntity(
        id = id.orEmpty(),
        phoneNumber = info?.phoneNumber,
        email = info?.email,
        name = info?.name,
        surname = info?.surname,
        instagram = info?.instagram,
        mediaId = info?.mediaId,
        gender = info?.gender,
        birthday = info?.birthday,
        region = info?.region,
        voucher = info?.voucher,
        pushToken = info?.pushToken,
        createdAt = createdAt,
        corporationId = info?.corporationId,
        personalTrainer = info?.personalTrainer,
        complimentaryAccess = info?.complimentaryAccess,
        isBookable = info?.bookable,
        accessWorkoutPlans = info?.accessWorkoutPlans
    )
}
