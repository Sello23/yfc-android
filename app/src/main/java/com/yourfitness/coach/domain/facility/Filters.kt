package com.yourfitness.coach.domain.facility

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Filters(
    val allTypes: List<String> = emptyList(),
    val allAmenities: List<String> = emptyList(),
    val types: List<String> = mutableListOf(),
    val amenities: List<String> = mutableListOf(),
    val distance: Int = FILTERS_NO_LIMIT,
    val images: List<String>? = null
) : Parcelable

const val FILTERS_NO_LIMIT = 20000

fun Filters.isEmpty(): Boolean {
    return types.isEmpty() && amenities.isEmpty() && distance == FILTERS_NO_LIMIT
}