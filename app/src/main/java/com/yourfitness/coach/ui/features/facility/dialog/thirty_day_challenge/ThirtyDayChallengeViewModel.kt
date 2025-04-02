package com.yourfitness.coach.ui.features.facility.dialog.thirty_day_challenge

import androidx.lifecycle.viewModelScope
import com.yourfitness.coach.ui.navigation.Navigator
import com.yourfitness.common.data.entity.ProfileEntity
import com.yourfitness.common.domain.ProfileRepository
import com.yourfitness.common.ui.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ThirtyDayChallengeViewModel @Inject constructor(
    val navigator: Navigator) : MviViewModel<Any, Any>() {}