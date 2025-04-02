package com.yourfitness.coach.ui.features.setup.almost_there

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.yourfitness.coach.databinding.FragmentAlmostThereBinding
import com.yourfitness.common.ui.mvi.MviFragment
import com.yourfitness.common.ui.mvi.viewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AlmostThereFragment : MviFragment<AlmostThereIntent, Any, AlmostThereViewModel>() {

    override val binding: FragmentAlmostThereBinding by viewBinding()
    override val viewModel: AlmostThereViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.root.setOnClickListener { viewModel.intent.postValue(AlmostThereIntent.Next) }
    }
}