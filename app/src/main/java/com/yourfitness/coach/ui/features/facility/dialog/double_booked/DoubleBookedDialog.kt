package com.yourfitness.coach.ui.features.facility.dialog.double_booked

import android.os.Bundle
import android.text.SpannableString
import android.view.View
import androidx.fragment.app.setFragmentResult
import com.yourfitness.coach.R
import com.yourfitness.coach.databinding.DialogDoubleBookedBinding
import com.yourfitness.coach.domain.models.CalendarBookClassData
import com.yourfitness.coach.ui.features.facility.booking_class_calendar.BookingClassCalendarFragment
import com.yourfitness.common.ui.base.BaseBottomSheetDialogFragment
import com.yourfitness.common.ui.mvi.viewBinding
import com.yourfitness.common.ui.utils.setCustomFont
import com.yourfitness.common.R as common

class DoubleBookedDialog : BaseBottomSheetDialogFragment() {

    override val binding: DialogDoubleBookedBinding by viewBinding()
    private val canReschedule by lazy {
        requireArguments().getBoolean("can_reschedule", false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar(
            binding.toolbar.root,
            getString(R.string.book_class_screen_double_booked_title),
            dismissId = R.id.close
        )
        setupData()
        binding.btnConfirmReschedule.setOnClickListener {
            onConfirmClick()
        }
    }

    private fun setupData() {
        val data = requireArguments().get("data") as? CalendarBookClassData ?: return
        val text = if (canReschedule) {
            getString(
                R.string.book_class_screen_double_booked_warning_with_conflict_format,
                data.conflictClassName,
                data.className
            )
        } else {
            getString(
                R.string.book_class_screen_double_booked_warning_format, data.conflictClassName
            )
        }
        binding.tvMessage.text = SpannableString(text).apply {
            val startConflictName = text.indexOf(data.conflictClassName)
            val endConflictName = startConflictName + data.conflictClassName.length
            setCustomFont(requireContext(), startConflictName, endConflictName, common.font.tajawal_bold)
            if (canReschedule) {
                val startClassName = text.indexOf(data.className)
                val endClassName = startClassName + data.className.length
                setCustomFont(requireContext(), startClassName, endClassName, common.font.tajawal_bold)
            }
        }
        binding.btnConfirmReschedule.setText(if (canReschedule) R.string.book_class_screen_btn_yes_reschedule_me else common.string.got_it)
    }

    private fun onConfirmClick() {
        dismiss()
        if (canReschedule) {
            setFragmentResult(
                BookingClassCalendarFragment.REQUEST_KEY_RESCHEDULE_CONFIRMATION, requireArguments()
            )
        }
    }
}