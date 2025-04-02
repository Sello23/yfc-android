package com.yourfitness.coach.ui.features.more.faq

import com.yourfitness.common.ui.mvi.MviViewModel
import com.yourfitness.coach.ui.navigation.Navigation
import com.yourfitness.coach.ui.navigation.Navigator
import com.yourfitness.pt.ui.navigation.PtNavigation
import com.yourfitness.pt.ui.navigation.PtNavigator
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FAQViewModel @Inject constructor(
    private val navigator: Navigator,
    private val ptNavigator: PtNavigator
) : MviViewModel<FAQIntent, Any>() {

    override fun handleIntent(intent: FAQIntent) {
        when (intent) {
            is FAQIntent.Story -> {
                navigator.navigate(Navigation.Story(intent.title))
            }
            is FAQIntent.ComingSoon -> ptNavigator.navigate(PtNavigation.ComingSoon)
        }
    }
}

open class FAQIntent {
    data class Story(val title: String) : FAQIntent()
    object ComingSoon : FAQIntent()
}