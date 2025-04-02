package com.yourfitness.coach.ui.features.facility.dialog.no_permission

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import by.kirich1409.viewbindingdelegate.CreateMethod
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.yourfitness.coach.R
import com.yourfitness.coach.databinding.DialogNoCameraPermissionBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NoCameraPermissionDialogFragment : BottomSheetDialogFragment() {

    private val binding: DialogNoCameraPermissionBinding by viewBinding(createMethod = CreateMethod.BIND)
    private val viewModel: NoPermissionViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.dialog_no_camera_permission, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.toolbarDialog.setOnMenuItemClickListener {
            onMenuItemClicked(it)
            return@setOnMenuItemClickListener true
        }
        observeIntent()
        binding.buttonGotToMail.setOnClickListener {
            showPermissionSettings()
            dialog?.dismiss()
        }
    }

    private fun showPermissionSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri: Uri = Uri.fromParts("package", activity?.packageName, null)
        intent.data = uri
        startActivity(intent)
    }

    private fun onMenuItemClicked(item: MenuItem) {
        when (item.itemId) {
            R.id.close -> dismiss()
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
}