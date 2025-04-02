package com.yourfitness.coach.ui.features.payments.success_subscription

import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import com.yourfitness.coach.R
import com.yourfitness.coach.databinding.DialogSuccessPaymentSubscriptionBinding
import com.yourfitness.coach.domain.models.PaymentFlow
import com.yourfitness.common.ui.base.BaseBottomSheetDialogFragment
import com.yourfitness.coach.ui.features.more.story.Stories
import com.yourfitness.coach.ui.features.profile.profile.ProfileFragment
import com.yourfitness.common.ui.mvi.viewBinding
import com.yourfitness.coach.ui.navigation.Navigation
import com.yourfitness.coach.ui.navigation.Navigator
import com.yourfitness.common.domain.ProfileRepository
import com.yourfitness.common.ui.utils.setupBottomInsets
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class SuccessPaymentSubscriptionDialog : BaseBottomSheetDialogFragment() {

    override val binding: DialogSuccessPaymentSubscriptionBinding by viewBinding()

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var profileRepository: ProfileRepository

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.textTitle.text = getString(R.string.success_payment_subscription_title)
        binding.toolbar.btnClose.setOnClickListener { navigate() }
        binding.btnOkay.setOnClickListener { navigate() }
        binding.btnTellMeMore.setOnClickListener {
            dismiss()
            navigator.navigate(Navigation.Profile)
            setFragmentResult(
                ProfileFragment.REQUEST_KET_SHOWING_STORY,
                bundleOf("story" to Stories.HOW_TO_BUY_GYM_SUBSCRIPTION.title)
            )
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        navigate()
        super.onDismiss(dialog)
    }

    private fun navigate() {
        when (requireArguments().get("flow") as? PaymentFlow
            ?: PaymentFlow.BUY_SUBSCRIPTION_FROM_PROFILE) {
            PaymentFlow.BUY_SUBSCRIPTION_FROM_FACILITY -> navigator.navigate(Navigation.BackToFacilityDetails)
            PaymentFlow.BUY_SUBSCRIPTION_FROM_PROFILE -> navigator.navigate(Navigation.Profile)
            PaymentFlow.BUY_SUBSCRIPTION_FROM_PROGRESS, PaymentFlow.BUY_SUBSCRIPTION_FROM_SIGN_UP -> {
                var isPtRole: Boolean
                var isBookable: Boolean
                var accessWorkoutPlans: Boolean


                runBlocking {
                    isPtRole = profileRepository.isPtRole()
                    isBookable = profileRepository.isBookable()
                    accessWorkoutPlans = profileRepository.accessWorkoutPlans()
                }
                navigator.navigate(Navigation.Progress(isPtRole,isBookable,accessWorkoutPlans))
            }
            PaymentFlow.BUY_SUBSCRIPTION_FROM_MAP -> navigator.navigate(Navigation.Map())
            else -> return
        }
    }
}