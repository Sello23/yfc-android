package com.yourfitness.shop.ui.features.payment.payment_options.services

import androidx.fragment.app.*
import com.yourfitness.shop.ui.features.payment.payment_options.PaymentOptionsFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ServicesPaymentOptionsFragment : PaymentOptionsFragment() {

    override val viewModel: ServicesPaymentOptionsViewModel by viewModels()

}
