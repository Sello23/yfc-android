package com.yourfitness.coach.ui.features.profile.profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.yourfitness.coach.BuildConfig
import com.yourfitness.coach.R
import com.yourfitness.coach.YFCApplication
import com.yourfitness.coach.databinding.FragmentProfileBinding
import com.yourfitness.coach.databinding.ItemListBinding
import com.yourfitness.coach.domain.EMPTY_UUID
import com.yourfitness.coach.domain.models.PaymentFlow
import com.yourfitness.coach.ui.features.sign_up.upload_photo.AvatarPicker
import com.yourfitness.coach.ui.navigation.Navigation
import com.yourfitness.common.CommonPreferencesStorage
import com.yourfitness.common.data.entity.fullName
import com.yourfitness.common.ui.mvi.MviFragment
import com.yourfitness.common.ui.mvi.viewBinding
import com.yourfitness.common.ui.utils.formatCoins
import com.yourfitness.common.ui.utils.openGetHelp
import com.yourfitness.common.ui.utils.toImageUri
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.yourfitness.common.R as common

@AndroidEntryPoint
class ProfileFragment : MviFragment<Any, Any, ProfileViewModel>() {

    override val binding: FragmentProfileBinding by viewBinding()
    override val viewModel: ProfileViewModel by viewModels()
    private var referralCode: String? = null
    private val avatarPicker = AvatarPicker(this, ::onAvatarSelected)
    private var coinsToVoucherOwner: String? = null

    @Inject
    lateinit var commonStorage: CommonPreferencesStorage

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.appVersionLabel.text = getString(R.string.app_version_label, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE)
        binding.itemPaymentHistory.root.isVisible = !commonStorage.isPtRole || ( commonStorage.isPtRole && !commonStorage.isBookable)
        binding.itemConnectedDevices.root.isVisible = !commonStorage.isPtRole || ( commonStorage.isPtRole && !commonStorage.isBookable)

        setFragmentResultListener(REQUEST_KET_SHOWING_STORY) { _, bundle ->
            val story = bundle.getString("story").orEmpty()
            viewModel.navigator.navigate(Navigation.Story(story))
        }

        setupToolbar()
        setupAvatarPicker()
        setupItems()
        viewModel.loadProfile()
        setupDebugFunction()
    }

    private fun onSubscriptionUpdated(isActive: Boolean) {
        val view = binding.root.findViewById<View>(R.id.item_subscription)
        val itemBinding = ItemListBinding.bind(view)
        val theme = requireActivity().theme
        itemBinding.itemLabel.apply {
            isVisible = true
            val drawable: Int
            val textId: Int
            if (isActive) {
                drawable = R.drawable.rounded_active
                textId = R.string.subscription_screen_active
            } else {
                drawable = R.drawable.rounded_inactive
                textId = R.string.inactive
            }
            text = getString(textId).uppercase()
            background = ResourcesCompat.getDrawable(resources, drawable, theme)
        }
    }

    private fun setupAvatarPicker() {
        avatarPicker.init(binding.imagePhoto)
    }

    private fun setupToolbar() {
        setupOptionsMenu(binding.toolbar.toolbar, R.menu.profile) { onMenuItemSelected(it) }
        binding.toolbar.toolbar.title = getString(R.string.profile_screen_profile)
    }

    private fun setupItems() {
        val isPtRole = commonStorage.isPtRole && commonStorage.isBookable
        binding.textCoins.isVisible = !isPtRole
        binding.itemSubscription.root.isVisible = !isPtRole
        binding.itemMyReferralCode.root.isVisible = !isPtRole
        val items = if (isPtRole) PT_ITEMS else ITEMS
        for (item in ITEMS) {
            val view = binding.root.findViewById<View>(item.itemId)
            val itemBinding = ItemListBinding.bind(view)
            itemBinding.itemTitle.setText(item.title)
            itemBinding.itemTitle.setCompoundDrawablesRelativeWithIntrinsicBounds(item.icon, 0, 0, 0)
            itemBinding.root.setOnClickListener { onProfileMenuClicked(itemBinding) }
        }
        binding.itemHelpCenter.viewDivider.isVisible = false
    }

    private fun onMenuItemSelected(menuItem: MenuItem) {
        when (menuItem.itemId) {
            R.id.sign_out -> showSignOutDialog()
        }
    }

    private fun onProfileMenuClicked(item: ItemListBinding) {
        when (item.root.id) {
            R.id.item_subscription -> viewModel.navigator.navigate(Navigation.Subscription(PaymentFlow.BUY_SUBSCRIPTION_FROM_PROFILE))
//            R.id.item_buy_credits -> viewModel.navigator.navigate(Navigation.BuyCredits(flow = PaymentFlow.BUY_CREDITS_FROM_PROFILE)) // TODO temporary removed Studios
            R.id.item_shop_orders -> viewModel.navigator.navigate(Navigation.ShopOrderDetails)
            R.id.item_profile_settings -> viewModel.navigator.navigate(Navigation.UpdateProfile)
            R.id.item_connected_devices -> viewModel.navigator.navigate(Navigation.ConnectedDevices)
            R.id.item_help_center -> openGetHelp()
            R.id.item_my_referral_code -> viewModel.navigator.navigate(Navigation.MyReferralCode(referralCode, coinsToVoucherOwner))
            R.id.item_payment_history -> viewModel.navigator.navigate(Navigation.PaymentHistory)
            R.id.item_deletion_request -> viewModel.navigator.navigate(Navigation.DeletionRequest)
            R.id.item_privacy_policy -> openLink("https://yourfitness.coach/privacy-policy")
            R.id.item_terms_and_conditions -> openLink("https://yourfitness.coach/terms-conditions")
        }
    }

    private fun onAvatarSelected(uri: Uri) {
        viewModel.intent.postValue(ProfileIntent.UploadPhoto(uri))
    }

    override fun renderState(state: Any) {
        when (state) {
            is ProfileState.Loading -> showLoading(true)
            is ProfileState.PhotoUploaded -> showPhoto(state.mediaId)
            is ProfileState.Success -> showProfileData(state)
            is ProfileState.Error -> {
                showLoading(false)
                showMessage(state.error.message)
            }
            is ProfileState.FirebaseUploadResult -> {
                Toast.makeText(requireActivity(), if (state.isSuccess) "Success" else "Error", Toast.LENGTH_LONG).show()
            }
            is ProfileState.SavedSubscriptionLoaded -> onSubscriptionUpdated(state.subscriptionActive)
        }
    }

    private fun showProfileData(state: ProfileState.Success) {
        showPhoto(state.profile.mediaId)
        binding.textName.text = state.profile.fullName
        binding.textCoins.text = resources.getQuantityString(common.plurals.profile_screen_format_coins, state.coins.toInt(), state.coins.formatCoins())
//        binding.textCredits.text = getString(R.string.profile_screen_format_credits, state.credits) // TODO temporary removed Studios
        referralCode = if (state.profile.voucher.isNullOrBlank()) {
            "No referral code"
        } else {
            state.profile.voucher
        }
//        binding.textCredits.setOnClickListener { // TODO temporary removed Studios
//            viewModel.navigator.navigate(Navigation.BuyCredits(flow = PaymentFlow.BUY_CREDITS_FROM_PROFILE))
//        }
        coinsToVoucherOwner = state.settingsRegion?.coinsToVoucherOwner
    }

    private fun showPhoto(mediaId: String?) {
        showLoading(false)
        Glide.with(this).load(mediaId?.toImageUri()).into(binding.imagePhoto)
        val photoAdded = mediaId != null && mediaId != EMPTY_UUID
        val photoIcon = if (photoAdded) R.drawable.ic_photo_edit else R.drawable.ic_photo_upload
        binding.imagePhotoIcon.setImageResource(photoIcon)
    }

    private fun showSignOutDialog() {
        viewModel.navigator.navigate(Navigation.SignOut)
    }

    private fun openLink(link: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
        startActivity(Intent.createChooser(intent, getString(R.string.browse_with)))
    }

    private fun setupDebugFunction() {
        binding.toolbar.root.setOnClickListener(object : View.OnClickListener {
            var numberOfTaps = 0
            var lastTapTimeMs: Long = 0

            override fun onClick(v: View?) {
                val tapInterval = System.currentTimeMillis() - lastTapTimeMs
                numberOfTaps = if (tapInterval >= 1000) 1 else numberOfTaps + 1
                lastTapTimeMs = System.currentTimeMillis()

                if (numberOfTaps == 3) {
                    val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
                    builder.setMessage("Do you want to send your steps and calories to developers?")
                        .setPositiveButton("Yes") { dialog, _ ->
                            viewModel.intent.postValue(ProfileIntent.SaveDbToRemoteStorage)
                            dialog.cancel()
                        }
                        .setNegativeButton("No") { dialog, _ -> dialog.cancel() }
                        .create()
                        .show()
                }
            }
        })
    }

    companion object {
        const val REQUEST_KET_SHOWING_STORY = "request_key_showing_story"
    }
}

data class ProfileItem(
    @IdRes val itemId: Int,
    @StringRes val title: Int,
    @DrawableRes val icon: Int
)

val ITEMS = arrayOf(
    ProfileItem(R.id.item_subscription, R.string.profile_screen_subscription, R.drawable.ic_menu_subscription),
//    ProfileItem(R.id.item_buy_credits, R.string.profile_screen_buy_credits, R.drawable.ic_menu_credits), // TODO temporary removed Studios
    ProfileItem(R.id.item_shop_orders, R.string.profile_screen_shop_orders, R.drawable.ic_menu_shop_orders),
    ProfileItem(R.id.item_profile_settings, R.string.profile_screen_profile_settings, R.drawable.ic_menu_profile_settings),
    ProfileItem(R.id.item_payment_history, R.string.profile_screen_payment_history, R.drawable.ic_menu_payment_history),
//    ProfileItem(R.id.item_notification_settings, R.string.profile_screen_notification_settings, R.drawable.ic_menu_notification_settings),
    ProfileItem(R.id.item_connected_devices, R.string.profile_screen_connected_devices, R.drawable.ic_menu_connected_devices),
    ProfileItem(R.id.item_my_referral_code, R.string.profile_screen_my_referral_code, R.drawable.ic_menu_voucher),
    ProfileItem(R.id.item_privacy_policy, R.string.profile_screen_privacy_policy, R.drawable.ic_menu_privacy_policy),
    ProfileItem(R.id.item_terms_and_conditions, R.string.profile_screen_terms_and_condition, R.drawable.ic_menu_privacy_policy),
    ProfileItem(R.id.item_deletion_request, R.string.profile_screen_deletion_request, R.drawable.ic_menu_message),
    ProfileItem(R.id.item_help_center, R.string.profile_screen_help_center, R.drawable.ic_menu_message)
)

val PT_ITEMS = arrayOf(
    ProfileItem(R.id.item_shop_orders, R.string.profile_screen_shop_orders, R.drawable.ic_menu_shop_orders),
    ProfileItem(R.id.item_profile_settings, R.string.profile_screen_profile_settings, R.drawable.ic_menu_profile_settings),
    ProfileItem(R.id.item_payment_history, R.string.profile_screen_payment_history, R.drawable.ic_menu_payment_history),
    ProfileItem(R.id.item_connected_devices, R.string.profile_screen_connected_devices, R.drawable.ic_menu_connected_devices),
    ProfileItem(R.id.item_privacy_policy, R.string.profile_screen_privacy_policy, R.drawable.ic_menu_privacy_policy),
    ProfileItem(R.id.item_terms_and_conditions, R.string.profile_screen_terms_and_condition, R.drawable.ic_menu_privacy_policy),
    ProfileItem(R.id.item_deletion_request, R.string.profile_screen_deletion_request, R.drawable.ic_menu_message),
    ProfileItem(R.id.item_help_center, R.string.profile_screen_help_center, R.drawable.ic_menu_message)
)

interface UpdateListener {
    fun onSubscriptionUpdated(isActive: Boolean)
}