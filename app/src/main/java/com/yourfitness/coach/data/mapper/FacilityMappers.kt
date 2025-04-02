package com.yourfitness.coach.data.mapper

import com.yourfitness.coach.data.entity.FacilityEntity
import com.yourfitness.coach.network.dto.CoordinatesDto
import com.yourfitness.coach.network.dto.FacilityDto

fun FacilityDto.toEntity(): FacilityEntity {
    return FacilityEntity(
        id = id,
        name = name,
        latitude = coordinates?.latitude,
        longitude = coordinates?.longitude,
        description = description,
        icon = icon,
        region = region,
        city = city,
        address = address,
        emailAddress = emailAddress,
        contactDetails = contactDetails,
        group = group,
        classification = classification,
        femaleOnly = femaleOnly,
        amenities = amenities,
        types = types,
        gallery = gallery,
//        customClasses = customClasses?.map { it.toClassEntity() },
//        licensedClasses = licensedClasses?.map { it.toClassEntity() },
        schedule = schedule,
        personalTrainersIds = personalTrainersIds,
        accessLimitationMessage = accessLimitationMessage,
        displayAccessLimitationMessage = displayAccessLimitationMessage ?: false,
        isYfcGym = isYfcGym ?: false
    )
}


fun FacilityEntity.toDto(): FacilityDto {
    return FacilityDto(
        id = id,
        name = name,
        coordinates = CoordinatesDto(latitude, longitude),
        description = description,
        icon = icon,
        region = region,
        city = city,
        address = address,
        emailAddress = emailAddress,
        contactDetails = contactDetails,
        group = group,
        classification = classification,
        femaleOnly = femaleOnly,
        amenities = amenities,
        types = types,
        gallery = gallery,
//        customClasses = customClasses?.map { it.toClassEntity() },
//        licensedClasses = licensedClasses?.map { it.toClassEntity() },
        schedule = schedule,
        personalTrainersIds = personalTrainersIds,
        accessLimitationMessage = accessLimitationMessage,
        displayAccessLimitationMessage = displayAccessLimitationMessage ?: false,
        isYfcGym = isYfcGym ?: false
    )
}
