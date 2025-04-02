package com.yourfitness.coach.ui.features.profile.connected_devices.dialog.need_permission

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import by.kirich1409.viewbindingdelegate.CreateMethod
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.yourfitness.coach.R
import com.yourfitness.coach.databinding.DialogNeedPermissionBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NeedPermissionDialog : BottomSheetDialogFragment() {

    private val binding: DialogNeedPermissionBinding by viewBinding(createMethod = CreateMethod.BIND)
    private var isOpenSettingsClicked = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.dialog_need_permission, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        

        binding.toolbar.toolbar.title = getString(R.string.need_permission_header)
        binding.toolbar.toolbar.setOnMenuItemClickListener {
            onMenuItemClicked(it)
            return@setOnMenuItemClickListener true
        }
        binding.buttonGetStarted.setOnClickListener {
            isOpenSettingsClicked = true
            dismiss()
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        if (isOpenSettingsClicked) {
            setFragmentResult(REQUEST_CODE, bundleOf(BOOLEAN_PAIR to false))
        } else {
            setFragmentResult(REQUEST_CODE, bundleOf(BOOLEAN_PAIR to true))
        }
    }

    private fun onMenuItemClicked(item: MenuItem) {
        when (item.itemId) {
            R.id.close -> dismiss()
        }
    }

    companion object {
        const val REQUEST_CODE = "need_permission"
        const val BOOLEAN_PAIR = "open"
    }
}