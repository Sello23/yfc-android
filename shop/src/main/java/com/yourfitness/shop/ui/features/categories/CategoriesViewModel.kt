package com.yourfitness.shop.ui.features.categories

import androidx.lifecycle.viewModelScope
import com.yourfitness.common.CommonPreferencesStorage
import com.yourfitness.common.domain.settings.RegionSettingsManager
import com.yourfitness.common.network.CommonRestApi
import com.yourfitness.common.ui.mvi.MviViewModel
import com.yourfitness.shop.data.entity.ProductTypeId
import com.yourfitness.shop.domain.cart_service.CoinsUsageService
import com.yourfitness.shop.domain.orders.CartRepository
import com.yourfitness.shop.domain.products.ProductFilters
import com.yourfitness.shop.ui.features.orders.cart.CartState
import com.yourfitness.shop.ui.navigation.ShopNavigation
import com.yourfitness.shop.ui.navigation.ShopNavigator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class CategoriesViewModel @Inject constructor(
    private val navigator: ShopNavigator,
    private val cartRepository: CartRepository,
    private val commonRestApi: CommonRestApi,
    private val regionSettingsManager: RegionSettingsManager,
    private val commonStorage: CommonPreferencesStorage,
    private val coinsUsageService: CoinsUsageService,
) : MviViewModel<CategoriesIntent, Any>() {

    override fun handleIntent(intent: CategoriesIntent) {
        when (intent) {
            is CategoriesIntent.ScreenOpened -> loadData()
            is CategoriesIntent.Apparel -> {
                navigator.navigate(ShopNavigation.Catalog(ProductTypeId.APPAREL))
            }
            is CategoriesIntent.Services -> {
                navigator.navigate(ShopNavigation.Catalog(ProductTypeId.SERVICES))
            }
            is CategoriesIntent.Equipment -> {
                navigator.navigate(ShopNavigation.Catalog(ProductTypeId.EQUIPMENT))
            }
            is CategoriesIntent.Accessories -> {
                navigator.navigate(ShopNavigation.Catalog(ProductTypeId.ACCESSORIES))
            }
            is CategoriesIntent.Cart -> {
                navigator.navigate(ShopNavigation.Cart())
            }
            is CategoriesIntent.OrderHistory -> {
                navigator.navigate(ShopNavigation.OrderHistory())
            }
            is CategoriesIntent.NavigateTo -> {
                navigator.navigate(intent.destination)
            }
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                val size = cartRepository.getCartSize()
                val settings = regionSettingsManager.getSettings(true)
                commonStorage.availableCoins = commonRestApi.coins()
                state.postValue(CategoriesState.CartSizeLoaded(
                    size,
                    coinsUsageService.notUsedCoins(),
                    settings?.coinValue ?: 0.0,
                    settings?.currency.orEmpty()
                    ))
            } catch (error: Exception) {
                Timber.e(error)
                state.postValue(CartState.Error(error))
            }
        }
    }
}

open class CategoriesIntent {
    object ScreenOpened : CategoriesIntent()
    data class Apparel(val title: String) : CategoriesIntent()
    data class Services(val title: String) : CategoriesIntent()
    data class Equipment(val title: String) : CategoriesIntent()
    data class Accessories(val title: String) : CategoriesIntent()
    object Cart : CategoriesIntent()
    object OrderHistory : CategoriesIntent()
    data class NavigateTo(val destination: ShopNavigation) : CategoriesIntent()
}

open class CategoriesState {
    data class CartSizeLoaded(
        val size: Int,
        val coins: Long,
        val coinsCost: Double,
        val currency: String,
    ) : CategoriesState()
}