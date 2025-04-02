package com.yourfitness.coach.ui.features.profile.payment_history.webview

import androidx.lifecycle.viewModelScope
import com.yourfitness.coach.network.YFCRestApi
import com.yourfitness.coach.ui.navigation.Navigator
import com.yourfitness.common.network.dto.PaymentReceipt
import com.yourfitness.common.ui.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class PaymentHistoryWebViewViewModel @Inject constructor(
    val navigator: Navigator,
    private val restApi: YFCRestApi
) : MviViewModel<Any, Any>() {

    fun fetchData(paymentIntent: String) {
        viewModelScope.launch {
            try {
                state.postValue(PaymentHistoryWebViewState.Loading)
                val stripeUrl = restApi.getStripeUrl(paymentIntent)
                state.postValue(PaymentHistoryWebViewState.Success(stripeUrl))
            } catch (error: Exception) {
                Timber.e(error)
                state.postValue(PaymentHistoryWebViewState.Error(error))
            }
        }
    }
}

open class PaymentHistoryWebViewState {
    object Loading : PaymentHistoryWebViewState()
    data class Success(
        val stripeUrl: PaymentReceipt
    ) : PaymentHistoryWebViewState()

    data class Error(val error: Exception) : PaymentHistoryWebViewState()
}