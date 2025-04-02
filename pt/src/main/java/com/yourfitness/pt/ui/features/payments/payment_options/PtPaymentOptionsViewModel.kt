package com.yourfitness.pt.ui.features.payments.payment_options

import android.content.res.Resources
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.yourfitness.common.domain.models.BuyOptionData
import com.yourfitness.common.domain.models.PaymentData
import com.yourfitness.common.domain.models.toRequestData
import com.yourfitness.common.network.CommonRestApi
import com.yourfitness.common.ui.features.payments.payment_options.BasePaymentOptionsViewModel
import com.yourfitness.common.ui.features.payments.payment_options.PaymentOptionIntent
import com.yourfitness.common.ui.features.payments.payment_options.PaymentOptionState
import com.yourfitness.common.ui.navigation.CommonNavigator
import com.yourfitness.pt.data.entity.fullName
import com.yourfitness.pt.domain.pt.PtRepository
import com.yourfitness.pt.domain.values.POP_AMOUNT
import com.yourfitness.pt.domain.values.PT_ID
import com.yourfitness.pt.domain.values.SESSIONS_PACKAGE
import com.yourfitness.pt.network.PtRestApi
import com.yourfitness.pt.network.dto.PtPackagePaymentRequest
import com.yourfitness.pt.ui.navigation.PtNavigation
import com.yourfitness.pt.ui.navigation.PtNavigator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PtPaymentOptionsViewModel @Inject constructor(
    private val navigator: PtNavigator,
    private val ptRestApi: PtRestApi,
    private val ptRepository: PtRepository,
    resources: Resources,
    commonNavigator: CommonNavigator,
    commonRestApi: CommonRestApi,
    savedState: SavedStateHandle,
) : BasePaymentOptionsViewModel(
    commonNavigator, commonRestApi, resources, savedState
) {

    private val ptId = savedState.get<String>(PT_ID).orEmpty()
    private val sessionPackage = savedState.get<BuyOptionData>(SESSIONS_PACKAGE)
    private val popAmount = savedState.get<Int>(POP_AMOUNT) ?: 3

    init {
        loadSavedCard()
    }

    override fun handleIntent(intent: PaymentOptionIntent) {
        super.handleIntent(intent)
        when (intent) {
            is PtPaymentOptionsIntent.PaymentConfirmed -> executeProductPayment()
            is PtPaymentOptionsIntent.PaymentSucceeded -> onPaymentSucceeded()
            is PtPaymentOptionsIntent.PaymentFailed -> onPaymentFailed()
            is PtPaymentOptionsIntent.PaymentCanceled -> onPaymentCanceled()
        }
    }

    private fun executeProductPayment() {
        if (isPaymentMethodAdded()) {
            executePayment()
        } else {
            navigateAddCart()
        }
    }

    private fun executePayment() {
        state.value = PaymentOptionState.Loading
        viewModelScope.launch {
            try {
                if (sessionPackage == null) return@launch
                val data = PtPackagePaymentRequest(sessionPackage.toRequestData(), ptId)
                val result = ptRestApi.createPackagePayment(data)
                paymentData.postValue(PaymentData(card, savedCardId, result.clientSecret))
            } catch (e: Exception) {
                state.postValue(PaymentOptionState.Error(e))
            }
        }
    }

    private fun onPaymentSucceeded() {
        viewModelScope.launch {
            ptRepository.updatePtBalance(ptId, sessionPackage?.optionAmount ?: 0)
            val ptName = ptRepository.getPtById(ptId)?.fullName.orEmpty()
            state.postValue(PtPaymentOptionsState.Loaded)
            navigator.navigate(
                PtNavigation.PaymentSuccess(ptId, sessionPackage?.optionAmount ?: 0, ptName)
            )
        }
    }

    private fun onPaymentFailed() {
        viewModelScope.launch {
            state.postValue(PtPaymentOptionsState.Loaded)
            navigator.navigate(PtNavigation.PaymentError(popAmount))
        }
    }

    private fun onPaymentCanceled() {
        state.value = PtPaymentOptionsState.Loaded
    }
}

open class PtPaymentOptionsState : PaymentOptionState() {
    object Loaded : PaymentOptionState()
}

open class PtPaymentOptionsIntent : PaymentOptionIntent() {
    object PaymentConfirmed : PaymentOptionIntent()
    object PaymentSucceeded : PaymentOptionIntent()
    object PaymentFailed : PaymentOptionIntent()
    object PaymentCanceled : PaymentOptionIntent()
}