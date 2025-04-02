package com.yourfitness.coach.ui.features.payments.credits_not_enough

import android.os.Bundle
import android.view.View
import com.yourfitness.coach.R
import com.yourfitness.coach.databinding.DialogCreditsNotEnoughBinding
import com.yourfitness.coach.domain.models.CalendarBookClassData
import com.yourfitness.coach.domain.models.PaymentFlow
import com.yourfitness.common.ui.base.BaseBottomSheetDialogFragment
import com.yourfitness.common.ui.mvi.viewBinding
import com.yourfitness.coach.ui.navigation.Navigation
import com.yourfitness.coach.ui.navigation.Navigator
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CreditsNotEnoughDialog : BaseBottomSheetDialogFragment() {

    override val binding: DialogCreditsNotEnoughBinding by viewBinding()

    @Inject
    lateinit var navigator: Navigator

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar(
            binding.toolbar.root,
            getString(R.string.credits_not_enough_screen_title),
            dismissId = R.id.close
        )
        binding.btnBuyCredits.setOnClickListener {
            dismiss()
            navigator.navigate(
                Navigation.BuyCredits(
                    data = requireArguments().get("confirm_booking_data") as? CalendarBookClassData,
                    flow = PaymentFlow.BUY_CREDITS_FROM_SCHEDULE
                )
            )
        }
    }
}