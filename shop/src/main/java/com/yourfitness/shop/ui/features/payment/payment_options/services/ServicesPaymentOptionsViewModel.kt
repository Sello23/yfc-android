package com.yourfitness.shop.ui.features.payment.payment_options.services

import android.content.res.Resources
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.yourfitness.common.CommonPreferencesStorage
import com.yourfitness.common.domain.models.PaymentData
import com.yourfitness.common.domain.settings.RegionSettingsManager
import com.yourfitness.common.network.CommonRestApi
import com.yourfitness.common.ui.features.payments.payment_options.PaymentOptionState
import com.yourfitness.common.ui.navigation.CommonNavigator
import com.yourfitness.common.ui.utils.toCoins
import com.yourfitness.common.ui.utils.toCurrencyRounded
import com.yourfitness.shop.data.ShopStorage
import com.yourfitness.shop.data.dao.AddressDao
import com.yourfitness.shop.data.entity.CartDataEntity
import com.yourfitness.shop.data.mapper.toEntity
import com.yourfitness.shop.domain.cart_service.PrepareCartDataService
import com.yourfitness.shop.domain.orders.CartRepository
import com.yourfitness.shop.network.ShopRestApi
import com.yourfitness.shop.network.dto.OrderPaymentItem
import com.yourfitness.shop.ui.constants.Constants
import com.yourfitness.shop.ui.features.orders.history.OrderClassification
import com.yourfitness.shop.ui.features.payment.payment_options.ProductsPaymentOptionsViewModel
import com.yourfitness.shop.ui.navigation.ShopNavigator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ServicesPaymentOptionsViewModel @Inject constructor(
    navigator: ShopNavigator,
    private val restApi: ShopRestApi,
    resources: Resources,
    commonNavigator: CommonNavigator,
    commonRestApi: CommonRestApi,
    cartRepository: CartRepository,
    cartDataService: PrepareCartDataService,
    dao: AddressDao,
    savedState: SavedStateHandle,
    regionSettingsManager: RegionSettingsManager,
    commonStorage: CommonPreferencesStorage,
) : ProductsPaymentOptionsViewModel(
    navigator, restApi, commonRestApi, cartRepository, cartDataService, dao,
    regionSettingsManager, commonStorage, resources, commonNavigator, savedState
) {

    private val productId = savedState.get<String>(Constants.PROD_ID).orEmpty()
    private val coinsCount = savedState.get<Long>(Constants.COINS_COUNT) ?: 0L

    override suspend fun getCartItems() = emptyList<CartDataEntity>()

    override suspend fun getPriceToPay() = price - getOverallPrice()

    override suspend fun getCoinsToPay() = coinsCount

    override suspend fun getOverallPrice() = (coinsValue * coinsCount).toCurrencyRounded().toCoins()

    override suspend fun getItemsSize() = 1

    override suspend fun getPrice() = price

    override fun executePayment(
        cityLabel: String,
        streetLabel: String,
        detailsLabel: String
    ) {
        state.value = PaymentOptionState.Loading
        viewModelScope.launch {
            try {
                val item = OrderPaymentItem(
                    coinsCount = coinsCount,
                    price = price,
                    productId = productId
                )
                val result = restApi.createServicePayment(item)
                paymentData.postValue(PaymentData(card, savedCardId, result.details.clientSecret))
                orderInfo = result.voucher.toEntity()
                startCheckPaymentStatusTimer()
            } catch (e: Exception) {
                state.postValue(PaymentOptionState.Error(e))
            }
        }
    }

    override suspend fun cancelPayment() {
        restApi.cancelServicePayment(orderInfo.id)
    }

    override suspend fun clearCart() {}

    override suspend fun getOrderStatus() = restApi.getServiceOrder(orderInfo.id).status.lowercase()

    override suspend fun getBackToShoppingPopAmount() = 2

    override suspend fun getOrderType() = OrderClassification.SERVICES
}
