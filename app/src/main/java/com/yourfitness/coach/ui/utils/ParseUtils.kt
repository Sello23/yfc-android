package com.yourfitness.coach.ui.utils

import com.google.gson.Gson
import com.yourfitness.pt.domain.models.FacilityInfo

fun List<FacilityInfo>.toGson(): String {
    return Gson().toJson(this)
}