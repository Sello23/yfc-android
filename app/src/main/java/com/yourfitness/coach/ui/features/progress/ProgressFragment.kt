package com.yourfitness.coach.ui.features.progress

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.yourfitness.coach.R
import com.yourfitness.coach.data.entity.SubscriptionEntity
import com.yourfitness.coach.databinding.FragmentProgressBinding
import com.yourfitness.coach.domain.models.PaymentFlow
import com.yourfitness.coach.domain.models.ProgressActionCard
import com.yourfitness.coach.domain.subscription.isActive
import com.yourfitness.coach.ui.features.bottom_menu.BottomMenuFragment.Companion.CAMERA_PERMISSION_CODE
import com.yourfitness.coach.ui.features.profile.connected_devices.ConnectedDevicesFragment
import com.yourfitness.coach.ui.navigation.Navigation
import com.yourfitness.coach.ui.utils.shareReferralCode
import com.yourfitness.common.domain.analytics.AnalyticsManager
import com.yourfitness.common.domain.analytics.ShopEvents
import com.yourfitness.common.ui.mvi.MviFragment
import com.yourfitness.common.ui.mvi.viewBinding
import com.yourfitness.common.ui.utils.setCompoundDrawables
import com.yourfitness.common.ui.utils.setupTopInsets
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
open class ProgressFragment : MviFragment<ProgressIntent, ProgressState, ProgressViewModel>() {

    override val viewModel: ProgressViewModel by viewModels()
    override val binding: FragmentProgressBinding by viewBinding()
    private val actionCardsAdapter by lazy { ActionCardGridAdapter() }

    @Inject
    lateinit var analyticsManager: AnalyticsManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupDubaiFeature()
        setupRecyclerView()

        setupTopInsets(binding.fitnessInfo.dubaiContainer.root)
        binding.fitnessInfo.dubaiContainer.viewDubaiInfo.dubaiImage.isVisible = true
        binding.fitnessInfo.dubaiContainer.trackProgress.setOnClickListener {
            viewModel.navigator.navigate(Navigation.Dubai30x30Calendar)
        }
        binding.fitnessInfo.dubaiContainer.joinChallenge.setOnClickListener {
            viewModel.navigator.navigate(Navigation.Challenges)
        }
        binding.fitnessInfo.dubaiContainer.viewDubaiInfo.infoTitle.setOnClickListener {
            viewModel.intent.value = ProgressIntent.OpenDubai30x30Details
        }
    }

    override fun onStart() {
        super.onStart()

        setFragmentResultListener(ConnectedDevicesFragment.GOOGLE_FIT_REQUEST_CODE) { _, bundle ->
            val status = bundle.getBoolean(ConnectedDevicesFragment.BOOLEAN)
            if (status) {
                findNavController().navigate(
                    R.id.fragment_progress,
                    arguments,
                    NavOptions.Builder()
                        .setPopUpTo(R.id.fragment_progress, true)
                        .build()
                )
            }
        }
    }

    override fun renderState(state: ProgressState) {
        when (state) {
            is ProgressState.SubscriptionValue -> {
                showBanner(state.subscription)
            }
            is ProgressState.ParamsUpdated -> setupDubaiFeature()
            is ProgressState.SubscriptionError -> showSubscriptionError()
            is ProgressState.VoucherLoaded -> {
                showLoading(false)
                shareReferralCode(state.voucher)
            }
        }
    }

    private fun setupDubaiFeature() {
        val dubaiContentVisible = viewModel.isDubai30x30Active
        val septemberContentVisible =
            viewModel.isIntercorporateStarted && !viewModel.isIntercorporateEnded
        val isActive = dubaiContentVisible || septemberContentVisible
        binding.fitnessInfo.dubaiContainer.root.isVisible = isActive
        if (!isActive) setupTopInsets(binding.fitnessInfo.viewProgressScreenDataChart.root)

        if (!isActive) return

        binding.fitnessInfo.dubaiContainer.apply {
            val bg = ContextCompat.getDrawable(
                requireContext(),
                if (septemberContentVisible) com.yourfitness.common.R.drawable.gradient_september_test
                else com.yourfitness.common.R.drawable.gradient_dubai,
            )
            root.background = bg
            viewDubaiInfo.dubaiImage.setImageResource(
                if (septemberContentVisible) R.drawable.yfc_logo_september_test
                else R.drawable.dubai_logo
            )

            if (septemberContentVisible) {
                viewDubaiInfo.infoTitle.text =
                    getString(R.string.the_september_step_challenge_is_here)
                viewDubaiInfo.infoMsg.text = getString(R.string.test_september_msg)
                trackProgress.isVisible = false
                viewDubaiInfo.infoTitle.setCompoundDrawables(end = null)
                joinChallenge.background = ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.button_background_border_white
                )
            } else if (viewModel.isDubai30x30Preview) {
                viewDubaiInfo.infoTitle.text = getString(R.string.dubai30x30_is_here)
                viewDubaiInfo.infoMsg.text = getString(R.string.dubai3030_info_msg)
                trackProgress.isVisible = false
                joinChallenge.background = ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.button_background_border_white
                )
            } else if (viewModel.isDubai30x30Dates || viewModel.isDubai30x30Ended) {
                viewDubaiInfo.infoTitle.text = getString(R.string.dubai30x30_is_here)
                viewDubaiInfo.infoMsg.text = getString(R.string.dubai3030_info_msg)
                trackProgress.isVisible =
                    viewModel.isDubai30x30Dates && !viewModel.isDubai30x30Ended
                joinChallenge.background = ContextCompat.getDrawable(
                    requireContext(),
                    if (viewModel.isDubai30x30Ended) R.drawable.button_background_border_white else com.yourfitness.common.R.color.transparent
                )
            }
        }
    }

    private fun showSubscriptionError() {
        binding.viewSubscribeBanner.root.isVisible = true
        binding.viewSubscribeBanner.textSubscribe.setOnClickListener {
            viewModel.navigator.navigate(Navigation.Subscription(PaymentFlow.BUY_SUBSCRIPTION_FROM_PROGRESS))
        }
    }

    private fun showBanner(subscription: SubscriptionEntity) {
        if (subscription.autoRenewal || subscription.isActive()) {
            binding.viewSubscribeBanner.root.isVisible = false
        } else {
            showSubscriptionError()
        }
    }

    private fun setupRecyclerView() {
        val listOfActionsCards = setupActionCards()
        val listOfActionsCardsUpdated: List<ProgressActionCard>

        if (viewModel.accessWorkoutPlans()) {
            actionCardsAdapter.setData(listOfActionsCards)
        } else {
            listOfActionsCardsUpdated =
                listOfActionsCards.dropWhile { it.title == getString(R.string.kinestex_screen_text) }
            actionCardsAdapter.setData(listOfActionsCardsUpdated)
        }

        binding.recyclerView.adapter = actionCardsAdapter
        binding.recyclerView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
    }

    private fun setupActionCards(): List<ProgressActionCard> {
        return listOf(
            ProgressActionCard(
                getString(R.string.kinestex_screen_text),
                R.drawable.fina_test,
                ::onThirtyDayChallengeClick
            ),
            ProgressActionCard(
                getString(R.string.shop),
                R.drawable.card_shop,
                ::onShopClick
            ),
            ProgressActionCard(
                getString(R.string.challenges_screen_challenges_text),
                R.drawable.card_challenges,
                ::onChallengesClick
            ),
            ProgressActionCard(
                getString(R.string.my_trainers),
                R.drawable.card_pt,
                ::onMyPtClick,
                true
            ),
            ProgressActionCard(
                getString(R.string.progress_screen_my_friends),
                R.drawable.card_refer_friend,
                ::onMyFriendsClick
            )
        )
    }

    private fun onChallengesClick() = viewModel.navigator.navigate(Navigation.Challenges)

    private fun onThirtyDayChallengeClick() {
        if (checkSystemCameraPermission()) {
            viewModel.navigator.navigate(Navigation.ThirtyDayChallenge)
        } else {
            if (canAskForPermission()) getAppPermissions()
            else showNoPermissionDialog()
        }
    }

    private fun onShopClick() {
        analyticsManager.trackEvent(ShopEvents.open(), true)
        viewModel.intent.value = ProgressIntent.OpenShop
    }

    private fun onMyFriendsClick() {
        viewModel.intent.value = ProgressIntent.OpenCommunityFriends
    }

    private fun onMyPtClick() {
        viewModel.intent.value = ProgressIntent.PersonalTrainerCardClicked
    }

    private fun checkSystemCameraPermission() =
        ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

    private fun getAppPermissions() {
        val permissions = mutableListOf<String>()

        if (!checkSystemCameraPermission()) {
            permissions.add(Manifest.permission.CAMERA)
        }

        if (permissions.isEmpty()) return
    }

    private fun canAskForPermission() =
        ActivityCompat.shouldShowRequestPermissionRationale(
            requireActivity(),
            Manifest.permission.ACCESS_FINE_LOCATION
        )

    private fun showNoPermissionDialog() {
        viewModel.navigator.navigate(Navigation.NoCameraPermission)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {

            CAMERA_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                }
            }

            else -> {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }
        }
    }

}
