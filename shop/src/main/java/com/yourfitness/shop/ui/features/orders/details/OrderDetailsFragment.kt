package com.yourfitness.shop.ui.features.orders.details

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.clearFragmentResult
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.yourfitness.common.ui.mvi.MviFragment
import com.yourfitness.common.ui.mvi.viewBinding
import com.yourfitness.common.ui.utils.applyTextColorRes
import com.yourfitness.common.ui.utils.formatAmount
import com.yourfitness.shop.R
import com.yourfitness.shop.databinding.FragmentOrderDetailsBinding
import com.yourfitness.shop.databinding.FragmentServiceOrderDetailsBinding
import com.yourfitness.shop.domain.model.BaseCard
import com.yourfitness.shop.domain.model.CartCard
import com.yourfitness.shop.ui.features.orders.cart.setDivider
import com.yourfitness.shop.ui.features.orders.details.services.ServiceCard
import com.yourfitness.shop.ui.features.orders.history.RECEIVED
import dagger.hilt.android.AndroidEntryPoint

const val REQUEST_CANCEL_CONFIRMED = "130"

@AndroidEntryPoint
open class OrderDetailsFragment : MviFragment<OrderDetailsIntent, OrderDetailsState, OrderDetailsViewModel>() {

    override val binding: FragmentOrderDetailsBinding by viewBinding()
    override val viewModel: OrderDetailsViewModel by viewModels()

    private val adapter by lazy { DetailsAdapter() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        configureToolBar()
        setupRecyclerView()
        setupViews()
    }

    override fun renderState(state: OrderDetailsState) {
        when (state) {
            is OrderDetailsState.Loading -> showLoading(true)
            is OrderDetailsState.Error -> showLoading(false)
            is OrderDetailsState.DataLoaded -> {
                showLoading(false)
                showOrders(state.items)
                setupOverallView(state)
            }
            is OrderDetailsState.OrderUpdated -> {
                showLoading(false)
                showOrders(state.items)
                configureCancelButton((state.items.first() as ServiceCard).status)
            }
            is OrderDetailsState.InvoiceLoaded -> {
                showLoading(false)
                openInvoiceLink(state.link)
            }
        }
    }

    private fun configureToolBar() {
        setupToolbar(binding.toolbar.root)
        binding.toolbar.root.title = getString(R.string.details_title)
    }

    protected open fun setupRecyclerView() {
        binding.cartRecyclerView.adapter = adapter
        binding.cartRecyclerView.setDivider(R.drawable.cart_item_divider)
    }

    private fun setupViews() {
        binding.subtotal.itemSubtotal.text = getString(R.string.total)
        binding.subtotal.btnCheckout.text = getString(R.string.download_invoice)
        val invoiceDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.ic_invoice)
        binding.subtotal.btnCheckout.icon = invoiceDrawable
        binding.subtotal.btnCancel.setOnClickListener {
            viewModel.intent.value = OrderDetailsIntent.OrderCancelledRequested
        }
        binding.subtotal.btnCheckout.setOnClickListener {
            viewModel.intent.value = OrderDetailsIntent.DownloadInvoiceRequested
        }
        setFragmentResultListener(REQUEST_CONFIRM_CANCEL) { _, bundle ->
            val confirmed = bundle.getBoolean(CANCEL_CONFIRMED)
            if (confirmed) {
                setFragmentResult(REQUEST_CANCEL_CONFIRMED, bundleOf(CANCEL_CONFIRMED to true))
                findNavController().navigateUp()
            }
            clearFragmentResult(REQUEST_CONFIRM_CANCEL)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    @Suppress("UNCHECKED_CAST")
    protected open fun showOrders(data: List<BaseCard>) {
        adapter.setData(data as List<CartCard>)
        adapter.notifyDataSetChanged()
    }

    private fun setupOverallView(state: OrderDetailsState.DataLoaded) {
        binding.subtotal.itemsAmount.text = resources.getQuantityString(R.plurals.items_amount, state.items.size, state.items.size)
        binding.subtotal.currencyPrice.text = state.priceWithoutCoins.formatAmount(state.currency).uppercase()
        binding.subtotal.oldCurrencyPrice.isVisible = state.price != state.priceWithoutCoins
        binding.subtotal.currencyPrice.applyTextColorRes(
            if (binding.subtotal.oldCurrencyPrice.isVisible) R.color.card_swipe_background
            else com.yourfitness.common.R.color.black
        )
        binding.subtotal.oldCurrencyPrice.text = state.price.formatAmount()
        binding.subtotal.oldCurrencyPrice.paintFlags =
            binding.subtotal.oldCurrencyPrice.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
        binding.subtotal.coinsPrice.text = buildCoinsPricePartText(
            state.overallCoins,
            state.currency,
            state.overallPrice
        )
        binding.subtotal.coinsPrice.applyTextColorRes(
            if (state.overallCoins == 0L) com.yourfitness.common.R.color.gray_light
            else com.yourfitness.common.R.color.black
        )
        configureCancelButton(state.orderStatus)
    }

    protected open fun configureCancelButton(orderStatus: String) {
        binding.subtotal.btnCancel.isVisible = orderStatus.lowercase() == RECEIVED
    }

    private fun buildCoinsPricePartText(
        coins: Long,
        currency: String,
        currencyValue: Long
    ): String {
        val currencyPart = currencyValue.formatAmount(currency).uppercase()
        return getString(R.string.cart_screen_redemption_data, coins, currencyPart)
    }

    private fun openInvoiceLink(link: String) {
        startActivity(Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(link)
        })
    }
}
