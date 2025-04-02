package com.yourfitness.common.utils

import timber.log.Timber
import java.time.ZoneOffset

val Int.getRegionZoneOffset: ZoneOffset
    get() {
        return try {
            val offset = this ?: 0
            ZoneOffset.ofHoursMinutes(offset / 60, offset % 60)
        } catch (e: Exception) {
            Timber.e(e)
            ZoneOffset.UTC
        }
    }
