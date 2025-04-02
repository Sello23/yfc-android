package com.yourfitness.coach.ui.features.progress.points_hint

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.CreateMethod
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.yourfitness.coach.R
import com.yourfitness.coach.databinding.DialogPointsHintBinding
import com.yourfitness.common.ui.utils.formatTwoDecimalSigns
import com.yourfitness.common.ui.utils.setupBottomInsets
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PointsHintFragment : BottomSheetDialogFragment() {

    private val binding: DialogPointsHintBinding by viewBinding(createMethod = CreateMethod.BIND)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.dialog_points_hint, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupBottomInsets(binding.root)
        binding.btnClose.setOnClickListener { findNavController().navigateUp() }
        binding.buttonActionPrimary.setOnClickListener { dismiss() }

        showHint()
    }

    private fun showHint() {
        val perStep = requireArguments().getDouble("pointsPerStep")
        val perCalorie = requireArguments().getDouble("pointsPerCalorie")
        binding.countInfo.stepValue.text = resources.getQuantityString(
            R.plurals.points_plural_format,
            perStep.toInt(),
            perStep.formatTwoDecimalSigns()
        )
        binding.countInfo.calorieValue.text = resources.getQuantityString(
            R.plurals.points_plural_format,
            perCalorie.toInt(),
            perCalorie.formatTwoDecimalSigns()
        )
    }
}
