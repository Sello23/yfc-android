package com.yourfitness.shop.ui.features.orders.details.services

import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.CreateMethod
import by.kirich1409.viewbindingdelegate.viewBinding
import com.bumptech.glide.Glide
import com.yourfitness.common.domain.date.formatted
import com.yourfitness.common.domain.date.toDate
import com.yourfitness.common.domain.date.toMilliseconds
import com.yourfitness.common.ui.utils.toImageUri
import com.yourfitness.shop.R
import com.yourfitness.shop.databinding.DialogClaimedVoucherBinding
import com.yourfitness.shop.network.dto.VoucherResponse
import com.yourfitness.shop.ui.constants.Constants.Companion.CLAIM_DATA
import dagger.hilt.android.AndroidEntryPoint

const val REQUEST_VOUCHER_CLAIMED = "369"
const val VOUCHER_CLAIMED = "voucher_claimed"
const val VOUCHER_NUMBER = "voucher_number"
const val SERVICE_NAME = "service_name"

@AndroidEntryPoint
class VoucherClaimedDialog : DialogFragment() {

    private val binding: DialogClaimedVoucherBinding by viewBinding(createMethod = CreateMethod.BIND)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.dialog_claimed_voucher, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val data: VoucherResponse? = requireArguments().getParcelable(CLAIM_DATA)

        binding.buttonClose.setOnClickListener { dismiss() }
        binding.buttonSuccessfullyPassed.setOnClickListener { dismiss() }
        setupData(data)
        showDialog()
    }

    override fun onDismiss(dialog: DialogInterface) {
        val data: VoucherResponse? = requireArguments().getParcelable(CLAIM_DATA)

        setFragmentResult(
            REQUEST_VOUCHER_CLAIMED,
            bundleOf(
                VOUCHER_CLAIMED to true, VOUCHER_NUMBER to data?.number.orEmpty(),
                SERVICE_NAME to data?.productName.orEmpty()
            )
        )

        findNavController().navigateUp()
    }

    private fun showDialog() {
        dialog?.setCancelable(false)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    private fun setupData(data: VoucherResponse?) {
        if (data == null) return

        Glide.with(this).load(data.clientImage.toImageUri()).into(binding.imageService)
        binding.textUsername.text = data.clientName
        binding.textDate.text = data.clientBirthday.toMilliseconds().toDate().formatted()
        binding.textServiceName.text = data.productName
        binding.textVendorName.text = data.vendorName
        binding.textAddress.text = data.vendorAddress
        binding.textPhone.text = data.vendorPhone
        Glide.with(this).load(data.vendorDefaultImageId.toImageUri()).into(binding.serviceLogo)
        binding.voucherNumber.text = data.number
    }
}
