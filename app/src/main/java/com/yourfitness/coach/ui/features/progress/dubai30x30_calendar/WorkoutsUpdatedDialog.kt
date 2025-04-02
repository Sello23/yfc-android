package com.yourfitness.coach.ui.features.progress.dubai30x30_calendar

import android.os.Bundle
import android.view.View
import com.yourfitness.common.databinding.DialogOneActionBinding
import com.yourfitness.common.ui.base.BaseBottomSheetDialogFragment
import com.yourfitness.common.ui.mvi.viewBinding
import com.yourfitness.common.ui.utils.setupBottomInsets
import com.yourfitness.shop.R
import com.yourfitness.shop.ui.navigation.ShopNavigator
import javax.inject.Inject

class WorkoutsUpdatedDialog : BaseBottomSheetDialogFragment() {

    override val binding: DialogOneActionBinding by viewBinding()

    @Inject
    lateinit var navigator: ShopNavigator

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupBottomInsets(binding.root)
        setupToolbar(
            binding.toolbar.root,
            getString(R.string.workouts_updated_title),
            dismissId = com.yourfitness.common.R.id.close
        )
        binding.message.text = getString(R.string.workouts_updated_msg)
        binding.buttonConfirm.setOnClickListener { dismiss() }
    }
}
