package com.yourfitness.shop.ui.features.orders.history

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.clearFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import com.google.android.material.tabs.TabLayout
import com.yourfitness.common.ui.mvi.MviFragment
import com.yourfitness.common.ui.mvi.viewBinding
import com.yourfitness.common.ui.utils.addOnTabSelectionListener
import com.yourfitness.common.ui.utils.selectTab
import com.yourfitness.shop.R
import com.yourfitness.shop.databinding.FragmentOrdersHistoryBinding
import com.yourfitness.shop.ui.features.orders.details.CANCEL_CONFIRMED
import com.yourfitness.shop.ui.features.orders.details.OrderDetailsIntent
import com.yourfitness.shop.ui.features.orders.details.REQUEST_CANCEL_CONFIRMED
import com.yourfitness.shop.ui.features.orders.details.services.REQUEST_VOUCHER_CLAIMED
import com.yourfitness.shop.ui.features.orders.details.services.SERVICE_NAME
import com.yourfitness.shop.ui.features.orders.details.services.VOUCHER_CLAIMED
import com.yourfitness.shop.ui.features.orders.details.services.VOUCHER_NUMBER
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

const val REQUEST_DETAILS_CLOSED = "148"

@AndroidEntryPoint
class OrdersHistoryFragment :
    MviFragment<OrdersHistoryIntent, OrdersHistoryState, OrdersHistoryViewModel>() {

    override val binding: FragmentOrdersHistoryBinding by viewBinding()
    override val viewModel: OrdersHistoryViewModel by viewModels()

    private val adapter by lazy {
        OrdersListAdapter(::onOrderClick, ::loadColor, ::onClaimClick)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        configureToolBar()
        setupConfigureView()
        setupRecyclerView()
        showTab(viewModel.orderClassification)

        setFragmentResultListener(REQUEST_VOUCHER_CLAIMED) { _, bundle ->
            val confirmed = bundle.getBoolean(VOUCHER_CLAIMED)
            if (confirmed) {
                val number = bundle.getString(VOUCHER_NUMBER).orEmpty()
                val name = bundle.getString(SERVICE_NAME).orEmpty()
                viewModel.intent.postValue(OrdersHistoryIntent.OrderClaimSuccessfully(number, name))
            }
            clearFragmentResult(REQUEST_VOUCHER_CLAIMED)
        }
    }

    override fun renderState(state: OrdersHistoryState) {
        when (state) {
            is OrdersHistoryState.Loading -> showLoading(true)
            is OrdersHistoryState.Error -> showError(state.error)
            is OrdersHistoryState.DataLoaded -> {
                showLoading(false)
                updateConfigureView(state.sorted, state.filtered)
                if (state.orderClassification == OrderClassification.GOODS) {
                    showOrders(state.orders, state.orderClassification)
                } else {
                    showOrders(state.serviceOrders, state.orderClassification)
                }
            }
        }
    }

    private fun configureToolBar() {
        setupToolbar(binding.toolbar.root)
        binding.toolbar.toolbarSolid.title = getString(R.string.orders_title)
    }

    private fun setupConfigureView() {
        binding.tabLayout.addOnTabSelectionListener { onTabSelected(it) }
        binding.configuration.sort.setOnClickListener {
            viewModel.intent.value = OrdersHistoryIntent.SortTapped
        }
        binding.configuration.filter.setOnClickListener {
            viewModel.intent.value = OrdersHistoryIntent.FilterTapped
        }
    }

    private fun updateConfigureView(sorted: Boolean, filtered: Boolean) {
        binding.configuration.filter.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                if (filtered) com.yourfitness.common.R.color.main_active else com.yourfitness.common.R.color.text_gray
            )
        )

        val invoiceDrawable = ContextCompat.getDrawable(
            requireContext(),
            if (!sorted) com.yourfitness.common.R.drawable.ic_toolbar_sort_reversed else com.yourfitness.common.R.drawable.ic_toolbar_sort
        )
        binding.configuration.sort.setCompoundDrawablesWithIntrinsicBounds(invoiceDrawable, null, null, null)
    }

    private fun setupRecyclerView() {
        binding.ordersRecyclerView.adapter = adapter
    }

    private fun onTabSelected(tab: TabLayout.Tab) {
        when (tab.position) {
            0 -> viewModel.intent.postValue(OrdersHistoryIntent.TabSelected(OrderClassification.GOODS))
            1 -> viewModel.intent.postValue(OrdersHistoryIntent.TabSelected(OrderClassification.SERVICES))
        }
    }

    private fun showOrders(orders: Map<String, List<OrderData>>, classification: OrderClassification) {
        adapter.setData(orders, classification)
        adapter.notifyDataSetChanged()
        binding.emptyState.isVisible = orders.isEmpty()
        binding.ordersContainer.isVisible = orders.isNotEmpty()
    }

    private fun onOrderClick(orderId: String) {
        viewModel.intent.value = OrdersHistoryIntent.OrderTapped(orderId)
        setFragmentResultListener(REQUEST_CANCEL_CONFIRMED) { _, bundle ->
            val confirmed = bundle.getBoolean(CANCEL_CONFIRMED)
            if (confirmed) {
                viewModel.intent.value = OrdersHistoryIntent.CancelOrder(orderId)
            }
            clearFragmentResult(REQUEST_CANCEL_CONFIRMED)
        }
        setFragmentResultListener(REQUEST_DETAILS_CLOSED) { _, _ ->
            viewModel.intent.postValue(OrdersHistoryIntent.ReloadData)
            clearFragmentResult(REQUEST_DETAILS_CLOSED)
        }
    }

    private fun onClaimClick(orderId: String) {
        viewModel.intent.value = OrdersHistoryIntent.ClaimVoucherTapped(orderId)
    }

    private fun loadColor(view: View, color: Int) {
        view.background = ContextCompat.getDrawable(requireContext(), color)
    }

    private fun showTab(classification: OrderClassification) {
        val position = when (classification) {
            OrderClassification.GOODS -> 0
            OrderClassification.SERVICES -> 1
        }
        binding.tabLayout.selectTab(position)
    }
}

open class OrderData(
    open val status: Int,
    open val date: Date
)

data class GoodsOrderData(
    val orderId: String,
    override val status: Int,
    val statusText: String,
    val orderNumber: String,
    val orderDate: String,
    override val date: Date,
    val images: List<String>
) : OrderData(status, date)

data class ServiceOrderData(
    val orderId: String,
    val name: String,
    val service: String,
    val address: String,
    val phone: String,
    val image: String,
    val logo: String,
    override val status: Int,
    val statusText: String,
    override val date: Date
) : OrderData(status, date)

enum class OrderClassification(val value: String) {
    GOODS("Goods"), SERVICES("services")
}
