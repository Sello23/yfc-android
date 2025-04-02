package com.yourfitness.coach.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.yourfitness.coach.data.entity.SubscriptionEntity.Companion.SUBSCRIPTION_TABLE

@Entity(tableName = SUBSCRIPTION_TABLE)
data class SubscriptionEntity(
    @PrimaryKey
    val createdTime: Long?,
    val autoRenewal: Boolean = false,
    val complimentaryAccess: Boolean = false,
    val expiredDate: Long?,
    val nextPaymentDate: Long?,
    val paidSubscription: Boolean = true,
    val lastUpdateTime: Long,
    val isSynchronizing: Boolean = false,
    val isOneTime: Boolean = false,
    val duration: Int?,
    val corporationName: String?
) {
    companion object {
        const val SUBSCRIPTION_TABLE = "subscription_table"
    }
}
