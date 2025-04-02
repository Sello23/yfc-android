package com.yourfitness.shop.ui.features.payment.payment_options

import android.content.res.Resources
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.yourfitness.common.CommonPreferencesStorage
import com.yourfitness.common.domain.models.PaymentData
import com.yourfitness.common.domain.settings.RegionSettingsManager
import com.yourfitness.common.network.CommonRestApi
import com.yourfitness.common.ui.features.payments.payment_options.BasePaymentOptionsViewModel
import com.yourfitness.common.ui.features.payments.payment_options.PaymentOptionIntent
import com.yourfitness.common.ui.features.payments.payment_options.PaymentOptionState
import com.yourfitness.common.ui.navigation.CommonNavigator
import com.yourfitness.shop.data.ShopStorage
import com.yourfitness.shop.data.dao.AddressDao
import com.yourfitness.shop.data.entity.AddressEntity
import com.yourfitness.shop.data.entity.CartDataEntity
import com.yourfitness.shop.data.entity.OrderInfoEntity
import com.yourfitness.shop.data.mapper.toEntity
import com.yourfitness.shop.data.mapper.toOrderPaymentItem
import com.yourfitness.shop.domain.cart_service.PrepareCartDataService
import com.yourfitness.shop.domain.model.AddressInfo
import com.yourfitness.shop.domain.model.ContactInfo
import com.yourfitness.shop.domain.model.toInfoString
import com.yourfitness.shop.domain.orders.CartRepository
import com.yourfitness.shop.network.ShopRestApi
import com.yourfitness.shop.network.dto.OrderPaymentRequest
import com.yourfitness.shop.ui.constants.Constants
import com.yourfitness.shop.ui.constants.Constants.Companion.PAYMENT_FAILED
import com.yourfitness.shop.ui.constants.Constants.Companion.PAYMENT_WAITING
import com.yourfitness.shop.ui.features.orders.history.OrderClassification
import com.yourfitness.shop.ui.navigation.ShopNavigation
import com.yourfitness.shop.ui.navigation.ShopNavigator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
open class ProductsPaymentOptionsViewModel @Inject constructor(
    private val navigator: ShopNavigator,
    private val restApi: ShopRestApi,
    private val commonRestApi: CommonRestApi,
    private val cartRepository: CartRepository,
    private val cartDataService: PrepareCartDataService,
    private val dao: AddressDao,
    private val regionSettingsManager: RegionSettingsManager,
    private val commonStorage: CommonPreferencesStorage,
    resources: Resources,
    commonNavigator: CommonNavigator,
    savedState: SavedStateHandle,
) : BasePaymentOptionsViewModel(
    commonNavigator, commonRestApi, resources, savedState
) {

    val coinsAmount = savedState.get<Long>(Constants.COINS_AMOUNT)
    private val address = savedState.get<AddressInfo>(Constants.ADDRESS)
    private val contactInfo = savedState.get<ContactInfo>(Constants.CONTACT_INFO)
    private val addressEntity = savedState.get<AddressEntity?>(Constants.ADDRESS_ENTITY)
    protected var currency = ""
    protected var coinsValue = 0.0

    private var cartItems: List<CartDataEntity> = listOf()
    private var priceToPay = 0L
    private var coinsToPay = 0L

    private var isPaymentProceed = false

    protected lateinit var orderInfo: OrderInfoEntity

    init {
        loadSavedCard()
        fetchProducts()
    }

    private fun fetchProducts() {
        viewModelScope.launch {
            try {
                val settings = regionSettingsManager.getSettings(true)
                currency = settings?.currency.orEmpty()
                coinsValue = settings?.coinValue ?: 0.0
                cartItems = getCartItems()
                priceToPay = getPriceToPay()
                coinsToPay = getCoinsToPay()
                state.postValue(
                    ProductPaymentOptionState.DataLoaded(
                        currency = currency,
                        itemsCount = getItemsSize(),
                        price = getPrice(),
                        priceWithoutCoins = priceToPay,
                        overallCoins = coinsToPay,
                        overallPrice = getOverallPrice()
                    )
                )
            } catch (error: Exception) {
                Timber.e(error)
            }
        }
    }

    protected open suspend fun getCartItems() = cartRepository.readCartItemsWithData()

    protected open suspend fun getPriceToPay() = cartDataService.getPriceWithoutCoins(cartItems, coinsValue)

    protected open suspend fun getCoinsToPay() = cartDataService.getOverallCoins(cartItems)

    protected open suspend fun getOverallPrice() = cartDataService.getOverallPrice(cartItems, coinsValue)

    protected open suspend fun getItemsSize() = cartItems.size

    protected open suspend fun getPrice() = cartDataService.getWholePrice(cartItems)

    override fun handleIntent(intent: PaymentOptionIntent) {
        super.handleIntent(intent)
        when (intent) {
            is ProductPaymentOptionIntent.PaymentConfirmed -> executeProductPayment(intent)
            is ProductPaymentOptionIntent.PaymentSucceeded -> onPaymentProceeded()
            is ProductPaymentOptionIntent.PaymentFailed -> onPaymentFailed()
            is ProductPaymentOptionIntent.PaymentCanceled -> onPaymentCanceled()
        }
    }

    private fun executeProductPayment(intent: ProductPaymentOptionIntent.PaymentConfirmed) {
        if (isPaymentMethodAdded()) {
            isPaymentProceed = false
            executePayment(intent.cityLabel, intent.streetLabel, intent.detailsLabel)
        } else {
            navigateAddCart()
        }
    }

    protected open fun executePayment(
        cityLabel: String,
        streetLabel: String,
        detailsLabel: String
    ) {
        state.value = PaymentOptionState.Loading
        viewModelScope.launch {
            try {
                val result = restApi.createOrderPayment(
                    buildOrderPaymentRequest(
                        cityLabel,
                        streetLabel,
                        detailsLabel
                    )
                )
                paymentData.postValue(PaymentData(card, savedCardId, result.details.clientSecret))
                orderInfo = result.order.toEntity()
                startCheckPaymentStatusTimer()
            } catch (e: Exception) {
                state.postValue(PaymentOptionState.Error(e))
            }
        }
    }

    private fun buildOrderPaymentRequest(
        cityLabel: String,
        streetLabel: String,
        detailsLabel: String
    ): OrderPaymentRequest {
        return OrderPaymentRequest(
            clientAddress = address.toInfoString(cityLabel, streetLabel, detailsLabel),
            clientPhone = contactInfo?.phoneNumber.orEmpty(),
            clientEmail = contactInfo?.email.orEmpty(),
            clientName = contactInfo?.fullName.orEmpty(),
            items = cartItems.map { it.toOrderPaymentItem() }.toList()
        )
    }

    protected suspend fun startCheckPaymentStatusTimer() {
        val maxAttempts = 10
        delay(1000)
        if (isPaymentProceed) return

        try {
            for (i in 0..maxAttempts) {
                if (checkOrderStatus()) return
                if (i != maxAttempts) delay(3000L)
                if (isPaymentProceed) return
            }
        } catch (error: Exception) {
            Timber.e(error)
        }

        onPaymentFailed()
    }

    protected open suspend fun cancelPayment() {
        restApi.cancelPayment(orderInfo.id)
    }

    private fun onPaymentProceeded() {
        viewModelScope.launch {
            try {
                checkOrderStatus()
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    private suspend fun checkOrderStatus(): Boolean {
        val status = getOrderStatus()
        return if (status == PAYMENT_FAILED) {
            true
        } else if (status != PAYMENT_WAITING) {
            onPaymentSucceeded()
            true
        } else {
            false
        }
    }

    protected open suspend fun getOrderStatus() = restApi.getOrderStatus(orderInfo.id).status.lowercase()

    private fun onPaymentSucceeded() {
        if (isPaymentProceed) return
        viewModelScope.launch {
            try {
                isPaymentProceed = true
                if (addressEntity != null) {
                    dao.saveAddress(addressEntity)
                }
                commonStorage.availableCoins = commonRestApi.coins()
                clearCart()
            } catch (e: Exception) {
                Timber.e(e)
            } finally {
                state.postValue(ProductPaymentOptionState.Loaded)
                navigator.navigate(
                    ShopNavigation.BackToShopping(
                        true,
                        getBackToShoppingPopAmount(),
                        getOrderType()
                    ))
            }
        }
    }

    protected open suspend fun getBackToShoppingPopAmount() = 3

    protected open suspend fun getOrderType() = OrderClassification.GOODS

    private fun onPaymentFailed() {
        if (isPaymentProceed) return
        viewModelScope.launch {
            try {
                isPaymentProceed = true
                cancelPayment()
            } catch (e: Exception) {
                Timber.e(e)
            } finally {
                state.postValue(ProductPaymentOptionState.Loaded)
                navigator.navigate(
                    ShopNavigation.PaymentError(
                        getBackToShoppingPopAmount(),
                        getOrderType()
                    )
                )
            }
        }
    }

    private fun onPaymentCanceled() {}

    protected open suspend fun clearCart() {
        cartRepository.emptyCart()
    }
}

open class ProductPaymentOptionState : PaymentOptionState() {
    data class DataLoaded(
        val currency: String,
        val itemsCount: Int,
        val price: Long,
        val priceWithoutCoins: Long,
        val overallCoins: Long,
        val overallPrice: Long
    ) : PaymentOptionState()

    object Loaded : PaymentOptionState()
}

open class ProductPaymentOptionIntent : PaymentOptionIntent() {
    data class PaymentConfirmed(
        val cityLabel: String,
        val streetLabel: String,
        val detailsLabel: String
    ) : PaymentOptionIntent()
    object PaymentSucceeded : PaymentOptionIntent()
    object PaymentFailed : PaymentOptionIntent()
    object PaymentCanceled : PaymentOptionIntent()
}
