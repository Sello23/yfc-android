package com.yourfitness.coach.ui.features.facility.dialog.confirm_booking_class

 import android.os.Bundle
 import android.text.SpannableString
 import android.view.View
 import androidx.fragment.app.setFragmentResult
 import com.bumptech.glide.Glide
 import com.yourfitness.coach.R
 import com.yourfitness.coach.databinding.DialogConfirmBookingClassBinding
 import com.yourfitness.coach.domain.models.CalendarBookClassData
 import com.yourfitness.common.ui.utils.toImageUri
 import com.yourfitness.coach.ui.features.facility.booking_class_calendar.BookingClassCalendarFragment
 import com.yourfitness.common.domain.date.toDateDayOfWeekMonth
 import com.yourfitness.common.domain.date.toDateTime
 import com.yourfitness.common.ui.utils.getColorCompat
 import com.yourfitness.common.ui.base.BaseBottomSheetDialogFragment
 import com.yourfitness.common.ui.mvi.viewBinding
 import com.yourfitness.common.ui.utils.setColor
 import com.yourfitness.common.R as common

class ConfirmBookingClassDialog : BaseBottomSheetDialogFragment() {

    override val binding: DialogConfirmBookingClassBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar(
            toolbar = binding.toolbar.root,
            title = getString(R.string.book_class_screen_title),
            dismissId = R.id.close
        )
        setupInfo()
        binding.btnConfirm.setOnClickListener {
            dismiss()
            setFragmentResult(BookingClassCalendarFragment.REQUEST_KEY_BOOKING_CONFIRMATION, requireArguments())
        }
    }

    private fun setupInfo() {
        val data = requireArguments().get("data") as? CalendarBookClassData ?: return
        with(binding) {
            tvBookingName.text = data.className
            tvBookingTime.text = data.time.toDateTime()
            tvCoach.text = data.coachName
            tvDate.text = data.date.toDateDayOfWeekMonth()
            tvAddress.text = data.address
            tvActivityType.text = data.facilityName
            tvDebitedCredits.text = buildDebitedCreditsText(data.credits)
            Glide.with(binding.root).load(data.icon.toImageUri()).into(binding.ivLogo)
        }
    }

    private fun buildDebitedCreditsText(credits: Int): SpannableString {
        val creditsText = getString(R.string.credits_screen_credits_format, credits)
        val text = getString(R.string.book_class_screen_credits_will_be_debited_format, creditsText)
        return SpannableString(text).apply {
            val start = text.indexOf(creditsText)
            val color = requireContext().getColorCompat(common.color.blue)
            setColor(color, start, start + creditsText.length)
        }
    }
}