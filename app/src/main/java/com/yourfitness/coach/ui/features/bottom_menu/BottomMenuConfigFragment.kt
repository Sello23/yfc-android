package com.yourfitness.coach.ui.features.bottom_menu

import com.google.android.material.bottomnavigation.BottomNavigationView
import com.yourfitness.coach.R
import com.yourfitness.common.domain.ProfileRepository
import com.yourfitness.common.ui.mvi.MviFragment
import com.yourfitness.common.ui.mvi.MviViewModel
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

abstract class BottomMenuConfigFragment<I, S, VM : MviViewModel<I, S>> : MviFragment<I, S, VM>() {

    @Inject
    lateinit var profileRepository: ProfileRepository

    fun setupBottomNavView(view: BottomNavigationView) {
        view.menu.clear()
        var isPtRole: Boolean
        var isBookable: Boolean

        runBlocking {
            isPtRole = profileRepository.isPtRole()
            isBookable = profileRepository.isBookable()
        }
        view.inflateMenu(if (isPtRole && isBookable) R.menu.pt_bottom_menu else R.menu.bottom_menu)
    }
}