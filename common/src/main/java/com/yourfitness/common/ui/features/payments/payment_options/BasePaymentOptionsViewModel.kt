package com.yourfitness.common.ui.features.payments.payment_options

import android.content.res.Resources
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.yourfitness.common.R
import com.yourfitness.common.domain.models.*
import com.yourfitness.common.network.CommonRestApi
import com.yourfitness.common.network.dto.PackagePaymentRequest
import com.yourfitness.common.network.dto.PackageRequest
import com.yourfitness.common.ui.mvi.MviViewModel
import com.yourfitness.common.ui.navigation.CommonNavigation
import com.yourfitness.common.ui.navigation.CommonNavigator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
open class BasePaymentOptionsViewModel @Inject constructor(
    private val commonNavigator: CommonNavigator,
    private val commonRestApi: CommonRestApi,
    private val resources: Resources,
    savedState: SavedStateHandle,
) : MviViewModel<PaymentOptionIntent, PaymentOptionState>() {

    protected val price: Long = savedState.get<Long>("price") ?: 0L
    private val creditData: BuyOptionData? = savedState.get<BuyOptionData>("credit_data")
    protected var savedCardId: String? = null
    protected var items = buildPaymentOptions()
    protected var card: CreditCard? = null

    private val _options = MutableLiveData<List<PaymentOption>>()
    val options: LiveData<List<PaymentOption>> = _options
    val paymentData = MutableLiveData<PaymentData?>()

    protected fun loadSavedCard() {
        state.value = PaymentOptionState.Loading
        viewModelScope.launch {
            try {
                val data = commonRestApi.getSavedCreditCard().details.firstOrNull()?.toSavedCreditCard()
                savedCardId = data?.id.orEmpty()
                data?.lastDigits
                    ?.let { updateLastDigitsCard("**** **** **** $it") }
                    ?: _options.postValue(items)
            } catch (e: Exception) {
                _options.postValue(items)
            }
        }
    }

    private fun buildPaymentOptions(): List<PaymentOption> {
        return listOf(
            PaymentOption(
                method = PaymentMethod.CARD,
                iconResId = R.drawable.ic_credit_card,
                title = resources.getString(R.string.payment_method_screen_credit_card),
                subtitle = resources.getString(R.string.payment_method_screen_mastercard_visa),
                isSelected = true
            )
        )
    }

    fun onAddedCard(card: CreditCard) {
        this.card = card
        updateLastDigitsCard(card.cardNumberOnly4DigitsVisible)
        this.savedCardId = null
    }

    private fun updateLastDigitsCard(value: String) {
        items = items.map { item ->
            if (item.method == PaymentMethod.CARD) {
                item.copy(subtitle = value)
            } else {
                item
            }
        }
        _options.postValue(items)
    }

    fun buyCredits() {
        if (isPaymentMethodAdded()) {
            executePayment()
        } else {
            navigateAddCart()
        }
    }

    protected fun isPaymentMethodAdded(): Boolean {
        return card != null || savedCardId != null
    }

    private fun executePayment() {
        state.value = PaymentOptionState.Loading
        viewModelScope.launch {
            try {
                val result = commonRestApi.createPayment(PackagePaymentRequest(buildPackageRequest()))
                paymentData.postValue(PaymentData(card, savedCardId, result.clientSecret))
            } catch (e: Exception) {
                state.postValue(PaymentOptionState.Error(e))
            }
        }
    }

    private fun buildPackageRequest(): PackageRequest {
        return PackageRequest(
            active = true,
            cost = price,
            credits = creditData?.optionAmount ?: 0,
            name = creditData?.plan.orEmpty()
        )
    }

    protected fun navigateAddCart() {
        commonNavigator.navigate(CommonNavigation.AddCreditCard(CreditCard()))
    }

    fun onPaymentOptionClick(item: PaymentOption) {
        items = items.map { it.copy(isSelected = it.method == item.method) }
        _options.postValue(items)
        if (item.method == PaymentMethod.CARD) {
            commonNavigator.navigate(CommonNavigation.AddCreditCard(card ?: CreditCard()))
        }
    }

    fun getPurchasedCredits(): Int {
        return creditData?.optionAmount ?: 0
    }
}

open class PaymentOptionState {
    object Loading : PaymentOptionState()
    data class Error(val error: Exception) : PaymentOptionState()
}

open class PaymentOptionIntent {
    object OpenProgress : PaymentOptionIntent()
}