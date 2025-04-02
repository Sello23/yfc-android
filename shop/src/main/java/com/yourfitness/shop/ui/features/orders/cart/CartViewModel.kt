package com.yourfitness.shop.ui.features.orders.cart

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.yourfitness.common.CommonPreferencesStorage
import com.yourfitness.common.domain.settings.RegionSettingsManager
import com.yourfitness.common.network.CommonRestApi
import com.yourfitness.common.ui.mvi.MviViewModel
import com.yourfitness.shop.data.ShopStorage
import com.yourfitness.shop.data.entity.CartDataEntity
import com.yourfitness.shop.domain.cart_service.CoinsUsageService
import com.yourfitness.shop.domain.orders.CartRepository
import com.yourfitness.shop.domain.cart_service.PrepareCartDataService
import com.yourfitness.shop.domain.model.CartCard
import com.yourfitness.shop.ui.constants.Constants
import com.yourfitness.shop.ui.features.catalog.CatalogIntent
import com.yourfitness.shop.ui.features.product_details.dialogs.SelectCoinsFlow
import com.yourfitness.shop.ui.navigation.ShopNavigation
import com.yourfitness.shop.ui.navigation.ShopNavigator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import java.lang.Long.max
import javax.inject.Inject
import kotlin.math.min

@HiltViewModel
class CartViewModel @Inject constructor(
    private val navigator: ShopNavigator,
    private val commonRestApi: CommonRestApi,
    private val cartRepository: CartRepository,
    private val cartDataService: PrepareCartDataService,
    private val shopStorage: ShopStorage,
    private val regionSettingsManager: RegionSettingsManager,
    private val commonStorage: CommonPreferencesStorage,
    private val coinsUsageService: CoinsUsageService,
    savedState: SavedStateHandle
) : MviViewModel<CartIntent, Any>() {

    private var currency = savedState.get<String>(Constants.CURRENCY).orEmpty()
    private var coinsValue = savedState.get<Double>(Constants.COINS_VALUE)

    private lateinit var items: List<CartDataEntity>
    private var deleteAnimationShowed = shopStorage.deleteAnimShowTimes > 2

    override fun handleIntent(intent: CartIntent) {
        when (intent) {
            is CartIntent.ScreenOpened -> loadData()
            is CartIntent.ItemDeleted -> deleteItemFromCart(intent)
            is CartIntent.CoinsPartChanged -> updateItemCoinsPart(intent)
            is CartIntent.EmptyCart -> emptyCart()
            is CartIntent.DeleteAnimationShowed -> {
                deleteAnimationShowed = true
                shopStorage.deleteAnimShowTimes++
            }
            is CartIntent.ChangeRedemptionCoinsTapped -> openEnterCoinsDialog(intent.itemId)
            is CartIntent.Checkout -> {
                viewModelScope.launch {
                    navigator.navigate(
                        ShopNavigation.DeliveryAddress(
                            coinsUsageService.notUsedCoins(),
                            coinsValue ?: 0.0,
                            currency
                        )
                    )
                }
            }
            is CartIntent.CoinsInfo -> {
                viewModelScope.launch {
                    navigator.navigate(
                        ShopNavigation.CoinsUsageInfo(
                            coinsUsageService.notUsedCoins(),
                            coinsValue ?: 0.0,
                            currency
                        )
                    )
                }
            }
        }
    }

    private suspend fun loadGeneralData() {
        state.postValue(CartState.Loading(true))
        val settings = regionSettingsManager.getSettings(true)
        currency = settings?.currency.orEmpty()
        coinsValue = settings?.coinValue ?: 0.0
        commonStorage.availableCoins = commonRestApi.coins()
        state.postValue(CartState.GeneralDataLoaded(coinsUsageService.notUsedCoins()))
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                if (currency.isBlank() || coinsValue == null) {
                    loadGeneralData()
                } else {
                    state.value = CartState.GeneralDataLoaded(coinsUsageService.notUsedCoins())
                }

                items = cartRepository.readCartItemsWithData()
                state.postValue(buildCartItemsState())
            } catch (error: Exception) {
                Timber.e(error)
                state.postValue(CartState.Error(error))
            }
        }
    }

    private fun deleteItemFromCart(item: CartIntent.ItemDeleted) {
        viewModelScope.launch {
            try {
                cartRepository.deleteById(item.uuid)
                items = cartRepository.readCartItemsWithData()
                if (items.isEmpty())
                    state.postValue(CartState.CartIsEmpty)
                else
                    state.postValue(buildCartItemsState())
            } catch (error: Exception) {
                Timber.e(error)
                state.postValue(CartState.Error(error))
            }
        }
    }

    private fun updateItemCoinsPart(item: CartIntent.CoinsPartChanged) {
        viewModelScope.launch {
            try {
                cartRepository.updateItemCoinsPart(item.uuid, item.coinsAmount)
                items = cartRepository.readCartItemsWithData()
                state.postValue(buildCartItemsState())
            } catch (error: Exception) {
                Timber.e(error)
                state.postValue(CartState.Error(error))
            }
        }
    }

    private fun emptyCart() {
        viewModelScope.launch {
        try {
            cartRepository.emptyCart()
            items = cartRepository.readCartItemsWithData()
            state.postValue(CartState.CartIsEmpty)
        } catch (error: Exception) {
            Timber.e(error)
            state.postValue(CartState.Error(error))
        }
    }
    }

    private suspend fun buildCartItemsState(): CartState.DataLoaded {
        val coinsValue = coinsValue ?: 0.0
        return CartState.DataLoaded(
            deleteAnimationShowed = deleteAnimationShowed,
            currency = currency,
            overallPrice = cartDataService.getOverallPrice(items, coinsValue),
            overallCoins = cartDataService.getOverallCoins(items),
            price = cartDataService.getWholePrice(items),
            priceWithoutCoins = cartDataService.getPriceWithoutCoins(items, coinsValue),
            items = items.map {
                CartCard(
                    uuid = it.cartItem.uuid,
                    title = it.product.product.name,
                    subtitle = it.product.product.brandName.orEmpty(),
                    currency = currency,
                    discountPrice = cartDataService.getItemPriceWithoutCoins(it, coinsValue),
                    wholePrice = it.product.product.price * it.cartItem.amount,
                    coinsAmount = it.cartItem.coinsPart,
                    coinsPriceEquivalent = cartDataService.getItemPricePart(it, coinsValue),
                    maxCoins = it.product.product.redeemableCoins,
                    coverImage = cartDataService.getCoverImage(it),
                    color = cartDataService.getColor(it),
                    size = it.cartItem.size,
                    sizeType = it.cartItem.sizeType,
                )
            },
            needSample = !deleteAnimationShowed,
            coins = coinsUsageService.notUsedCoins()
        )
    }

    private fun openEnterCoinsDialog(id: String) {
        val item = items.find { it.cartItem.uuid == id } ?: return
        val overallCoins = cartDataService.getOverallCoins(items)

        val availableToSelect =
            (commonStorage.availableCoins) - overallCoins + item.cartItem.amount * item.cartItem.coinsPart
        val selectedCoins = item.cartItem.amount * item.cartItem.coinsPart
        val maxCoinsToRedeem = max(selectedCoins, min(item.product.product.redeemableCoins * item.cartItem.amount, availableToSelect))
        if (maxCoinsToRedeem <= 0L && selectedCoins <= 0L) return
        navigator.navigate(ShopNavigation.SelectCoinsAmount(
            item.product.product.price,
            maxCoinsToRedeem,
            coinsValue ?: 0.0,
            currency,
            selectedCoins,
            SelectCoinsFlow.UPDATE
        ))
    }
}

open class CartIntent {
    object ScreenOpened : CartIntent()
    data class ItemDeleted(val uuid: String) : CartIntent()
    data class CoinsPartChanged(val uuid: String, val coinsAmount: Long) : CartIntent()
    object EmptyCart : CartIntent()
    object DeleteAnimationShowed : CartIntent()
    data class ChangeRedemptionCoinsTapped(val itemId: String) : CartIntent()
    object Checkout : CartIntent()
    object CoinsInfo : CartIntent()
}

open class CartState {
    data class Loading(val loading: Boolean): CartState()
    data class Error(val error: Exception) : CartState()
    data class GeneralDataLoaded(val coins: Long): CartState()
    data class DataLoaded(
        val deleteAnimationShowed: Boolean,
        val currency: String,
        val overallPrice: Long,
        val price: Long,
        val priceWithoutCoins: Long,
        val overallCoins: Long,
        val items: List<CartCard>,
        val needSample: Boolean,
        val coins: Long
    ) : CartState()
    object CartIsEmpty: CartState()
}
