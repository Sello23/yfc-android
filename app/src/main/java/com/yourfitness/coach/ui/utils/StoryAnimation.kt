package com.yourfitness.common.ui.utils

import android.view.View
import android.view.animation.AnimationUtils
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.yourfitness.coach.R

fun View.animationSlideStart(animTime: Long, startOffset: Long) {
    val slideStart = AnimationUtils.loadAnimation(context, R.anim.animation_slide_start).apply {
        duration = animTime
        interpolator = FastOutSlowInInterpolator()
        this.startOffset = startOffset
    }
    startAnimation(slideStart)
}