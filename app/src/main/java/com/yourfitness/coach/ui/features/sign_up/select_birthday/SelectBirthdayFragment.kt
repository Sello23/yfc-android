package com.yourfitness.coach.ui.features.sign_up.select_birthday

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.yourfitness.coach.R
import com.yourfitness.coach.databinding.FragmentSelectBirthdayBinding
import com.yourfitness.coach.domain.date.formatDate
import com.yourfitness.coach.ui.features.sign_up.setupStepIndicator
import com.yourfitness.common.domain.date.minusYears
import com.yourfitness.common.domain.date.toDate
import com.yourfitness.common.domain.date.today
import com.yourfitness.common.ui.mvi.MviFragment
import com.yourfitness.common.ui.mvi.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import java.util.Date

@AndroidEntryPoint
class SelectBirthdayFragment : MviFragment<Any, Any, SelectBirthdayViewModel>() {

    override val binding: FragmentSelectBirthdayBinding by viewBinding()
    override val viewModel: SelectBirthdayViewModel by viewModels()

    private var datePicker: MaterialDatePicker<Long>? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupStepIndicator(binding.toolbar.progressStep)
        binding.editBirthday.root.setOnClickListener { showDatePicker() }
        binding.buttonNext.setOnClickListener { onNextClicked() }
        showDate(defaultBirthday())
    }

    private fun onNextClicked() {
        val birthday = datePicker?.selection.toDate() ?: defaultBirthday()
        viewModel.intent.postValue(SelectBirthdayIntent.Next(birthday))
    }

    private fun defaultBirthday() = today().minusYears(-18)

    private fun showDatePicker() {
        val end = today().time
        val constraints = CalendarConstraints.Builder()
            .setValidator(DateValidatorPointBackward.before(end))
            .setEnd(end)
            .build()
        datePicker = MaterialDatePicker.Builder.datePicker()
            .setCalendarConstraints(constraints)
            .setSelection(datePicker?.selection ?: defaultBirthday().time)
            .build()
            .apply { addOnPositiveButtonClickListener { showDate(selection.toDate()) } }
        datePicker?.show(requireActivity().supportFragmentManager, "date_picker")
    }

    private fun showDate(date: Date?) {
        binding.editBirthday.textLabel.text = getString(R.string.date_of_birth)
        binding.editBirthday.editValue.text = date.formatDate()
    }
}