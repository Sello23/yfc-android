package com.yourfitness.coach.ui.features.thirty_day_challenge

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.kinestex.kinestexsdkkotlin.GenericWebView
import com.kinestex.kinestexsdkkotlin.KinesteXSDK
import com.kinestex.kinestexsdkkotlin.WebViewMessage
import com.yourfitness.coach.databinding.FragmentAccessThirtyDayChallengeBinding
import com.yourfitness.common.CommonPreferencesStorage
import com.yourfitness.common.ui.mvi.MviFragment
import com.yourfitness.common.ui.mvi.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
open class AccessThirtyDayChallenge :
    MviFragment<Any, Any, AccessThirtyDayChallengeViewModel>() {

    private var tvMistake: TextView? = null
    private var tvReps: TextView? = null

    private var webView: GenericWebView? = null

    private val apiKey =
        "599d52c66f62615b9e5dce54cd72d31f434dbea75ae82936acb30a345b7573e7" // store this key securely
    private val company = "YFC"
    private val userId = "user1"

    override val binding: FragmentAccessThirtyDayChallengeBinding by viewBinding()
    override val viewModel: AccessThirtyDayChallengeViewModel by viewModels()


    @Inject
    lateinit var commonStorage: CommonPreferencesStorage

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observe()
        createWebView()

        activity?.fragmentManager?.popBackStackImmediate();
        activity?.fragmentManager?.popBackStackImmediate();

    }


    private fun createPlanWebView() {
        val data = mutableMapOf<String, Any>()
        data["style"] = "light"

        webView = KinesteXSDK.createPlanView(
            requireActivity(),
            apiKey,
            company,
            userId,
            "YFC Plan",
            null,
            customParams = data,
            viewModel.isLoading,
            ::handleWebViewMessage
        ) as GenericWebView?

        hideSystemUI()
        binding.layoutWebView.addView(webView?.let { setLayoutParamsFullScreen(it) })
    }

    private fun createHowToWebView() {
        val howToView = KinesteXSDK.createHowToView(
            context = requireActivity(),
            onVideoEnd = { didEnd ->
                if (didEnd) {
                    binding.layoutWebView.removeAllViews()
                    requireActivity().supportFragmentManager.popBackStack()
                }
            },
            videoURL = "https://cdn.kinestex.com/SDK%2Fhow-to-video%2Ffinal%20light%20theme.mp4?alt=media&token=a0284982-f17b-4415-b109-36a7c623f982",
            onCloseClick = {
                binding.layoutWebView.removeAllViews()
                requireActivity().supportFragmentManager.popBackStack()
            }
        )

        hideSystemUI()
        binding.layoutWebView.addView(setLayoutParamsFullScreen(howToView))
    }

    private fun createWebView() {
        val selectedOption = requireArguments().get("kinestexNavId") as Int

        when (selectedOption) {
            0 -> {
                createPlanWebView()
            }

            1 -> {
                createHowToWebView()
            }

            else -> {}
        }
    }


    private fun handleWebViewMessage(message: WebViewMessage) {
        when (message) {
            is WebViewMessage.ExitKinestex -> lifecycleScope.launch {
                requireActivity().supportFragmentManager.popBackStack()
                viewModel.showWebView.emit(
                    WebViewState.ERROR
                )
            }


            is WebViewMessage.Reps -> {
                (message.data["value"] as? Int)?.let { viewModel.setReps(it) }
            }

            is WebViewMessage.Mistake -> {
                (message.data["value"] as? String)?.let {
                    viewModel.setMistake(
                        it
                    )
                }
            }

            else -> {
                Log.d("Message received", message.toString())
            }
        }
    }

    private fun observe() {
        lifecycleScope.launch {
            viewModel.showWebView.collect { state ->
                when (state) {
                    WebViewState.LOADING -> { /* show loading */
                    }

                    WebViewState.ERROR -> binding.layoutWebView.removeAllViews()
                        .also { binding.layoutWebView.visibility = View.GONE }

                    WebViewState.SUCCESS -> {
                        webView?.let { binding.layoutWebView.addView(setLayoutParamsFullScreen(it)) }
                        hideSystemUI()
                    }
                }
            }
        }
    }

    private fun setLayoutParamsFullScreen(view: View): View {
        view.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
        )
        return view
    }

    private fun hideSystemUI() {
        activity?.window?.decorView?.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                )
    }
}