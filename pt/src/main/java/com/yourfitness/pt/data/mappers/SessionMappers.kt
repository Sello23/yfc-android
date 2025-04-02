package com.yourfitness.pt.data.mappers

import com.yourfitness.common.domain.date.toMilliseconds
import com.yourfitness.pt.data.entity.ProfileInfo
import com.yourfitness.pt.data.entity.SessionEntity
import com.yourfitness.pt.network.dto.ProfileInfoDto
import com.yourfitness.pt.network.dto.SessionDto
import java.util.UUID

fun SessionDto.toEntity(customStatus: String? = null): SessionEntity {
    return SessionEntity(
        id = id ?: UUID.randomUUID().toString(),
        facilityId = facilityId.orEmpty(),
        personalTrainerId = personalTrainerId.orEmpty(),
        profileId = profileId.orEmpty(),
        from = from?.toMilliseconds() ?: 0L,
        to = to?.toMilliseconds() ?: 0L,
        status = customStatus ?: status.orEmpty(),
        chainId = chainId ?: UUID.randomUUID().toString(),
        deleted = deleted ?: false,
        repeats = repeats ?: 1,
        profileInfo = profileInfo?.toEntity() ?: ProfileInfo.empty()
    )
}

fun ProfileInfoDto.toEntity(): ProfileInfo {
    return ProfileInfo(
        name = name.orEmpty(),
        surname = surname.orEmpty(),
        birthday = birthday ?: 0L,
        phoneNumber = phoneNumber.orEmpty(),
        email = email.orEmpty(),
        mediaId = mediaId.orEmpty()
    )
}
