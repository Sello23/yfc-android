package com.yourfitness.coach.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.yourfitness.coach.data.entity.ReferralCodeEntity.Companion.REFERRAL_CODE_TABLE

@Entity(tableName = REFERRAL_CODE_TABLE)
data class ReferralCodeEntity(
    @PrimaryKey
    val referralCode: String,
    var userRewardAmount: Int,
    val type: String,
    val startDate: Long?,
    val endDate: Long?,
    val currency: String?,
    val cost: Int?
) {
    companion object {
        const val REFERRAL_CODE_TABLE = "referral_code"
    }
}
