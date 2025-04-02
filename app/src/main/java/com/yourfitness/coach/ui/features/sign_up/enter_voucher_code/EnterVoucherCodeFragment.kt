package com.yourfitness.coach.ui.features.sign_up.enter_voucher_code

import android.os.Bundle
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.view.View
import androidx.activity.addCallback
import androidx.core.view.isInvisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.clearFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import com.yourfitness.coach.databinding.FragmentEnterCorporateCodeBinding
import com.yourfitness.coach.domain.referral.CodeErrorType
import com.yourfitness.coach.ui.features.sign_up.setupStepIndicator
import com.yourfitness.common.R
import com.yourfitness.common.ui.mvi.MviFragment
import com.yourfitness.common.ui.mvi.viewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EnterVoucherCodeFragment : MviFragment<EnterCorporateCodeIntent, EnterCorporateCodeState, EnterCorporateCodeViewModel>() {

    override val binding: FragmentEnterCorporateCodeBinding by viewBinding()
    override val viewModel: EnterCorporateCodeViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setFragmentResultListeners()
        setupStepIndicator(binding.toolbar.progressStep)
        binding.buttonNext.setOnClickListener { onNextClicked() }
        binding.buttonSkip.setOnClickListener { viewModel.intent.postValue(EnterCorporateCodeIntent.Skip) }
        binding.enterCode.apply {
            filters += arrayOf<InputFilter>(
                LengthFilter(100),
                InputFilter.AllCaps()
            )
            doOnTextChanged { _, _, _, _ -> showCodeTypeError(null) }
        }
        setFragmentResultListener()

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            viewModel.intent.postValue(EnterCorporateCodeIntent.Skip)
        }
    }

    private fun onNextClicked() {
        val code = binding.enterCode.text.toString().trim().uppercase()
        val intent = EnterCorporateCodeIntent.Next(code)
        viewModel.intent.postValue(intent)
    }

    private fun setFragmentResultListeners() {
        setFragmentResultListener(ERROR_DIALOG_RETRY) { _, _ ->
            binding.enterCode.text?.clear()
            clearFragmentResult(ERROR_DIALOG_RETRY)
        }
    }

    override fun renderState(state: EnterCorporateCodeState) {
        when (state) {
            is EnterCorporateCodeState.Loading -> showLoading(true)
            is EnterCorporateCodeState.Success -> showLoading(false)
            is EnterCorporateCodeState.SubmitError -> showCodeTypeError(state.codeType)
            is EnterCorporateCodeState.Error -> showError(state.error)
        }
    }

    private fun showCodeTypeError(codeType: String? = null) {
        showLoading(false)
        val isEmpty = codeType.isNullOrEmpty()
        val background =
            if (isEmpty) R.drawable.edit_text_background else R.drawable.edit_text_background_error
        binding.enterCode.setBackgroundResource(background)
        binding.textCodeTypeError.isInvisible = isEmpty
        binding.textCodeTypeError.text = getString(getCodeErrorMessage(codeType))
    }

    private fun getCodeErrorMessage(codeType: String?): Int {
        return when (codeType) {
            CodeErrorType.REFERRAL.value -> com.yourfitness.coach.R.string.sign_up_error_referral_code_title
            CodeErrorType.CHALLENGE.value -> com.yourfitness.coach.R.string.sign_up_error_challenge_code_title
            else -> com.yourfitness.coach.R.string.sign_up_invalid_corporate_code
        }
    }

    private fun setFragmentResultListener() {
        setFragmentResultListener(VoucherCodeStatusDialog.RETRY_OPTION) { _, bundle ->
            val retry = bundle.getBoolean(VoucherCodeStatusDialog.RETRY_OPTION)
            if (retry) {
                binding.enterCode.text?.clear()
            }
            clearFragmentResult(VoucherCodeStatusDialog.RETRY_OPTION)
            setFragmentResultListener()
        }
    }

    companion object {
        const val ERROR_DIALOG_RETRY = "error_dialog_retry"
    }
}