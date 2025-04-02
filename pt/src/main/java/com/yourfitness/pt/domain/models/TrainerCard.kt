package com.yourfitness.pt.domain.models

data class TrainerCard(
    val id: String,
    val name: String,
    val image: String,
    val focusAreas: List<String>,
    val facilitiesInfo: List<Pair<String?,Double>>,
    val availableSessions: Int,
    val isBookable: Boolean
)
