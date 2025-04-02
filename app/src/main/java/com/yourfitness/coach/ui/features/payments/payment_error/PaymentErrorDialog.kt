package com.yourfitness.coach.ui.features.payments.payment_error

import android.os.Bundle
import android.view.View
import com.yourfitness.coach.databinding.DialogPaymentErrorBinding
import com.yourfitness.coach.ui.navigation.Navigation
import com.yourfitness.coach.ui.navigation.Navigator
import com.yourfitness.common.R
import com.yourfitness.common.ui.base.BaseBottomSheetDialogFragment
import com.yourfitness.common.ui.mvi.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PaymentErrorDialog : BaseBottomSheetDialogFragment() {

    override val binding: DialogPaymentErrorBinding by viewBinding()

    @Inject
    lateinit var navigator: Navigator

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar(
            binding.toolbar.root,
            getString(R.string.payment_method_screen_payment_method_doesnt_work_title),
            dismissId = R.id.close
        )
        binding.btnRetry.setOnClickListener { dismiss() }
        binding.btnBackToMap.setOnClickListener { navigator.navigate(Navigation.Map()) }
    }
}