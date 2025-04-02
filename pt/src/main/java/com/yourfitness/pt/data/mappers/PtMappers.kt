package com.yourfitness.pt.data.mappers

import com.yourfitness.pt.data.entity.EducationEntity
import com.yourfitness.pt.data.entity.PersonalTrainerEntity
import com.yourfitness.pt.network.dto.EducationDto
import com.yourfitness.pt.network.dto.PersonalTrainerDto

fun PersonalTrainerDto.toEntity(): PersonalTrainerEntity {
    return PersonalTrainerEntity(
        id = id,
        amenities = amenities,
        description = description,
        name = name,
        surname = surname,
        educations = educations?.map { it.toEntity() },
        facilityIDs = facilityIDs,
        focusAreas = focusAreas,
        phoneNumber = phoneNumber,
        email = email,
        birthday = birthday,
        mediaId = mediaId,
        isBookable = isBookable,
        instagram = instagram
    )
}

fun EducationDto.toEntity(): EducationEntity {
    return EducationEntity(
        institute = institute,
        qualification = qualification,
        year = createdAt
    )
}
