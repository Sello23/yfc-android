package com.yourfitness.coach.domain.models

data class BonusCredits(
    val amount: String,
    val color: String,
    val credits: String,
    val name: String,
    val maxVisits: String,
    val isFirst: Boolean,
)