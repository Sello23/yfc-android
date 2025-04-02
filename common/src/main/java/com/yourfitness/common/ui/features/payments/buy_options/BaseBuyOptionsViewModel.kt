package com.yourfitness.common.ui.features.payments.buy_options

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.yourfitness.common.domain.models.BuyOptionData
import com.yourfitness.common.ui.mvi.MviViewModel
import kotlinx.coroutines.launch

abstract class BaseBuyOptionsViewModel : MviViewModel<Any, BuyOptionsState>() {

    private var credits = listOf<BuyOptionData>()

    private val _creditsData = MutableLiveData<List<BuyOptionData>>()
    val creditsData: LiveData<List<BuyOptionData>> = _creditsData
    private val _selectedCreditData = MutableLiveData<BuyOptionData>()
    val selectedCreditData: LiveData<BuyOptionData> = _selectedCreditData

    protected fun loadCredits() {
        state.postValue(BuyOptionsState.Loading)
        viewModelScope.launch {
            try {
                credits = getOptions()
                val selected = credits.firstOrNull()
                selected?.isSelected = true
                state.postValue(BuyOptionsState.Success(credits))
                _creditsData.postValue(credits)
                selected?.let { _selectedCreditData.postValue(it) }
            } catch (e: Exception) {
                state.postValue(BuyOptionsState.Error(e))
            }
        }
    }

    fun onCreditClick(item: BuyOptionData) {
        credits = credits.map { it.copy(isSelected = it.plan == item.plan) }
        _creditsData.postValue(credits)
        _selectedCreditData.postValue(item)
    }

    abstract fun onProceedClick()

    abstract suspend fun getOptions(): List<BuyOptionData>
}

open class BuyOptionsState {
    object Loading : BuyOptionsState()
    data class Success(val credits: List<BuyOptionData>) : BuyOptionsState()
    data class Error(val error: Exception) : BuyOptionsState()
}