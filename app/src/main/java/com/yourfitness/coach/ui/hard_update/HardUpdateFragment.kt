package com.yourfitness.coach.ui.hard_update

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.viewModels
import com.yourfitness.coach.R
import com.yourfitness.coach.databinding.FragmentHardUpdateBinding
import com.yourfitness.common.ui.mvi.MviFragment
import com.yourfitness.common.ui.mvi.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlin.random.Random

@AndroidEntryPoint
class HardUpdateFragment : MviFragment<Any, Any, HardUpdateViewModule>() {

    override val binding: FragmentHardUpdateBinding by viewBinding()
    override val viewModel: HardUpdateViewModule by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        setupListeners()
        showLoading(false)
    }

    private fun setupView() {
        val randomIndex = Random.nextInt(0, 3)
        binding.container.background = ResourcesCompat.getDrawable(resources,
            backgroundImages[randomIndex], null)

        if (viewModel.title.isNotBlank()) {
            binding.title.text = viewModel.title
        }
        if (viewModel.description.isNotBlank()) {
            binding.message.text = viewModel.description
        }
    }

    private fun setupListeners() {
        binding.buttonGotIt.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://play.google.com/store/apps/details?id=com.yourfitness.coach")
                setPackage("com.android.vending")
            }
            try {
                startActivity(intent)
            } catch (e: Exception) {
                try {
                    startActivity(Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse("https://play.google.com/store/apps/details?id=com.yourfitness.coach")
                    })
                } catch (e: Exception) {}
            }
        }
    }

    companion object {
        val backgroundImages = listOf(
            R.drawable.hard_update_1,
            R.drawable.hard_update_2,
            R.drawable.hard_update_3
        )
    }
}
