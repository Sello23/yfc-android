package com.yourfitness.coach.ui.features.progress.coin_reward

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.annotation.DrawableRes
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.yourfitness.coach.R
import com.yourfitness.coach.databinding.FragmentRewardBinding
import com.yourfitness.coach.domain.entity.CoinReward
import com.yourfitness.coach.domain.entity.CreditReward
import com.yourfitness.coach.domain.entity.LevelUpReward
import com.yourfitness.coach.domain.entity.Reward
import com.yourfitness.coach.domain.entity.SubscriptionVoucherReward
import com.yourfitness.coach.domain.entity.VoucherReward
import com.yourfitness.coach.ui.features.sign_up.confirm_phone.toHtmlSpanned
import com.yourfitness.coach.ui.navigation.Navigation
import com.yourfitness.coach.ui.utils.startBackgroundAnimation
import com.yourfitness.coach.ui.utils.startViewAnimation
import com.yourfitness.common.ui.mvi.MviFragment
import com.yourfitness.common.ui.mvi.viewBinding
import com.yourfitness.common.ui.utils.doubleToStringNoDecimal
import com.yourfitness.common.ui.utils.formatProgressAmount
import com.yourfitness.common.ui.utils.setCompoundDrawables
import com.yourfitness.common.ui.utils.setupBottomInsets
import com.yourfitness.common.ui.utils.setupTopInsets
import com.yourfitness.common.ui.utils.toImageUri
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RewardFragment : MviFragment<Any, Any, RewardViewModel>() {

    override val binding: FragmentRewardBinding by viewBinding()
    override val viewModel: RewardViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar(binding.toolbar.toolbar)
        setupTopInsets(binding.toolbar.root)
        setupBottomInsets(binding.content)
        binding.layoutRewardCard.buttonGreat.setOnClickListener {
           if (viewModel.isStartup) viewModel.navigator.navigate(Navigation.AlmostThere)
            else findNavController().navigateUp()
        }

        if (viewModel.isStartup) {
            requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
                viewModel.navigator.navigate(Navigation.AlmostThere)
            }
        }
    }

    override fun setupToolbar(toolbar: Toolbar) {
        super.setupToolbar(toolbar)

        toolbar.setNavigationOnClickListener {
            if (viewModel.isStartup) viewModel.navigator.navigate(Navigation.AlmostThere)
            else findNavController().navigateUp()
        }
    }

    override fun onDetach() {
        super.onDetach()
        if (viewModel.reward is SubscriptionVoucherReward) {
            setFragmentResult(REQUEST_KEY_DETACH_EVENT, requireArguments())
        }
    }

    override fun renderState(state: Any) {
        when (state) {
            is CoinRewardState.Success -> showReward(state.reward)
        }
    }

    private fun showReward(reward: Reward) {
        when (reward) {
            is CoinReward -> showCoinReward(reward)
            is CreditReward -> showCreditReward(reward)
            is VoucherReward -> showVoucherReward(reward)
            is SubscriptionVoucherReward -> showSubscriptionVoucherReward(reward)
            is LevelUpReward -> showLevelUpReward(reward)
        }
    }

    private fun showVoucherReward(reward: VoucherReward) {
        binding.layoutCoinReward.root.isVisible = true
        binding.layoutCoinReward.textTitle.text = getString(R.string.coin_reward_yay)
        binding.layoutCoinReward.textMessage.text = getString(reward.msg)
        binding.layoutRewardCard.root.setBackgroundResource(R.drawable.background_coin_reward_card)
        binding.layoutRewardCard.textPrizeLabel.text = getString(R.string.coin_reward_your_gift)
        binding.layoutRewardCard.textPrize.text = reward.coins.toString()
        binding.layoutRewardCard.textPrize.setCompoundDrawables(start = R.drawable.ic_reward_coins)
        showVoucherCoin()
    }

    private fun showSubscriptionVoucherReward(reward: SubscriptionVoucherReward) {
        binding.layoutCoinReward.root.isVisible = true
        binding.layoutCoinReward.textTitle.text = getString(R.string.coin_reward_yay)
        binding.layoutCoinReward.textMessage.text = getString(R.string.coin_reward_received_bonus_coins_via_voucher)
        binding.layoutRewardCard.root.setBackgroundResource(R.drawable.background_coin_reward_card)
        binding.layoutRewardCard.textPrizeLabel.text = getString(R.string.coin_reward_your_gift)
        binding.layoutRewardCard.textPrize.text = reward.coins.toString()
        binding.layoutRewardCard.textPrize.setCompoundDrawables(start = R.drawable.ic_reward_coins)
        showVoucherCoin()
    }

    private fun showCreditReward(reward: CreditReward) {
        binding.layoutCreditReward.root.isVisible = true
        binding.layoutCreditReward.textMessage.text = getString(R.string.coin_reward_great_news_you_new_on_level_now, reward.name).toHtmlSpanned()
        binding.layoutCreditReward.textStudio.text = reward.studio.name
        Glide.with(this).load(reward.studio.icon?.toImageUri()).into(binding.layoutCreditReward.imageIcon)
        val text = if (reward.isFirst) R.string.coin_reward_your_first_prize else R.string.coin_reward_your_prize
        binding.layoutRewardCard.root.setBackgroundResource(R.drawable.background_credit_reward_card)
        binding.layoutRewardCard.textPrizeLabel.text = getString(text)
        binding.layoutRewardCard.textPrize.text = reward.credits.toString()
        binding.layoutRewardCard.textPrize.setCompoundDrawables(start = R.drawable.ic_reward_credits)
        when (reward.name) {
            "Bronze" -> showCredit(R.drawable.anim_reward_credit_bronze_animated)
            "Silver" -> showCredit(R.drawable.anim_reward_credit_silver_animated)
            "Gold" -> showCredit(R.drawable.anim_reward_credit_gold_animated)
            "Diamond" -> showDiamond()
        }
    }

    private fun showCoinReward(reward: CoinReward) {
        binding.layoutCoinReward.root.isVisible = true
        binding.layoutCoinReward.textMessage.text =
            getString(R.string.coin_reward_you_have_reached_your_goal, doubleToStringNoDecimal(reward.points.toDouble()))
        val text = if (reward.isFirst) R.string.coin_reward_your_first_prize else R.string.coin_reward_your_prize
        binding.layoutRewardCard.root.setBackgroundResource(R.drawable.background_coin_reward_card)
        binding.layoutRewardCard.textPrizeLabel.text = getString(text)
        binding.layoutRewardCard.textPrize.text = reward.coins.toString()
        binding.layoutRewardCard.textPrize.setCompoundDrawables(start = R.drawable.ic_reward_coins)
        if (reward.isFirst || reward.isCoin) showCoin() else showCup()
    }

    private fun showLevelUpReward(reward: LevelUpReward) {
        binding.layoutCreditReward.root.isVisible = true
        val pointsFormatted = resources.getQuantityString(R.plurals.points_plural_format, reward.points.toInt(), reward.points.formatProgressAmount())
        binding.layoutCreditReward.levelMessage.text = getString(R.string.level_reward_msg, reward.level, pointsFormatted).toHtmlSpanned()
        binding.layoutCreditReward.textLevelCongrats.text = getString(R.string.leveled_up_msg)
        binding.layoutCreditReward.imageIcon.isVisible = false
        binding.layoutCreditReward.textTitle.isVisible = false
        binding.layoutCreditReward.textSubtitle.isVisible = false
        binding.layoutCreditReward.textStudio.isVisible = false
        binding.layoutCreditReward.textMessage.isVisible = false
        binding.layoutCreditReward.textLevelCongrats.isVisible = true
        binding.layoutCreditReward.imageLevelReward.isVisible = true
        Glide.with(this).load(reward.imageId.toImageUri()).into(binding.layoutCreditReward.imageLevelReward)
        binding.layoutRewardCard.root.setBackgroundResource(R.drawable.background_coin_reward_card)
        binding.layoutRewardCard.textPrizeLabel.text = getString(R.string.coin_reward_your_prize)
        binding.layoutRewardCard.textPrize.text = reward.coins.toString()
        binding.layoutRewardCard.textPrize.setCompoundDrawables(start = R.drawable.ic_reward_coins)

        showLevelLogo()
    }

    private fun showCoin() {
        binding.layoutCoinReward.textTitle.text = getString(R.string.coin_reward_thats_a_great_start)
        binding.layoutCoinReward.imageReward.setImageResource(R.drawable.anim_reward_coin_animated)
        binding.layoutCoinReward.imageReward.startBackgroundAnimation()
        binding.layoutCoinReward.imageCoin.isVisible = true
        binding.layoutCoinReward.imageCoin.startViewAnimation(R.anim.scale_aftera_delay)
    }

    private fun showCup() {
        binding.layoutCoinReward.textTitle.text = getString(R.string.coin_reward_thats_a_next_level_performance)
        binding.layoutCoinReward.imageReward.setImageResource(R.drawable.anim_reward_cup_animated)
        binding.layoutCoinReward.imageReward.startBackgroundAnimation()
        binding.layoutCoinReward.imageCup.isVisible = true
        binding.layoutCoinReward.imageCup.startViewAnimation(R.anim.scale_aftera_delay)
    }

    private fun showCredit(@DrawableRes animation: Int) {
        binding.layoutCreditReward.imageReward.setImageResource(animation)
        binding.layoutCreditReward.imageReward.startBackgroundAnimation()
    }

    private fun showDiamond() {
        binding.layoutCreditReward.imageReward.setImageResource(R.drawable.anim_reward_diamond_animated)
        binding.layoutCreditReward.imageReward.startBackgroundAnimation()
        binding.layoutCreditReward.imageDiamond.isVisible = true
        binding.layoutCreditReward.imageDiamond.startViewAnimation(R.anim.scale_aftera_delay)
    }

    private fun showVoucherCoin() {
        binding.layoutCoinReward.imageReward.setImageResource(R.drawable.anim_reward_cup_animated)
        binding.layoutCoinReward.imageReward.startBackgroundAnimation()
        binding.layoutCoinReward.imageCoin.isVisible = true
        binding.layoutCoinReward.imageCoin.startViewAnimation(R.anim.scale_aftera_delay)
    }

    private fun showLevelLogo() {
        binding.layoutCreditReward.imageReward.setImageResource(R.drawable.anim_reward_coin_animated)
        binding.layoutCreditReward.imageReward.startBackgroundAnimation()
        binding.layoutCreditReward.imageDiamond.isVisible = false
    }

    companion object {
        const val REQUEST_KEY_DETACH_EVENT = "request_key_detach_event"
    }
}
