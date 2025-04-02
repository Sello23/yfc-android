package com.yourfitness.community.data.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.yourfitness.community.data.entity.FriendsEntity.Companion.FRIENDS_TABLE
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = FRIENDS_TABLE)
data class FriendsEntity(
    @PrimaryKey val id: String,
    val mediaId: String,
    val name: String,
    val progressLevelId: String,
    val progressLevelMediaId: String,
    val progressLevelName: String,
    val surname: String,
    var isFriend: Boolean,
    var requestIn: Boolean,
    var requestOut: Boolean,
    val levelNumber: Int,
) : Parcelable {
    companion object {
        const val FRIENDS_TABLE = "friends_table"
    }
}

val FriendsEntity.fullName get() = listOfNotNull(name, surname).joinToString(" ")
