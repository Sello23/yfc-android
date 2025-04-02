package com.yourfitness.coach.ui.features.facility.dialog.bit_quite_here

import com.yourfitness.common.ui.mvi.MviViewModel
import com.yourfitness.coach.ui.navigation.Navigator
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class BitQuietHereViewModel @Inject constructor(
    val navigator: Navigator,
) : MviViewModel<Any, Any>() {}