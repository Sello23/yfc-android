package com.yourfitness.coach.ui.features.profile.help_center

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import by.kirich1409.viewbindingdelegate.CreateMethod
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.yourfitness.coach.R
import com.yourfitness.coach.databinding.DialogHelpCenterBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HelpCenterDialogFragment : BottomSheetDialogFragment() {

    private val binding: DialogHelpCenterBinding by viewBinding(createMethod = CreateMethod.BIND)
    private val viewModel: HelpCenterViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.dialog_help_center, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.toolbar.title = getString(R.string.help_center_screen_feeling_a_bit_lost_text)
        binding.toolbar.toolbar.setOnMenuItemClickListener {
            onMenuItemClicked(it)
            return@setOnMenuItemClickListener true
        }
        observeIntent()
        createListeners()
    }

    private fun createListeners() {
        binding.editMail.setEndIconOnClickListener { copyMailUrl() }
        binding.buttonGotToMail.setOnClickListener { openMailApp() }
    }

    private fun copyMailUrl() {
        val mailAddress = binding.editMailText.text
        val clipboard: ClipboardManager? = context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
        val clip = ClipData.newPlainText("label", mailAddress)
        clipboard?.setPrimaryClip(clip)
        Toast.makeText(context, getString(R.string.help_center_screen_mail_copy_text, mailAddress), Toast.LENGTH_SHORT).show()
    }

    private fun openMailApp() {
        val emailIntent = Intent(
            Intent.ACTION_SENDTO, Uri.parse(
                getString(
                    R.string.help_center_screen_mail_to_text, binding.editMailText.text
                )
            )
        )
        startActivity(Intent(emailIntent))
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