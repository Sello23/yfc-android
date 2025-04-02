package com.yourfitness.coach.domain.models

import androidx.annotation.DrawableRes

data class FAQCard(
    val title: String,
    val image: Int?,
    val backgroundImage: Int,
    @DrawableRes val comingSoonBg: Int? = null
)
