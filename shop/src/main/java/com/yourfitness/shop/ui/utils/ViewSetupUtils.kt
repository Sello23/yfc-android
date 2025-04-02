package com.yourfitness.shop.ui.utils

import androidx.core.view.isVisible
import com.yourfitness.common.ui.utils.formatCoins
import com.yourfitness.shop.databinding.ViewCoinBalanceBinding
import com.yourfitness.shop.ui.navigation.ShopNavigation

fun ViewCoinBalanceBinding.setupCoinBalanceCard(
    coins: Long,
    coinsCost: Double,
    currency: String,
    navigateTo: (ShopNavigation) -> Unit,
) {
    image3.isVisible = coins > 0L
    image2.isVisible = coins == 0L
    secondaryMessage.isVisible = coins == 0L
    helperAction.isVisible = coins > 0L
    helperActionNoCoins.isVisible = coins == 0L
    coinsAmount.text = this.root.context.resources.getQuantityString(
        com.yourfitness.common.R.plurals.profile_screen_format_coins,
        coins.toInt(),
        coins.formatCoins()
    )
    helperAction.setOnClickListener {
        navigateTo(ShopNavigation.CoinsUsageInfo(coins, coinsCost, currency))
    }
    helperActionNoCoins.setOnClickListener {
        navigateTo(ShopNavigation.HowToEarnCoins())
    }
}