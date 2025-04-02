package com.yourfitness.shop.ui.features.orders.cart.enter_coins

import androidx.lifecycle.SavedStateHandle
import com.yourfitness.common.ui.mvi.MviViewModel
import com.yourfitness.common.ui.utils.toCoins
import com.yourfitness.shop.ui.constants.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.math.max

@HiltViewModel
class EnterCoinsViewModel @Inject constructor(
    savedState: SavedStateHandle
) : MviViewModel<Any, Any>() {

    val currency = savedState.get<String>(Constants.CURRENCY).orEmpty().uppercase()
    val maxRedemption = savedState.get<Long>("maxRedemption") ?: 0L
    val availableCoins = max(0, savedState.get<Long>("availableCoins") ?: 0)
    var coinsValue = savedState.get<Double>(Constants.COINS_VALUE) ?: 0.0

    var enteredCoins: Long = 0L
        private set

    override fun handleIntent(intent: Any) {
        when (intent) {
            is EnterCoinsIntent.EnteredCoinsChanged -> {
                enteredCoins = intent.coins
                val errorType =
                    if (enteredCoins > maxRedemption) EnterAmountError.LIMIT_ERROR
                    else if (enteredCoins > availableCoins) EnterAmountError.NOT_ENOUGH_ERROR
                    else null
                state.value = EnterCoinsState.UpdateAction(errorType, maxRedemption)
            }
        }
    }
}

open class EnterCoinsIntent {
    data class EnteredCoinsChanged(val coins: Long) : EnterCoinsIntent()
}

open class EnterCoinsState {
    data class UpdateAction(
        val errorType: EnterAmountError?,
        val maxRedemption: Long
    ) : EnterCoinsState()
}