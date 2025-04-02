package com.yourfitness.coach.ui.features.splash

import android.annotation.SuppressLint
import androidx.fragment.app.viewModels
import com.yourfitness.coach.databinding.FragmentSplashScreenBinding
import com.yourfitness.common.ui.mvi.MviFragment
import com.yourfitness.common.ui.mvi.viewBinding
import dagger.hilt.android.AndroidEntryPoint

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashScreenFragment : MviFragment<Any, Any, SplashScreenViewModel>() {

    override val binding: FragmentSplashScreenBinding by viewBinding()
    override val viewModel: SplashScreenViewModel by viewModels()
}