package com.yourfitness.pt.ui.features.payments.payment_successful

import android.os.Bundle
import android.text.SpannableString
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.yourfitness.common.CommonPreferencesStorage
import com.yourfitness.common.R
import com.yourfitness.common.domain.ProfileRepository
import com.yourfitness.common.ui.base.BaseBottomSheetDialogFragment
import com.yourfitness.common.ui.mvi.viewBinding
import com.yourfitness.common.ui.utils.setCustomFont
import com.yourfitness.common.ui.utils.setupBottomInsets
import com.yourfitness.pt.databinding.DialogPtPaymentSuccessBinding
import com.yourfitness.pt.domain.values.PT_NAME
import com.yourfitness.pt.domain.values.SESSIONS_AMOUNT
import com.yourfitness.pt.ui.navigation.PtNavigator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PaymentSuccessfulDialog : BaseBottomSheetDialogFragment() {

    override val binding: DialogPtPaymentSuccessBinding by viewBinding()

    @Inject
    lateinit var profileRepository: ProfileRepository

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupBottomInsets(binding.root)
        setupToolbar(
            binding.toolbar.root,
            dismissId = R.id.close
        )

        lifecycleScope.launch {
            val sessionsAmount = requireArguments().getInt(SESSIONS_AMOUNT)
            val ptName = requireArguments().getString(PT_NAME).orEmpty()
            val username = profileRepository.getProfile().name
            binding.toolbar.sessions.textSessions.text = resources.getQuantityString(
                com.yourfitness.pt.R.plurals.sessions_number,
                sessionsAmount,
                sessionsAmount
            )
            binding.successMessage.text = getString(
                com.yourfitness.pt.R.string.pt_success_payment_msg,
                username,
                ptName
            ).buildSpannedText(ptName)
        }

        binding.buttonGoToOrders.setOnClickListener { findNavController().navigateUp() }
    }

    private fun String.buildSpannedText(span: String): SpannableString {
        return SpannableString(this).apply {
            val spanStart = indexOf(span)
            setCustomFont(requireContext(), spanStart, spanStart + span.length, R.font.work_sans_bold)
        }
    }
}
