package com.yourfitness.common.ui.features.payments.add_credit_card

import android.util.Range
import com.yourfitness.common.domain.date.month
import com.yourfitness.common.domain.date.year
import com.yourfitness.common.domain.models.CreditCard
import com.yourfitness.common.ui.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.*
import javax.inject.Inject

@HiltViewModel
class AddCreditCardViewModel @Inject constructor() : MviViewModel<Any, AddCardState>() {

    val card = CreditCard()
    private var expDate = ""

    fun onCardNumberChanged(cardNumber: String) {
        card.cardNumber = cardNumber
        validateData()
    }

    fun onCardHolderChanged(cardHolder: String) {
        card.cardHolder = cardHolder
        validateData()
    }

    fun onCardExpDateChanged(expDate: String) {
        this.expDate = expDate
        validateData()
    }

    fun onCvvChanged(cvv: String) {
        card.cvv = cvv
        validateData()
    }

    private fun isValidExpDate(expDate: String): Boolean {
        val expMonth = expDate.split("/").firstOrNull()?.toIntOrNull() ?: 0
        val expYear = (expDate.split("/").lastOrNull()?.toIntOrNull() ?: 0) + 2000
        val now = Date()
        val isValid = expMonth in Range(1, 12)
                && (expYear > now.year() || (expMonth >= (month() + 1) && expYear == now.year()))
        if (isValid) {
            card.expMonth = expMonth
            card.expYear = expYear
        }
        return isValid
    }

    private fun validateData() {
        state.value = AddCardState.FilledCardData(isValidData())
    }

    private fun isValidData(): Boolean {
        val isValidExpDate = isValidExpDate(expDate)
        state.value =
            AddCardState.DisplayExpDateError(if (expDate.length != LENGTH_EXP_DATE) true else isValidExpDate)
        return card.cardNumber.length == LENGTH_CARD_NUMBER
                && card.cardHolder.isNotBlank()
                && isValidExpDate
                && card.cvv.length == LENGTH_CVV
    }

    companion object {
        const val LENGTH_CARD_NUMBER = 19
        const val LENGTH_EXP_DATE = 5
        const val LENGTH_CVV = 3
    }
}

sealed class AddCardState {
    data class DisplayExpDateError(val isHidden: Boolean) : AddCardState()
    data class FilledCardData(val isValid: Boolean) : AddCardState()
}