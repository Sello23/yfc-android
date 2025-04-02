package com.yourfitness.shop.ui.features.product_details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.maps.android.SphericalUtil
import com.yourfitness.common.domain.location.LocationRepository
import com.yourfitness.common.ui.mvi.MviViewModel
import com.yourfitness.common.ui.utils.toCoins
import com.yourfitness.common.ui.utils.toCurrencyRounded
import com.yourfitness.shop.data.entity.CartEntity
import com.yourfitness.shop.data.entity.ItemColorEntity
import com.yourfitness.shop.data.entity.ItemSizeEntity
import com.yourfitness.shop.data.entity.ItemSizeEntity.Companion.NO_STOCK
import com.yourfitness.shop.data.entity.ProductDataEntity
import com.yourfitness.shop.data.entity.ProductTypeId
import com.yourfitness.shop.domain.cart_service.CoinsUsageService
import com.yourfitness.shop.domain.orders.CartRepository
import com.yourfitness.shop.domain.products.ProductsRepository
import com.yourfitness.shop.ui.constants.Constants
import com.yourfitness.shop.ui.features.orders.cart.CartState
import com.yourfitness.shop.ui.features.product_details.dialogs.SelectCoinsFlow
import com.yourfitness.shop.ui.navigation.ShopNavigation
import com.yourfitness.shop.ui.navigation.ShopNavigator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.lang.Long.max
import java.util.UUID
import javax.inject.Inject
import kotlin.math.min
import kotlin.math.roundToLong

@HiltViewModel
class ProductDetailsViewModel @Inject constructor(
    private val navigator: ShopNavigator,
    private val productsRepository: ProductsRepository,
    private val cartRepository: CartRepository,
    private val locationRepository: LocationRepository,
    private val coinsUsageService: CoinsUsageService,
    savedState: SavedStateHandle
) : MviViewModel<ProductDetailsIntent, Any>() {

    private val productId = savedState.get<String>(Constants.PROD_ID).orEmpty()
    private val currency = savedState.get<String>(Constants.CURRENCY).orEmpty()
    private val coinsValue = savedState.get<Double>(Constants.COINS_VALUE) ?: 0.0
    private val productTypeId = savedState.get<Int>(Constants.PRODUCT_TYPE_ID) ?: 0

    var product: ProductDataEntity? = null
        private set
    private lateinit var productInfo: Map<String, Any>
    private var selectedColor = ""
    private var selectedColorId = ""
    private var selectedSize = ""
    private var selectedSizeId = -1
    private var selectedSizeType = ""
    private var currencyPart = 0L
    private var stockState = NO_STOCK

    private var coinsSelected = 0L
    private var initialInfoShown = false

    override fun handleIntent(intent: ProductDetailsIntent) {
        when (intent) {
            is ProductDetailsIntent.ScreenOpened -> {
                loadData()
                loadInitData()
            }
            is ProductDetailsIntent.ColorChanged -> {
                selectedColor = intent.colorImageId
                selectedColorId = intent.id
                initDataUpdate()
            }
            is ProductDetailsIntent.SizeChanged -> {
                selectedSize = intent.size
                selectedSizeId = intent.id
                selectedSizeType = intent.type
                initDataUpdate()
            }
            is ProductDetailsIntent.ActionButtonClicked -> {
                viewModelScope.launch {
                    val maxCoinsToRedeem =
                        min(product?.product?.redeemableCoins ?: 0L, coinsUsageService.notUsedCoins())
                    if (maxCoinsToRedeem > 0) {
                        navigator.navigate(
                            ShopNavigation.SelectCoinsAmount(
                                product?.product?.price ?: 0L,
                                maxCoinsToRedeem,
                                coinsValue,
                                currency,
                            )
                        )
                    } else {
                        saveItemToCart()
                    }
                }
            }
            is ProductDetailsIntent.CoinsAmountSelected -> {
                coinsSelected = intent.coinsUsed
                if (productTypeId == ProductTypeId.SERVICES.value) {
                    initServicePurchase()
                } else {
                    saveItemToCart()
                }
            }
            is ProductDetailsIntent.Cart -> {
                navigator.navigate(ShopNavigation.Cart(currency, coinsValue))
            }
            is ProductDetailsIntent.FavoriteChanged -> updateFavorites(intent.checked)
            is ProductDetailsIntent.UploadFavorites -> uploadFavorites()
            is ProductDetailsIntent.RedemptionInfoTapped -> {
                if ((product?.product?.redeemableCoins ?: 0L) > 0) {
                    navigator.navigate(ShopNavigation.DetailInitialInfo(product?.product?.redeemableCoins ?: 0L))
                }
            }
            is ProductDetailsIntent.CoinsPartChanged -> {
                coinsSelected = intent.coinsAmount
                loadData()
            }
            is ProductDetailsIntent.ChangeRedemptionCoinsTapped -> openEnterCoinsDialog()
            is ProductDetailsIntent.PurchaseServiceTapped -> openEnterCoinsDialog()
            is ProductDetailsIntent.CoinsInfo -> {
                viewModelScope.launch {
                    navigator.navigate(
                        ShopNavigation.CoinsUsageInfo(
                            coinsUsageService.notUsedCoins(),
                            coinsValue,
                            currency
                        )
                    )
                }
            }
        }
    }

    private fun loadInitData() {
        viewModelScope.launch {
            try {
                val size = cartRepository.getCartSize()
                state.postValue(ProductDetailsState.CartSizeLoaded(size))
            } catch (error: Exception) {
                Timber.e(error)
                state.postValue(CartState.Error(error))
            }
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                product = productsRepository.getProductDetails(productId, productTypeId)
                if (!initialInfoShown && (product?.product?.redeemableCoins ?: 0L) > 0) {
                    navigator.navigate(ShopNavigation.DetailInitialInfo(product?.product?.redeemableCoins ?: 0L))
                }
                initialInfoShown = true
                product?.product?.info?.let {
                    val type = object : TypeToken<Map<String, Any>?>() {}.type
                    productInfo = Gson().fromJson(it, type)
                }
                currencyPart = product?.product?.price ?: 0L
                initDataUpdate()
                val colorEntity = product?.colors?.find { it.itemColor.isDefault }
                selectedColor = colorEntity?.itemColor?.color.orEmpty()
                selectedColorId = colorEntity?.itemColor?.colorId.orEmpty()
                selectedSize = colorEntity?.sizes?.first()?.size.orEmpty()
                selectedSizeId =  colorEntity?.sizes?.first()?.sizeId ?: -1
                selectedSizeType = colorEntity?.sizes?.first()?.type.orEmpty()
                stockState = colorEntity?.sizes?.find { it.size == selectedSize }?.stockLevel.orEmpty()
                if (stockState.isBlank()) {
                    stockState = product?.product?.stockLevel ?: NO_STOCK
                }
                val size = cartRepository.getCartSize()
                val distance = calculateDistance(product?.product?.latitude, product?.product?.longitude)
                val coinsPriceEquivalent = (coinsValue * coinsSelected).toCurrencyRounded().toCoins()
                product?.let {
                    state.postValue(
                        ProductDetailsState.ProductLoaded(
                            size,
                            it,
                            currency,
                            coinsValue,
                            selectedColor,
                            selectedSize,
                            stockState,
                            coinsUsageService.notUsedCoins(),
                            productInfo,
                            product?.favourite != null,
                            distance,
                            coinsSelected,
                            coinsPriceEquivalent,
                            max(0L, ((product?.product?.price ?: 0L) - coinsPriceEquivalent))
                        )
                    )
                }
            } catch (error: Exception) {
                Timber.e(error)
                state.postValue(ProductDetailsState.Error(error))
            }
        }
    }

    private fun initDataUpdate() {
        val selectedColorItem = product?.colors?.find { it.itemColor.color == selectedColor }
        val images = selectedColorItem?.images?.map { it.image }.orEmpty()
        val colors = product?.colors?.map { it.itemColor }.orEmpty()
        val sizes = selectedColorItem?.sizes.orEmpty()
        if (sizes.isNotEmpty() && !sizes.map { it.size }.contains(selectedSize)) {
            selectedSize = sizes.first().size
            selectedSizeId = sizes.first().sizeId
        }
        stockState = selectedColorItem?.sizes?.find { it.size == selectedSize }?.stockLevel.orEmpty()

        state.value = ProductDetailsState.UpdateSizesList(
            images,
            colors,
            sizes,
            selectedSize,
            selectedColor,
            stockState
        )
    }

    private fun saveItemToCart() {
        viewModelScope.launch {
            try {
                val item = CartEntity(
                    uuid = UUID.randomUUID().toString(),
                    itemId = product?.product?.id.orEmpty(),
                    color = selectedColor,
                    colorId = selectedColorId,
                    size = selectedSize,
                    sizeId = selectedSizeId,
                    sizeType = selectedSizeType,
                    coinsPart = coinsSelected,
                    amount = 1
                )
                cartRepository.saveItemToCart(item)

                val size = cartRepository.getCartSize()
                coinsSelected = 0L
                state.postValue(
                    ProductDetailsState.ItemAddedToCart(
                        size,
                        coinsUsageService.notUsedCoins()
                    )
                )
                loadInitData()
            } catch (error: Exception) {
                Timber.e(error)
                state.postValue(ProductDetailsState.Error(error))
            }
        }
    }

    private fun updateFavorites(checked: Boolean) {
        viewModelScope.launch {
            val id = productsRepository.getProductId(productId, productTypeId) ?: return@launch
            productsRepository.saveFavorite(checked, id, productId)
            product = productsRepository.getProductDetails(productId, productTypeId)
            state.postValue(ProductDetailsState.FavoriteStateUpdated(checked))
        }
    }

    private fun uploadFavorites() {
        GlobalScope.launch {
            productsRepository.syncFavorites()
        }
    }

    private fun openEnterCoinsDialog() {
        viewModelScope.launch {
            val maxCoinsToRedeem =
                min(product?.product?.redeemableCoins ?: 0L, coinsUsageService.notUsedCoins())
            if (maxCoinsToRedeem > 0) {
                navigator.navigate(
                    ShopNavigation.SelectCoinsAmount(
                        product?.product?.price ?: 0L,
                        maxCoinsToRedeem,
                        coinsValue,
                        currency,
                        if (coinsSelected == 0L) maxCoinsToRedeem else coinsSelected,
                        flow = SelectCoinsFlow.UPDATE
                    )
                )
            } else {
                initServicePurchase()
            }
        }
    }

    private suspend fun calculateDistance(latitude: Double?, longitude: Double?): Long? {
        return if (latitude != null && longitude != null) {
            val currentLatLang = locationRepository.lastLocation()
            SphericalUtil.computeDistanceBetween(LatLng(latitude, longitude), currentLatLang).roundToLong()
        } else {
            null
        }
    }

    private fun initServicePurchase() {
        viewModelScope.launch {
            navigator.navigate(
                ShopNavigation.ServicePaymentOptions(
                    coinsUsageService.notUsedCoins(),
                    coinsSelected,
                    product?.product?.price ?: 0L,
                    productId
                )
            )
        }
    }
}

open class ProductDetailsIntent {
    object ScreenOpened : ProductDetailsIntent()
    data class ColorChanged(val colorImageId: String, val id: String) : ProductDetailsIntent()
    data class SizeChanged(val size: String, val type: String, val id: Int) : ProductDetailsIntent()
    object ActionButtonClicked : ProductDetailsIntent()
    data class CoinsAmountSelected(val coinsUsed: Long) : ProductDetailsIntent()
    object Cart : ProductDetailsIntent()
    data class FavoriteChanged(val checked: Boolean) : ProductDetailsIntent()
    object UploadFavorites : ProductDetailsIntent()
    object RedemptionInfoTapped : ProductDetailsIntent()
    data class CoinsPartChanged(val coinsAmount: Long) : ProductDetailsIntent()
    object ChangeRedemptionCoinsTapped : ProductDetailsIntent()
    object PurchaseServiceTapped: ProductDetailsIntent()
    object CoinsInfo: ProductDetailsIntent()
}

open class ProductDetailsState {
    data class CartSizeLoaded(val size: Int) : ProductDetailsState()
    data class ProductLoaded(
        val cartSize: Int,
        val product: ProductDataEntity,
        val currency: String,
        val coinsValue: Double,
        val selectedColor: String,
        val selectedSize: String,
        val stockState: String,
        val coinsAmount: Long,
        val info: Map<String, Any>,
        val isFavorite: Boolean,
        val distance: Long?,
        val coinsSelected: Long,
        val coinsPriceEquivalent: Long,
        val discountPrice: Long,
    ) : ProductDetailsState()
    data class Error(val error: Exception) : ProductDetailsState()
    data class UpdateSizesList(
        val images: List<String>,
        val colors: List<ItemColorEntity>,
        val sizes: List<ItemSizeEntity>,
        val selectedSize: String,
        val selectedColor: String,
        val stockState: String
    )
    data class FavoriteStateUpdated(val checked: Boolean) : ProductDetailsState()

    data class ItemAddedToCart(val cartSize: Int, val coinsAmount: Long) : ProductDetailsState()
}
