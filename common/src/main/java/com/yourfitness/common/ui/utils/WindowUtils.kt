package com.yourfitness.common.ui.utils

import android.os.Build
import android.util.DisplayMetrics
import android.view.ViewGroup
import android.view.Window
import com.yourfitness.common.ui.features.fitness_calendar.toPx

object WindowUtils {
     fun Window?.getDisplayWidth(correction: Int = 0): Int {
        val width = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            this?.windowManager?.currentWindowMetrics?.bounds?.width()
        } else {
            val displayMetrics = DisplayMetrics()
            this?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)
            displayMetrics.widthPixels
        }
        return if (width == null) ViewGroup.LayoutParams.WRAP_CONTENT
        else width + correction.toPx()
    }
}