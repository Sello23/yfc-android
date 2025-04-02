package com.yourfitness.pt.ui.utils

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.yourfitness.pt.domain.models.FacilityInfo

fun String.fromGson(): List<FacilityInfo> {
    val listType = object : TypeToken<List<FacilityInfo?>?>() {}.type
    return Gson().fromJson(this, listType)
}