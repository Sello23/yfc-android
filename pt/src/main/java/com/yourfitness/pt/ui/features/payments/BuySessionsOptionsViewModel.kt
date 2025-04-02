package com.yourfitness.pt.ui.features.payments

import androidx.lifecycle.SavedStateHandle
import com.yourfitness.common.domain.models.BuyOptionData
import com.yourfitness.common.domain.models.toSessionData
import com.yourfitness.common.network.CommonRestApi
import com.yourfitness.common.ui.features.payments.buy_options.BaseBuyOptionsViewModel
import com.yourfitness.pt.domain.values.POP_AMOUNT
import com.yourfitness.pt.domain.values.PT_ID
import com.yourfitness.pt.ui.navigation.PtNavigation
import com.yourfitness.pt.ui.navigation.PtNavigator
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class BuySessionsOptionsViewModel @Inject constructor(
    val navigator: PtNavigator,
    private val commonRestApi: CommonRestApi,
    savedState: SavedStateHandle
) : BaseBuyOptionsViewModel() {

    var ptId = savedState.get<String>(PT_ID).orEmpty()
    var popAmount = savedState.get<Int>(POP_AMOUNT)

    init {
        loadCredits()
    }

    override suspend fun getOptions(): List<BuyOptionData> {
        val region = commonRestApi.getSettingsRegion()
        val currency = region.currency?.uppercase().orEmpty()
        return region.ptPackages
            ?.filter { it.active ?: false }
            ?.map { it.toSessionData(currency) }
            ?.sortedBy { it.optionAmount }
            .orEmpty()
    }

    override fun onProceedClick() {
        val selected = selectedCreditData.value ?: return
        navigator.navigate(PtNavigation.PaymentOptions(selected, ptId, popAmount))
    }
}
