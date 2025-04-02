package com.yourfitness.coach.ui.features.facility.dialog.something_went_wrong_gym

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import by.kirich1409.viewbindingdelegate.CreateMethod
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.yourfitness.coach.R
import com.yourfitness.coach.databinding.DialogSomethingWentWrongGymDialogBinding
import com.yourfitness.coach.ui.navigation.Navigation
import com.yourfitness.coach.ui.navigation.Navigator
import com.yourfitness.common.ui.utils.setupBottomInsets
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.yourfitness.common.R as common

@AndroidEntryPoint
class SomethingWentWrongGymDialogFragment : BottomSheetDialogFragment() {

    private val binding: DialogSomethingWentWrongGymDialogBinding by viewBinding(createMethod = CreateMethod.BIND)

    @Inject
    lateinit var navigator: Navigator

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.dialog_something_went_wrong_gym_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupBottomInsets(binding.root)
        binding.toolbar.toolbar.title = getString(common.string.error_something_went_wrong)
        binding.toolbar.toolbar.setOnMenuItemClickListener {
            onMenuItemClicked(it)
            return@setOnMenuItemClickListener true
        }

        val isPtRole = requireArguments().getBoolean("is_pt_role") ?: false
        binding.buttonFindYfcFacility.apply {
            isVisible = !isPtRole
            setOnClickListener {
                navigator.navigate(Navigation.Map())
            }
        }
    }

    private fun onMenuItemClicked(item: MenuItem) {
        when (item.itemId) {
            R.id.close -> dismiss()
        }
    }
}
