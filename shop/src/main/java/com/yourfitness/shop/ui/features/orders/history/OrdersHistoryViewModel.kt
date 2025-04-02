package com.yourfitness.shop.ui.features.orders.history

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.yourfitness.common.domain.date.*
import com.yourfitness.common.ui.mvi.MviViewModel
import com.yourfitness.shop.data.ShopStorage
import com.yourfitness.shop.domain.orders.OrderRepository
import com.yourfitness.shop.network.ShopRestApi
import com.yourfitness.shop.ui.constants.Constants.Companion.ORDER_CLASSIFICATION
import com.yourfitness.shop.ui.features.orders.details.OrderDetailsIntent
import com.yourfitness.shop.ui.navigation.ShopNavigation
import com.yourfitness.shop.ui.navigation.ShopNavigator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*
import javax.inject.Inject

@HiltViewModel
class OrdersHistoryViewModel @Inject constructor(
    private val navigator: ShopNavigator,
    private val orderRepository: OrderRepository,
    private val shopStorage: ShopStorage,
    private val shopRestApi: ShopRestApi,
    savedState: SavedStateHandle
) : MviViewModel<OrdersHistoryIntent, OrdersHistoryState>() {

    var orderClassification = savedState.get<OrderClassification>(ORDER_CLASSIFICATION) ?: OrderClassification.GOODS
        private set

    private var sorted: Boolean = false
    private var filtered: Boolean = false
    private var goodsOrders: MutableMap<String, MutableList<GoodsOrderData>> = mutableMapOf()
    private var servicesOrders: MutableMap<String, MutableList<ServiceOrderData>> = mutableMapOf()

    init {
        loadInitialData()
    }

    override fun handleIntent(intent: OrdersHistoryIntent) {
        when (intent) {
            is OrdersHistoryIntent.OrderTapped -> {
                if (orderClassification == OrderClassification.GOODS) {
                    navigator.navigate(ShopNavigation.OrderHistoryDetails(intent.orderId))
                } else {
                    navigator.navigate(ShopNavigation.ServiceOrderHistoryDetails(intent.orderId))
                }
            }
            is OrdersHistoryIntent.SortTapped -> sort()
            is OrdersHistoryIntent.FilterTapped -> filter()
            is OrdersHistoryIntent.CancelOrder -> cancelOrder(intent.orderId)
            is OrdersHistoryIntent.TabSelected -> loadOrders(intent.tab)
            is OrdersHistoryIntent.ClaimVoucherTapped -> {
                navigator.navigate(ShopNavigation.ConfirmClaimVoucher(intent.orderId))
            }
            is OrdersHistoryIntent.OrderClaimSuccessfully -> {
                loadServicesData()
                openClaimedSuccessDialog(intent)
            }
            is OrdersHistoryIntent.ReloadData -> loadInitialData(false)
        }
    }

    private fun loadInitialData(needDownload: Boolean = true) {
        if (needDownload) state.value = OrdersHistoryState.Loading
        val now = getNowUtcFormatted()
        viewModelScope.launch {
            updateGoods(now, needDownload)
            updateServices(now, needDownload)
            postData()
        }
    }

    private fun loadServicesData() {
        state.value = OrdersHistoryState.Loading
        val now = getNowUtcFormatted()
        viewModelScope.launch {
            updateServices(now)
            postData()
        }
    }

    private suspend fun updateGoods(now: String, needDownload: Boolean = true) {
        try {
            val items =
                if (needDownload) {
                    orderRepository.downloadGoodsOrders(shopStorage.ordersLastSyncedAt)
                }
                else orderRepository.readGoodsOrders()
            if (needDownload) shopStorage.ordersLastSyncedAt = now
            goodsOrders = mutableMapOf()
            items.forEach {
                val date = it.order.createdAt.toMilliseconds().toDate()
                val year = date.year().toString()
                var ordersData: MutableList<GoodsOrderData> = goodsOrders[year] ?: mutableListOf()
                ordersData.add(GoodsOrderData(
                    it.order.id,
                    getStatusValueByText(it.order.status),
                    it.order.status,
                    it.order.number,
                    date.formatted(),
                    date,
                    it.items.map { item -> item.imageId }
                ))
                ordersData =
                    ordersData.sortedWith(compareBy<GoodsOrderData> { item -> item.status }.thenByDescending { item -> item.date })
                        .toMutableList()
                goodsOrders[year] = ordersData
            }
        } catch (error: Exception) {
            Timber.e(error)
        }
    }

    private suspend fun updateServices(now: String, needDownload: Boolean = true) {
        try {
            val items =
                if (needDownload) orderRepository.downloadServicesOrders(shopStorage.serviceOrdersLastSyncedAt)
                else orderRepository.readServicesOrders()
            if (needDownload) shopStorage.serviceOrdersLastSyncedAt = now
            servicesOrders = mutableMapOf()
            items.forEach {
                val date = it.dateBought.toMilliseconds().toDate()
                val year = date.year().toString()
                var ordersData: MutableList<ServiceOrderData> = servicesOrders[year] ?: mutableListOf()
                ordersData.add(ServiceOrderData(
                    it.id,
                    it.productName,
                    it.vendorName,
                    it.address,
                    it.vendorPhone,
                    it.defaultImageId,
                    it.vendorImage,
                    getServiceStatusValueByText(it.status),
                    it.status,
                    date
                ))
                ordersData = ordersData.sortedWith(compareBy<ServiceOrderData> { item -> item.status }
                    .thenByDescending { item -> item.date })
                    .toMutableList()
                servicesOrders[year] = ordersData
            }
        } catch (error: Exception) {
            Timber.e(error)
        }
    }

    private fun postData() {
        state.postValue(OrdersHistoryState.DataLoaded(orderClassification, goodsOrders, servicesOrders))
    }

    private fun sort() {
        sorted = !sorted
        loadActualData()
    }

    private fun filter() {
        filtered = !filtered
        loadActualData()
    }

    private fun loadOrders(classification: OrderClassification) {
        orderClassification = classification
        loadActualData()
    }

   private fun loadActualData() {
        viewModelScope.launch(Dispatchers.IO) {
            val goods = loadActualData(goodsOrders.toMutableMap())
            val services = loadActualData(servicesOrders.toMutableMap())
            state.postValue(
                OrdersHistoryState.DataLoaded(
                    orderClassification,
                    goods,
                    services,
                    sorted,
                    filtered
                )
            )
        }
    }

    private fun <T : OrderData> loadActualData(items: MutableMap<String, List<T>>): MutableMap<String, List<T>> {
        try {
            items.forEach { item ->
                if (sorted) {
                    items[item.key] =
                        item.value.sortedWith(compareByDescending<OrderData> { it.status }.thenByDescending { it.date })
                            .toMutableList()
                } else {
                    items[item.key] =
                        item.value.sortedWith(compareBy<OrderData> { it.status }.thenByDescending { it.date })
                            .toMutableList()
                }
            }

            if (filtered) {
                val minOrderDate = today().subtractMonths(3)
                items.forEach { item ->
                    items[item.key] =
                        item.value.filter { it.date.after(minOrderDate) }.toMutableList()
                }
            }
        } catch (error: Exception) {
            Timber.e(error)
        }
        return items
    }

    private fun getStatusValueByText(value: String): Int {
        return when (value.lowercase()) {
            RECEIVED -> 0
            "processing" -> 1
            "request cancellation" -> 2
            "fulfilled" -> 3
            else -> 4
        }
    }

    private fun getServiceStatusValueByText(value: String): Int {
        return when (value.lowercase()) {
            UNCLAIMED -> 0
            "claimed" -> 1
            "request cancellation" -> 2
            else -> 3
        }
    }


    private fun cancelOrder(orderId: String) {
        state.value = OrdersHistoryState.Loading
        val now = getNowUtcFormatted()
        viewModelScope.launch {
            try {
                if (orderClassification == OrderClassification.GOODS) {
                    shopRestApi.cancelOrder(orderId)
                    updateGoods(now)
                } else {
                    shopRestApi.cancelServiceOrder(orderId)
                    updateServices(now)
                }
                postData()
                navigator.navigate(ShopNavigation.OrderCancelled)
            } catch (error: Exception) {
                Timber.e(error)
                state.postValue(OrdersHistoryState.Error(error))
            }
        }
    }

    private fun openClaimedSuccessDialog(intent: OrdersHistoryIntent.OrderClaimSuccessfully) {
        try {
            navigator.navigate(
                ShopNavigation.VoucherClaimedSuccessfully(
                    intent.name,
                    intent.number
                )
            )
        } catch (e: java.lang.Exception) {
            Timber.e(e)
        }
    }
}

const val RECEIVED = "received"
const val UNCLAIMED = "unclaimed"

open class OrdersHistoryState {
    object Loading : OrdersHistoryState()
    data class Error(val error: Exception) : OrdersHistoryState()
    data class DataLoaded(
        val orderClassification: OrderClassification,
        val orders: Map<String, List<GoodsOrderData>>,
        val serviceOrders: Map<String, List<ServiceOrderData>>,
        val sorted: Boolean = false,
        val filtered: Boolean = false
    ) : OrdersHistoryState()
}

open class OrdersHistoryIntent {
    data class OrderTapped(val orderId: String) : OrdersHistoryIntent()
    object SortTapped : OrdersHistoryIntent()
    object FilterTapped : OrdersHistoryIntent()
    data class CancelOrder(val orderId: String) : OrdersHistoryIntent()
    data class TabSelected(val tab: OrderClassification) : OrdersHistoryIntent()
    data class ClaimVoucherTapped(val orderId: String) : OrdersHistoryIntent()
    data class OrderClaimSuccessfully(val number: String, val name: String) : OrdersHistoryIntent()
    object ReloadData : OrdersHistoryIntent()
}
