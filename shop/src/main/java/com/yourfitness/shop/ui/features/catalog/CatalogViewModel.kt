package com.yourfitness.shop.ui.features.catalog

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil
import com.yourfitness.common.domain.location.LocationRepository
import com.yourfitness.common.domain.settings.RegionSettingsManager
import com.yourfitness.common.ui.mvi.MviViewModel
import com.yourfitness.common.ui.utils.formatAmount
import com.yourfitness.common.ui.utils.formatDistance
import com.yourfitness.common.ui.utils.toCoins
import com.yourfitness.common.ui.utils.toCurrencyRounded
import com.yourfitness.common.ui.utils.toImageUri
import com.yourfitness.common.ui.utils.toLocalGenderValue
import com.yourfitness.shop.data.entity.ProductDataEntity
import com.yourfitness.shop.data.entity.ProductEntity
import com.yourfitness.shop.data.entity.ProductTypeId
import com.yourfitness.shop.domain.cart_service.CoinsUsageService
import com.yourfitness.shop.domain.cart_service.SortCartDataService
import com.yourfitness.shop.domain.cart_service.SortType
import com.yourfitness.shop.domain.model.CatalogCard
import com.yourfitness.shop.domain.orders.CartRepository
import com.yourfitness.shop.domain.products.ProductFilters
import com.yourfitness.shop.domain.products.ProductsRepository
import com.yourfitness.shop.domain.products.isEmpty
import com.yourfitness.shop.ui.constants.Constants.Companion.FILTERS
import com.yourfitness.shop.ui.constants.Constants.Companion.PRODUCT_TYPE_ID
import com.yourfitness.shop.ui.navigation.ShopNavigation
import com.yourfitness.shop.ui.navigation.ShopNavigator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.lang.Integer.min
import javax.inject.Inject
import kotlin.math.roundToLong

@HiltViewModel
class ServicesViewModel @Inject constructor(
    navigator: ShopNavigator,
    productsRepository: ProductsRepository,
    cartRepository: CartRepository,
    sortService: SortCartDataService,
    locationRepository: LocationRepository,
    regionSettingsManager: RegionSettingsManager,
    coinsUsageService: CoinsUsageService,
    savedState: SavedStateHandle
) : CatalogViewModel(
    navigator,
    productsRepository,
    cartRepository,
    sortService,
    locationRepository,
    regionSettingsManager,
    coinsUsageService,
    savedState
) {

    override fun openFilterDialog() {
        state.value = CatalogState.StubState
        navigator.navigate(
            ShopNavigation.ServicesFilter(
                searchQuery, filters, isFavoriteChecked, productTypeId.value
            )
        )
    }

    override suspend fun List<ProductDataEntity>.filterProductsBase(): List<ProductDataEntity> {
        return filter {
            val distance = calculateDistance(it.product.latitude, it.product.longitude) ?: -1L
            distance in 0..filters.maxPrice
        }
    }

    override fun ProductEntity.getScreenCardSubtitle() = vendorName
}

@HiltViewModel
open class CatalogViewModel @Inject constructor(
    protected val navigator: ShopNavigator,
    private val productsRepository: ProductsRepository,
    private val cartRepository: CartRepository,
    private val sortService: SortCartDataService,
    private val locationRepository: LocationRepository,
    private val regionSettingsManager: RegionSettingsManager,
    private val coinsUsageService: CoinsUsageService,
    savedState: SavedStateHandle
) : MviViewModel<CatalogIntent, CatalogState>() {

    private var modelInited = false

    val productTypeId: ProductTypeId = savedState[PRODUCT_TYPE_ID] ?:ProductTypeId.APPAREL

    private var currency: String = ""
    private var coinsValue: Double = 0.0

    protected var products: List<ProductDataEntity> = listOf()
    private var screenData: MutableList<CatalogCard> = mutableListOf()
    private var shuffledIndexes: List<Int> = emptyList()

    var filters: ProductFilters = savedState[FILTERS] ?:ProductFilters()
        private set
    private var sortType: SortType? = null
    protected var searchQuery: String = ""

    var isFavoriteChecked: Boolean = false
        private set
    var isSearchActive = false

    private var currentLatLang: LatLng? = null


    init {
        fetchData()
    }

    private fun fetchData() {
        viewModelScope.launch {
            try {
                state.postValue(CatalogState.Loading)
                productsRepository.downloadProducts(productTypeId)
                productsRepository.downloadFavorites()
                loadScreenData()
                modelInited = true
            } catch (error: Exception) {
                Timber.e(error)
                state.postValue(CatalogState.Error(error))
            }
        }
    }

    private suspend fun loadScreenData() {
        try {
            val settings = regionSettingsManager.getSettings()
            currency = settings?.currency.orEmpty()
            coinsValue = settings?.coinValue ?: 0.0
            loadActualData()
        } catch (error: Exception) {
            Timber.e(error)
        }
    }

    override fun handleIntent(intent: CatalogIntent) {
        when (intent) {
            is CatalogIntent.ScreenOpened -> if (modelInited) loadActualData()
            is CatalogIntent.Details -> openDetailsScreen(intent)
            is CatalogIntent.Cart -> openCartScreen()
            is CatalogIntent.FilterTapped -> openFilterDialog()
            is CatalogIntent.SortTypeChanged -> sort(intent.type)
            is CatalogIntent.FilterApplied -> filter(intent.filters)
            is CatalogIntent.Search -> search(intent.query)
            is CatalogIntent.FavoriteChanged -> updateFavorites(intent)
            is CatalogIntent.UploadFavorites -> uploadFavorites()
            is CatalogIntent.FilterFavoriteChanged -> favoritesFilter(intent.isChecked)
            is CatalogIntent.OnNewPage -> loadPage(intent.page, intent.totalNumber)
            is CatalogIntent.CoinsInfo -> {
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
            is CatalogIntent.CartClosed -> loadCartSize()
        }
    }

    private fun openCartScreen() {
        state.value = CatalogState.StubState
        navigator.navigate(
            ShopNavigation.Cart(currency, coinsValue)
        )
    }

    private fun openDetailsScreen(intent: CatalogIntent.Details) {
        state.value = CatalogState.StubState
        navigator.navigate(
            ShopNavigation.ProductDetails(
                intent.category,
                intent.productId,
                currency,
                coinsValue,
                productTypeId.value,
                productTypeId == ProductTypeId.SERVICES
            )
        )
    }

    protected open fun openFilterDialog() {
        state.value = CatalogState.StubState
        navigator.navigate(
            ShopNavigation.ProductFilter(
                currency, searchQuery, filters, isFavoriteChecked, productTypeId.value
            )
        )
    }

    private fun updateFavorites(data: CatalogIntent.FavoriteChanged) {
        viewModelScope.launch {
            productsRepository.saveFavorite(data.checked, data.dbId, data.productId)

            if (data.checked) {
                state.postValue(CatalogState.FavoriteAdded)
            }

            if (isFavoriteChecked) {
                loadActualData(EmptyState.NO_FAVORITE_ITEMS)
                return@launch
            }

            val index = screenData.indexOfFirst { it.id == data.productId }
            if (index >= 0) {
                screenData[index] = screenData[index].copy(isFavorite = data.checked)
                state.postValue(
                    CatalogState.FavoritesUpdated(
                        screenData,
                        index,
                        coinsUsageService.notUsedCoins(),
                        cartRepository.getCartSize()
                    )
                )
            }
        }
    }

    private fun sort(type: SortType) {
        sortType = type
        loadActualData()
    }

    private fun search(query: String) {
        searchQuery = query
        loadActualData(EmptyState.NO_SEARCH_ITEMS)
    }

    private fun filter(filter: ProductFilters) {
        filters = filter
        loadActualData()
    }

    private fun favoritesFilter(isChecked: Boolean) {
        isFavoriteChecked = isChecked
        loadActualData(EmptyState.NO_FAVORITE_ITEMS)
    }

    protected open suspend fun List<ProductDataEntity>.filterProductsBase(): List<ProductDataEntity> {
        return filter { it.product.price in filters.minPrice..filters.maxPrice }
    }

    private fun List<ProductDataEntity>.filterByCategory(): List<ProductDataEntity> {
        return filter { item ->
            filters.types.isEmpty() || filters.types.contains(item.product.subcategory.orEmpty())
        }
    }

    private fun List<ProductDataEntity>.filterByBrand(): List<ProductDataEntity> {
        return filter { item ->
            filters.brands.isEmpty() || filters.brands.contains(item.product.brandName.orEmpty())
        }
    }

    private fun List<ProductDataEntity>.filterByGender(): List<ProductDataEntity> {
        return filter { item ->
            filters.genders.isEmpty() || filters.genders.contains(item.product.gender.toLocalGenderValue())
        }
    }

    private fun List<ProductDataEntity>.searchProducts(): List<ProductDataEntity> {
        val normalizedQuery = searchQuery.trim().lowercase()
        return filter { it.product.name.lowercase().contains(normalizedQuery) }
    }

    private fun List<ProductDataEntity>.filterByFavorite(): List<ProductDataEntity> {
        if (!isFavoriteChecked) return this
        return filter { it.favourite != null }
    }

    private fun loadActualData(emptyState: EmptyState? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                products = productsRepository.getProductList(productTypeId.value)
                if (shuffledIndexes.isEmpty()) {
                    shuffledIndexes = List(products.size) { it }.shuffled()
                }
                val shuffledItems = mutableListOf<ProductDataEntity>()
                shuffledIndexes.forEach { shuffledItems.add(products[it]) }
                var items = shuffledItems.toList()
                    .filterByFavorite()
                    .searchProducts()
                    .filterProductsBase()
                    .filterByCategory()
                    .filterByBrand()
                    .filterByGender()

                items = sortService.sortByPrice(items, sortType)
                currentLatLang = null
                screenData = items.buildScreenData()
                state.postValue(
                    CatalogState.ProductsUpdated(
                        screenData.getPageData(0),
                        currency,
                        coinsValue,
                        coinsUsageService.notUsedCoins(),
                        searchQuery,
                        emptyState ?: EmptyState.NO_SEARCH_ITEMS,
                        !filters.isEmpty(),
                        cartRepository.getCartSize()
                    )
                )
            } catch (error: Exception) {
                Timber.e(error)
            }
        }
    }

    private suspend fun List<ProductDataEntity>.buildScreenData(): MutableList<CatalogCard> {
        val cardList = mutableListOf<CatalogCard>()
        forEach { item ->
            val product = item.product
            val distance = calculateDistance(product.latitude, product.longitude).formatDistance().orEmpty()
            cardList.add(
                CatalogCard(
                    product._id,
                    product.id,
                    product.name,
                    product.getScreenCardSubtitle(),
                    product.defaultImageId.toImageUri(),
                    product.price.formatAmount(currency.uppercase()),
                    product.redeemableCoins,
                    (product.price - (product.redeemableCoins * coinsValue).toCurrencyRounded().toCoins())
                        .formatAmount(currency.uppercase()),
                    item.favourite != null,
                    distance,
                    product.vendorImageId?.toImageUri(),
                    product.vendorAddress
                )
            )
        }
        currentLatLang = null
        return cardList
    }

    protected open fun ProductEntity.getScreenCardSubtitle() = brandName.orEmpty()

    protected suspend fun calculateDistance(latitude: Double?, longitude: Double?): Long? {
        return if (latitude != null && longitude != null) {
            if (currentLatLang == null) {
                currentLatLang = locationRepository.lastLocation()
            }
            SphericalUtil.computeDistanceBetween(LatLng(latitude, longitude), currentLatLang).roundToLong()
        } else {
            null
        }
    }

    private fun uploadFavorites() {
        GlobalScope.launch {
            productsRepository.syncFavorites()
        }
    }

    private fun loadPage(page: Int, totalNumber: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                state.postValue(
                    CatalogState.PageLoaded(
                        screenData.getPageData(totalNumber),
                        coinsUsageService.notUsedCoins(),
                    )
                )
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    private fun List<CatalogCard>.getPageData(totalNumber: Int): List<CatalogCard> {
        val endIndex = min(totalNumber + 16, screenData.size)
        return if (totalNumber >= endIndex) emptyList() else subList(totalNumber, endIndex)
    }

    private fun loadCartSize() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                state.postValue(
                    CatalogState.CartSizeUpdated(
                        coinsUsageService.notUsedCoins(),
                        cartRepository.getCartSize()
                    )
                )
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }
}

open class CatalogIntent {
    data class Details(val category: String, val productId: String) : CatalogIntent()
    object Cart : CatalogIntent()
    object CoinsInfo : CatalogIntent()
    object ScreenOpened : CatalogIntent()
    data class SortTypeChanged(val type: SortType) : CatalogIntent()
    object FilterTapped : CatalogIntent()
    data class FilterApplied(
        val filters: ProductFilters
    ) : CatalogIntent()

    data class Search(val query: String) : CatalogIntent()
    data class FavoriteChanged(
        val dbId: Int,
        val productId: String,
        val checked: Boolean
    ) : CatalogIntent()
    object UploadFavorites : CatalogIntent()
    data class FilterFavoriteChanged(val isChecked: Boolean) : CatalogIntent()
    data class OnNewPage(val page: Int, val totalNumber: Int) : CatalogIntent()
    object CartClosed : CatalogIntent()
}

open class CatalogState {
    object Loading : CatalogState()
    data class Loaded(
        val productList: List<CatalogCard>,
        val coins: Long,
        val cartSize: Int
    ) : CatalogState()

    data class Error(val error: Exception) : CatalogState()
    data class CartSizeLoaded(val size: Int) : CatalogState()
    data class ProductsUpdated(
        val products: List<CatalogCard>,
        val currency: String,
        val coinsValue: Double,
        val coins: Long,
        val query: String? = null,
        val emptyState: EmptyState? = null,
        val filterApplied: Boolean = false,
        val cartSize: Int
    ) : CatalogState()

    object StubState : CatalogState()
    object FavoriteAdded : CatalogState()
    data class PageLoaded(val productList: List<CatalogCard>, val coins: Long) : CatalogState()
    data class FavoritesUpdated(
        val products: List<CatalogCard>,
        val index: Int,
        val coins: Long,
        val cartSize: Int
    ) : CatalogState()
    data class CartSizeUpdated(
        val coins: Long,
        val cartSize: Int
    ) : CatalogState()
}
