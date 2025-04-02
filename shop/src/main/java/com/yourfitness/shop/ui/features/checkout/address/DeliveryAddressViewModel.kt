package com.yourfitness.shop.ui.features.checkout.address

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.yourfitness.common.CommonPreferencesStorage
import com.yourfitness.common.data.entity.fullName
import com.yourfitness.common.domain.ProfileRepository
import com.yourfitness.common.ui.mvi.MviViewModel
import com.yourfitness.shop.data.dao.AddressDao
import com.yourfitness.shop.data.entity.AddressEntity
import com.yourfitness.shop.domain.cart_service.CoinsUsageService
import com.yourfitness.shop.domain.model.AddressInfo
import com.yourfitness.shop.domain.model.ContactInfo
import com.yourfitness.shop.ui.constants.Constants
import com.yourfitness.shop.ui.navigation.ShopNavigation
import com.yourfitness.shop.ui.navigation.ShopNavigator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class DeliveryAddressViewModel @Inject constructor(
    private val navigator: ShopNavigator,
    private val dao: AddressDao,
    private val profileRepository: ProfileRepository,
    private val commonStorage: CommonPreferencesStorage,
    private val coinsUsageService: CoinsUsageService,
    savedState: SavedStateHandle
) : MviViewModel<Any, Any>() {

    val coinsAmount = savedState.get<Long>(Constants.COINS_AMOUNT) ?: 0
    val coinsValue = savedState.get<Double>(Constants.COINS_VALUE) ?: 0.0
    val currency = savedState.get<String>(Constants.CURRENCY).orEmpty()
    private var address: AddressEntity = AddressEntity()

    init {
        fetchData()
    }

    override fun handleIntent(intent: Any) {
        when (intent) {
            is DeliveryAddressIntent.Save -> confirmAddressInfo(intent)
        }
    }

    private fun fetchData() {
        viewModelScope.launch {
            try {
                val profile = profileRepository.getProfile()
                var name = profile.fullName
                var email = profile.email
                var phone = profile.phoneNumber
                if (name.isBlank() || email == null || phone == null) {
                    profileRepository.getProfile(true)
                    name = profile.fullName
                    email = profile.email.orEmpty()
                    phone = profile.phoneNumber.orEmpty()
                }
                address = dao.getAddress() ?: AddressEntity()
                state.postValue(DeliveryAddressState.AddressLoaded(address, name, email, phone))
            } catch (error: Exception) {
                Timber.e(error)
                state.postValue(DeliveryAddressState.Error(error))
            }
        }
    }

    private fun confirmAddressInfo(intent: DeliveryAddressIntent.Save) {
        viewModelScope.launch {
            try {
                val contactInfo = ContactInfo(intent.fullName, intent.email, intent.phoneNumber)
                val addressInfo = AddressInfo(
                    city = intent.city,
                    street = intent.street,
                    addressDetails = intent.addressDetails
                )
                var addressEntity: AddressEntity? = null
                if (intent.saveAddress) {
                    addressEntity = AddressEntity(
                        city = intent.city,
                        street = intent.street,
                        details = intent.addressDetails
                    )
                }
                state.postValue(DeliveryAddressState.LastState)
                navigator.navigate(
                    ShopNavigation.PaymentOptions(
                        coinsAmount,
                        addressInfo,
                        contactInfo,
                        addressEntity
                    )
                )

            } catch (error: Exception) {
                Timber.e(error)
                state.postValue(DeliveryAddressState.Error(error))
            }
        }
    }
}

open class DeliveryAddressState {
    data class AddressLoaded(
        val address: AddressEntity,
        val fullName: String,
        val email: String,
        val phone: String
    ) : DeliveryAddressState()
    data class Error(val error: Exception) : DeliveryAddressState()
    object LastState : DeliveryAddressState()
}

open class DeliveryAddressIntent {
    data class Save(
        val saveAddress: Boolean,
        val city: String,
        val street: String,
        val addressDetails: String,
        val fullName: String,
        val email: String,
        val phoneNumber: String
    ) : DeliveryAddressIntent()
}
