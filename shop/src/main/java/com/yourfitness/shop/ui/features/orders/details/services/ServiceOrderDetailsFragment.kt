package com.yourfitness.shop.ui.features.orders.details.services

import android.os.Bundle
import android.text.SpannableString
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.clearFragmentResult
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.yourfitness.common.ui.utils.getColorCompat
import com.yourfitness.common.ui.utils.setColor
import com.yourfitness.common.ui.utils.toImageUri
import com.yourfitness.shop.R
import com.yourfitness.shop.domain.model.BaseCard
import com.yourfitness.shop.ui.features.orders.details.OrderDetailsFragment
import com.yourfitness.shop.ui.features.orders.details.OrderDetailsIntent
import com.yourfitness.shop.ui.features.orders.details.ServiceOrderDetailsViewModel
import com.yourfitness.shop.ui.features.orders.history.REQUEST_DETAILS_CLOSED
import com.yourfitness.shop.ui.features.orders.history.UNCLAIMED
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ServiceOrderDetailsFragment : OrderDetailsFragment() {

    override val viewModel: ServiceOrderDetailsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setFragmentResultListener(REQUEST_VOUCHER_CLAIMED) { _, bundle ->
            val confirmed = bundle.getBoolean(VOUCHER_CLAIMED)
            if (confirmed) {
                val number = bundle.getString(VOUCHER_NUMBER).orEmpty()
                val name = bundle.getString(SERVICE_NAME).orEmpty()
                viewModel.intent.postValue(OrderDetailsIntent.OrderClaimSuccessfully(number, name))
            }
            clearFragmentResult(REQUEST_VOUCHER_CLAIMED)
        }
    }

    override fun onDestroy() {
        setFragmentResult(REQUEST_DETAILS_CLOSED, bundleOf())
        super.onDestroy()
    }

    override fun setupRecyclerView() {
        binding.cartRecyclerView.isVisible = false
        binding.subtotal.dot.visibility = View.INVISIBLE
        binding.subtotal.itemsAmount.visibility = View.INVISIBLE
    }

    override fun showOrders(data: List<BaseCard>) {
        binding.serviceContainer.root.isVisible = true
        val serviceData = data.first() as ServiceCard
        Glide.with(this).load(serviceData.image.toImageUri()).into(binding.serviceContainer.image)
        binding.serviceContainer.productTitle.text = serviceData.title
        binding.serviceContainer.serviceName.text = serviceData.subtitle
        binding.serviceContainer.textAddress.text = serviceData.address
        binding.serviceContainer.textPhone.text = serviceData.phone
        if (serviceData.logo != null) {
            Glide.with(this).load(serviceData.logo.toImageUri())
                .into(binding.serviceContainer.serviceLogo)
        }
        binding.serviceContainer.orderDate.text =
            getString(R.string.order_date_formatted, serviceData.orderDate)
        binding.serviceContainer.claimDate.isVisible = serviceData.claimDate != null
        if (serviceData.claimDate != null) {
            binding.serviceContainer.claimDate.text =
                getString(R.string.claim_date_formatted, serviceData.claimDate)
        } else {
            binding.serviceContainer.btnClaim.setOnClickListener {
                viewModel.intent.value = OrderDetailsIntent.OrderClaimRequested
            }
        }
        binding.serviceContainer.status.text =
            buildStatusText(serviceData.status, serviceData.statusValue)
        binding.serviceContainer.btnClaim.isVisible = serviceData.status.lowercase() == UNCLAIMED
    }

    override fun configureCancelButton(orderStatus: String) {
        binding.subtotal.btnCancel.isVisible = orderStatus.lowercase() == UNCLAIMED
    }

    private fun buildStatusText(statusValue: String, status: Int): SpannableString {
        val text = requireContext().getString(R.string.item_status, statusValue)
        return SpannableString(text).apply {
            val start = text.indexOf(":")
            val color = requireContext().getColorCompat(getStatusColor(status))
            setColor(color, start + 1, text.length)
        }
    }

    private fun getStatusColor(status: Int): Int {
        return when (status) {
            0 -> R.color.status_text_delivered
            1 -> com.yourfitness.common.R.color.main_active
            2 -> com.yourfitness.common.R.color.issue_red
            else -> com.yourfitness.common.R.color.grey
        }
    }
}

data class ServiceCard(
    val uuid: String,
    val image: String,
    val logo: String?,
    val title: String,
    val subtitle: String,
    val address: String,
    val phone: String,
    val orderDate: String,
    val claimDate: String?,
    val statusValue: Int,
    val status: String
) : BaseCard()
