package com.yourfitness.coach.data.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil
import com.yourfitness.coach.data.entity.FacilityEntity.Companion.FACILITIES_TABLE
import com.yourfitness.coach.network.dto.Classification
import com.yourfitness.coach.ui.utils.Weekday
import com.yourfitness.common.domain.date.DAY_END_MINUTES
import com.yourfitness.common.domain.date.minutesOfDay
import com.yourfitness.common.domain.date.nowTimeValues
import com.yourfitness.common.domain.date.toDateTimeUtc0
import com.yourfitness.common.domain.date.weekDays
import com.yourfitness.common.domain.models.WorkTimeData
import com.yourfitness.common.domain.models.WorkTimeDto
import kotlinx.parcelize.Parcelize
import com.yourfitness.common.R as common

@Parcelize
@Entity(
    tableName = FACILITIES_TABLE,
    indices = [Index(value = ["id"], unique = true)]
)
data class FacilityEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "_id") var _id: Int = 0,
    @ColumnInfo(name = "personalTrainersIDs") var personalTrainersIds: List<String>? = null,
    @ColumnInfo(name = "address") var address: String? = null,
    @ColumnInfo(name = "city") var city: String? = null,
    @ColumnInfo(name = "classification") var classification: String? = null,
    @ColumnInfo(name = "contactDetails") var contactDetails: String? = null,
    @ColumnInfo(name = "description") var description: String? = null,
    @ColumnInfo(name = "emailAddress") var emailAddress: String? = null,
    @ColumnInfo(name = "femaleOnly") var femaleOnly: Boolean? = null,
    @ColumnInfo(name = "group") var group: String? = null,
    @ColumnInfo(name = "icon") var icon: String? = null,
    @ColumnInfo(name = "id") var id: String? = null,
    @ColumnInfo(name = "latitude") var latitude: Double? = null,
    @ColumnInfo(name = "longitude") var longitude: Double? = null,
    @ColumnInfo(name = "name") var name: String? = null,
    @ColumnInfo(name = "region") var region: String? = null,
    @ColumnInfo(name = "amenities") var amenities: List<String>? = null,
    @ColumnInfo(name = "types") var types: List<String>? = null,
    @ColumnInfo(name = "gallery") var gallery: List<String>? = null,
    @Ignore @ColumnInfo(name = "customClasses") var customClasses: List<ClassEntity>? = null,
    @Ignore @ColumnInfo(name = "licensedClasses") var licensedClasses: List<ClassEntity>? = null,
    @Ignore @ColumnInfo(name = "instructors") var instructors: List<InstructorEntity>? = null,
    @ColumnInfo(name = "isVisit") var isVisit: Boolean = false,
    @ColumnInfo(name = "schedule") var schedule: List<WorkTimeDto>? = null,
    @ColumnInfo(name = "accessLimitationMessage") var accessLimitationMessage: String? = null,
    @ColumnInfo(name = "displayAccessLimitationMessage") var displayAccessLimitationMessage: Boolean = false,
    @ColumnInfo(name = "isYfcGym") var isYfcGym: Boolean = false,

//    @SerializedName("createdAt") val createdAt: Int? = null,
//    @SerializedName("updatedAt") val updatedAt: Int? = null
//    val instructors: List<InstructorDto>? = null,
) : Parcelable {
    companion object {
        const val FACILITIES_TABLE = "facility"
    }
}

@Parcelize
data class ClassEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "_id") var _id: Int = 0,
    @ColumnInfo(name = "availablePlaces") var availablePlaces: Int? = null,
    @ColumnInfo(name = "description") var description: String? = null,
    @ColumnInfo(name = "facilityID") var facilityID: String? = null,
    @ColumnInfo(name = "iconID") var iconID: String? = null,
    @ColumnInfo(name = "id") var id: String? = null,
    @ColumnInfo(name = "licensedProviderClassID") var licensedProviderClassID: String? = null,
    @ColumnInfo(name = "licensedProviderID") var licensedProviderID: String? = null,
    @ColumnInfo(name = "name") var name: String? = null,
    @ColumnInfo(name = "price") var price: Int? = null,
    @ColumnInfo(name = "priceCoins") var priceCoins: Int? = null,
    @ColumnInfo(name = "type") var type: String? = null,
    @ColumnInfo(name = "galleryIDs") var galleryIDs: List<String>? = null,
) : Parcelable

@Parcelize
data class InstructorEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "_id") var _id: Int = 0,
    @ColumnInfo(name = "createdAt") var createdAt: Int? = null,
    @ColumnInfo(name = "description") var description: String? = null,
    @ColumnInfo(name = "facilityID") var facilityID: String? = null,
    @ColumnInfo(name = "id") var id: String? = null,
    @ColumnInfo(name = "name") var name: String? = null,
    @ColumnInfo(name = "photoID") var photoID: String? = null,
    @ColumnInfo(name = "updatedAt") var updatedAt: Int? = null,
) : Parcelable

fun FacilityEntity.toLatLng(): LatLng? {
    val latitude = latitude
    val longitude = longitude
    return if (latitude != null && longitude != null) LatLng(latitude, longitude) else null
}

fun FacilityEntity.distance(location: LatLng): Double {
    return SphericalUtil.computeDistanceBetween(this.toLatLng(), location)
}

val FacilityEntity.classTypes: List<String>
    get() = classes.mapNotNull { it.type }.distinct()

val FacilityEntity.classes: List<ClassEntity>
    get() = licensedClasses.orEmpty() + customClasses.orEmpty()

val FacilityEntity.isAvailable: Boolean
    get() {
        val now = nowTimeValues()
        val workTime = schedule?.firstOrNull { it.weekDay == weekDays[now.second] } ?: return false
        val workTimeStart = workTime.from?.minutesOfDay() ?: -1
        var workTimeEnd = workTime.to?.minutesOfDay() ?: -1
        if (workTimeEnd == 0) workTimeEnd = DAY_END_MINUTES
        return now.first in (workTimeStart..workTimeEnd)
    }

val FacilityEntity.workTimeData: WorkTimeData? // String id, string arguments, background id
    get() {
        if (classification != Classification.GYM.value || schedule == null) return null
        val now = nowTimeValues()
        val workTime = schedule?.firstOrNull { it.weekDay == weekDays[now.second] }
            ?: return WorkTimeData(
                common.string.no_access_for_today,
                null,
                common.drawable.rounded_border_no_access,
                common.drawable.ic_info_red
            )

        val workTimeStart = workTime.from?.minutesOfDay() ?: -1
        var workTimeEnd = workTime.to?.minutesOfDay() ?: -1
        if (workTimeEnd == 0) workTimeEnd = DAY_END_MINUTES

        val iconResId: Int
        val bgResId: Int
        if (now.first in (workTimeStart..workTimeEnd)) {
            bgResId = common.drawable.rounded_border_access_hours
            iconResId = common.drawable.ic_info_yellow
        } else {
            bgResId = common.drawable.rounded_border_no_access
            iconResId = common.drawable.ic_info_red
        }
        val from = workTime.from
        val to = workTime.to
        val timeInterval =
            if (from != null && to != null) {
                "${from.toDateTimeUtc0()} - ${to.toDateTimeUtc0()}"
            } else {
                null
            }
        return WorkTimeData(common.string.access_hours, timeInterval, bgResId, iconResId)
    }

val FacilityEntity.timetable: Timetable
    get() {
        val list = Timetable()
        val set: MutableMap<String, WorkTimeDto> = Weekday.values().associate {
            it.label to WorkTimeDto(weekDay = it.label)
        }.toMutableMap()
        schedule?.forEach {
            val weekday = it.weekDay
            if (weekday != null) {
                set[weekday] = it
            }
        }
        list.addAll(set.values)
        return list
    }

@Parcelize
class Timetable : ArrayList<WorkTimeDto>(), Parcelable
