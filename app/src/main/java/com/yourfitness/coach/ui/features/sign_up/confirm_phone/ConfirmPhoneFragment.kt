package com.yourfitness.coach.ui.features.sign_up.confirm_phone

import android.os.Bundle
import android.text.Html
import android.text.Spanned
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isInvisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import com.yourfitness.coach.R
import com.yourfitness.coach.databinding.FragmentConfirmPhoneBinding
import com.yourfitness.coach.domain.entity.User
import com.yourfitness.coach.ui.features.sign_up.enter_phone.Flow
import com.yourfitness.coach.ui.features.sign_up.setupStepIndicator
import com.yourfitness.common.ui.mvi.MviFragment
import com.yourfitness.common.ui.mvi.viewBinding
import com.yourfitness.common.ui.utils.setTextColorRes
import dagger.hilt.android.AndroidEntryPoint
import com.yourfitness.common.R as common
import com.yourfitness.common.ui.utils.setCompoundDrawables

@AndroidEntryPoint
class ConfirmPhoneFragment : MviFragment<ConfirmPhoneIntent, ConfirmPhoneState, ConfirmPhoneViewModel>() {

    override val binding: FragmentConfirmPhoneBinding by viewBinding()
    override val viewModel: ConfirmPhoneViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar(binding.toolbar.root)
        setupStepIndicator(binding.toolbar.progressStep, viewModel.flow == Flow.SIGN_UP)
        val user = arguments?.get("user") as User? ?: User()
        binding.textWeSentCodeTo.text = getString(R.string.sign_up_we_sent_the_code_to, user.phone).toHtmlSpanned()
        binding.editCode.doOnTextChanged { text, _, _, _ -> onCodeChanged(text ?: "") }
        binding.textResendCode.setOnClickListener { resendCode() }
        binding.codeSecondOption.setOnClickListener { resendCodeAnotherOption() }

        viewModel.resendTimer.observe(viewLifecycleOwner) { seconds ->
            if (seconds == null) return@observe
            binding.textResendCode.text = getString(
                if (viewModel.activeOtp == OtpSender.SMS) R.string.sign_up_resend_code_timer
                else R.string.sign_up_resend_code_whatsapp_timer,
                seconds
            )
        }
    }

    private fun onCodeChanged(code: CharSequence) {
        if (code.length == 1) { showPinError("") }
        if (code.length == 6) {
            binding.editCode.isEnabled = false
            submitCode()
        }
    }

    private fun submitCode() {
        val code = binding.editCode.text.toString()
        binding.editCode.setText("")
        viewModel.intent.postValue(ConfirmPhoneIntent.SubmitCode(code))
    }

    private fun resendCode() {
        viewModel.intent.postValue(ConfirmPhoneIntent.ResendCode)
    }

    private fun resendCodeAnotherOption() {
        viewModel.intent.postValue(ConfirmPhoneIntent.ResendCodeAnotherOption)
    }

    override fun renderState(state: ConfirmPhoneState) {
        when (state) {
            is ConfirmPhoneState.Loading -> showLoading(true)
            is ConfirmPhoneState.Success -> showLoading(false)
            is ConfirmPhoneState.OtpSenderUpdated -> {
                if (state.phone != null) showCodeSent(state.phone)
                setupOtpCodeActionFields(state.sender, state.timerActive)
            }
            is ConfirmPhoneState.SubmitError -> showPinError(state.error.message)
            is ConfirmPhoneState.ResendError -> showError(state.error)
            is ConfirmPhoneState.Error -> showError(state.error)
        }
    }

    private fun showCodeSent(phone: String) {
        showLoading(false)
        showMessage(getString(R.string.sign_up_code_was_sent_to_phone, phone))
    }

    private fun setupOtpCodeActionFields(sender: OtpSender, timerActive: Boolean) {
        binding.apply {
            textResendCode.isEnabled = !timerActive
            textResendCode.setTextColorRes(
                if (timerActive) com.yourfitness.common.R.color.gray_light
                else com.yourfitness.common.R.color.main_active
            )
            textResendCode.text = if (timerActive) null else getString(R.string.sign_up_resend_code)
            var secondCodeText = getString(R.string.sign_up_send_code_via)
            if (sender == OtpSender.WHATSAPP) {
                secondCodeText += " ${getString(R.string.sign_up_sms)}"
            }
            codeSecondOption.text = secondCodeText
            codeSecondOption.setCompoundDrawables(end = if (sender == OtpSender.WHATSAPP) 0 else R.drawable.whats_app)
        }
    }

    private fun showPinError(message: String?) {
        showLoading(false)
        val theme = requireActivity().theme
        val errorEmpty = message.isNullOrEmpty()
        val background = when {
            errorEmpty -> ResourcesCompat.getDrawable(resources, R.drawable.pin_background, theme)
            else -> ResourcesCompat.getDrawable(resources, common.drawable.edit_text_background_error, theme)
        }
        binding.editCode.isEnabled = true
        binding.editCode.setItemBackground(background)
        if (!errorEmpty) binding.editCode.setText("")
        binding.textCodeError.text = message
        binding.textCodeError.isInvisible = errorEmpty
    }
}

internal fun String.toHtmlSpanned(): Spanned {
    return Html.fromHtml(this)
}