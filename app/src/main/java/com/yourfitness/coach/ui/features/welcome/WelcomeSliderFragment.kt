package com.yourfitness.coach.ui.features.welcome

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.fragment.app.viewModels
import androidx.viewpager2.widget.ViewPager2
import com.yourfitness.coach.R
import com.yourfitness.coach.databinding.FragmentWelcomeSliderBinding
import com.yourfitness.coach.domain.models.Slide
import com.yourfitness.common.ui.mvi.MviFragment
import com.yourfitness.common.ui.mvi.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import com.yourfitness.common.R as common

@AndroidEntryPoint
class WelcomeSliderFragment : MviFragment<WelcomeSliderIntent, WelcomeSliderState, WelcomeSliderViewModel>() {

    override val binding: FragmentWelcomeSliderBinding by viewBinding()
    override val viewModel: WelcomeSliderViewModel by viewModels()
    private val adapter by lazy { WelcomeSliderAdapter() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewpager()
        createListeners()
    }

    override fun renderState(state: WelcomeSliderState) {
        when (state) {
            is WelcomeSliderState.ConnectionError -> showConnectionErrorMsg()
        }
    }

    private fun createListeners() {
        binding.buttonSignUp.setOnClickListener { checkCurrentSlidePosition() }
        binding.textHaveAccount.setOnClickListener { openWelcomeBackFragment() }
    }

    private fun checkCurrentSlidePosition() {
        if (binding.viewPager.currentItem + 1 < adapter.itemCount) {
            binding.viewPager.currentItem++
        } else {
            val intent = WelcomeSliderIntent.SignUp
            viewModel.intent.postValue(intent)
        }
    }

    private fun openWelcomeBackFragment() {
        val intent = WelcomeSliderIntent.HaveAccount
        viewModel.intent.postValue(intent)
    }

    private fun setupViewpager() {
        adapter.setData(setupSlides())
        binding.viewPager.adapter = adapter
        setupIndicators()
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                setCurrentIndicators(position)
                changeButtonText()
            }
        })
    }

    private fun setupSlides(): List<Slide> {
        return listOf(
            Slide(
                getString(R.string.welcome_slider_screen_premium_gym_locations_text),
                getString(R.string.welcome_slider_screen_smash_your_text),
                R.drawable.image_welcome_1_update,
            ),
            Slide(
                getString(R.string.welcome_slider_screen_cancel_anytime_text),
                getString(R.string.welcome_slider_screen_one_app_text),
                R.drawable.image_welcome_2_update,
            ),
            Slide(
                getString(R.string.welcome_slider_screen_leaderboard_challenge_text),
                getString(R.string.welcome_slider_screen_connect_your_text),
                R.drawable.image_welcome_3_update,
            ),
        )
    }

    private fun changeButtonText() {
        if (binding.viewPager.currentItem == LAST_SLIDE) {
            binding.buttonSignUp.text = getString(R.string.welcome_slider_screen_sign_up_button)
        } else {
            binding.buttonSignUp.text = getString(R.string.welcome_slider_screen_got_it_button)
        }
    }

    private fun setupIndicators() {
        val indicators = arrayOfNulls<ImageView>(adapter.itemCount)
        val layoutParams: LinearLayout.LayoutParams =
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        layoutParams.setMargins(16, 0, 16, 0)
        for (i in indicators.indices) {
            indicators[i] = ImageView(requireActivity())
            indicators[i].apply {
                this?.setImageDrawable(
                    (ContextCompat.getDrawable(requireActivity(), common.drawable.ic_ellipse_671_inactive))
                )
                this?.layoutParams = layoutParams
            }
            binding.indicators.addView(indicators[i])
        }
    }

    private fun setCurrentIndicators(index: Int) {
        val childCount = binding.indicators.childCount
        for (i in 0 until childCount) {
            val imageView = binding.indicators[i] as ImageView
            if (i == index) {
                imageView.setImageDrawable(
                    (ContextCompat.getDrawable(
                        requireActivity(), common.drawable.ic_ellipse_670_active
                    ))
                )
            } else {
                imageView.setImageDrawable(
                    (ContextCompat.getDrawable(
                        requireActivity(), common.drawable.ic_ellipse_671_inactive
                    ))
                )
            }
        }
    }

    companion object {
        private const val LAST_SLIDE = 2
    }
}