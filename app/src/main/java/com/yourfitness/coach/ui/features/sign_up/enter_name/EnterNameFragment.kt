package com.yourfitness.coach.ui.features.sign_up.enter_name

import android.os.Bundle
import android.view.View
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import com.yourfitness.coach.databinding.FragmentEnterNameBinding
import com.yourfitness.coach.ui.features.sign_up.setupStepIndicator
import com.yourfitness.common.ui.mvi.MviFragment
import com.yourfitness.common.ui.mvi.viewBinding
import com.yourfitness.common.ui.utils.trimLine
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EnterNameFragment : MviFragment<EnterNameIntent, EnterNameState, EnterNameViewModel>() {

    override val binding: FragmentEnterNameBinding by viewBinding()
    override val viewModel: EnterNameViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar(binding.toolbar.root)
        setupStepIndicator(binding.toolbar.progressStep)
        binding.editName.doOnTextChanged { text, _, _, _ -> onNameChanged(text ?: "") }
        binding.buttonNext.isEnabled = false
        binding.buttonNext.setOnClickListener { onNextClicked() }
    }

    private fun onNextClicked() {
        val name = binding.editName.text.toString().trim()
        val intent = EnterNameIntent.Next(name)
        viewModel.intent.postValue(intent)
    }

    private fun onNameChanged(name: CharSequence) {
        val updatedName = name.trimLine()
        if (binding.editName.text.toString() != updatedName) {
            binding.editName.setText(updatedName)
            binding.editName.setSelection(updatedName.length)
        }
        binding.buttonNext.isEnabled = name.trim().isNotEmpty()
    }
}