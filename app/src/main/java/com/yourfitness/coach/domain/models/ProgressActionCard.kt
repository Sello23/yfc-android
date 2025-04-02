package com.yourfitness.coach.domain.models

data class ProgressActionCard(
    val title: String,
    val backgroundImage: Int,
    val onClick: () -> Unit,
    val comingSoon: Boolean = false,
)
