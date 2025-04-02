package com.yourfitness.coach.ui.features.sign_in.welcome_back

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.yourfitness.coach.databinding.FragmentWelcomeBackBinding
import com.yourfitness.common.ui.mvi.MviFragment
import com.yourfitness.common.ui.mvi.viewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WelcomeBackFragment : MviFragment<WelcomeBackIntent, WelcomeBackState, WelcomeBackViewModel>() {

    override val binding: FragmentWelcomeBackBinding by viewBinding()
    override val viewModel: WelcomeBackViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        createListeners()
        showLoading(false)
    }

    override fun renderState(state: WelcomeBackState) {
        when (state) {
            is WelcomeBackState.ConnectionError -> showConnectionErrorMsg()
        }
    }

    private fun createListeners() {
        binding.buttonLogIn.setOnClickListener { onLoginClicked() }
        binding.textDontHaveAccount.setOnClickListener { singIn() }
    }

    private fun onLoginClicked() {
        viewModel.intent.postValue(WelcomeBackIntent.SignIn)
    }

    private fun singIn() {
        viewModel.intent.postValue(WelcomeBackIntent.DontHaveAccount)
    }
}