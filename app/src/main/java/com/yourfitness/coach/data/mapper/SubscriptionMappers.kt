package com.yourfitness.coach.data.mapper

import com.yourfitness.coach.data.entity.SubscriptionEntity
import com.yourfitness.coach.network.dto.Subscription
import com.yourfitness.common.domain.date.today

fun Subscription.toEntity(): SubscriptionEntity {
    return SubscriptionEntity(
        createdTime = createdTime,
        autoRenewal = autoRenewal ?: false,
        complimentaryAccess = complimentaryAccess ?: false,
        expiredDate = expiredDate,
        nextPaymentDate = nextPaymentDate,
        paidSubscription = paidSubscription ?: true,
        lastUpdateTime = today().time,
        isOneTime = isOneTime ?: false,
        duration = duration,
        isSynchronizing = false,
        corporationName = corporationName
    )
}