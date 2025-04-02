package com.yourfitness.shop.ui.features.product_details.dialogs

import androidx.lifecycle.SavedStateHandle
import com.yourfitness.common.ui.mvi.MviViewModel
import com.yourfitness.shop.ui.constants.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SelectCoinsAmountViewModel @Inject constructor(
    savedState: SavedStateHandle
) : MviViewModel<SelectCoinsAmountIntent, SelectCoinsAmountState>() {

    val initialPrice = savedState[Constants.PRICE] ?: 0L
    val currency = savedState.get<String?>(Constants.CURRENCY).orEmpty()
    val coinsCost = savedState[Constants.COINS_COST] ?: 0.0
    val maxCoins = savedState[Constants.COINS_AMOUNT] ?: 0L
    private val selectedCoins = savedState[Constants.SELECTED_COINS] ?: 0L
    val flow = savedState.get<SelectCoinsFlow>(Constants.SELECT_COINS_FLOW) ?: SelectCoinsFlow.ADD_TO_CART

    var coinsUsed = selectedCoins
        private set

    override fun handleIntent(intent: SelectCoinsAmountIntent) {
        when (intent) {
            is SelectCoinsAmountIntent.RangeChanged -> {
                coinsUsed = intent.coinsUsed
                state.value = SelectCoinsAmountState.Update
            }
        }
    }
}

sealed class SelectCoinsAmountIntent {
    data class RangeChanged(val coinsUsed: Long) : SelectCoinsAmountIntent()

}

sealed class SelectCoinsAmountState {
    object Loading : SelectCoinsAmountState()
    object Update : SelectCoinsAmountState()

}

enum class SelectCoinsFlow(val value: Int) {
    ADD_TO_CART(0),
    UPDATE(1),
}