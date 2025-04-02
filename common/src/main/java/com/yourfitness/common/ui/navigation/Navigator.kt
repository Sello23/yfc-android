package com.yourfitness.common.ui.navigation

import androidx.lifecycle.MutableLiveData
import com.yourfitness.common.domain.models.CreditCard
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommonNavigator @Inject constructor() {

    val navigation = MutableLiveData<CommonNavigation?>()

    fun navigate(node: CommonNavigation) {
        navigation.postValue(node)
    }

    suspend fun navigateDelayed(node: CommonNavigation) {
        delay(500)
        navigate(node)
    }
}

open class CommonNavigation {
    data class ConfiguredRoute(val popUpToId: Int, val destinationId: Int) : CommonNavigation()
    object WelcomeBack : CommonNavigation()
    data class AddCreditCard(val card: CreditCard) : CommonNavigation()
    object CommonError : CommonNavigation()
    object CommonError2Pop : CommonNavigation()
}