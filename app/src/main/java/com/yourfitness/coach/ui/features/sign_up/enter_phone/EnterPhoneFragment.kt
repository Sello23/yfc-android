package com.yourfitness.coach.ui.features.sign_up.enter_phone

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isInvisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import com.mukesh.countrypicker.Country
import com.mukesh.countrypicker.CountryPicker
import com.yourfitness.coach.R
import com.yourfitness.coach.databinding.FragmentEnterPhoneBinding
import com.yourfitness.common.domain.validation.PHONE_MASKS
import com.yourfitness.coach.ui.features.sign_up.setupStepIndicator
import com.yourfitness.common.ui.mvi.MviFragment
import com.yourfitness.common.ui.mvi.viewBinding
import com.yourfitness.coach.ui.utils.MaskWatcher
import com.yourfitness.common.ui.utils.hideKeyboard
import dagger.hilt.android.AndroidEntryPoint
import com.yourfitness.common.R as common

enum class Flow {
    SIGN_UP, SIGN_IN
}

@AndroidEntryPoint
class EnterPhoneFragment : MviFragment<EnterPhoneIntent, Any, EnterPhoneViewModel>() {

    override val binding: FragmentEnterPhoneBinding by viewBinding()
    override val viewModel: EnterPhoneViewModel by viewModels()

    private var maskWatcher: MaskWatcher? = null
    private var region: String = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar(binding.toolbar.root)
        setupStepIndicator(binding.toolbar.progressStep, viewModel.flow == Flow.SIGN_UP)
        binding.editCountryCode.setOnClickListener { showCountryPicker() }
        binding.editPhone.doOnTextChanged { _, _, _, _ -> validateInput() }
        binding.buttonNext.setOnClickListener { onNextClicked() }
        showCountryCode(viewModel.country)
        showNextButton(viewModel.flow)
        validateInput()
    }

    private fun showCountryPicker() {
        val dialog = CountryPicker.Builder()
            .sortBy(CountryPicker.SORT_BY_NAME)
            .listener { country -> showCountryCode(country) }
            .build()
        dialog.showBottomSheet(activity as AppCompatActivity)
    }

    private fun validateInput() {
        showPhoneError(null)
        val phone = binding.editPhone.text.toString()
        val maskLength = maskWatcher?.mask?.length ?: phone.length
        val isValid = phone.isNotEmpty() && phone.length == maskLength
        binding.buttonNext.isEnabled = isValid
    }

    private fun onNextClicked() {
        val countryCode = binding.editCountryCode.text.toString()
        val phone = binding.editPhone.text.toString()
        viewModel.intent.postValue(EnterPhoneIntent.Next(countryCode, phone, region))
    }

    override fun renderState(state: Any) {
        when (state) {
            is EnterPhoneState.Initial -> showLoading(false)
            is EnterPhoneState.Loading -> {
                showLoading(true)
                hideKeyboard()
            }
            is EnterPhoneState.Error -> showError(state.error)
            is EnterPhoneState.PhoneError -> showPhoneError(state.error.message ?: "")
        }
    }

    private fun showNextButton(flow: Flow) {
        if (flow == Flow.SIGN_IN) binding.buttonNext.setText(R.string.log_in)
    }

    private fun showCountryCode(country: Country) {
        val flag = ResourcesCompat.getDrawable(resources, country.flag, requireActivity().theme)
        flag?.setBounds(0, 0, flag.intrinsicWidth.div(2), flag.intrinsicHeight / 2)
        binding.editCountryCode.setCompoundDrawablesRelative(flag, null, null, null)
        binding.editCountryCode.text = country.dialCode.replace("-", " ")
        region = country.code
        setupInputMask(country.code)
    }

    private fun showPhoneError(message: String?) {
        showLoading(false)
        val isEmpty = message.isNullOrEmpty()
        val background =
            if (isEmpty) common.drawable.edit_text_background else common.drawable.edit_text_background_error
        binding.editPhone.setBackgroundResource(background)
        binding.textPhoneError.text = message
        binding.textPhoneError.isInvisible = isEmpty
    }

    private fun setupInputMask(country: String) {
        val mask = PHONE_MASKS[country]
        binding.editPhone.removeTextChangedListener(maskWatcher)
        binding.editPhone.setText("")
        maskWatcher = null
        if (mask != null) {
            maskWatcher = MaskWatcher(mask)
            binding.editPhone.addTextChangedListener(maskWatcher)
        }
    }
}