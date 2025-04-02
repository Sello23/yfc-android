package com.yourfitness.coach.domain.models

import android.widget.ImageView

data class StoryPage(
    val title: String?,
    val subtitle: String?,
    val textSize: Float,
    val pageCountColor: Int?,
    val textButton: String?,
    val image: Int,
    val imageScaleType: ImageView.ScaleType?,
    val backgroundImage: Int,
)
