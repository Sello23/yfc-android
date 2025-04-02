package com.yourfitness.coach.ui.features.facility.no_result

import androidx.lifecycle.SavedStateHandle
import com.yourfitness.coach.network.dto.Classification
import com.yourfitness.common.ui.mvi.MviViewModel
import com.yourfitness.coach.ui.navigation.Navigator
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NoResultViewModel @Inject constructor(
    val navigator: Navigator,
    savedState: SavedStateHandle
) : MviViewModel<Any, Any>() {

    val classification: Classification = savedState.get<Classification>("classification") ?: Classification.GYM
}
