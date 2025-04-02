package com.yourfitness.coach.ui.features.facility.dialog.thirty_day_challenge

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.CreateMethod
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.kinestex.kinestexsdkkotlin.KinesteXSDK
import com.kinestex.kinestexsdkkotlin.WebViewMessage
import com.yourfitness.coach.R
import com.yourfitness.coach.databinding.DailogThirtyDayChallengeBinding
import com.yourfitness.coach.domain.models.PaymentFlow
import com.yourfitness.coach.ui.features.thirty_day_challenge.WebViewState
import com.yourfitness.coach.ui.navigation.Navigation
import com.yourfitness.common.ui.utils.setupBottomInsets
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ThirtyDayChallengeDialogFragment : BottomSheetDialogFragment() {

    private val binding: DailogThirtyDayChallengeBinding by viewBinding(createMethod = CreateMethod.BIND)
    private val viewModel: ThirtyDayChallengeViewModel by viewModels()



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.dailog_thirty_day_challenge, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupBottomInsets(binding.root)
        binding.close.setOnClickListener { findNavController().navigateUp() }

        initUiListeners()
        observeIntent()
        showLoading(false)

    }

    private fun initUiListeners() {
        binding.buttonThirtyDayChallenge.setOnClickListener { viewModel.navigator.navigate(Navigation.AccessThirtyDayChallenge(0)) }
        binding.textThirtyDayChallengeSecondaryButton.setOnClickListener {
            viewModel.navigator.navigate(Navigation.AccessThirtyDayChallenge(1))
        }
    }

    private fun observeIntent() {
        viewModel.intent.observe(viewLifecycleOwner) {
            if (it != null) {
                viewModel.intent.value = null
                viewModel.handleIntent(it)
            }
        }
    }

    fun showLoading(isLoading: Boolean) {
        val progress = requireActivity().findViewById<View>(R.id.progress)
        progress.isVisible = isLoading
    }
}