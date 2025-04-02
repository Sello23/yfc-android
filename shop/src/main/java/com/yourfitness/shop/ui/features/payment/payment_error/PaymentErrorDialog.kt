package com.yourfitness.shop.ui.features.payment.payment_error

import android.os.Bundle
import android.view.View
import com.yourfitness.common.databinding.DialogTwoActionBinding
import com.yourfitness.common.ui.base.BaseBottomSheetDialogFragment
import com.yourfitness.common.ui.mvi.viewBinding
import com.yourfitness.common.ui.utils.setupBottomInsets
import com.yourfitness.shop.ui.constants.Constants
import com.yourfitness.shop.ui.features.orders.history.OrderClassification
import com.yourfitness.shop.ui.navigation.ShopNavigation
import com.yourfitness.shop.ui.navigation.ShopNavigator
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.yourfitness.common.R as common

@AndroidEntryPoint
class PaymentErrorDialog : BaseBottomSheetDialogFragment() {

    override val binding: DialogTwoActionBinding by viewBinding()

    @Inject
    lateinit var navigator: ShopNavigator

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupBottomInsets(binding.root)
        setupToolbar(
            binding.toolbar.root,
            getString(common.string.error_something_went_wrong),
            dismissId = common.id.close
        )
        binding.buttonRetry.setOnClickListener { dismiss() }

        val popAmount = requireArguments().getInt("popAmount")
        val orderClassification = requireArguments().get(Constants.ORDER_CLASSIFICATION) as OrderClassification
        binding.buttonBack.setOnClickListener {
            navigator.navigate(
                ShopNavigation.BackToShopping(
                    false,
                    popAmount,
                    orderClassification
                )
            )
        }
    }
}