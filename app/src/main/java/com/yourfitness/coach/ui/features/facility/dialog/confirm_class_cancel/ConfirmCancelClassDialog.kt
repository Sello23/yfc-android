package com.yourfitness.coach.ui.features.facility.dialog.confirm_class_cancel

import android.os.Bundle
import android.text.SpannableString
import android.view.View
import androidx.fragment.app.setFragmentResult
import com.bumptech.glide.Glide
import com.yourfitness.coach.R
import com.yourfitness.coach.databinding.DialogConfirmCancelClassBinding
import com.yourfitness.coach.domain.models.CalendarBookClassData
import com.yourfitness.coach.ui.features.facility.class_operations.ClassOperationsFragment
import com.yourfitness.common.domain.date.toDateDayOfWeekMonth
import com.yourfitness.common.domain.date.toDateTime
import com.yourfitness.common.ui.base.BaseBottomSheetDialogFragment
import com.yourfitness.common.ui.mvi.viewBinding
import com.yourfitness.common.ui.utils.setColor
import com.yourfitness.common.ui.utils.setCompoundDrawables
import com.yourfitness.common.ui.utils.toImageUri

class ConfirmCancelClassDialog : BaseBottomSheetDialogFragment() {

    override val binding: DialogConfirmCancelClassBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar(binding.toolbar.root, "", dismissId = R.id.close)
        setupInfo()
        binding.buttonCancel.setOnClickListener {
            dismiss()
            setFragmentResult(
                ClassOperationsFragment.REQUEST_KEY_CANCEL_CONFIRMATION,
                requireArguments()
            )
        }
    }

    private fun setupInfo() {
        val data = requireArguments().get("data") as? CalendarBookClassData ?: return
        with(binding.card) {
            title.text = data.className
            time.text = data.time.toDateTime()
            subtitle.text = data.coachName
            date.text = data.date.toDateDayOfWeekMonth()
            address.text = data.address
            info.text = data.facilityName
            actionLabel.text = getString(R.string.book_class_screen_refunded_info)
                .buildSpannedText(getString(R.string.credits, data.credits))
            actionLabel.setCompoundDrawables(start = com.yourfitness.common.R.drawable.ic_profile_credits)
            Glide.with(requireContext()).load(data.icon.toImageUri()).into(imageIcon)
        }
    }

    private fun String.buildSpannedText(span: String): SpannableString {
        return SpannableString(this).apply {
            val spanStart = indexOf(span)
            setColor(com.yourfitness.common.R.color.main_active, spanStart, spanStart + span.length)
        }
    }
}