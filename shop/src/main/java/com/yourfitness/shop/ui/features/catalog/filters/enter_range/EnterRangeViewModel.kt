package com.yourfitness.shop.ui.features.catalog.filters.enter_range

import androidx.lifecycle.SavedStateHandle
import com.yourfitness.common.ui.mvi.MviViewModel
import com.yourfitness.common.ui.utils.toCoins
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class EnterRangeViewModel @Inject constructor(
    savedState: SavedStateHandle
) : MviViewModel<Any, Any>() {

    val currency = savedState.get<String>("currency").orEmpty()
    val rangeMin = savedState.get<Long>("rangeMin") ?: 0
    val rangeMax = savedState.get<Long>("rangeMax") ?: 0

    var priceMin = 0L
        private set
    var priceMax = 0L
        private set
    private var minRangeValid = false
    private var maxRangeValid = false

    override fun handleIntent(intent: Any) {
        when (intent) {
            is EnterRangeIntent.PriceMinChanged -> {
                priceMin = intent.price.toCoins()
                minRangeValid = validateRange(priceMin)
                state.value = EnterRangeState.UpdateAction(isInputValid())
            }
            is EnterRangeIntent.PriceMaxChanged -> {
                priceMax = intent.price.toCoins()
                maxRangeValid = validateRange(priceMax)
                state.value = EnterRangeState.UpdateAction(isInputValid())
            }
        }
    }

    private fun validateRange(value: Long): Boolean {
        return value in rangeMin..rangeMax
    }

    private fun isInputValid(): Boolean {
        return minRangeValid && maxRangeValid && priceMin <= priceMax
    }
}

open class EnterRangeIntent {
    data class PriceMinChanged(val price: Long) : EnterRangeIntent()
    data class PriceMaxChanged(val price: Long) : EnterRangeIntent()
}

open class EnterRangeState {
    data class UpdateAction(val isEnabled: Boolean) : EnterRangeState()
}