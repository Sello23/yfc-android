package com.yourfitness.coach.ui.features.more.story

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.yourfitness.coach.R
import com.yourfitness.coach.databinding.FragmentStoryBinding
import com.yourfitness.coach.domain.models.StoryPage
import com.yourfitness.coach.network.dto.Classification
import com.yourfitness.coach.ui.navigation.Navigation
import com.yourfitness.common.network.dto.Packages
import com.yourfitness.common.ui.mvi.MviFragment
import com.yourfitness.common.ui.mvi.viewBinding
import com.yourfitness.coach.ui.utils.StoryGenerator
import com.yourfitness.common.ui.utils.animationSlideStart
import com.yourfitness.common.ui.utils.doubleToStringNoDecimal
import dagger.hilt.android.AndroidEntryPoint
import jp.shts.android.storiesprogressview.StoriesProgressView

@AndroidEntryPoint
class StoryFragment : MviFragment<StoryIntent, Any, StoryViewModel>(), StoriesProgressView.StoriesListener {

    override val binding: FragmentStoryBinding by viewBinding()
    override val viewModel: StoryViewModel by viewModels()

    private val storyList by lazy { setupStoryList() }
    private var pressTime = PRESS_TIME
    private var limit = LIMIT
    private var page = START_STORY_PAGE
    private val title by lazy { requireArguments().get("title").toString() }
    private val howToEarnCreditsStoryAdapter by lazy { HowToBuyCreditsStoryAdapter() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showLoading(false)
        setupStory()
        setStartStoryPage()
        showStartStoryPage()
        setupCustomView()
        binding.buttonClose.setOnClickListener { closeStory() }
    }

    override fun onDestroy() {
        binding.storiesIndicators.destroy()
        setFragmentResult("faq_result", bundleOf())
        super.onDestroy()
    }

    private fun setupStory() {
        binding.storiesIndicators.setStoriesCount(storyList.size)
        binding.storiesIndicators.setStoryDuration(STORY_DURATION)
        binding.storiesIndicators.setStoriesListener(this)
        binding.storiesIndicators.startStories(page)

        binding.reverse.setOnClickListener { binding.storiesIndicators.reverse() }
        binding.reverse.setOnTouchListener(onTouchListener)

        binding.skip.setOnClickListener { binding.storiesIndicators.skip() }
        binding.skip.setOnTouchListener(onTouchListener)
    }

    @SuppressLint("ClickableViewAccessibility")
    private val onTouchListener = OnTouchListener { _, event ->

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                pressTime = System.currentTimeMillis()
                binding.storiesIndicators.pause()
                return@OnTouchListener false
            }
            MotionEvent.ACTION_UP -> {
                val now = System.currentTimeMillis()
                binding.storiesIndicators.resume()
                return@OnTouchListener limit < now - pressTime
            }
        }
        false
    }

    override fun onNext() {
        if (page + 1 > storyList.size) return
        page++
        if (page - 1 == START_STORY_PAGE) {
            hideStartStoryPage()
        }
        showStoryPage()
        setStoryPage()
    }

    override fun onPrev() {
        if (page - 1 < START_STORY_PAGE) return
        page--
        if (page == START_STORY_PAGE) {
            hideStoryPage()
            setStartStoryPage()
            showStartStoryPage()
        } else {
            setStoryPage()
            showStoryPage()
        }
    }

    override fun onComplete() {
        findNavController().navigate(
            R.id.fragment_story,
            arguments,
            NavOptions.Builder()
                .setPopUpTo(R.id.fragment_story, true)
                .setPopExitAnim(androidx.transition.R.anim.abc_slide_out_bottom)
                .build()
        )
    }

    private fun setStartStoryPage() {
        when (title) {
            Stories.HOW_TO_EARN_COINS.title,
            Stories.HOW_TO_EARN_COINS_2.title,
            Stories.HOW_TO_EARN_CREDITS.title,
            Stories.HOW_TO_BUY_GYM_SUBSCRIPTION.title,
            Stories.HOW_TO_BUY_CLASS_CREDITS.title,
            Stories.HOW_TO_MANAGE_YOUR_FITNESS_CALENDAR.title,
            Stories.STUCK_NEED_HELP.title,
            Stories.HOW_TO_USE_YOUR_CLASS_ACCESS_CARD.title,
            Stories.HOW_TO_USE_YOUR_GYM_ACCESS_CARD.title,
            Stories.HOW_TO_BUY_PT_SESSIONS.title,
            Stories.HOW_TO_BOOK_PT_SESSIONS.title,
            Stories.HOW_TO_MANAGE_PT_SESSIONS.title,
            Stories.HOW_TO_SPEND_COINS.title,
            Stories.HOW_TO_JOIN_CHALLENGES.title,
            Stories.HOW_TO_JOIN_COMMUNITY.title,
            -> setStoryStartPageStandard()
            Stories.FIRST_TIME_HERE.title -> setStoryStartPageDifferent()
            Stories.YFC_PARTNERS.title -> setStoryPageYfcPartners()
        }
    }

    private fun setStoryStartPageStandard() {
        binding.imageBackground.setImageResource(storyList[page].backgroundImage)
        binding.viewStoryStartPageStandard.textTitle.text = storyList[page].title
        binding.viewStoryStartPageStandard.textTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, storyList[page].textSize)
        binding.viewStoryStartPageStandard.image.setImageResource(storyList[page].image)
        binding.viewStoryStartPageStandard.buttonNext.text = storyList[page].textButton
    }

    private fun setStoryStartPageDifferent() {
        binding.imageBackground.setImageResource(storyList[page].backgroundImage)
        binding.viewStoryStartPageDifferent.textTitle.text = storyList[page].title
        binding.viewStoryStartPageDifferent.textTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, storyList[page].textSize)
        binding.viewStoryStartPageDifferent.image.setImageResource(storyList[page].image)
        binding.viewStoryStartPageDifferent.buttonNext.text = storyList[page].textButton
    }

    private fun showStartStoryPage() {
        when (title) {
            Stories.HOW_TO_EARN_COINS.title,
            Stories.HOW_TO_EARN_COINS_2.title,
            Stories.HOW_TO_EARN_CREDITS.title,
            Stories.HOW_TO_BUY_GYM_SUBSCRIPTION.title,
            Stories.HOW_TO_BUY_CLASS_CREDITS.title,
            Stories.HOW_TO_MANAGE_YOUR_FITNESS_CALENDAR.title,
            Stories.STUCK_NEED_HELP.title,
            Stories.HOW_TO_USE_YOUR_CLASS_ACCESS_CARD.title,
            Stories.HOW_TO_USE_YOUR_GYM_ACCESS_CARD.title,
            Stories.HOW_TO_BUY_PT_SESSIONS.title,
            Stories.HOW_TO_BOOK_PT_SESSIONS.title,
            Stories.HOW_TO_MANAGE_PT_SESSIONS.title,
            Stories.HOW_TO_SPEND_COINS.title,
            Stories.HOW_TO_JOIN_CHALLENGES.title,
            Stories.HOW_TO_JOIN_COMMUNITY.title,
            -> binding.groupStartStoryPageStandard.isVisible = true
            Stories.FIRST_TIME_HERE.title -> binding.groupStartStoryPageDifferent.isVisible = true
            Stories.YFC_PARTNERS.title -> {
                binding.groupPageYfcPartners.isVisible = true
                binding.buttonGoTo.isVisible = true
            }
        }
    }

    private fun hideStartStoryPage() {
        when (title) {
            Stories.HOW_TO_EARN_COINS.title,
            Stories.HOW_TO_EARN_COINS_2.title,
            Stories.HOW_TO_EARN_CREDITS.title,
            Stories.HOW_TO_BUY_GYM_SUBSCRIPTION.title,
            Stories.HOW_TO_BUY_CLASS_CREDITS.title,
            Stories.HOW_TO_MANAGE_YOUR_FITNESS_CALENDAR.title,
            Stories.STUCK_NEED_HELP.title,
            Stories.HOW_TO_USE_YOUR_CLASS_ACCESS_CARD.title,
            Stories.HOW_TO_USE_YOUR_GYM_ACCESS_CARD.title,
            Stories.HOW_TO_BUY_PT_SESSIONS.title,
            Stories.HOW_TO_BOOK_PT_SESSIONS.title,
            Stories.HOW_TO_MANAGE_PT_SESSIONS.title,
            Stories.HOW_TO_SPEND_COINS.title,
            Stories.HOW_TO_JOIN_CHALLENGES.title,
            Stories.HOW_TO_JOIN_COMMUNITY.title,
            Stories.HOW_TO_JOIN_CHALLENGES.title,
            Stories.HOW_TO_JOIN_COMMUNITY.title,
            -> binding.groupStartStoryPageStandard.isVisible = false
            Stories.FIRST_TIME_HERE.title -> binding.groupStartStoryPageDifferent.isVisible = false
            Stories.YFC_PARTNERS.title -> binding.groupPageYfcPartners.isVisible = false
        }
    }

    private fun setStoryPage() {
        binding.imageBackground.setImageResource(storyList[page].backgroundImage)
        binding.viewStoryPage.textSubtitle.text = storyList[page].subtitle
        binding.viewStoryPage.textSubtitle.animationSlideStart(ANIMATION_DURATION, ANIMATION_OFFSET)
        binding.viewStoryPage.textSubtitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, storyList[page].textSize)
        binding.viewStoryPage.image.setImageResource(storyList[page].image)
        binding.viewStoryPage.image.animationSlideStart(ANIMATION_DURATION, ANIMATION_OFFSET)
        if (storyList[START_STORY_PAGE].title != MEET_OUR_PARTNERS) {
            binding.viewStoryPage.image.scaleType = storyList[page].imageScaleType
        }
        if (storyList[START_STORY_PAGE].title == Stories.STUCK_NEED_HELP.title) {
            binding.viewStoryPage.textPage.isVisible = false
        }
        setPageTextColor()
        showCustomView()
    }

    private fun showStoryPage() {
        binding.groupStoryPage.isVisible = true
    }

    private fun hideStoryPage() {
        binding.groupStoryPage.isVisible = false
    }

    private fun setupCustomView() {
        when (title) {
            Stories.HOW_TO_EARN_CREDITS.title -> setupCreditsStoryView()
            Stories.FIRST_TIME_HERE.title -> setupFirstTimeHereStoryView()
            Stories.HOW_TO_BUY_GYM_SUBSCRIPTION.title -> setupHowToBuySubscriptionStoryView()
            Stories.HOW_TO_BUY_CLASS_CREDITS.title -> setupHowToBuyClassCreditsStoryView()
            Stories.STUCK_NEED_HELP.title -> setupStuckNeedHelpView()
            Stories.HOW_TO_MANAGE_YOUR_FITNESS_CALENDAR.title -> setupHowToManageFitnessCalendarView()
            Stories.HOW_TO_USE_YOUR_CLASS_ACCESS_CARD.title -> setupHowToUseClassAccessCard()
        }
    }

    private fun showCustomView() {
        when (title) {
            Stories.HOW_TO_EARN_CREDITS.title -> showCreditsView()
            Stories.FIRST_TIME_HERE.title -> showFirstTimeHereStoryView()
            Stories.YFC_PARTNERS.title -> showYfcPartnersView()
            Stories.HOW_TO_BUY_GYM_SUBSCRIPTION.title -> showHowToBuySubscriptionStoryView()
            Stories.HOW_TO_BUY_CLASS_CREDITS.title -> showHowToBuyClassCreditsStoryView()
            Stories.STUCK_NEED_HELP.title -> showStuckNeedHelpView()
            Stories.HOW_TO_EARN_COINS.title -> showHowToEarnCoinsView()
            Stories.HOW_TO_EARN_COINS_2.title -> showHowToEarnCoinsView()
            Stories.HOW_TO_MANAGE_YOUR_FITNESS_CALENDAR.title -> showHowToManageFitnessCalendarView()
            Stories.HOW_TO_USE_YOUR_CLASS_ACCESS_CARD.title -> showHowToUseClassAccessCard()
        }
    }

    private fun showCreditsView() {
        binding.viewStoryPage.groupCredits.isVisible = page == FIRST_STORY_PAGE
        binding.viewStoryPage.viewCredits.recyclerView.animationSlideStart(ANIMATION_DURATION, ANIMATION_OFFSET)
    }

    private fun setupCreditsStoryView() {
        viewModel.getSettingsGlobal()
        viewModel.bonuses.observe(viewLifecycleOwner) {
            howToEarnCreditsStoryAdapter.setData(it.bonuses)
            binding.viewStoryPage.viewCredits.recyclerView.adapter = howToEarnCreditsStoryAdapter
            binding.viewStoryPage.viewCredits.recyclerView.layoutManager = GridLayoutManager(context, 2, GridLayoutManager.HORIZONTAL, false)
        }
    }

    private fun showFirstTimeHereStoryView() {
        binding.viewStoryPage.groupFirstTimeHereStoryPage2.isVisible = page == SECOND_STORY_PAGE
        binding.viewStoryPage.viewFirstTimeHereStoryPage2.textSubscriptionCost.animationSlideStart(ANIMATION_DURATION, ANIMATION_OFFSET)
        binding.viewStoryPage.viewFirstTimeHereStoryPage2.imageBackground.animationSlideStart(ANIMATION_DURATION, ANIMATION_OFFSET)

        binding.viewStoryPage.groupFirstTimeHereStoryPage5.isVisible = page == FIFTH_STORY_PAGE
        binding.viewStoryPage.viewFirstTimeHereStoryPage5.imageBottom.animationSlideStart(ANIMATION_DURATION, ANIMATION_OFFSET)
        binding.viewStoryPage.viewFirstTimeHereStoryPage5.imageTop.animationSlideStart(ANIMATION_DURATION, ANIMATION_OFFSET)
    }

    private fun setupFirstTimeHereStoryView() {
        viewModel.getSettingsRegion()
        binding.viewStoryPage.viewFirstTimeHereStoryPage3.root.isVisible = false
        viewModel.packages.observe(viewLifecycleOwner) { settingsRegion ->
            binding.viewStoryPage.viewFirstTimeHereStoryPage2.textSubscriptionCost.text = getString(
                R.string.story_screen_cost_subscription_month_text,
                (settingsRegion.subscriptionCost?.div(100) ?: 100).toString()
            )
        }
    }

    private fun showYfcPartnersView() {
        if (page == FIRST_STORY_PAGE) {
            binding.groupStoryPage.isVisible = false
            binding.groupPageYfcPartners.isVisible = true
            binding.viewStoryPageYfcPartners.image.animationSlideStart(ANIMATION_DURATION, ANIMATION_OFFSET)
            binding.viewStoryPageYfcPartners.textTitle.animationSlideStart(ANIMATION_DURATION, ANIMATION_OFFSET)
            setStoryPageYfcPartners()
        } else {
            binding.groupStoryPage.isVisible = true
            binding.groupPageYfcPartners.isVisible = false
        }
    }

    private fun setStoryPageYfcPartners() {
        binding.imageBackground.setImageResource(storyList[page].backgroundImage)
        binding.viewStoryPageYfcPartners.textTitle.text = storyList[page].title
        binding.viewStoryPageYfcPartners.textTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, storyList[page].textSize)
        binding.viewStoryPageYfcPartners.image.setImageResource(storyList[page].image)
        binding.buttonGoTo.setOnClickListener { closeStory() }
        if (page == FIRST_STORY_PAGE) {
            binding.buttonGoTo.text = getString(R.string.story_screen_go_to_studio_list_text)
            binding.buttonGoTo.setOnClickListener { viewModel.navigator.navigate(Navigation.List(Classification.STUDIO)) }
        } else {
            binding.buttonGoTo.text = getString(R.string.story_screen_go_to_gym_list_text)
            binding.buttonGoTo.setOnClickListener { viewModel.navigator.navigate(Navigation.List(Classification.GYM)) }
        }
    }

    private fun showStuckNeedHelpView() {
        binding.viewStoryPage.groupStuckNeedHelpStory.isVisible = page == FIRST_STORY_PAGE
        binding.viewStoryPage.viewStuckNeedHelpStory.textPage.animationSlideStart(ANIMATION_DURATION, ANIMATION_OFFSET)
        binding.viewStoryPage.viewStuckNeedHelpStory.image.animationSlideStart(ANIMATION_DURATION, ANIMATION_OFFSET)
    }

    private fun setupStuckNeedHelpView() {
        binding.viewStoryPage.viewStuckNeedHelpStory.textPage.text = getString(R.string.story_screen_any_issues_text)
        binding.viewStoryPage.viewStuckNeedHelpStory.image.setImageResource(R.drawable.image_any_issues)
    }

    private fun showHowToBuySubscriptionStoryView() {
        binding.viewStoryPage.groupHowToBuySubscriptionStoryPage2.isVisible = page == SECOND_STORY_PAGE
        binding.viewStoryPage.viewHowToBuySubscriptionStoryPage2.imageDistance.animationSlideStart(ANIMATION_DURATION, ANIMATION_OFFSET)

        binding.viewStoryPage.groupHowToBuySubscriptionStoryPage5.isVisible = page == FIFTH_STORY_PAGE
        binding.viewStoryPage.viewHowToBuySubscriptionStoryPage5.textSubscriptionCost.animationSlideStart(ANIMATION_DURATION, ANIMATION_OFFSET)
        binding.viewStoryPage.viewHowToBuySubscriptionStoryPage5.textSubscriptionCostProceedToPayment.animationSlideStart(
            ANIMATION_DURATION,
            ANIMATION_OFFSET
        )
        binding.viewStoryPage.viewHowToBuySubscriptionStoryPage5.image.animationSlideStart(ANIMATION_DURATION, ANIMATION_OFFSET)
        binding.viewStoryPage.viewHowToBuySubscriptionStoryPage5.imageBackground.animationSlideStart(ANIMATION_DURATION, ANIMATION_OFFSET)
        binding.viewStoryPage.viewHowToBuySubscriptionStoryPage5.imageVector.animationSlideStart(ANIMATION_DURATION, ANIMATION_OFFSET)
        binding.viewStoryPage.viewHowToBuySubscriptionStoryPage5.viewBackground.animationSlideStart(ANIMATION_DURATION, ANIMATION_OFFSET)
    }

    private fun setupHowToBuySubscriptionStoryView() {
        viewModel.getSettingsRegion()
        viewModel.packages.observe(viewLifecycleOwner) {
            binding.viewStoryPage.viewHowToBuySubscriptionStoryPage5.textSubscriptionCost.text = getString(
                R.string.story_screen_cost_subscription_month_text,
                (it.subscriptionCost?.div(100) ?: 100).toString()
            )
            binding.viewStoryPage.viewHowToBuySubscriptionStoryPage5.textSubscriptionCostProceedToPayment.text = getString(
                R.string.story_screen_proceed_payment_text,
                (it.subscriptionCost?.div(100) ?: 100).toString()
            )
        }
    }

    private fun showHowToBuyClassCreditsStoryView() {
        binding.viewStoryPage.groupHowToBuyClassCreditsStoryPage2.isVisible = page == SECOND_STORY_PAGE
        binding.viewStoryPage.viewHowToBuyClassCreditsStoryPage2.imageProfileSettings.animationSlideStart(ANIMATION_DURATION, ANIMATION_OFFSET)
        binding.viewStoryPage.viewHowToBuyClassCreditsStoryPage2.textFirstLevelCredits.animationSlideStart(ANIMATION_DURATION, ANIMATION_OFFSET)
        binding.viewStoryPage.viewHowToBuyClassCreditsStoryPage2.textFirstLevelCost.animationSlideStart(ANIMATION_DURATION, ANIMATION_OFFSET)
        binding.viewStoryPage.viewHowToBuyClassCreditsStoryPage2.textFirstLevelTitle.animationSlideStart(ANIMATION_DURATION, ANIMATION_OFFSET)
        binding.viewStoryPage.viewHowToBuyClassCreditsStoryPage2.imageBackgroundFirstLevel.animationSlideStart(ANIMATION_DURATION, ANIMATION_OFFSET)

        binding.viewStoryPage.viewHowToBuyClassCreditsStoryPage2.textSecondLevelCredits.animationSlideStart(ANIMATION_DURATION, ANIMATION_OFFSET)
        binding.viewStoryPage.viewHowToBuyClassCreditsStoryPage2.textSecondLevelCost.animationSlideStart(ANIMATION_DURATION, ANIMATION_OFFSET)
        binding.viewStoryPage.viewHowToBuyClassCreditsStoryPage2.textSecondLevelTitle.animationSlideStart(ANIMATION_DURATION, ANIMATION_OFFSET)
        binding.viewStoryPage.viewHowToBuyClassCreditsStoryPage2.imageBackgroundSecondLevel.animationSlideStart(ANIMATION_DURATION, ANIMATION_OFFSET)

        binding.viewStoryPage.viewHowToBuyClassCreditsStoryPage2.textThirdLevelCredits.animationSlideStart(ANIMATION_DURATION, ANIMATION_OFFSET)
        binding.viewStoryPage.viewHowToBuyClassCreditsStoryPage2.textThirdLevelCost.animationSlideStart(ANIMATION_DURATION, ANIMATION_OFFSET)
        binding.viewStoryPage.viewHowToBuyClassCreditsStoryPage2.textThirdLevelTitle.animationSlideStart(ANIMATION_DURATION, ANIMATION_OFFSET)
        binding.viewStoryPage.viewHowToBuyClassCreditsStoryPage2.imageBackgroundThirdLevel.animationSlideStart(ANIMATION_DURATION, ANIMATION_OFFSET)

        binding.viewStoryPage.groupHowToBuyClassCreditsStoryPage8.isVisible = page == EIGHTH_STORY_PAGE
        binding.viewStoryPage.viewHowToBuyClassCreditsStoryPage8.textFirstCredits.animationSlideStart(ANIMATION_DURATION, ANIMATION_OFFSET)
        binding.viewStoryPage.viewHowToBuyClassCreditsStoryPage8.textFirstCost.animationSlideStart(ANIMATION_DURATION, ANIMATION_OFFSET)
        binding.viewStoryPage.viewHowToBuyClassCreditsStoryPage8.textFirstTitle.animationSlideStart(ANIMATION_DURATION, ANIMATION_OFFSET)
        binding.viewStoryPage.viewHowToBuyClassCreditsStoryPage8.imageBackgroundFirst.animationSlideStart(ANIMATION_DURATION, ANIMATION_OFFSET)

        binding.viewStoryPage.viewHowToBuyClassCreditsStoryPage8.textSecondCredits.animationSlideStart(ANIMATION_DURATION, ANIMATION_OFFSET)
        binding.viewStoryPage.viewHowToBuyClassCreditsStoryPage8.textSecondCost.animationSlideStart(ANIMATION_DURATION, ANIMATION_OFFSET)
        binding.viewStoryPage.viewHowToBuyClassCreditsStoryPage8.textSecondTitle.animationSlideStart(ANIMATION_DURATION, ANIMATION_OFFSET)
        binding.viewStoryPage.viewHowToBuyClassCreditsStoryPage8.imageBackgroundSecond.animationSlideStart(ANIMATION_DURATION, ANIMATION_OFFSET)

        binding.viewStoryPage.viewHowToBuyClassCreditsStoryPage8.textThirdCredits.animationSlideStart(ANIMATION_DURATION, ANIMATION_OFFSET)
        binding.viewStoryPage.viewHowToBuyClassCreditsStoryPage8.textThirdCost.animationSlideStart(ANIMATION_DURATION, ANIMATION_OFFSET)
        binding.viewStoryPage.viewHowToBuyClassCreditsStoryPage8.textThirdTitle.animationSlideStart(ANIMATION_DURATION, ANIMATION_OFFSET)
        binding.viewStoryPage.viewHowToBuyClassCreditsStoryPage8.imageBackgroundThird.animationSlideStart(ANIMATION_DURATION, ANIMATION_OFFSET)

        binding.viewStoryPage.viewHowToBuyClassCreditsStoryPage8.textFourthCredits.animationSlideStart(ANIMATION_DURATION, ANIMATION_OFFSET)
        binding.viewStoryPage.viewHowToBuyClassCreditsStoryPage8.textFourthCost.animationSlideStart(ANIMATION_DURATION, ANIMATION_OFFSET)
        binding.viewStoryPage.viewHowToBuyClassCreditsStoryPage8.textFourthTitle.animationSlideStart(ANIMATION_DURATION, ANIMATION_OFFSET)
        binding.viewStoryPage.viewHowToBuyClassCreditsStoryPage8.imageBackgroundFourth.animationSlideStart(ANIMATION_DURATION, ANIMATION_OFFSET)

        binding.viewStoryPage.viewHowToBuyClassCreditsStoryPage8.textFifthCredits.animationSlideStart(ANIMATION_DURATION, ANIMATION_OFFSET)
        binding.viewStoryPage.viewHowToBuyClassCreditsStoryPage8.textFifthCost.animationSlideStart(ANIMATION_DURATION, ANIMATION_OFFSET)
        binding.viewStoryPage.viewHowToBuyClassCreditsStoryPage8.textFifthTitle.animationSlideStart(ANIMATION_DURATION, ANIMATION_OFFSET)
        binding.viewStoryPage.viewHowToBuyClassCreditsStoryPage8.imageBackgroundFifth.animationSlideStart(ANIMATION_DURATION, ANIMATION_OFFSET)
    }

    private fun setupHowToBuyClassCreditsStoryView() {
        viewModel.getSettingsRegion()
        viewModel.packages.observe(viewLifecycleOwner) { settingsRegion ->
            val data = mutableListOf<Packages>()
            settingsRegion.packages?.forEach {
                if (it.active == true) {
                    data.add(it)
                }
            }
            data.sortBy { it.cost }
            // viewHowToBuyClassCreditsStoryPage2
            binding.viewStoryPage.viewHowToBuyClassCreditsStoryPage2.textFirstLevelTitle.text = data[0].name
            binding.viewStoryPage.viewHowToBuyClassCreditsStoryPage2.textFirstLevelCost.text = getString(
                R.string.story_screen_cost_subscription_revers_text,
                ((data[0].cost)?.div(100)).toString()
            )
            binding.viewStoryPage.viewHowToBuyClassCreditsStoryPage2.textFirstLevelCredits.text = getString(
                R.string.story_screen_number_credits_text,
                data[0].credits.toString()
            )

            binding.viewStoryPage.viewHowToBuyClassCreditsStoryPage2.textSecondLevelTitle.text = data[1].name
            binding.viewStoryPage.viewHowToBuyClassCreditsStoryPage2.textSecondLevelCost.text = getString(
                R.string.story_screen_cost_subscription_revers_text,
                ((data[1].cost)?.div(100)).toString()
            )
            binding.viewStoryPage.viewHowToBuyClassCreditsStoryPage2.textSecondLevelCredits.text = getString(
                R.string.story_screen_number_credits_text,
                data[1].credits.toString()
            )

            binding.viewStoryPage.viewHowToBuyClassCreditsStoryPage2.textThirdLevelTitle.text = data[2].name
            binding.viewStoryPage.viewHowToBuyClassCreditsStoryPage2.textThirdLevelCost.text = getString(
                R.string.story_screen_cost_subscription_revers_text,
                ((data[2].cost)?.div(100)).toString()
            )
            binding.viewStoryPage.viewHowToBuyClassCreditsStoryPage2.textThirdLevelCredits.text = getString(
                R.string.story_screen_number_credits_text,
                data[2].credits.toString()
            )

            // viewHowToBuyClassCreditsStoryPage8
            binding.viewStoryPage.viewHowToBuyClassCreditsStoryPage8.textFirstTitle.text = data[0].name
            binding.viewStoryPage.viewHowToBuyClassCreditsStoryPage8.textFirstCost.text = getString(
                R.string.story_screen_cost_subscription_revers_text,
                doubleToStringNoDecimal(((data[0].cost)?.div(100))?.toDouble() ?: 100.0)
            )
            binding.viewStoryPage.viewHowToBuyClassCreditsStoryPage8.textFirstCredits.text = getString(
                R.string.story_screen_number_credits_text,
                doubleToStringNoDecimal(data[0].credits?.toDouble() ?: 100.0)
            )

            if (data.size > 1) {
                binding.viewStoryPage.viewHowToBuyClassCreditsStoryPage8.textSecondTitle.text = data[1].name
                binding.viewStoryPage.viewHowToBuyClassCreditsStoryPage8.textSecondCost.text = getString(
                    R.string.story_screen_cost_subscription_revers_text,
                    doubleToStringNoDecimal(((data[1].cost)?.div(100))?.toDouble() ?: 100.0)
                )
                binding.viewStoryPage.viewHowToBuyClassCreditsStoryPage8.textSecondCredits.text = getString(
                    R.string.story_screen_number_credits_text,
                    doubleToStringNoDecimal(data[1].credits?.toDouble() ?: 100.0)
                )
            } else {
                binding.viewStoryPage.viewHowToBuyClassCreditsStoryPage8.imageBackgroundSecond.isVisible = false
            }
            if (data.size > 2) {
                binding.viewStoryPage.viewHowToBuyClassCreditsStoryPage8.textThirdTitle.text = data[2].name
                binding.viewStoryPage.viewHowToBuyClassCreditsStoryPage8.textThirdCost.text = getString(
                    R.string.story_screen_cost_subscription_revers_text,
                    doubleToStringNoDecimal(((data[2].cost)?.div(100))?.toDouble() ?: 100.0)
                )
                binding.viewStoryPage.viewHowToBuyClassCreditsStoryPage8.textThirdCredits.text = getString(
                    R.string.story_screen_number_credits_text,
                    doubleToStringNoDecimal(data[2].credits?.toDouble() ?: 100.0)
                )
            } else {
                binding.viewStoryPage.viewHowToBuyClassCreditsStoryPage8.imageBackgroundThird.isVisible = false
            }
            if (data.size > 3) {
                binding.viewStoryPage.viewHowToBuyClassCreditsStoryPage8.textFourthTitle.text = data[3].name
                binding.viewStoryPage.viewHowToBuyClassCreditsStoryPage8.textFourthCost.text = getString(
                    R.string.story_screen_cost_subscription_revers_text,
                    doubleToStringNoDecimal(((data[3].cost)?.div(100))?.toDouble() ?: 100.0)
                )
                binding.viewStoryPage.viewHowToBuyClassCreditsStoryPage8.textFourthCredits.text = getString(
                    R.string.story_screen_number_credits_text,
                    doubleToStringNoDecimal(data[3].credits?.toDouble() ?: 100.0)
                )
            } else {
                binding.viewStoryPage.viewHowToBuyClassCreditsStoryPage8.imageBackgroundFourth.isVisible = false
            }
            if (data.size > 4) {
                binding.viewStoryPage.viewHowToBuyClassCreditsStoryPage8.textFifthTitle.text = data[4].name
                binding.viewStoryPage.viewHowToBuyClassCreditsStoryPage8.textFifthCost.text = getString(
                    R.string.story_screen_cost_subscription_revers_text,
                    doubleToStringNoDecimal(((data[4].cost)?.div(100))?.toDouble() ?: 100.0)
                )
                binding.viewStoryPage.viewHowToBuyClassCreditsStoryPage8.textFifthCredits.text = getString(
                    R.string.story_screen_number_credits_text,
                    doubleToStringNoDecimal(data[4].credits?.toDouble() ?: 100.0)
                )
            } else {
                binding.viewStoryPage.viewHowToBuyClassCreditsStoryPage8.imageBackgroundFifth.isVisible = false
            }
        }
    }

    private fun showHowToEarnCoinsView() {
        binding.viewStoryPage.groupHowToEarnCoinsStoryPage3.isVisible = page == THIRD_STORY_PAGE
        binding.viewStoryPage.viewHowToEarnCoinsStoryPage3.textSubtitle.animationSlideStart(ANIMATION_DURATION, ANIMATION_OFFSET)
    }

    private fun showHowToManageFitnessCalendarView() {
        binding.viewStoryPage.groupManageYourFitnessCalendarStoryPage4.isVisible = page == FOURTH_STORY_PAGE
        binding.viewStoryPage.viewManageYourFitnessCalendarStoryPage4.textSubtitle.animationSlideStart(ANIMATION_DURATION, ANIMATION_OFFSET)
    }

    private fun setupHowToManageFitnessCalendarView() {
        viewModel.getSettingsGlobal()
        viewModel.bonuses.observe(viewLifecycleOwner) {
            binding.viewStoryPage.viewManageYourFitnessCalendarStoryPage4.textSubtitle.text =
                getString(R.string.story_screen_schedule_classes_text, it.classCancellationTime)
        }
    }

    private fun showHowToUseClassAccessCard() {
        binding.viewStoryPage.groupHowToUseYourClassAccessCardStoryPage1.isVisible = page == FIRST_STORY_PAGE
        binding.viewStoryPage.viewHowToUseYourClassAccessCardStoryPage1.textSubtitle.animationSlideStart(ANIMATION_DURATION, ANIMATION_OFFSET)
    }

    private fun setupHowToUseClassAccessCard() {
        viewModel.getSettingsGlobal()
        viewModel.bonuses.observe(viewLifecycleOwner) {
            binding.viewStoryPage.viewHowToUseYourClassAccessCardStoryPage1.textSubtitle.text =
                getString(R.string.story_screen_class_cards_available_text, it.classEntryLeadTime)
        }
    }

    private fun setPageTextColor() {
        val spannableString = SpannableString(getString(R.string.story_screen_page_text, page, storyList.size - 1))
        val foregroundSpan = storyList[page].pageCountColor?.let { ForegroundColorSpan(it) }
        spannableString.setSpan(foregroundSpan, FIRST_STORY_PAGE, spannableString.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        binding.viewStoryPage.textPage.text = spannableString
        binding.viewStoryPage.textPage.animationSlideStart(ANIMATION_DURATION, ANIMATION_OFFSET)
    }

    private fun setupStoryList(): List<StoryPage> {
        return when (title) {
            Stories.HOW_TO_EARN_COINS.title -> StoryGenerator(requireContext()).createHowToEarnCoinsStory()
            Stories.HOW_TO_EARN_COINS_2.title -> StoryGenerator(requireContext()).createHowToEarnCoinsStory()
            Stories.HOW_TO_EARN_CREDITS.title -> StoryGenerator(requireContext()).createHowToEarnCreditsStory()
            Stories.FIRST_TIME_HERE.title -> StoryGenerator(requireContext()).createFirstTimeHereStory()
            Stories.YFC_PARTNERS.title -> StoryGenerator(requireContext()).createYfcPartnersStory()
            Stories.HOW_TO_BUY_GYM_SUBSCRIPTION.title -> StoryGenerator(requireContext()).createSubscriptionGuideStory()
            Stories.HOW_TO_BUY_CLASS_CREDITS.title -> StoryGenerator(requireContext()).createHowToBuyClassCreditsStory()
            Stories.HOW_TO_MANAGE_YOUR_FITNESS_CALENDAR.title -> StoryGenerator(requireContext()).createHowToManageYourFitnessCalendarStory()
            Stories.STUCK_NEED_HELP.title -> StoryGenerator(requireContext()).createStuckNeedHelpStory()
            Stories.HOW_TO_USE_YOUR_CLASS_ACCESS_CARD.title -> StoryGenerator(requireContext()).createHowUseYourClassAccessCardStory()
            Stories.HOW_TO_USE_YOUR_GYM_ACCESS_CARD.title -> StoryGenerator(requireContext()).createHowUseYourGymAccessCardStory()
            Stories.HOW_TO_BUY_PT_SESSIONS.title -> StoryGenerator(requireContext()).createHowToBuyPtSessions()
            Stories.HOW_TO_BOOK_PT_SESSIONS.title -> StoryGenerator(requireContext()).createHowToBookPtSessions()
            Stories.HOW_TO_MANAGE_PT_SESSIONS.title -> StoryGenerator(requireContext()).createHowToManagePtSessions()
            Stories.HOW_TO_SPEND_COINS.title -> StoryGenerator(requireContext()).createHowToSpendCoins()
            Stories.HOW_TO_JOIN_CHALLENGES.title -> StoryGenerator(requireContext()).createHowToJoinChallenges()
            Stories.HOW_TO_JOIN_COMMUNITY.title -> StoryGenerator(requireContext()).createHowToJoinCommunity()
            else -> StoryGenerator(requireContext()).createHowToEarnCoinsStory()
        }
    }

    private fun closeStory() {
        findNavController().navigateUp()
    }

    companion object {
        private const val LIMIT = 500L
        private const val PRESS_TIME = 0L
        private const val STORY_DURATION = 7200L
        private const val START_STORY_PAGE = 0
        private const val FIRST_STORY_PAGE = 1
        private const val SECOND_STORY_PAGE = 2
        private const val THIRD_STORY_PAGE = 3
        private const val FOURTH_STORY_PAGE = 4
        private const val FIFTH_STORY_PAGE = 5
        private const val EIGHTH_STORY_PAGE = 8
        private const val MEET_OUR_PARTNERS = "Meet our Gym partners!"
        private const val ANIMATION_DURATION = 500L
        private const val ANIMATION_OFFSET = 0L
    }
}

enum class Stories(val title: String) {
    HOW_TO_EARN_COINS("How to earn coins?"),
    HOW_TO_EARN_COINS_2("How to earn coins"),
    HOW_TO_EARN_CREDITS("How to earn credits?"),
    FIRST_TIME_HERE("First time here?"),
    YFC_PARTNERS("YFC partners"),
    HOW_TO_BUY_GYM_SUBSCRIPTION("How to buy a\ngym\nsubscription?"),
    HOW_TO_BUY_CLASS_CREDITS("How to buy class credits?"),
    HOW_TO_MANAGE_YOUR_FITNESS_CALENDAR("How to manage your fitness calendar?"),
    STUCK_NEED_HELP("Stuck! Need Help?"),
    HOW_TO_USE_YOUR_CLASS_ACCESS_CARD("How to use your Class Access card?"),
    HOW_TO_USE_YOUR_GYM_ACCESS_CARD("How to use your Gym Access card?"),
    HOW_TO_BUY_PT_SESSIONS("How to buy PT sessions?"),
    HOW_TO_BOOK_PT_SESSIONS("How to book PT sessions?"),
    HOW_TO_MANAGE_PT_SESSIONS("How to manage PT sessions?"),
    HOW_TO_SPEND_COINS("How to spend your YFC coins?"),
    HOW_TO_JOIN_CHALLENGES("How to join a challenge?"),
    HOW_TO_JOIN_COMMUNITY("Join the YFC Community"),
}