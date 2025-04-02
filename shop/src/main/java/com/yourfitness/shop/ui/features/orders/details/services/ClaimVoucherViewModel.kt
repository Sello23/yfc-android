package com.yourfitness.shop.ui.features.orders.details.services

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.yourfitness.common.network.CommonRestApi
import com.yourfitness.common.ui.mvi.MviViewModel
import com.yourfitness.shop.domain.model.BaseCard
import com.yourfitness.shop.domain.model.CartCard
import com.yourfitness.shop.domain.orders.OrderRepository
import com.yourfitness.shop.network.ShopRestApi
import com.yourfitness.shop.network.dto.VoucherResponse
import com.yourfitness.shop.ui.constants.Constants
import com.yourfitness.shop.ui.features.orders.details.services.ServiceCard
import com.yourfitness.shop.ui.navigation.ShopNavigation
import com.yourfitness.shop.ui.navigation.ShopNavigator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject


@HiltViewModel
class ClaimVoucherViewModel @Inject constructor(
    val navigator: ShopNavigator,
    private val shopRestApi: ShopRestApi,
    savedState: SavedStateHandle
) : MviViewModel<ClaimVoucherIntent, ClaimVoucherState>() {

    val orderId = savedState.get<String>(Constants.ORDER_ID).orEmpty()

    override fun handleIntent(intent: ClaimVoucherIntent) {
        when (intent) {
            is ClaimVoucherIntent.ClaimVoucherRequested -> claimVoucher()
        }
    }

    private fun claimVoucher() {
        state.value = ClaimVoucherState.Loading
        viewModelScope.launch {
            try {
                val claimData = shopRestApi.claimVoucher(orderId)
                state.postValue(ClaimVoucherState.VoucherLoaded(claimData))
            } catch (e: Exception) {
                Timber.e(e)
                state.postValue(ClaimVoucherState.Error)
            }
        }
    }
}

open class ClaimVoucherState {
    object Loading : ClaimVoucherState()
    object Error : ClaimVoucherState()
    data class VoucherLoaded(val data: VoucherResponse) : ClaimVoucherState()
}

open class ClaimVoucherIntent {
    object ClaimVoucherRequested : ClaimVoucherIntent()
}
