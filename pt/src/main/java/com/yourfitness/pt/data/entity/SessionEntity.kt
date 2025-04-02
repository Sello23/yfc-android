package com.yourfitness.pt.data.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.yourfitness.pt.data.entity.SessionEntity.Companion.SESSION_TABLE
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = SESSION_TABLE)
data class SessionEntity(
    @PrimaryKey @ColumnInfo("id") val id: String,
    @ColumnInfo("facilityId") val facilityId: String,
    @ColumnInfo("personalTrainerId") val personalTrainerId: String,
    @ColumnInfo("profileId") val profileId: String,
    @ColumnInfo("dateFrom") val from: Long,
    @ColumnInfo("dateTo") val to: Long,
    @ColumnInfo("status") val status: String,
    @ColumnInfo("chainId")val chainId: String,
    @ColumnInfo("deleted") val deleted: Boolean,
    @ColumnInfo("repeats") val repeats: Int,
    @ColumnInfo("profileInfo") val profileInfo: ProfileInfo
) : Parcelable {
    companion object {
        const val SESSION_TABLE = "session_table"
    }
}

@Parcelize
data class ProfileInfo(
    val name: String,
    val surname: String,
    val birthday: Long,
    val phoneNumber: String,
    val email: String,
    val mediaId: String
) : Parcelable {
    companion object {
        fun empty() = ProfileInfo("", "", 0, "", "", "")
    }

    val fullName get() = "$name $surname"
}
