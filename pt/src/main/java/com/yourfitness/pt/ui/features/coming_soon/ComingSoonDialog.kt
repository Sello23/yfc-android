package com.yourfitness.pt.ui.features.coming_soon

import android.os.Bundle
import android.view.View
import com.yourfitness.common.ui.base.BaseBottomSheetDialogFragment
import com.yourfitness.common.ui.mvi.viewBinding
import com.yourfitness.common.ui.utils.setupBottomInsets
import com.yourfitness.pt.databinding.DialogComingSoonBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ComingSoonDialog : BaseBottomSheetDialogFragment() {

    override val binding: DialogComingSoonBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupBottomInsets(binding.root)
        binding.close.setOnClickListener { dismiss() }
        binding.gotIt.setOnClickListener { dismiss() }
    }
}
