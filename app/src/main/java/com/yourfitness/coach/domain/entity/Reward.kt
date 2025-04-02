package com.yourfitness.coach.domain.entity

import android.os.Parcelable
import androidx.annotation.StringRes
import com.yourfitness.coach.R
import com.yourfitness.coach.data.entity.FacilityEntity
import kotlinx.parcelize.Parcelize

@Parcelize
open class Reward : Parcelable

@Parcelize
data class CoinReward(
    val coins: Long = 0L,
    val points: Long = 0L,
    val isFirst: Boolean = true,
    val isReward: Boolean = false,
    val isCoin: Boolean = false
) : Reward()

@Parcelize
data class CreditReward(
    val credits: Int = 0,
    val visits: Int = 0,
    val name: String = "",
    val isFirst: Boolean = true,
    val studio: FacilityEntity = FacilityEntity()
) : Reward()

@Parcelize
data class VoucherReward(
    val coins: Int = 0,
    @StringRes val msg: Int = R.string.coin_reward_someone_redeemed_your_voucher_code
) : Reward()

@Parcelize
data class SubscriptionVoucherReward(
    val coins: Int = 0
) : Reward()

@Parcelize
data class LevelUpReward(
    val coins: Long = 0,
    val points: Long = 0,
    val level: String,
    val imageId: String,
) : Reward()
