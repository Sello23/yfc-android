package com.yourfitness.coach.ui.features.profile.my_referral_code

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
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
import com.yourfitness.coach.databinding.DialogMyReferralCodeBinding
import com.yourfitness.coach.ui.utils.shareReferralCode
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MyReferralCodeDialogFragment : BottomSheetDialogFragment() {

    private val binding: DialogMyReferralCodeBinding by viewBinding(createMethod = CreateMethod.BIND)
    private val viewModel: MyReferralCodeViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.dialog_my_referral_code, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.toolbar.title = getString(R.string.my_referral_screen_referral_code_text)
        binding.toolbar.toolbar.setOnMenuItemClickListener {
            onMenuItemClicked(it)
            return@setOnMenuItemClickListener true
        }
        val referralCode = requireArguments().getString("voucher")
        val coinsToVoucherOwner = requireArguments().getString("coinsToVoucherOwner")
        binding.editCodeText.setText((referralCode.orEmpty()))
        binding.textForEachFriends.text = getString(R.string.my_referral_screen_for_each_friends_text, coinsToVoucherOwner.orEmpty())
        observeIntent()
        createListeners()
    }

    private fun createListeners() {
        binding.editCode.setEndIconOnClickListener { copyReferralCode() }
        binding.buttonShare.setOnClickListener { shareReferralCode() }
    }

    private fun copyReferralCode() {
        val referralCode = binding.editCodeText.text
        val clipboard: ClipboardManager? = context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
        val clip = ClipData.newPlainText("label", referralCode)
        clipboard?.setPrimaryClip(clip)
        Toast.makeText(context, getString(R.string.my_referral_screen_referral_code_copied_text, referralCode), Toast.LENGTH_SHORT).show()
    }

    private fun shareReferralCode() {
        val referralCode = requireArguments().getString("voucher")
        shareReferralCode(referralCode)
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