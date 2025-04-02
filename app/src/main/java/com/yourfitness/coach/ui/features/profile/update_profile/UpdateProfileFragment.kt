package com.yourfitness.coach.ui.features.profile.update_profile

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.annotation.IdRes
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import com.yourfitness.coach.R
import com.yourfitness.coach.databinding.FragmentUpdateProfileBinding
import com.yourfitness.coach.domain.date.formatDate
import com.yourfitness.coach.domain.date.toSeconds
import com.yourfitness.common.domain.date.toDate
import com.yourfitness.common.domain.date.toMilliseconds
import com.yourfitness.common.domain.date.today
import com.yourfitness.common.network.dto.Gender
import com.yourfitness.common.ui.mvi.MviFragment
import com.yourfitness.common.ui.mvi.viewBinding
import com.yourfitness.coach.ui.utils.MaskWatcher
import com.yourfitness.common.ui.utils.trimLine
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import com.yourfitness.common.R as common

private val GENDER_ITEMS = arrayOf("Male", "Female", "Other")

@AndroidEntryPoint
class UpdateProfileFragment : MviFragment<Any, Any, UpdateProfileViewModel>() {

    override val binding: FragmentUpdateProfileBinding by viewBinding()
    override val viewModel: UpdateProfileViewModel by viewModels()

    private var datePicker: MaterialDatePicker<Long>? = null
    private var genderPicker: AlertDialog? = null
    private val instagramMask = MaskWatcher("@#########", "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789+-=,._'<>&", false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar(binding.toolbar.root)
        setupToolbar()
        setupFields()
        showSave(false)
    }

    private fun setupToolbar() {
        setupOptionsMenu(binding.toolbar.toolbar, R.menu.update_profile) { onMenuItemClicked(it) }
        binding.toolbar.toolbar.title = getString(R.string.account)
    }

    private fun setupFields() {
        binding.fieldName.textLabel.setText(R.string.name)
        binding.fieldName.editValue.doOnTextChanged { text, _, _, _ -> onNameChanged(binding.fieldName.editValue, text ?: "") }
        binding.fieldName.editValue.addTextChangedListener { validateInput(binding.fieldName.root.id) }
        binding.fieldSurname.textLabel.setText(R.string.surname)
        binding.fieldSurname.editValue.doOnTextChanged { text, _, _, _ -> onNameChanged(binding.fieldSurname.editValue, text ?: "") }
        binding.fieldSurname.editValue.addTextChangedListener { validateInput(binding.fieldSurname.root.id) }
        binding.fieldEmail.textLabel.setText(common.string.email)
        binding.fieldEmail.editValue.addTextChangedListener { validateInput(binding.fieldEmail.root.id) }
        binding.fieldInstagram.textLabel.setText(R.string.instagram)
        binding.fieldInstagram.editValue.setHint(R.string.username)
        binding.fieldInstagram.editValue.addTextChangedListener(instagramMask)
        binding.fieldInstagram.editValue.addTextChangedListener { validateInput(binding.fieldInstagram.root.id) }
        binding.fieldBirthdate.textLabel.setText(R.string.date_of_birth)
        binding.fieldBirthdate.root.setOnClickListener { showDatePicker() }
        binding.fieldGender.textLabel.setText(R.string.gender)
        binding.fieldGender.root.setOnClickListener { showGenderPicker() }
    }

    private fun onMenuItemClicked(item: MenuItem) {
        when (item.itemId) {
            R.id.save -> saveProfile()
        }
    }

    private fun validateInput(@IdRes fieldId: Int) {
        if (fieldId == binding.fieldEmail.root.id) {
            showEmailError(null)
        }
        val nameChanged = binding.fieldName.editValue.text.toString() != viewModel.profile.name.orEmpty()
        val surnameChanged = binding.fieldSurname.editValue.text.toString() != viewModel.profile.surname.orEmpty()
        val emailChanged = binding.fieldEmail.editValue.text.toString() != viewModel.profile.email.orEmpty()
        val instagramChanged = binding.fieldInstagram.editValue.text.toString() != viewModel.profile.instagram.orEmpty()
        val birthdayChanged = datePicker?.selection != null && datePicker?.selection.toSeconds() != viewModel.profile.birthday
        val genderChanged = genderPicker?.selection != null && genderPicker?.selection != viewModel.profile.gender.formatGender()
        showSave(nameChanged || surnameChanged || emailChanged || instagramChanged || birthdayChanged || genderChanged)
    }

    private fun onNameChanged(inputField: TextInputEditText, name: CharSequence) {
        val updatedName = name.trimLine()
        if (inputField.text.toString() != updatedName.toString()) {
            inputField.setText(updatedName)
            inputField.setSelection(updatedName.length)
        }
    }

    override fun renderState(state: Any) {
        when (state) {
            is UpdateProfileState.Success -> showProfileData(state)
            is UpdateProfileState.Loading -> showLoading(true)
            is UpdateProfileState.Updated -> {
                showLoading(false)
                findNavController().navigateUp()
            }
            is UpdateProfileState.EmailError -> showEmailError(state.error)
            is UpdateProfileState.Error -> showError(state.error)
        }
    }

    private fun showProfileData(state: UpdateProfileState.Success) {
        showLoading(false)
        val profile = state.profile
        binding.fieldName.editValue.setText(profile.name)
        binding.fieldSurname.editValue.setText(profile.surname)
        binding.fieldEmail.editValue.setText(profile.email)
        if (!profile.instagram.isNullOrEmpty()) { binding.fieldInstagram.editValue.setText(profile.instagram) }
        binding.fieldGender.editValue.text = profile.gender.formatGender()
        showDate(profile.birthday?.toMilliseconds()?.toDate())
    }

    private fun saveProfile() {
        val name = binding.fieldName.editValue.text.toString()
        val surname = binding.fieldSurname.editValue.text.toString()
        val email = binding.fieldEmail.editValue.text.toString()
        val instagram = binding.fieldInstagram.editValue.text.toString()
        val birthday = datePicker?.selection.toSeconds()
        val gender = genderPicker?.selection.toGender()
        val intent = UpdateProfileIntent.Save(name, surname, email, instagram, birthday, gender)
        viewModel.intent.postValue(intent)
    }

    private fun showSave(visible: Boolean) {
        binding.toolbar.root.menu.findItem(R.id.save).isVisible = visible
    }

    private fun showDatePicker() {
        val validator = DateValidatorPointBackward.before(today().time)
        val constraints = CalendarConstraints.Builder()
            .setValidator(validator)
            .setEnd(today().time)
            .build()
        datePicker = MaterialDatePicker.Builder.datePicker()
            .setCalendarConstraints(constraints)
            .setSelection(datePicker?.selection ?: viewModel.profile.birthday?.toMilliseconds())
            .build()
            .apply { addOnPositiveButtonClickListener { showDate(selection?.toDate()) } }
        datePicker?.show(requireActivity().supportFragmentManager, "date_picker")
    }

    private fun showDate(date: Date?) {
        binding.fieldBirthdate.editValue.text = date.formatDate()
        validateInput(binding.fieldBirthdate.root.id)
    }

    private fun showGenderPicker() {
        val selectedItem = GENDER_ITEMS.indexOf(genderPicker?.selection ?: viewModel.profile.gender.formatGender())
        genderPicker = AlertDialog.Builder(requireContext())
            .setSingleChoiceItems(GENDER_ITEMS, selectedItem) { _, _ -> showGender(genderPicker?.selection.toGender()) }
            .setPositiveButton(android.R.string.ok) { _, _ ->  }
            .create()
        genderPicker?.show()
    }

    private fun showGender(gender: Gender?) {
        binding.fieldGender.editValue.text = gender.formatGender()
        validateInput(binding.fieldGender.root.id)
    }

    private fun showEmailError(error: Throwable?) {
        showLoading(false)
        val message = error?.message ?: ""
        val isEmpty = message.isEmpty()
        val background = if (isEmpty) common.drawable.background_toolbar else R.drawable.background_error
        binding.fieldEmail.textError.isVisible = !isEmpty
        binding.fieldEmail.textError.text = message
        binding.fieldEmail.root.setBackgroundResource(background)
    }

    private fun Gender?.formatGender(): String {
        val stringRes = when (this) {
            Gender.MALE -> R.string.male
            Gender.FEMALE -> R.string.female
            Gender.OTHER -> R.string.other
            else -> R.string.unknown
        }
        return getString(stringRes)
    }

    private fun CharSequence?.toGender(): Gender? {
        return when (this) {
            getString(R.string.male) -> Gender.MALE
            getString(R.string.female) -> Gender.FEMALE
            getString(R.string.other) -> Gender.OTHER
            else -> null
        }
    }
}

val AlertDialog.selection: CharSequence get() {
    val list = this.listView
    val adapter = list.adapter
    return adapter.getItem(list.checkedItemPosition) as String
}