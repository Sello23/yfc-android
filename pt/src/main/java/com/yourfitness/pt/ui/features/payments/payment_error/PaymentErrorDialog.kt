package com.yourfitness.pt.ui.features.payments.payment_error

import android.os.Bundle
import android.view.View
import com.yourfitness.common.R as common
import com.yourfitness.pt.R
import com.yourfitness.common.databinding.DialogTwoActionBinding
import com.yourfitness.common.ui.base.BaseBottomSheetDialogFragment
import com.yourfitness.common.ui.mvi.viewBinding
import com.yourfitness.common.ui.utils.setupBottomInsets
import com.yourfitness.pt.domain.values.POP_AMOUNT
import com.yourfitness.pt.ui.navigation.PtNavigation
import com.yourfitness.pt.ui.navigation.PtNavigator
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PaymentErrorDialog : BaseBottomSheetDialogFragment() {

    override val binding: DialogTwoActionBinding by viewBinding()

    @Inject
    lateinit var navigator: PtNavigator

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupBottomInsets(binding.root)
        setupToolbar(
            binding.toolbar.root,
            getString(common.string.payment_method_screen_payment_method_doesnt_work_title),
            dismissId = common.id.close
        )
        binding.buttonRetry.setOnClickListener { dismiss() }

        binding.buttonBack.text = getString(R.string.back_to_trainers_list)
        val popAmount = requireArguments().getInt(POP_AMOUNT)
        binding.buttonBack.setOnClickListener {
            navigator.navigate(
                PtNavigation.BackToPtList(popAmount)
            )
        }
    }
}