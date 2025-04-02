package com.yourfitness.shop.ui.navigation

import androidx.lifecycle.MutableLiveData
import com.yourfitness.shop.data.entity.AddressEntity
import com.yourfitness.shop.data.entity.ProductTypeId
import com.yourfitness.shop.domain.model.AddressInfo
import com.yourfitness.shop.domain.model.ContactInfo
import com.yourfitness.shop.domain.products.ProductFilters
import com.yourfitness.shop.network.dto.VoucherResponse
import com.yourfitness.shop.ui.features.orders.history.OrderClassification
import com.yourfitness.shop.ui.features.product_details.dialogs.SelectCoinsFlow
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShopNavigator @Inject constructor() {

    val navigation = MutableLiveData<ShopNavigation?>()

    fun navigate(node: ShopNavigation) {
        navigation.postValue(node)
    }

    suspend fun navigateDelayed(node: ShopNavigation) {
        delay(500)
        navigate(node)
    }
}

open class ShopNavigation {
    data class ShopIntro(
        val coins: Int,
        val coinsCost: Float,
        val currency: String?
    ) : ShopNavigation()
    object ShopCategories : ShopNavigation()
    data class Catalog(
        val productTypeId: ProductTypeId,
        val filter: ProductFilters? = null
    ) : ShopNavigation()
    data class Cart(
        val currency: String? = null,
        val coinsValue: Double? = null,
    ) : ShopNavigation()
    data class ProductDetails(
        val category: String,
        val productId: String,
        val currency: String,
        val coinsValue: Double,
        val productTypeId: Int,
        val isServices: Boolean = false,
    ) : ShopNavigation()
    data class ProductFilter(
        val currency: String,
        val searchQuery: String,
        val filters: ProductFilters = ProductFilters(),
        val isFavorites: Boolean,
        val productTypeId: Int
    ) : ShopNavigation()

    data class ServicesFilter(
        val searchQuery: String,
        val filters: ProductFilters = ProductFilters(),
        val isFavorites: Boolean,
        val productTypeId: Int
    ) : ShopNavigation()
    data class EnterRange(
        val currency: String,
        val rangeMin: Long,
        val rangeMax: Long
    ) : ShopNavigation()
    data class EnterCoins(
        val currency: String,
        val maxRedemption: Long,
        val availableCoins: Long,
        val coinsValue: Double
    ) : ShopNavigation()
    data class DeliveryAddress(
        val coinsAmount: Long,
        val coinsValue: Double,
        val currency: String,
    ) : ShopNavigation()
    data class PaymentOptions(
        val coinsAmount: Long,
        val address: AddressInfo,
        val contactInfo: ContactInfo,
        val addressEntity: AddressEntity?
    ) : ShopNavigation()
    data class ServicePaymentOptions(
        val balanceCoins: Long,
        val selectedCoins: Long,
        val price: Long,
        val productId: String
    ) : ShopNavigation()
    data class BackToShopping(
        val paymentSuccess: Boolean,
        val popAmount: Int = 3,
        val orderClassification: OrderClassification
    ) : ShopNavigation()
    data class PaymentError(
        val popAmount: Int,
        val orderClassification: OrderClassification = OrderClassification.GOODS
    ) : ShopNavigation()
    data class OrderHistory(
        val orderClassification: OrderClassification = OrderClassification.GOODS
    ) : ShopNavigation()
    data class OrderHistoryDetails(val orderId: String) : ShopNavigation()
    data class ServiceOrderHistoryDetails(val orderId: String) : ShopNavigation()
    data class CoinRedemptionInfo(val redeemableAmount: String) : ShopNavigation()
    data class DetailInitialInfo(val coinsAmount: Long) : ShopNavigation()
    data class CancelOrderConfirmDialog(
        val totalCoins: Long,
        val totalPrice: Long,
        val currency: String
    ) : ShopNavigation()
    object OrderCancelled : ShopNavigation()
    data class ConfirmClaimVoucher(val orderId: String) : ShopNavigation()
    data class ClaimVoucherConfirmed(val data: VoucherResponse) : ShopNavigation()
    data class VoucherClaimedSuccessfully(val name: String, val number: String) : ShopNavigation()
    data class CoinsUsageInfo(
        val coins: Long,
        val coinsCost: Double,
        val currency: String?
    ) : ShopNavigation()
    data class SelectCoinsAmount(
        val price: Long,
        val maxCoins: Long,
        val coinsCost: Double,
        val currency: String?,
        val currentlySelectedCoins: Long = maxCoins,
        val flow: SelectCoinsFlow = SelectCoinsFlow.ADD_TO_CART
    ) : ShopNavigation()
    data class HowToEarnCoins(val title: String? = null) : ShopNavigation()
    data class PopScreen(val times: Int = 1) : ShopNavigation()
}