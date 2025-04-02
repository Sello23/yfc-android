package com.yourfitness.coach.domain.models

import com.yourfitness.coach.data.entity.FacilityEntity

data class VisitedClass(
    val studioIcon: String,
    val studioCurrentLevel: String,
    val studioName: String,
    val currentVisits: String,
    val visitsToNextLevel: String,
    val studioNextLevel: String,
    val creditsLevelUpBonus: String,
    val availableBonusCredits: String,
    val progressbarPosition: Int,
    val facilityId: String,
    val color: String,
    val nextLevelColor: String,
    val ringProgress: String,
    val facilityEntity: FacilityEntity,
)