package com.yourfitness.coach.ui.features.more.faq

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.yourfitness.coach.R
import com.yourfitness.coach.databinding.FragmentFaqBinding
import com.yourfitness.coach.domain.models.FAQCard
import com.yourfitness.common.ui.mvi.MviFragment
import com.yourfitness.common.ui.mvi.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import com.yourfitness.common.R as common

@AndroidEntryPoint
class FAQFragment : MviFragment<FAQIntent, Any, FAQViewModel>() {

    override val binding: FragmentFaqBinding by viewBinding()
    override val viewModel: FAQViewModel by viewModels()
    private val adapter by lazy { FAQAdapter(::onCardClick) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupToolbar(binding.toolbar.root)
        binding.toolbar.toolbar.title = getString(R.string.faq_screen_faq_text)
    }

    private fun setupRecyclerView() {
        adapter.setData(setupFAQCards())
        binding.recyclerView.adapter = adapter
    }

    private fun onCardClick(cardTitle: String, enabled: Boolean) {
        val intent = /*if (enabled)*/ FAQIntent.Story(cardTitle)
//        else FAQIntent.ComingSoon
        viewModel.intent.postValue(intent)
    }

    private fun setupFAQCards(): List<FAQCard> {
        return listOf(
            FAQCard(
                getString(R.string.faq_screen_first_time_text),
                null,
                R.drawable.image_first_time_here,
            ),
            FAQCard(
                getString(R.string.faq_screen_how_to_buy_a_gym_text),
                R.drawable.image_how_to_buy_gym_subscription,
                common.drawable.gradient_blue,
            ),
            FAQCard(
                getString(R.string.faq_screen_how_to_spend_coins),
                R.drawable.image_how_to_spend_coins,
                R.drawable.gradient_yellow,
            ),
            FAQCard(
                getString(R.string.faq_screen_how_to_use_your_gym_text),
                R.drawable.image_how_to_use_your_gym_access,
                common.drawable.gradient_blue,
            ),
            FAQCard(
                getString(R.string.faq_screen_how_to_earn_coins_text),
                R.drawable.image_coin,
                common.drawable.gradient_blue,
            ),
            FAQCard(
                getString(R.string.faq_screen_yfc_partners_text),
                R.drawable.image_yfc_partners,
                common.drawable.gradient_blue,
            ),
            FAQCard(
                getString(R.string.faq_screen_how_to_join_challenge),
                R.drawable.image_how_to_join_challenge,
                common.drawable.gradient_blue,
            ),
            FAQCard(
                getString(R.string.faq_screen_stuck_need_text),
                R.drawable.image_stuck,
                common.drawable.gradient_blue,
            ),
            FAQCard(
                getString(R.string.faq_screen_how_to_join_community),
                R.drawable.image_how_to_join_community,
                R.drawable.gradient_yellow,
            ),

//            FAQCard(
//                getString(R.string.faq_screen_how_to_buy_pt_sessions),
//                R.drawable.image_how_to_use_pt_sessions,
//                R.drawable.gradient_yellow,
//                com.yourfitness.common.R.drawable.background_blue_rounded_4
//            ),
//            FAQCard(
//                getString(R.string.faq_screen_how_to_book_pt_sessions),
//                R.drawable.image_how_to_manage_your_calendar,
//                common.drawable.gradient_blue,
//                com.yourfitness.common.R.drawable.background_yellow_rounded_4
//            ),
//            FAQCard(
//                getString(R.string.faq_screen_how_to_manage_pt_sessions),
//                R.drawable.image_how_to_manage_pt_sessions,
//                common.drawable.gradient_blue,
//                com.yourfitness.common.R.drawable.background_yellow_rounded_4
//            ),
//            FAQCard(
//                getString(R.string.faq_screen_how_to_join_challenge),
//                R.drawable.image_how_to_join_challenge,
//                common.drawable.gradient_blue,
//            ),
//
            /*FAQCard(
                getString(R.string.faq_screen_how_to_buy_class_text),
                R.drawable.image_how_to_buy_class,
                R.drawable.gradient_yellow,
            ),
            FAQCard(
                getString(R.string.faq_screen_how_to_use_your_class_text),
                R.drawable.image_id_card,
                common.drawable.gradient_blue,
            ),
            FAQCard(
                getString(R.string.faq_screen_how_to_manage_your_text),
                R.drawable.image_how_to_manage_your_calendar,
                R.drawable.gradient_yellow,
            ),
            FAQCard(
                getString(R.string.faq_screen_how_to_earn_credits_text),
                R.drawable.image_credit,
                R.drawable.gradient_yellow,
            ),*/
        )
    }
}