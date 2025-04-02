package com.yourfitness.coach.ui.utils

import android.content.Context
import android.widget.ImageView
import com.yourfitness.coach.R
import com.yourfitness.common.R as common
import com.yourfitness.coach.domain.models.StoryPage

@Suppress("DEPRECATION")
class StoryGenerator(private val context: Context) {

    fun createHowToEarnCoinsStory(): List<StoryPage> {
        return listOf(
            StoryPage(
                title = context.getString(R.string.story_screen_want_to_ear_coins_text),
                subtitle = null,
                textSize = 45F,
                pageCountColor = null,
                textButton = context.getString(R.string.story_screen_take_look_text),
                image = R.drawable.image_coins,
                imageScaleType = null,
                backgroundImage = common.drawable.gradient_blue
            ),
            StoryPage(
                title = null,
                subtitle = context.getString(R.string.story_screen_sync_your_fitness_text),
                textSize = 28F,
                pageCountColor = context.resources.getColor(common.color.gray_light),
                textButton = null,
                image = R.drawable.image_apple_watch,
                imageScaleType = ImageView.ScaleType.FIT_XY,
                backgroundImage = common.drawable.gradient_blue
            ),
            StoryPage(
                title = null,
                subtitle = context.getString(R.string.story_screen_burn_calories_text),
                textSize = 28F,
                pageCountColor = context.resources.getColor(common.color.gray_light),
                textButton = null,
                image = R.drawable.image_mobile,
                imageScaleType = ImageView.ScaleType.CENTER,
                backgroundImage = common.drawable.gradient_blue
            ),
            StoryPage(
                title = null,
                subtitle = context.getString(R.string.story_screen_earn_coins_text),
                textSize = 24F,
                pageCountColor = context.resources.getColor(common.color.gray_light),
                textButton = null,
                image = R.drawable.image_dumbbells,
                imageScaleType = ImageView.ScaleType.FIT_END,
                backgroundImage = common.drawable.gradient_blue
            ),
        )
    }

    fun createHowToEarnCreditsStory(): List<StoryPage> {
        return listOf(
            StoryPage(
                title = context.getString(R.string.faq_screen_how_to_earn_credits_text),
                subtitle = null,
                textSize = 45F,
                pageCountColor = null,
                textButton = context.getString(R.string.story_screen_its_easy_text),
                image = R.drawable.image_credits,
                imageScaleType = null,
                backgroundImage = R.drawable.image_gradient_blue_1
            ),
            StoryPage(
                title = null,
                subtitle = context.getString(R.string.story_screen_the_more_classes_you_text),
                textSize = 20F,
                pageCountColor = context.resources.getColor(common.color.gray_light),
                textButton = null,
                image = R.drawable.image_map,
                imageScaleType = ImageView.ScaleType.FIT_END,
                backgroundImage = R.drawable.image_gradient_blue_credits_1
            ),
            StoryPage(
                title = null,
                subtitle = context.getString(R.string.story_screen_each_studio_counts_its_text),
                textSize = 20F,
                pageCountColor = context.resources.getColor(common.color.gray_light),
                textButton = null,
                image = R.drawable.image_visits,
                imageScaleType = ImageView.ScaleType.CENTER,
                backgroundImage = common.drawable.gradient_blue
            ),
        )
    }

    fun createFirstTimeHereStory(): List<StoryPage> {
        return listOf(
            StoryPage(
                title = context.getString(R.string.faq_screen_first_time_text),
                subtitle = null,
                textSize = 45F,
                pageCountColor = null,
                textButton = context.getString(R.string.story_screen_lets_roll_text),
                image = R.drawable.image_first_time_here_dumbbells,
                imageScaleType = null,
                backgroundImage = R.drawable.gradietn_yellow_light
            ),
            StoryPage(
                title = null,
                subtitle = context.getString(R.string.story_screen_use_the_map_text),
                textSize = 24F,
                pageCountColor = context.resources.getColor(common.color.yellow_transparent),
                textButton = null,
                image = R.drawable.image_map_story,
                imageScaleType = ImageView.ScaleType.FIT_END,
                backgroundImage = R.drawable.gradietn_yellow_light
            ),
            StoryPage(
                title = null,
                subtitle = context.getString(R.string.story_screen_to_work_out_text),
                textSize = 24F,
                pageCountColor = context.resources.getColor(common.color.yellow_transparent),
                textButton = null,
                image = R.drawable.image_to_work_group,
                imageScaleType = ImageView.ScaleType.FIT_XY,
                backgroundImage = R.drawable.gradietn_yellow_light
            ),
            StoryPage(
                title = null,
                subtitle = context.getString(R.string.story_screen_earn_coins_for_text),
                textSize = 20F,
                pageCountColor = context.resources.getColor(common.color.yellow_transparent),
                textButton = null,
                image = R.drawable.image_earn_coins,
                imageScaleType = ImageView.ScaleType.FIT_XY,
                backgroundImage = R.drawable.gradietn_yellow_light
            ),
            StoryPage(
                title = null,
                subtitle = context.getString(R.string.story_screen_your_access_text),
                textSize = 24F,
                pageCountColor = context.resources.getColor(common.color.yellow_transparent),
                textButton = null,
                image = R.drawable.image_your_access_card,
                imageScaleType = ImageView.ScaleType.FIT_START,
                backgroundImage = R.drawable.gradietn_yellow_light
            ),
        )
    }

    fun createYfcPartnersStory(): List<StoryPage> {
        return listOf(
            StoryPage(
                title = context.getString(R.string.story_screen_meet_our_gym_text),
                subtitle = null,
                textSize = 32F,
                pageCountColor = null,
                textButton = null,
                image = R.drawable.image_yfc_partners_first,
                imageScaleType = null,
                backgroundImage = R.drawable.image_background_yfc_partners_first
            )
        )
    }

    fun createSubscriptionGuideStory(): List<StoryPage> {
        return listOf(
            StoryPage(
                title = context.getString(R.string.faq_screen_how_to_buy_a_gym_normal_text),
                subtitle = null,
                textSize = 32F,
                pageCountColor = null,
                textButton = context.getString(R.string.story_screen_lets_find_out_text),
                image = R.drawable.image_subscription_badge,
                imageScaleType = ImageView.ScaleType.FIT_START,
                backgroundImage = R.drawable.image_gradient_blue_0
            ),
            StoryPage(
                title = null,
                subtitle = context.getString(R.string.story_screen_once_youve_bought_text),
                textSize = 22F,
                pageCountColor = context.resources.getColor(common.color.gray_light),
                textButton = null,
                image = R.drawable.image_gym_card,
                imageScaleType = ImageView.ScaleType.FIT_END,
                backgroundImage = R.drawable.image_gradient_blue_1
            ),
            StoryPage(
                title = null,
                subtitle = context.getString(R.string.story_screen_use_our_map_text),
                textSize = 22F,
                pageCountColor = context.resources.getColor(common.color.gray_light),
                textButton = null,
                image = R.drawable.image_map_distance,
                imageScaleType = ImageView.ScaleType.FIT_START,
                backgroundImage = R.drawable.image_gradient_blue_2
            ),
            StoryPage(
                title = null,
                subtitle = context.getString(R.string.story_screen_use_our_filters_text),
                textSize = 22F,
                pageCountColor = context.resources.getColor(common.color.gray_light),
                textButton = null,
                image = R.drawable.image_filter,
                imageScaleType = ImageView.ScaleType.CENTER,
                backgroundImage = R.drawable.image_gradient_blue_3
            ),
            StoryPage(
                title = null,
                subtitle = context.getString(R.string.story_screen_dont_have_subscription_text),
                textSize = 22F,
                pageCountColor = context.resources.getColor(common.color.gray_light),
                textButton = null,
                image = R.drawable.image_subscription_screen,
                imageScaleType = ImageView.ScaleType.FIT_END,
                backgroundImage = R.drawable.image_gradient_blue_4
            ),
            StoryPage(
                title = null,
                subtitle = context.getString(R.string.story_screen_choose_payment_text),
                textSize = 20F,
                pageCountColor = context.resources.getColor(common.color.gray_light),
                textButton = null,
                image = R.drawable.image_transparent,
                imageScaleType = ImageView.ScaleType.CENTER,
                backgroundImage = R.drawable.image_gradient_blue_5
            ),
            StoryPage(
                title = null,
                subtitle = context.getString(R.string.story_screen_cancel_subscription_text),
                textSize = 20F,
                pageCountColor = context.resources.getColor(common.color.gray_light),
                textButton = null,
                image = R.drawable.image_cancel_subscription,
                imageScaleType = ImageView.ScaleType.FIT_END,
                backgroundImage = R.drawable.image_gradient_blue_6
            ),
            StoryPage(
                title = null,
                subtitle = context.getString(R.string.story_screen_use_quick_access_text),
                textSize = 24F,
                pageCountColor = context.resources.getColor(common.color.gray_light),
                textButton = null,
                image = R.drawable.image_quick_access,
                imageScaleType = ImageView.ScaleType.CENTER,
                backgroundImage = R.drawable.image_gradient_blue_7
            ),
        )
    }

    fun createHowToBuyClassCreditsStory(): List<StoryPage> {
        return listOf(
            StoryPage(
                title = context.getString(R.string.faq_screen_how_to_buy_class_text),
                subtitle = null,
                textSize = 45F,
                pageCountColor = null,
                textButton = context.getString(R.string.story_screen_lets_us_explain_text),
                image = R.drawable.image_class_credits_shaker,
                imageScaleType = null,
                backgroundImage = R.drawable.gradietn_yellow_light
            ),
            StoryPage(
                title = null,
                subtitle = context.getString(R.string.story_screen_once_purchased_yfc_text),
                textSize = 24F,
                pageCountColor = context.resources.getColor(common.color.yellow_transparent),
                textButton = null,
                image = R.drawable.image_once_purchased_yfc,
                imageScaleType = ImageView.ScaleType.FIT_XY,
                backgroundImage = R.drawable.gradietn_yellow_light
            ),
            StoryPage(
                title = null,
                subtitle = context.getString(R.string.story_screen_on_profile_screen_text),
                textSize = 24F,
                pageCountColor = context.resources.getColor(common.color.yellow_transparent),
                textButton = null,
                image = R.drawable.image_transparent,
                imageScaleType = ImageView.ScaleType.FIT_XY,
                backgroundImage = R.drawable.gradietn_yellow_light
            ),
            StoryPage(
                title = null,
                subtitle = context.getString(R.string.story_screen_use_map_find_studios_text),
                textSize = 24F,
                pageCountColor = context.resources.getColor(common.color.yellow_transparent),
                textButton = null,
                image = R.drawable.image_map_distance,
                imageScaleType = ImageView.ScaleType.FIT_START,
                backgroundImage = R.drawable.gradietn_yellow_light
            ),
            StoryPage(
                title = null,
                subtitle = context.getString(R.string.story_screen_use_our_filters_text),
                textSize = 24F,
                pageCountColor = context.resources.getColor(common.color.yellow_transparent),
                textButton = null,
                image = R.drawable.image_filter,
                imageScaleType = ImageView.ScaleType.CENTER,
                backgroundImage = R.drawable.gradietn_yellow_light
            ),
            StoryPage(
                title = null,
                subtitle = context.getString(R.string.story_screen_pick_studio_text),
                textSize = 24F,
                pageCountColor = context.resources.getColor(common.color.yellow_transparent),
                textButton = null,
                image = R.drawable.image_pick_your_studio,
                imageScaleType = ImageView.ScaleType.FIT_END,
                backgroundImage = R.drawable.gradietn_yellow_light
            ),
            StoryPage(
                title = null,
                subtitle = context.getString(R.string.story_screen_tap_class_you_want_text),
                textSize = 24F,
                pageCountColor = context.resources.getColor(common.color.yellow_transparent),
                textButton = null,
                image = R.drawable.image_book_class,
                imageScaleType = ImageView.ScaleType.FIT_START,
                backgroundImage = R.drawable.gradietn_yellow_light
            ),
            StoryPage(
                title = null,
                subtitle = context.getString(R.string.story_screen_dont_have_enough_credits_text),
                textSize = 20F,
                pageCountColor = context.resources.getColor(common.color.yellow_transparent),
                textButton = null,
                image = R.drawable.image_buy_credits_button,
                imageScaleType = ImageView.ScaleType.CENTER_CROP,
                backgroundImage = R.drawable.gradietn_yellow_light
            ),
            StoryPage(
                title = null,
                subtitle = context.getString(R.string.story_screen_choose_payment_option_text),
                textSize = 20F,
                pageCountColor = context.resources.getColor(common.color.yellow_transparent),
                textButton = null,
                image = R.drawable.image_transparent,
                imageScaleType = ImageView.ScaleType.FIT_XY,
                backgroundImage = R.drawable.gradietn_yellow_light
            ),
        )
    }

    fun createHowToManageYourFitnessCalendarStory(): List<StoryPage> {
        return listOf(
            StoryPage(
                title = context.getString(R.string.faq_screen_how_to_manage_your_text),
                subtitle = null,
                textSize = 32F,
                pageCountColor = null,
                textButton = context.getString(R.string.story_screen_lets_find_out_text),
                image = R.drawable.image_how_manage_your_calendar,
                imageScaleType = null,
                backgroundImage = R.drawable.gradietn_yellow_light
            ),
            StoryPage(
                title = null,
                subtitle = context.getString(R.string.story_screen_easily_manage_future_text),
                textSize = 28F,
                pageCountColor = context.resources.getColor(common.color.yellow_transparent),
                textButton = null,
                image = R.drawable.image_easile_manage_future,
                imageScaleType = ImageView.ScaleType.FIT_XY,
                backgroundImage = R.drawable.gradietn_yellow_light
            ),
            StoryPage(
                title = null,
                subtitle = context.getString(R.string.story_screen_go_to_fitness_calendar_text),
                textSize = 20F,
                pageCountColor = context.resources.getColor(common.color.yellow_transparent),
                textButton = null,
                image = R.drawable.image_my_fitness_calendar_link,
                imageScaleType = ImageView.ScaleType.CENTER_CROP,
                backgroundImage = R.drawable.gradietn_yellow_light
            ),
            StoryPage(
                title = null,
                subtitle = context.getString(R.string.story_screen_can_access_text),
                textSize = 24F,
                pageCountColor = context.resources.getColor(common.color.yellow_transparent),
                textButton = null,
                image = R.drawable.image_or_can_access,
                imageScaleType = ImageView.ScaleType.CENTER_CROP,
                backgroundImage = R.drawable.gradietn_yellow_light
            ),
            StoryPage(
                title = null,
                subtitle = null,
                textSize = 24F,
                pageCountColor = context.resources.getColor(common.color.yellow_transparent),
                textButton = null,
                image = R.drawable.image_scheduled_classes,
                imageScaleType = ImageView.ScaleType.CENTER,
                backgroundImage = R.drawable.gradietn_yellow_light
            ),
        )
    }

    fun createStuckNeedHelpStory(): List<StoryPage> {
        return listOf(
            StoryPage(
                title = context.getString(R.string.faq_screen_stuck_need_text),
                subtitle = null,
                textSize = 32F,
                pageCountColor = null,
                textButton = context.getString(R.string.story_screen_lets_find_out_text),
                image = R.drawable.image_stuck_neen_help,
                imageScaleType = null,
                backgroundImage = common.drawable.gradient_blue
            ),
            StoryPage(
                title = null,
                subtitle = null,
                textSize = 20F,
                pageCountColor = null,
                textButton = null,
                image = R.drawable.image_transparent,
                imageScaleType = ImageView.ScaleType.FIT_XY,
                backgroundImage = common.drawable.gradient_blue
            ),
        )
    }

    fun createHowUseYourClassAccessCardStory(): List<StoryPage> {
        return listOf(
            StoryPage(
                title = context.getString(R.string.faq_screen_how_to_use_your_class_text),
                subtitle = null,
                textSize = 32F,
                pageCountColor = null,
                textButton = context.getString(R.string.story_screen_lets_find_out_text),
                image = R.drawable.image_class_access_card,
                imageScaleType = null,
                backgroundImage = common.drawable.gradient_blue
            ),
            StoryPage(
                title = null,
                subtitle = null,
                textSize = 20F,
                pageCountColor = context.resources.getColor(common.color.gray_light),
                textButton = null,
                image = R.drawable.image_access_cards_available,
                imageScaleType = ImageView.ScaleType.FIT_XY,
                backgroundImage = common.drawable.gradient_blue
            ),
            StoryPage(
                title = null,
                subtitle = context.getString(R.string.story_screen_confirm_that_text),
                textSize = 20F,
                pageCountColor = context.resources.getColor(common.color.gray_light),
                textButton = null,
                image = R.drawable.image_confrim_that,
                imageScaleType = ImageView.ScaleType.FIT_END,
                backgroundImage = common.drawable.gradient_blue
            ),
        )
    }

    fun createHowUseYourGymAccessCardStory(): List<StoryPage> {
        return listOf(
            StoryPage(
                title = context.getString(R.string.faq_screen_how_to_use_your_gym_text),
                subtitle = null,
                textSize = 32F,
                pageCountColor = null,
                textButton = context.getString(R.string.story_screen_lets_find_out_text),
                image = R.drawable.image_how_to_use_gym_access_card,
                imageScaleType = null,
                backgroundImage = common.drawable.gradient_blue
            ),
            StoryPage(
                title = null,
                subtitle = context.getString(R.string.story_screen_tap_on_quick_access_text),
                textSize = 24F,
                pageCountColor = context.resources.getColor(common.color.gray_light),
                textButton = null,
                image = R.drawable.image_access_cards_available,
                imageScaleType = ImageView.ScaleType.FIT_XY,
                backgroundImage = common.drawable.gradient_blue
            ),
            StoryPage(
                title = null,
                subtitle = context.getString(R.string.story_screen_confirm_location_text),
                textSize = 24F,
                pageCountColor = context.resources.getColor(common.color.gray_light),
                textButton = null,
                image = R.drawable.image_confirm_right_local,
                imageScaleType = ImageView.ScaleType.FIT_XY,
                backgroundImage = common.drawable.gradient_blue
            ),
            StoryPage(
                title = null,
                subtitle = context.getString(R.string.story_screen_show_access_text),
                textSize = 26F,
                pageCountColor = context.resources.getColor(common.color.gray_light),
                textButton = null,
                image = R.drawable.image_show_your_access,
                imageScaleType = ImageView.ScaleType.FIT_START,
                backgroundImage = common.drawable.gradient_blue
            ),
            StoryPage(
                title = null,
                subtitle = context.getString(R.string.story_screen_different_location_text),
                textSize = 24F,
                pageCountColor = context.resources.getColor(common.color.gray_light),
                textButton = null,
                image = R.drawable.image_different_location,
                imageScaleType = ImageView.ScaleType.FIT_XY,
                backgroundImage = common.drawable.gradient_blue
            ),
            StoryPage(
                title = null,
                subtitle = context.getString(R.string.story_screen_still_havent_text),
                textSize = 24F,
                pageCountColor = context.resources.getColor(common.color.gray_light),
                textButton = null,
                image = R.drawable.image_still_havent_found,
                imageScaleType = ImageView.ScaleType.FIT_XY,
                backgroundImage = common.drawable.gradient_blue
            ),
        )
    }

    fun createHowToBuyPtSessions(): List<StoryPage> {
        return listOf(
            StoryPage(
                title = context.getString(R.string.faq_screen_how_to_buy_pt_sessions),
                subtitle = null,
                textSize = 45F,
                pageCountColor = null,
                textButton = context.getString(R.string.story_screen_lets_us_explain_text),
                image = R.drawable.story_buy_pt_sessions_1,
                imageScaleType = ImageView.ScaleType.FIT_END,
                backgroundImage = R.drawable.gradietn_yellow_light
            ),
            StoryPage(
                title = null,
                subtitle = context.getString(R.string.story_buy_pt_sessions_2),
                textSize = 24F,
                pageCountColor = context.resources.getColor(common.color.yellow_transparent),
                textButton = null,
                image = R.drawable.story_buy_pt_sessions_2,
                imageScaleType = ImageView.ScaleType.CENTER,
                backgroundImage = R.drawable.gradietn_yellow_light
            ),
            StoryPage(
                title = null,
                subtitle = context.getString(R.string.story_buy_pt_sessions_3),
                textSize = 24F,
                pageCountColor = context.resources.getColor(common.color.yellow_transparent),
                textButton = null,
                image = R.drawable.story_buy_pt_sessions_3,
                imageScaleType = ImageView.ScaleType.FIT_XY,
                backgroundImage = R.drawable.gradietn_yellow_light
            ),
            StoryPage(
                title = null,
                subtitle = context.getString(R.string.story_buy_pt_sessions_4),
                textSize = 24F,
                pageCountColor = context.resources.getColor(common.color.yellow_transparent),
                textButton = null,
                image = R.drawable.story_buy_pt_sessions_4,
                imageScaleType = ImageView.ScaleType.CENTER,
                backgroundImage = R.drawable.gradietn_yellow_light
            ),
            StoryPage(
                title = null,
                subtitle = context.getString(R.string.story_buy_pt_sessions_5),
                textSize = 24F,
                pageCountColor = context.resources.getColor(common.color.yellow_transparent),
                textButton = null,
                image = R.drawable.story_buy_pt_sessions_5,
                imageScaleType = ImageView.ScaleType.FIT_XY,
                backgroundImage = R.drawable.gradietn_yellow_light
            ),
            StoryPage(
                title = null,
                subtitle = context.getString(R.string.story_buy_pt_sessions_6),
                textSize = 24F,
                pageCountColor = context.resources.getColor(common.color.yellow_transparent),
                textButton = null,
                image = R.drawable.story_buy_pt_sessions_6,
                imageScaleType = ImageView.ScaleType.FIT_XY,
                backgroundImage = R.drawable.gradietn_yellow_light
            ),
        )
    }

    fun createHowToBookPtSessions(): List<StoryPage> {
        return listOf(
            StoryPage(
                title = context.getString(R.string.faq_screen_how_to_book_pt_sessions),
                subtitle = null,
                textSize = 45F,
                pageCountColor = null,
                textButton = context.getString(R.string.story_screen_take_look_text),
                image = R.drawable.story_book_pt_sessions_1,
                imageScaleType = ImageView.ScaleType.FIT_XY,
                backgroundImage = common.drawable.gradient_blue
            ),
            StoryPage(
                title = null,
                subtitle = context.getString(R.string.story_book_pt_sessions_2),
                textSize = 24F,
                pageCountColor = context.resources.getColor(common.color.gray_light),
                textButton = null,
                image = R.drawable.story_book_pt_sessions_2,
                imageScaleType = ImageView.ScaleType.FIT_XY,
                backgroundImage = common.drawable.gradient_blue
            ),
            StoryPage(
                title = null,
                subtitle = context.getString(R.string.story_book_pt_sessions_3),
                textSize = 24F,
                pageCountColor = context.resources.getColor(common.color.gray_light),
                textButton = null,
                image = R.drawable.story_book_pt_sessions_3,
                imageScaleType = ImageView.ScaleType.CENTER,
                backgroundImage = common.drawable.gradient_blue
            ),
            StoryPage(
                title = null,
                subtitle = context.getString(R.string.story_book_pt_sessions_4),
                textSize = 24F,
                pageCountColor = context.resources.getColor(common.color.gray_light),
                textButton = null,
                image = R.drawable.story_book_pt_sessions_4,
                imageScaleType = ImageView.ScaleType.CENTER,
                backgroundImage = common.drawable.gradient_blue
            ),
            StoryPage(
                title = null,
                subtitle = context.getString(R.string.story_book_pt_sessions_5),
                textSize = 24F,
                pageCountColor = context.resources.getColor(common.color.gray_light),
                textButton = null,
                image = R.drawable.story_book_pt_sessions_5,
                imageScaleType = ImageView.ScaleType.FIT_XY,
                backgroundImage = common.drawable.gradient_blue
            ),
        )
    }

    fun createHowToManagePtSessions(): List<StoryPage> {
        return listOf(
            StoryPage(
                title = context.getString(R.string.faq_screen_how_to_manage_pt_sessions),
                subtitle = null,
                textSize = 45F,
                pageCountColor = null,
                textButton = context.getString(R.string.story_screen_take_look_text),
                image = R.drawable.story_manage_pt_sessions_1,
                imageScaleType = ImageView.ScaleType.FIT_XY,
                backgroundImage = common.drawable.gradient_blue
            ),
            StoryPage(
                title = null,
                subtitle = context.getString(R.string.story_manage_pt_sessions_2),
                textSize = 24F,
                pageCountColor = context.resources.getColor(common.color.gray_light),
                textButton = null,
                image = R.drawable.story_manage_pt_sessions_2,
                imageScaleType = ImageView.ScaleType.FIT_XY,
                backgroundImage = common.drawable.gradient_blue
            ),
            StoryPage(
                title = null,
                subtitle = context.getString(R.string.story_manage_pt_sessions_3),
                textSize = 24F,
                pageCountColor = context.resources.getColor(common.color.gray_light),
                textButton = null,
                image = R.drawable.story_manage_pt_sessions_3,
                imageScaleType = ImageView.ScaleType.CENTER,
                backgroundImage = common.drawable.gradient_blue
            ),
            StoryPage(
                title = null,
                subtitle = context.getString(R.string.story_manage_pt_sessions_4),
                textSize = 24F,
                pageCountColor = context.resources.getColor(common.color.gray_light),
                textButton = null,
                image = R.drawable.story_manage_pt_sessions_4,
                imageScaleType = ImageView.ScaleType.FIT_XY,
                backgroundImage = common.drawable.gradient_blue
            )
        )
    }

    fun createHowToSpendCoins(): List<StoryPage> {
        return listOf(
            StoryPage(
                title = context.getString(R.string.faq_screen_how_to_spend_coins),
                subtitle = null,
                textSize = 45F,
                pageCountColor = null,
                textButton = context.getString(R.string.story_screen_take_look_text),
                image = R.drawable.story_how_to_spend_coins_1,
                imageScaleType = ImageView.ScaleType.FIT_XY,
                backgroundImage = R.drawable.gradietn_yellow_light
            ),
            StoryPage(
                title = null,
                subtitle = context.getString(R.string.story_how_to_spend_coins_2),
                textSize = 24F,
                pageCountColor = context.resources.getColor(common.color.yellow_transparent),
                textButton = null,
                image = R.drawable.story_how_to_spend_coins_2,
                imageScaleType = ImageView.ScaleType.FIT_END,
                backgroundImage = R.drawable.gradietn_yellow_light
            ),
            StoryPage(
                title = null,
                subtitle = context.getString(R.string.story_how_to_spend_coins_3),
                textSize = 24F,
                pageCountColor = context.resources.getColor(common.color.yellow_transparent),
                textButton = null,
                image = R.drawable.story_how_to_spend_coins_3,
                imageScaleType = ImageView.ScaleType.FIT_XY,
                backgroundImage = R.drawable.gradietn_yellow_light
            ),
            StoryPage(
                title = null,
                subtitle = context.getString(R.string.story_how_to_spend_coins_4),
                textSize = 24F,
                pageCountColor = context.resources.getColor(common.color.yellow_transparent),
                textButton = null,
                image = R.drawable.story_how_to_spend_coins_4,
                imageScaleType = ImageView.ScaleType.CENTER,
                backgroundImage = R.drawable.gradietn_yellow_light
            ),
            StoryPage(
                title = null,
                subtitle = context.getString(R.string.story_how_to_spend_coins_5),
                textSize = 24F,
                pageCountColor = context.resources.getColor(common.color.yellow_transparent),
                textButton = null,
                image = R.drawable.story_how_to_spend_coins_5,
                imageScaleType = ImageView.ScaleType.CENTER,
                backgroundImage = R.drawable.gradietn_yellow_light
            ),
        )
    }

    fun createHowToJoinChallenges(): List<StoryPage> {
        return listOf(
            StoryPage(
                title = context.getString(R.string.faq_screen_how_to_join_challenge),
                subtitle = null,
                textSize = 45F,
                pageCountColor = null,
                textButton = context.getString(R.string.story_screen_lets_find_out_text),
                image = R.drawable.story_join_challenge_1,
                imageScaleType = ImageView.ScaleType.CENTER,
                backgroundImage = common.drawable.gradient_blue
            ),
            StoryPage(
                title = null,
                subtitle = context.getString(R.string.story_join_challenge_2),
                textSize = 24F,
                pageCountColor = context.resources.getColor(common.color.gray_light),
                textButton = null,
                image = R.drawable.story_join_challenge_2,
                imageScaleType = ImageView.ScaleType.FIT_XY,
                backgroundImage = common.drawable.gradient_blue
            ),
            StoryPage(
                title = null,
                subtitle = context.getString(R.string.story_join_challenge_3),
                textSize = 24F,
                pageCountColor = context.resources.getColor(common.color.gray_light),
                textButton = null,
                image = R.drawable.story_join_challenge_3,
                imageScaleType = ImageView.ScaleType.FIT_XY,
                backgroundImage = common.drawable.gradient_blue
            ),
        )
    }

    fun createHowToJoinCommunity(): List<StoryPage> {
        return listOf(
            StoryPage(
                title = context.getString(R.string.faq_screen_how_to_join_community),
                subtitle = null,
                textSize = 45F,
                pageCountColor = null,
                textButton = context.getString(R.string.story_screen_here_is_how),
                image = R.drawable.story_join_community_1,
                imageScaleType = ImageView.ScaleType.CENTER,
                backgroundImage = R.drawable.gradietn_yellow_light
            ),
            StoryPage(
                title = null,
                subtitle = context.getString(R.string.story_join_community_2),
                textSize = 24F,
                pageCountColor = context.resources.getColor(common.color.yellow_transparent),
                textButton = null,
                image = R.drawable.story_join_community_2,
                imageScaleType = ImageView.ScaleType.FIT_XY,
                backgroundImage = R.drawable.gradietn_yellow_light
            ),
            StoryPage(
                title = null,
                subtitle = context.getString(R.string.story_join_community_3),
                textSize = 24F,
                pageCountColor = context.resources.getColor(common.color.yellow_transparent),
                textButton = null,
                image = R.drawable.story_join_community_3,
                imageScaleType = ImageView.ScaleType.FIT_XY,
                backgroundImage = R.drawable.gradietn_yellow_light
            ),
        )
    }
}