package com.yourfitness.pt.data.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.yourfitness.pt.data.entity.PtBalanceEntity.Companion.PT_BALANCE_TABLE
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = PT_BALANCE_TABLE)
data class PtBalanceEntity(
    @PrimaryKey @ColumnInfo("personalTrainerId") val ptId: String,
    @ColumnInfo("amount") val amount: Int,
    @ColumnInfo("profileId") val profileId: String
) : Parcelable {
    companion object {
        const val PT_BALANCE_TABLE = "pt_balance"
    }
}
