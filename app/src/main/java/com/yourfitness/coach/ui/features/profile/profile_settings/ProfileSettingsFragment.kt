package com.yourfitness.coach.ui.features.profile.profile_settings

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.yourfitness.coach.R
import com.yourfitness.coach.databinding.FragmentProfileSettingsBinding
import com.yourfitness.common.ui.mvi.MviFragment
import com.yourfitness.common.ui.mvi.viewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileSettingsFragment : MviFragment<Any, Any, ProfileSettingsViewModel>() {

    override val binding: FragmentProfileSettingsBinding by viewBinding()
    override val viewModel: ProfileSettingsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
    }

    private fun setupToolbar() {
        binding.toolbar.toolbar.title = getString(R.string.settings)
    }
}