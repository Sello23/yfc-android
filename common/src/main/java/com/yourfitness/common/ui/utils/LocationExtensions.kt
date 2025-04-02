package com.yourfitness.common.ui.utils

import java.util.*

fun Double?.formatDistance(): String? {
    return if (this != null) "%.1f km".format(Locale.US,this / 1000.0) else null
}

fun Long?.formatDistance() : String? = this?.toDouble().formatDistance()

fun Float?.formatDistance() : String? = this?.toDouble().formatDistance()
