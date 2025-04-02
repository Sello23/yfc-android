package com.yourfitness.coach.ui.features.facility.dialog.confirm_booking_result

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.os.Bundle
import android.text.SpannableString
import android.view.View
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import com.yourfitness.coach.R
import com.yourfitness.coach.databinding.DialogConfirmBookingResultBinding
import com.yourfitness.coach.domain.models.BookingResultData
import com.yourfitness.coach.ui.navigation.Navigation
import com.yourfitness.coach.ui.navigation.Navigator
import com.yourfitness.common.domain.date.toDateDayOfWeekMonth
import com.yourfitness.common.domain.date.toDateTime
import com.yourfitness.common.ui.base.BaseBottomSheetDialogFragment
import com.yourfitness.common.ui.mvi.viewBinding
import com.yourfitness.common.ui.utils.setCustomFont
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.yourfitness.common.R as common

@AndroidEntryPoint
class ConfirmBookingResultDialog : BaseBottomSheetDialogFragment() {

    override val binding: DialogConfirmBookingResultBinding by viewBinding()

    @Inject
    lateinit var navigator: Navigator

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupData()
        binding.btnClose.setOnClickListener { navigateToCalendar() }
        binding.btnGotIt.setOnClickListener { navigateToCalendar() }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        navigateToCalendar()
    }

    private fun navigateToCalendar() {
        navigator.navigate(Navigation.BackToClassCalendar)
    }

    @SuppressLint("SetTextI18n")
    private fun setupData() {
        val data = requireArguments().get("data") as? BookingResultData ?: return
        if (data.isRebook) {
            setupRebookData(data)
        } else {
            setupBookData(data)
        }
        with(binding) {
            tvBoughtCredits.isVisible = data.purchasedCredits != null
            tvBoughtCredits.text = getString(R.string.credits, data.purchasedCredits ?: 0)
            tvTotalCredits.text = if (data.isRebook) getString(R.string.credits, data.credits) else "${getString(R.string.credits, data.credits)} ・ "
            tvBonus.text = getString(R.string.bonus_credits, data.bonusesCredits)
            tvBonus.isVisible = !data.isRebook
        }
    }

    private fun setupRebookData(data: BookingResultData) {
        with(binding) {
            tvTitle.setText(R.string.book_class_result_screen_title_booking_is_your_again)
            tvMessage.text = buildMessage(R.string.book_class_result_screen_title_booking_is_your_again_description, data)
            btnGotIt.setText(R.string.thanks)
        }
    }

    private fun setupBookData(data: BookingResultData) {
        with(binding) {
            val isCreditsPurchased = data.purchasedCredits != null
            tvTitle.setText(if (isCreditsPurchased) R.string.book_class_result_screen_title_boom_you_are_loaded_with else R.string.book_class_result_screen_title_happy_days)
            tvMessage.text = buildMessage(if (isCreditsPurchased) R.string.book_class_result_screen_message else R.string.book_class_result_screen_message_congratulations, data)
            btnGotIt.setText(if (isCreditsPurchased) R.string.thanks else common.string.got_it)
        }
    }

    private fun buildMessage(@StringRes resId: Int, data: BookingResultData): SpannableString {
        val userName = data.userName
        val date = data.date.toDateDayOfWeekMonth()
        val time = data.time.toDateTime()
        val text = getString(resId, userName, date, time)
        return SpannableString(text).apply {
            val startDate = text.indexOf(date)
            setCustomFont(requireContext(), startDate, startDate + date.length, common.font.tajawal_bold)
            val startTime = text.indexOf(time)
            setCustomFont(requireContext(), startTime, startTime + time.length, common.font.tajawal_bold)
        }
    }
}