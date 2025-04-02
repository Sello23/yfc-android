package com.yourfitness.coach.ui.features.payments.credits

import androidx.lifecycle.SavedStateHandle
import com.yourfitness.coach.domain.models.CalendarBookClassData
import com.yourfitness.coach.domain.models.PaymentFlow
import com.yourfitness.coach.ui.navigation.Navigation
import com.yourfitness.coach.ui.navigation.Navigator
import com.yourfitness.common.domain.models.BuyOptionData
import com.yourfitness.common.domain.models.toCreditData
import com.yourfitness.common.domain.settings.RegionSettingsManager
import com.yourfitness.common.ui.features.payments.buy_options.BaseBuyOptionsViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CreditsViewModel @Inject constructor(
    val navigator: Navigator,
    private val regionSettingsManager: RegionSettingsManager,
    savedState: SavedStateHandle
) : BaseBuyOptionsViewModel() {

    private val flow = savedState.get<PaymentFlow>("flow")
    private val bookClassData = savedState.get<CalendarBookClassData>("confirm_booking_data")

    init {
//        loadCredits()
    }

//    private fun loadCredits() {
//        state.postValue(CreditsState.Loading)
//        viewModelScope.launch {
//            try {
//                val region = regionSettingsManager.getSettings(true)
//                val currency = region?.currency?.uppercase().orEmpty()
//                credits = region?.packages
//                    ?.filter { it.active ?: false }
//                    ?.map { it.toCreditData(currency) }
//                    .orEmpty()
//                val selected = credits.firstOrNull()
//                selected?.isSelected = true
//                state.postValue(CreditsState.Success(credits))
//                _creditsData.postValue(credits)
//                selected?.let { _selectedCreditData.postValue(it) }
//            } catch (e: Exception) {
//                state.postValue(CreditsState.Error(e))
//            }
//        }
//    }

//    fun onCreditClick(item: CreditData) {
//        credits = credits.map { it.copy(isSelected = it.plan == item.plan) }
//        _creditsData.postValue(credits)
//        _selectedCreditData.postValue(item)
//    }

    override suspend fun getOptions(): List<BuyOptionData> {
        val region = regionSettingsManager.getSettings(true)
        val currency = region?.currency?.uppercase().orEmpty()
        return region?.packages
            ?.filter { it.active ?: false }
            ?.map { it.toCreditData(currency) }
            .orEmpty()
    }

    override fun onProceedClick() {
        val selected = selectedCreditData.value ?: return
        flow ?: return
        navigator.navigate(
            Navigation.PaymentOptions(
                price = selected.price.toLong(),
                currency = selected.currency,
                creditData = selected,
                bookClassData = bookClassData,
                flow = flow
            )
        )
    }
}
