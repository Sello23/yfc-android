package com.yourfitness.common.data.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.yourfitness.common.data.entity.ProfileEntity.Companion.PROFILE_TABLE
import com.yourfitness.common.network.dto.Gender
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = PROFILE_TABLE)
data class ProfileEntity(
    @PrimaryKey
    val _id: Int = 0,
    val id: String,
    val phoneNumber: String?,
    val email: String?,
    val name: String?,
    val surname: String?,
    val instagram: String?,
    val mediaId: String?,
    val gender: Gender?,
    val birthday: Long?,
    val region: String?,
    val voucher: String?,
    val pushToken: String?,
    val createdAt: Long?,
    val corporationId: String?,
    val personalTrainer: Boolean?,
    val complimentaryAccess: Boolean?,
    val isBookable: Boolean?,
    val accessWorkoutPlans: Boolean?
) : Parcelable {
    companion object {
        const val PROFILE_TABLE = "profile"

        val empty
            get() = ProfileEntity(
                0,
                "",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
            )
    }
}

val ProfileEntity.fullName get() = listOfNotNull(name, surname).joinToString(" ")
