package com.yourfitness.coach.ui.features.more

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.yourfitness.coach.R
import com.yourfitness.coach.databinding.FragmentMoreBinding
import com.yourfitness.common.CommonPreferencesStorage
import com.yourfitness.common.domain.analytics.AnalyticsManager
import com.yourfitness.common.domain.analytics.ShopEvents
import com.yourfitness.common.ui.mvi.MviFragment
import com.yourfitness.common.ui.mvi.viewBinding
import com.yourfitness.common.ui.utils.openGetHelp
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MoreFragment : MviFragment<MoreIntent, Any, MoreViewModel>() {

    override val binding: FragmentMoreBinding by viewBinding()
    override val viewModel: MoreViewModel by viewModels()

    @Inject
    lateinit var analyticsManager: AnalyticsManager
    @Inject
    lateinit var commonStorage: CommonPreferencesStorage

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupCards()
        createListeners()
    }

    private fun createListeners() {
        binding.viewFaq.root.setOnClickListener { openFAQ() }
        binding.viewShop.root.setOnClickListener {
            analyticsManager.trackEvent(ShopEvents.open(), true)
            openShop()
        }
//        binding.viewProfile.root.setOnClickListener { openProfile() }
        binding.viewChallenges.root.setOnClickListener { openChallenges() }
        binding.viewCalendar.root.setOnClickListener { openFitnessCalendar() }
        binding.viewCommunity.root.setOnClickListener { openCommunity() }
        binding.viewHelp.root.setOnClickListener { openGetHelp() }
    }

    private fun openFitnessCalendar() {
        val intent = MoreIntent.FitnessCalendar
        viewModel.intent.postValue(intent)
    }

    private fun openCommunity() {
        val intent = MoreIntent.Community
        viewModel.intent.postValue(intent)
    }

    private fun openFAQ() {
        val intent = MoreIntent.FAQ
        viewModel.intent.postValue(intent)
    }

//    private fun openProfile() {
//        val intent = MoreIntent.Profile
//        viewModel.intent.postValue(intent)
//    }

    private fun openChallenges() {
        val intent = MoreIntent.Challenges
        viewModel.intent.postValue(intent)
    }

    private fun openShop() {
        val intent = MoreIntent.Shop
        viewModel.intent.postValue(intent)
    }

    private fun setupCards() {
        binding.viewFaq.textTitle.text = getString(R.string.more_screen_faq_text)
        binding.viewFaq.textSubtitle.text = getString(R.string.more_screen_all_the_text)
        binding.viewFaq.image.setImageResource(R.drawable.image_notebook)
        binding.viewFaq.textButton.text = getString(R.string.more_screen_learn_more_text)

        binding.viewShop.textTitle.text = getString(R.string.more_screen_yfc_shop_text)
        binding.viewShop.textSubtitle.isVisible = false
        binding.viewShop.image.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
        val radius = resources.getDimension(R.dimen.more_card_corner_radius)
        val shapeAppearanceModel = binding.viewShop.image.shapeAppearanceModel.toBuilder()
            .setAllCornerSizes(radius)
            .build()
        binding.viewShop.image.shapeAppearanceModel = shapeAppearanceModel
        binding.viewShop.image.setImageResource(R.drawable.image_shop)
        binding.viewShop.imageLogo.isVisible = true
        binding.viewShop.imageLogo.setImageResource(R.drawable.image_shop_logo)
        binding.viewShop.textButton.text = getString(R.string.more_screen_go_to_shop)

        binding.viewCalendar.textTitle.text = getString(
            if (commonStorage.isPtRole) R.string.more_screen_fitness_calendar_text
            else R.string.more_screen_fitness_calendar_text2
        )
        binding.viewCalendar.textSubtitle.text = getString(R.string.more_screen_manage_your_text)
        binding.viewCalendar.image.setImageResource(R.drawable.image_calendar)
        binding.viewCalendar.textButton.text = getString(R.string.more_screen_go_to_text)

        binding.viewChallenges.textTitle.text = getString(R.string.more_screen_challenges_text)
        binding.viewChallenges.textSubtitle.text = getString(R.string.more_screen_compete_with_users_text)
        binding.viewChallenges.image.setImageResource(R.drawable.image_aim)
        binding.viewChallenges.textButton.text = getString(R.string.more_screen_go_to_challenges_text)

        binding.viewCommunity.textTitle.text = getString(com.yourfitness.comunity.R.string.community)
        binding.viewCommunity.textSubtitle.text = getString(com.yourfitness.comunity.R.string.community_card_msg)
        binding.viewCommunity.image.setImageResource(com.yourfitness.comunity.R.drawable.community_card_image)
        binding.viewCommunity.textButton.text = getString(com.yourfitness.comunity.R.string.community_card_action)

        binding.viewHelp.textTitle.text = getString(R.string.more_screen_get_help)
        binding.viewHelp.textSubtitle.text = getString(R.string.more_screen_get_help_msg)
        binding.viewHelp.image.setImageResource(R.drawable.more_screen_get_help_image)
        binding.viewHelp.textButton.text = getString(R.string.more_screen_get_help_action)

//        binding.viewProfile .textTitle.text = getString(com.yourfitness.comunity.R.string.community)
//        binding.viewProfile.textSubtitle.text = getString(com.yourfitness.comunity.R.string.profile_card_msg)
//        binding.viewProfile.image.setImageResource(com.yourfitness.comunity.R.drawable.community_card_image)
//        binding.viewProfile.textButton.text = getString(com.yourfitness.comunity.R.string.community_card_action)
    }
}