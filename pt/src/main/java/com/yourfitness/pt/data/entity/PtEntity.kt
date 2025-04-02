package com.yourfitness.pt.data.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.yourfitness.pt.data.entity.PersonalTrainerEntity.Companion.PERSONAL_TRAINERS_TABLE
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(
    tableName = PERSONAL_TRAINERS_TABLE,
    indices = [androidx.room.Index(value = ["id"], unique = true)]
)
data class PersonalTrainerEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "_id") val _id: Int = 0,
    @ColumnInfo("id") val id: String? = null,
    @ColumnInfo("amenities") val amenities: List<String>? = null,
    @ColumnInfo("birthday") val birthday: String? = null,
    @ColumnInfo("description") val description: String? = null,
    @ColumnInfo("name") val name: String? = null,
    @ColumnInfo("surname") val surname: String? = null,
    @ColumnInfo("phoneNumber") val phoneNumber: String? = null,
    @ColumnInfo("email") val email: String? = null,
    @ColumnInfo("mediaID") val mediaId: String? = null,
    @ColumnInfo("educations") val educations: List<EducationEntity>? = null,
    @ColumnInfo("facilityIDs") val facilityIDs: List<String>? = null,
    @ColumnInfo("focusAreas") val focusAreas: List<String>? = null,
    @ColumnInfo("bookable") val isBookable: Boolean? = null,
    @ColumnInfo("instagram") val instagram: String? = null
) : Parcelable {
    companion object {
        const val PERSONAL_TRAINERS_TABLE = "personal_trainer"
    }
}

@Parcelize
data class EducationEntity(
    @ColumnInfo("institute") val institute: String? = null,
    @ColumnInfo("qualification") val qualification: String? = null,
    @ColumnInfo("year") val year: Int? = null
) : Parcelable

val PersonalTrainerEntity.fullName: String
    get() = "$name $surname"
