package com.yourfitness.coach.ui.features.facility.dialog.subscription_error

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import by.kirich1409.viewbindingdelegate.CreateMethod
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.yourfitness.coach.R
import com.yourfitness.coach.databinding.DialogSubscriptionErrorBinding
import com.yourfitness.coach.domain.models.PaymentFlow
import com.yourfitness.coach.ui.navigation.Navigation
import com.yourfitness.common.ui.utils.setupBottomInsets
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SubscriptionErrorDialogFragment : BottomSheetDialogFragment() {

    private val binding: DialogSubscriptionErrorBinding by viewBinding(createMethod = CreateMethod.BIND)
    private val viewModel: SubscriptionErrorViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.dialog_subscription_error, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupBottomInsets(binding.root)
        binding.toolbar.toolbar.title = getString(R.string.map_screen_no_subscription_text)
        binding.toolbar.toolbar.setOnMenuItemClickListener {
            onMenuItemClicked(it)
            return@setOnMenuItemClickListener true
        }
        binding.buttonSignMeUp.setOnClickListener {
            viewModel.navigator.navigate(Navigation.Subscription(PaymentFlow.BUY_SUBSCRIPTION_FROM_PROFILE))
        }
        observeIntent()
    }

    private fun onMenuItemClicked(item: MenuItem) {
        when (item.itemId) {
            R.id.close -> dismiss()
        }
    }

    private fun observeIntent() {
        viewModel.intent.observe(viewLifecycleOwner) {
            if (it != null) {
                viewModel.intent.value = null
                viewModel.handleIntent(it)
            }
        }
    }
}