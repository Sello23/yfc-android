package com.yourfitness.shop.ui.features.checkout.address

import android.os.Bundle
import android.util.Patterns
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import com.google.android.material.textfield.TextInputEditText
import com.yourfitness.common.domain.validation.Validation
import com.yourfitness.common.ui.mvi.MviFragment
import com.yourfitness.common.ui.mvi.viewBinding
import com.yourfitness.shop.R
import com.yourfitness.shop.data.entity.AddressEntity
import com.yourfitness.shop.databinding.FragmentDeliveryAddressBinding
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import com.yourfitness.common.R as common

@AndroidEntryPoint
class DeliveryAddressFragment : MviFragment<Any, Any, DeliveryAddressViewModel>() {

    override val binding: FragmentDeliveryAddressBinding by viewBinding()
    override val viewModel: DeliveryAddressViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar(binding.toolbar.root)
        setupToolbar()
        setupFields()
    }

    private fun setupToolbar() {
        binding.toolbar.toolbar.title = getString(R.string.checkout)
    }

    private fun setupFields() {
        binding.buttonAction.text = getString(R.string.continue_text)

        binding.fieldCity.apply {
            textLabel.setText(R.string.city_town)
            editValue.apply {
                id = R.id.fieldCityId
                inputType = EditorInfo.TYPE_TEXT_FLAG_CAP_WORDS
                setOnDoneListener()
                addTextChangedListener { validateField(this, forceClearStart = true) }
                nextFocusDownId = binding.fieldStreet.editValue.id
                requestFocus()
            }
        }

        binding.fieldStreet.apply {
            textLabel.setText(R.string.street)
            editValue.apply {
                id = R.id.fieldStreetId
                inputType = EditorInfo.TYPE_TEXT_FLAG_CAP_WORDS
                setOnDoneListener()
                addTextChangedListener { validateField(this, forceClearStart = true) }
                nextFocusDownId = binding.fieldAddressDetails.editValue.id
            }
        }

        binding.fieldAddressDetails.apply {
            textLabel.setText(R.string.address_details)
            editValue.apply {
                id = R.id.fieldFloorId
                inputType = EditorInfo.TYPE_CLASS_TEXT
                hint = getString(R.string.address_details_hint)
                setOnDoneListener()
                addTextChangedListener { validateField(this, forceClearStart = true) }
                nextFocusDownId = binding.fieldName.editValue.id
            }
        }

        binding.fieldName.apply {
            textLabel.setText(R.string.full_name)
            editValue.apply {
                id = R.id.fieldNameId
                inputType = EditorInfo.TYPE_TEXT_VARIATION_PERSON_NAME
                setOnDoneListener()
                nextFocusDownId = binding.fieldEmail.editValue.id
            }
        }

        binding.fieldEmail.apply {
            textLabel.setText(common.string.email)
            editValue.apply {
                id = R.id.fieldEmailId
                inputType = EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
                setOnDoneListener { email -> Validation.isValidEmail(email.toString()) }
                addTextChangedListener { validateField(this, forceClearStart = true) }
                nextFocusDownId = binding.fieldNumber.editValue.id
            }
        }

        binding.fieldNumber.apply {
            textLabel.setText(R.string.phone_number)
            editValue.apply {
                id = R.id.fieldNumberId
                inputType = EditorInfo.TYPE_CLASS_PHONE
                setOnDoneListener { phone -> Patterns.PHONE.matcher(phone).matches() }
                addTextChangedListener { validateField(this, forceClearStart = true) }
                imeOptions = EditorInfo.IME_ACTION_DONE
            }
        }

        binding.buttonAction.setOnClickListener {
            if (validateForm()) {
                viewModel.intent.value = DeliveryAddressIntent.Save(
                    saveAddress = binding.saveAddress.isChecked,
                    city = binding.fieldCity.editValue.text.toString().trim(),
                    street = binding.fieldStreet.editValue.text.toString().trim(),
                    addressDetails = binding.fieldAddressDetails.editValue.text.toString().trim(),
                    fullName= binding.fieldName.editValue.text.toString().trim(),
                    email = binding.fieldEmail.editValue.text.toString().trim(),
                    phoneNumber = binding.fieldNumber.editValue.text.toString().trim(),
                )
            }
        }
    }

    override fun renderState(state: Any) {
        when (state) {
            is DeliveryAddressState.AddressLoaded -> {
                showSavedAddress(state.address)
                showContactInfo(state)
            }
            is DeliveryAddressState.Error -> showError(state.error)
        }
    }

    private fun showSavedAddress(address: AddressEntity) {
        binding.fieldCity.editValue.setText(address.city, TextView.BufferType.EDITABLE)
        binding.fieldStreet.editValue.setText(address.street, TextView.BufferType.EDITABLE)
        binding.fieldAddressDetails.editValue.setText(address.details, TextView.BufferType.EDITABLE)
    }

    private fun showContactInfo(state: DeliveryAddressState.AddressLoaded) {
        binding.fieldName.editValue.setText(state.fullName, TextView.BufferType.EDITABLE)
        binding.fieldEmail.editValue.setText(state.email, TextView.BufferType.EDITABLE)
        binding.fieldNumber.editValue.setText(state.phone, TextView.BufferType.EDITABLE)
    }

    private fun validateForm(): Boolean {
        val isPhoneValid = validateField(binding.fieldNumber.editValue, focusIfNeeded = true,
            additionalValidation = { phone -> Patterns.PHONE.matcher(phone).matches() })
        val isEmailValid = validateField(
            binding.fieldEmail.editValue,
            focusIfNeeded = true,
            additionalValidation = { email -> Validation.isValidEmail(email.toString()) })
        val isNameValid = validateField(binding.fieldName.editValue, focusIfNeeded = true)
        val isStreetValid = validateField(binding.fieldStreet.editValue, focusIfNeeded = true)
        val isCityValid = validateField(binding.fieldCity.editValue, focusIfNeeded = true)

        return isPhoneValid && isEmailValid && isNameValid && isStreetValid && isCityValid
    }

    private fun validateField(
        editText: TextInputEditText,
        focusIfNeeded: Boolean = false,
        forceClearStart: Boolean = false,
        additionalValidation: (text: CharSequence) -> Boolean = {true}
    ): Boolean {
        val isClearStart = !Objects.equals(
            editText.background?.constantState,
            ContextCompat.getDrawable(
                requireContext(),
                R.drawable.edit_text_background_delivety_error
            )?.constantState
        )
        if (isClearStart && forceClearStart) return true

        val text = editText.text?.trim()
        return if (text?.isBlank() == true || !additionalValidation(text ?: "")) {

            showFieldError(false, editText)
            if (focusIfNeeded) {
                editText.requestFocus()
            }
            false
        } else {
            showFieldError(true, editText)
            true
        }
    }

    private fun showFieldError(isValid: Boolean, editText: TextInputEditText) {
        val background =
            if (isValid) common.color.white_background else R.drawable.edit_text_background_delivety_error
        editText.setBackgroundResource(background)
    }

    private fun TextInputEditText.setOnDoneListener(additionalValidation: (text: CharSequence) -> Boolean = {true}) {
        setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                validateField(this, true, additionalValidation = additionalValidation)
            }
            false
        }
    }
}
