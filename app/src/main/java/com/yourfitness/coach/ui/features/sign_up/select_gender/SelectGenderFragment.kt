package com.yourfitness.coach.ui.features.sign_up.select_gender

import android.os.Bundle
import android.view.View
import androidx.annotation.IdRes
import androidx.fragment.app.viewModels
import com.yourfitness.coach.R
import com.yourfitness.coach.databinding.FragmentSelectGenderBinding
import com.yourfitness.coach.ui.features.sign_up.setupStepIndicator
import com.yourfitness.common.network.dto.Gender
import com.yourfitness.common.ui.mvi.MviFragment
import com.yourfitness.common.ui.mvi.viewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SelectGenderFragment : MviFragment<Any, Any, SelectGenderViewModel>() {

    override val binding: FragmentSelectGenderBinding by viewBinding()
    override val viewModel: SelectGenderViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar(binding.toolbar.root)
        setupStepIndicator(binding.toolbar.progressStep)
        binding.radioGender.setOnCheckedChangeListener { _, _ -> validateInput() }
        binding.buttonNext.setOnClickListener { onNextClicked() }
        validateInput()
    }

    private fun validateInput() {
        binding.buttonNext.isEnabled = binding.radioGender.checkedRadioButtonId != -1
    }

    private fun onNextClicked() {
        val gender = mapButtonIdToGender(binding.radioGender.checkedRadioButtonId)
        viewModel.intent.postValue(SelectGenderIntent.Next(gender))
    }

    private fun mapButtonIdToGender(@IdRes buttonId: Int): Gender {
        return when (buttonId) {
            R.id.radio_male -> Gender.MALE
            R.id.radio_female -> Gender.FEMALE
            R.id.radio_other -> Gender.OTHER
            else -> Gender.UNKNOWN
        }
    }
}