package com.yourfitness.coach.ui.features.sign_up.enter_email

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import androidx.core.view.isInvisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import com.yourfitness.coach.R
import com.yourfitness.coach.databinding.FragmentEnterEmailBinding
import com.yourfitness.coach.domain.entity.User
import com.yourfitness.coach.domain.entity.fullName
import com.yourfitness.common.domain.validation.Validation
import com.yourfitness.coach.ui.features.sign_up.setupStepIndicator
import com.yourfitness.common.ui.mvi.MviFragment
import com.yourfitness.common.ui.mvi.viewBinding
import com.yourfitness.coach.ui.utils.removeLinksUnderline
import dagger.hilt.android.AndroidEntryPoint
import com.yourfitness.common.R as common

@AndroidEntryPoint
class EnterEmailFragment : MviFragment<EnterEmailIntent, Any, EnterEmailViewModel>() {

    override val binding: FragmentEnterEmailBinding by viewBinding()
    override val viewModel: EnterEmailViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar(binding.toolbar.root)
        setupStepIndicator(binding.toolbar.progressStep)
        val user = requireArguments().get("user") as User
        val name = user.fullName
        binding.textNiceToMeet.text = resources.getString(R.string.sign_up_nice_to_meet_you, name)
        binding.editEmail.doOnTextChanged { text, _, _, _ -> onEmailChanged(text ?: "") }
        binding.textTermsAndPrivacy.movementMethod = LinkMovementMethod.getInstance()
        binding.textTermsAndPrivacy.removeLinksUnderline()
        binding.buttonNext.isEnabled = false
        binding.buttonNext.setOnClickListener { onNextClicked() }
    }

    private fun onNextClicked() {
        val name = binding.editEmail.text.toString()
        val intent = EnterEmailIntent.Next(name)
        viewModel.intent.postValue(intent)
    }

    private fun onEmailChanged(email: CharSequence) {
        val updatedEmail = email.trim()
        if (binding.editEmail.text.toString() != updatedEmail.toString()) {
            binding.editEmail.setText(updatedEmail)
            binding.editEmail.setSelection(updatedEmail.length)
        }
        showEmailError(null)
        binding.buttonNext.isEnabled = Validation.isValidEmail(updatedEmail.toString())
    }

    override fun renderState(state: Any) {
        when (state) {
            is EnterEmailState.Loading -> showLoading(true)
            is EnterEmailState.Success -> showLoading(false)
            is EnterEmailState.EmailError -> showEmailError(state.error.message)
            is EnterEmailState.Error -> showError(state.error)
        }
    }

    private fun showEmailError(message: String?) {
        showLoading(false)
        val isEmpty = message.isNullOrEmpty()
        val background =
            if (isEmpty) common.drawable.edit_text_background else common.drawable.edit_text_background_error
        binding.editEmail.setBackgroundResource(background)
        binding.textEmailError.text = message
        binding.textEmailError.isInvisible = isEmpty
    }
}