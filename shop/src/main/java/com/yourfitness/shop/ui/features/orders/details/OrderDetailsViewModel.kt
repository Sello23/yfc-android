package com.yourfitness.shop.ui.features.orders.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.yourfitness.common.domain.date.formatted
import com.yourfitness.common.domain.date.getNowUtcFormatted
import com.yourfitness.common.domain.date.toDate
import com.yourfitness.common.domain.date.toMilliseconds
import com.yourfitness.common.domain.settings.RegionSettingsManager
import com.yourfitness.common.ui.mvi.MviViewModel
import com.yourfitness.shop.data.ShopStorage
import com.yourfitness.shop.data.entity.GoodsOrderItemEntity
import com.yourfitness.shop.data.entity.ServicesOrderItemEntity
import com.yourfitness.shop.domain.model.BaseCard
import com.yourfitness.shop.domain.model.CartCard
import com.yourfitness.shop.domain.orders.OrderRepository
import com.yourfitness.shop.network.ShopRestApi
import com.yourfitness.shop.ui.constants.Constants
import com.yourfitness.shop.ui.features.orders.details.services.ServiceCard
import com.yourfitness.shop.ui.features.orders.history.UNCLAIMED
import com.yourfitness.shop.ui.navigation.ShopNavigation
import com.yourfitness.shop.ui.navigation.ShopNavigator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ServiceOrderDetailsViewModel @Inject constructor(
    private val navigator: ShopNavigator,
    private val orderRepository: OrderRepository,
    private val shopStorage: ShopStorage,
    shopRestApi: ShopRestApi,
    regionSettingsManager: RegionSettingsManager,
    savedState: SavedStateHandle
) : OrderDetailsViewModel(navigator, orderRepository, shopRestApi, regionSettingsManager, savedState) {

    override fun handleIntent(intent: OrderDetailsIntent) {
        when (intent) {
            is OrderDetailsIntent.OrderClaimSuccessfully -> {
                updateServiceData()
                openClaimedSuccessDialog(intent)
            }
        }
        super.handleIntent(intent)
    }

    override suspend fun prepareData(): List<BaseCard> {
        val items = orderRepository.getServiceOrderItemsByOrderId(orderId)
        price = items.sumOf { it.price }
        overallPrice = items.sumOf { it.coinsValue }
        totalCoins = items.sumOf { it.coinsCount }
        totalPrice = price - overallPrice
        return items.map { createServiceCard(it) }
    }

    private fun createServiceCard(it: ServicesOrderItemEntity) =
        ServiceCard(
            uuid = "",
            image = it.defaultImageId,
            logo = it.vendorImage,
            title = it.productName,
            subtitle = it.vendorName,
            address = it.address,
            phone = it.vendorPhone,
            orderDate = it.dateBought.toMilliseconds().toDate().formatted(),
            claimDate = it.dateClaimed?.toMilliseconds()?.toDate()?.formatted(),
            statusValue = getStatusValueByText(it.status),
            status = it.status
        )

    override suspend fun getStatus() = orderRepository.getServiceOrderStatusById(orderId)

    private fun getStatusValueByText(value: String): Int {
        return when (value.lowercase()) {
            UNCLAIMED -> 0
            "claimed" -> 1
            "request cancellation" -> 2
            else -> 3
        }
    }

    private fun openClaimedSuccessDialog(intent: OrderDetailsIntent.OrderClaimSuccessfully) {
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

    private fun updateServiceData() {
        viewModelScope.launch {
            try {
                val items = orderRepository.downloadServicesOrders(shopStorage.serviceOrdersLastSyncedAt)
                shopStorage.serviceOrdersLastSyncedAt = getNowUtcFormatted()
                val order = items.find { it.id == orderId } ?: return@launch
                state.postValue(OrderDetailsState.OrderUpdated(listOf(createServiceCard(order))))
            } catch (e: java.lang.Exception) {
                Timber.e(e)
            }
        }
    }

    override fun GoodsOrderItemEntity.getScreenCardSubtitle() = vendorName
}

@HiltViewModel
open class OrderDetailsViewModel @Inject constructor(
    private val navigator: ShopNavigator,
    private val orderRepository: OrderRepository,
    private val shopRestApi: ShopRestApi,
    private val regionSettingsManager: RegionSettingsManager,
    savedState: SavedStateHandle
) : MviViewModel<OrderDetailsIntent, OrderDetailsState>() {

    protected val orderId = savedState.get<String>(Constants.ORDER_ID).orEmpty()
    protected var currency = ""

    protected var totalCoins: Long = 0L
    protected var price: Long = 0L
    protected var overallPrice: Long = 0L
    protected var totalPrice: Long = 0L

    init {
        loadInitialData()
    }

    override fun handleIntent(intent: OrderDetailsIntent) {
        when (intent) {
            is OrderDetailsIntent.OrderCancelledRequested -> openConfirmDialog()
            is OrderDetailsIntent.DownloadInvoiceRequested -> downloadInvoice()
            is OrderDetailsIntent.OrderClaimRequested -> navigator.navigate(ShopNavigation.ConfirmClaimVoucher(orderId))
        }
    }

    private fun loadInitialData() {
        state.value = OrderDetailsState.Loading
        viewModelScope.launch {
            try {
                val settings = regionSettingsManager.getSettings()
                currency = settings?.currency.orEmpty().uppercase()
                val orderStatus = getStatus()
                val items = prepareData()
                val data = OrderDetailsState.DataLoaded(
                    currency = currency,
                    overallPrice = overallPrice,
                    overallCoins = totalCoins,
                    price = price,
                    priceWithoutCoins = totalPrice,
                    orderStatus = orderStatus.lowercase(),
                    items = items,
                )
                state.postValue(data)
            } catch (e: java.lang.Exception) {
                Timber.e(e)
            }
        }
    }

    protected open suspend fun getStatus() = orderRepository.getOrderStatusById(orderId)

    protected open suspend fun prepareData(): List<BaseCard> {
        val items = orderRepository.getOrderItemsByOrderId(orderId)
        price = items.sumOf { it.price }
        overallPrice = items.sumOf { it.coinsValue }
        totalCoins = items.sumOf { it.coinsCount }
        totalPrice = price - overallPrice
        return items.map {
            CartCard(
                uuid = "",
                title = it.productName,
                subtitle = it.getScreenCardSubtitle(),
                currency = currency,
                discountPrice = it.price - it.coinsValue,
                wholePrice = it.price,
                coinsAmount = it.coinsCount,
                coinsPriceEquivalent = it.coinsValue,
                coverImage = it.imageId,
                color = it.color,
                size = it.sizeValue,
                sizeType = it.sizeType,
                status = getStatusValueByText(it.status),
                statusText = it.status,
            )
        }
    }

    protected open fun GoodsOrderItemEntity.getScreenCardSubtitle() = brandName

    private fun openConfirmDialog() {
        viewModelScope.launch {
            try {
                navigator.navigate(
                    ShopNavigation.CancelOrderConfirmDialog(
                        totalCoins,
                        totalPrice,
                        currency
                    )
                )
            } catch (error: Exception) {
                Timber.e(error)
            }
        }
    }

    private fun downloadInvoice() {
        state.value = OrderDetailsState.Loading
        viewModelScope.launch {
            try {
                val invoiceLink = shopRestApi.getInvoiceLink(orderId).receipt
                state.postValue(OrderDetailsState.InvoiceLoaded(invoiceLink))
            } catch (error: Exception) {
                Timber.e(error)
                state.postValue(OrderDetailsState.Error(error))
            }
        }
    }

    private fun getStatusValueByText(value: String): Int {
        return when (value) {
            "Processing" -> 0
            "Shipped" -> 1
            "Delivered" -> 2
            "Awaiting stock" -> 3
            "Not available" -> 4
            else -> 5
        }
    }
}

open class OrderDetailsState {
    object Loading : OrderDetailsState()
    data class Error(val error: Exception) : OrderDetailsState()
    data class DataLoaded(
        val currency: String,
        val overallPrice: Long,
        val price: Long,
        val priceWithoutCoins: Long,
        val overallCoins: Long,
        val orderStatus: String,
        val items: List<BaseCard>
    ) : OrderDetailsState()
    data class OrderUpdated(val items: List<BaseCard>) : OrderDetailsState()
    data class InvoiceLoaded(val link: String) : OrderDetailsState()
}

open class OrderDetailsIntent {
    object OrderCancelledRequested : OrderDetailsIntent()
    object DownloadInvoiceRequested : OrderDetailsIntent()
    object OrderClaimRequested : OrderDetailsIntent()
    data class OrderClaimSuccessfully(val number: String, val name: String) : OrderDetailsIntent()
}
