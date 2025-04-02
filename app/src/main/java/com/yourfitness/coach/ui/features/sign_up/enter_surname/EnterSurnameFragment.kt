package com.yourfitness.coach.ui.features.sign_up.enter_surname

import android.os.Bundle
import android.view.View
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import com.yourfitness.coach.databinding.FragmentEnterSurnameBinding
import com.yourfitness.coach.ui.features.sign_up.setupStepIndicator
import com.yourfitness.common.ui.mvi.MviFragment
import com.yourfitness.common.ui.mvi.viewBinding
import com.yourfitness.common.ui.utils.trimLine
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EnterSurnameFragment : MviFragment<EnterSurnameIntent, EnterSurnameState, EnterSurnameViewModel>() {

    override val binding: FragmentEnterSurnameBinding by viewBinding()
    override val viewModel: EnterSurnameViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar(binding.toolbar.root)
        setupStepIndicator(binding.toolbar.progressStep)
        binding.editSurname.doOnTextChanged { text, _, _, _ -> onNameChanged(text ?: "") }
        binding.buttonNext.isEnabled = false
        binding.buttonNext.setOnClickListener { onNextClicked() }
    }

    private fun onNextClicked() {
        val surname = binding.editSurname.text.toString().trim()
        val intent = EnterSurnameIntent.Next(surname)
        viewModel.intent.postValue(intent)
    }

    private fun onNameChanged(name: CharSequence) {
        val updatedName = name.trimLine()
        if (binding.editSurname.text.toString() != updatedName) {
            binding.editSurname.setText(updatedName)
            binding.editSurname.setSelection(updatedName.length)
        }
        binding.buttonNext.isEnabled = name.trim().isNotEmpty()
    }
}