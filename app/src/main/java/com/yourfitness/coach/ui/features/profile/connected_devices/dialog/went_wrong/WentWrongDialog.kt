package com.yourfitness.coach.ui.features.profile.connected_devices.dialog.went_wrong

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import by.kirich1409.viewbindingdelegate.CreateMethod
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.yourfitness.coach.R
import com.yourfitness.coach.databinding.DialogWentWrongBinding
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class WentWrongDialog : BottomSheetDialogFragment() {

    private val binding: DialogWentWrongBinding by viewBinding(createMethod = CreateMethod.BIND)
    private val viewModel: WentWrongViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.dialog_went_wrong, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val state = errorDialogState[viewModel.type] ?: errorDialogState.values.first()
        binding.toolbar.toolbar.title =
            getString(R.string.connected_devices_screen_wait_went_wrong_text)
        binding.toolbar.toolbar.setOnMenuItemClickListener {
            onMenuItemClicked(it)
            return@setOnMenuItemClickListener true
        }
        observeIntent()
        binding.textLetsGive.text = getString(state.message)
        binding.buttonLetsGo.setOnClickListener { state.onConfirm() }
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

    private val errorDialogState = mapOf(
        ErrorType.Default to ErrorDialogState(
            R.string.connected_devices_screen_lets_give_text,
            onConfirm = {
                dialog?.dismiss()
            }
        ),
        ErrorType.HealthConnectMissing to ErrorDialogState(
            R.string.connected_devices_screen_health_connect_missing,
            onConfirm = {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.apps.healthdata")
                    setPackage("com.android.vending")
                }
                try {
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    intent.setPackage(null)
                    startActivity(intent)
                } finally {
                    dialog?.dismiss()
                }
            }
        ),
    )
}
