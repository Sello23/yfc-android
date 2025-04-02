package com.yourfitness.pt.domain.models

import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil
import com.yourfitness.common.domain.models.WorkTimeData
import com.yourfitness.common.domain.models.WorkTimeDto
import kotlinx.parcelize.Parcelize

@Parcelize
data class FacilityInfo(
    val image: String,
    val name: String,
    val address: String,
    val latitude: Double = .0,
    val longitude: Double = .0,
    val id: String = "",
    val timetable: List<WorkTimeDto>?,
    val workTimeData: WorkTimeData? = null
) : Parcelable

fun FacilityInfo.distance(location: LatLng): Double {
    return SphericalUtil.computeDistanceBetween(this.toLatLng(), location)
}

fun FacilityInfo.toLatLng(): LatLng = LatLng(latitude, longitude)
