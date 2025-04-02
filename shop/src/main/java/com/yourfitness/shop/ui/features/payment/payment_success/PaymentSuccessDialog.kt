package com.yourfitness.shop.ui.features.payment.payment_success

import android.os.Bundle
import android.view.View
import com.yourfitness.common.ui.base.BaseBottomSheetDialogFragment
import com.yourfitness.common.ui.mvi.viewBinding
import com.yourfitness.common.ui.utils.setupBottomInsets
import com.yourfitness.shop.R
import com.yourfitness.shop.databinding.DialogProductPaymentSuccessBinding
import com.yourfitness.shop.ui.constants.Constants.Companion.ORDER_CLASSIFICATION
import com.yourfitness.shop.ui.features.orders.history.OrderClassification
import com.yourfitness.shop.ui.navigation.ShopNavigation
import com.yourfitness.shop.ui.navigation.ShopNavigator
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.yourfitness.common.R as common

@AndroidEntryPoint
class PaymentSuccessDialog : BaseBottomSheetDialogFragment() {

    override val binding: DialogProductPaymentSuccessBinding by viewBinding()

    @Inject
    lateinit var navigator: ShopNavigator

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupBottomInsets(binding.root)
        setupToolbar(
            binding.toolbar.root,
            getString(R.string.order_placed),
            dismissId = common.id.close
        )

        val orderClassification = requireArguments().get(ORDER_CLASSIFICATION) as OrderClassification
        binding.buttonGoToOrders.setOnClickListener { navigator.navigate(ShopNavigation.OrderHistory(orderClassification)) }
        binding.buttonBack.setOnClickListener { dismiss() }
    }
}
