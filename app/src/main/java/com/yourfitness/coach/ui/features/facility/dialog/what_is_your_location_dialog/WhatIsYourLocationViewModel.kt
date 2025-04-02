package com.yourfitness.coach.ui.features.facility.dialog.what_is_your_location_dialog

import com.yourfitness.common.ui.mvi.MviViewModel
import com.yourfitness.coach.ui.navigation.Navigator
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class WhatIsYourLocationViewModel @Inject constructor(
    private val navigator: Navigator,
) : MviViewModel<Any, Any>() {}