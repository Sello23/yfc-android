package com.yourfitness.shop.data

import android.content.Context
import android.content.SharedPreferences
import com.yourfitness.shop.data.PreferenceKeys.ACCESSORIES_SYNCED_AT
import com.yourfitness.shop.data.PreferenceKeys.APPAREL_SYNCED_AT
import com.yourfitness.shop.data.PreferenceKeys.DELETE_ANIM_TIMES
import com.yourfitness.shop.data.PreferenceKeys.EQUIPMENT_SYNCED_AT
import com.yourfitness.shop.data.PreferenceKeys.INTRO_SHOWED
import com.yourfitness.shop.data.PreferenceKeys.ORDERS_SYNCED_AT
import com.yourfitness.shop.data.PreferenceKeys.SERVICES_SYNCED_AT
import com.yourfitness.shop.data.PreferenceKeys.SERVICE_ORDERS_SYNCED_AT
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShopStorage @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("shop", Context.MODE_PRIVATE)

    var introShown: Boolean
        get() = getBoolean(INTRO_SHOWED)
        set(value) = setBoolean(INTRO_SHOWED, value)

    var deleteAnimShowTimes: Int
        get() = getInt(DELETE_ANIM_TIMES)
        set(times) = setInt(DELETE_ANIM_TIMES, times)

    var apparelLastSyncedAt: Long?
        get() = getLastSyncedAtTime(APPAREL_SYNCED_AT)
        set(time) = setLastSyncedAtTime(APPAREL_SYNCED_AT, time)

    var equipmentLastSyncedAt: Long?
        get() = getLastSyncedAtTime(EQUIPMENT_SYNCED_AT)
        set(time) = setLastSyncedAtTime(EQUIPMENT_SYNCED_AT, time)

    var accessoriesLastSyncedAt: Long?
        get() = getLastSyncedAtTime(ACCESSORIES_SYNCED_AT)
        set(time) = setLastSyncedAtTime(ACCESSORIES_SYNCED_AT, time)

    var servicesLastSyncedAt: Long?
        get() = getLastSyncedAtTime(SERVICES_SYNCED_AT)
        set(time) = setLastSyncedAtTime(SERVICES_SYNCED_AT, time)

    var ordersLastSyncedAt: String?
        get() = getLastSyncedAt(ORDERS_SYNCED_AT)
        set(time) = setLastSyncedAt(ORDERS_SYNCED_AT, time)

    var serviceOrdersLastSyncedAt: String?
        get() = getLastSyncedAt(SERVICE_ORDERS_SYNCED_AT)
        set(time) = setLastSyncedAt(SERVICE_ORDERS_SYNCED_AT, time)


    fun clearShopData() {
        sharedPreferences.edit()
            .remove(APPAREL_SYNCED_AT)
            .remove(EQUIPMENT_SYNCED_AT)
            .remove(ACCESSORIES_SYNCED_AT)
            .remove(DELETE_ANIM_TIMES)
            .remove(ORDERS_SYNCED_AT)
            .remove(SERVICES_SYNCED_AT)
            .remove(SERVICE_ORDERS_SYNCED_AT)
            .remove(INTRO_SHOWED)
            .apply()
    }

    private fun getInt(name: String): Int {
        return sharedPreferences.getInt(name, 0)
    }

    private fun setInt(name: String, value: Int) {
        sharedPreferences.edit().putInt(name, value).apply()
    }

    private fun getLong(name: String): Long {
        return sharedPreferences.getLong(name, 0L)
    }

    private fun setLong(name: String, value: Long) {
        sharedPreferences.edit().putLong(name, value).apply()
    }

    private fun getBoolean(name: String): Boolean {
        return sharedPreferences.getBoolean(name, false)
    }

    private fun setBoolean(name: String, value: Boolean) {
        sharedPreferences.edit().putBoolean(name, value).apply()
    }

    private fun getLastSyncedAt(name: String): String? {
        return sharedPreferences.getString(name, null)
    }

    private fun setLastSyncedAt(name: String, value: String?) {
        sharedPreferences.edit().putString(name, value).apply()
    }

    private fun getLastSyncedAtTime(name: String): Long? {
        val res = sharedPreferences.getLong(name, -1L)
        return if (res == -1L) null else res
    }

    private fun setLastSyncedAtTime(name: String, value: Long?) {
        sharedPreferences.edit().putLong(name, value ?: -1L).apply()
    }
}

private object PreferenceKeys {
    const val INTRO_SHOWED = "intro_SHOWED"
    const val APPAREL_SYNCED_AT = "apparel_synced_at2"
    const val EQUIPMENT_SYNCED_AT = "equipment_synced_at2"
    const val ACCESSORIES_SYNCED_AT = "accessories_synced_at2"
    const val SERVICES_SYNCED_AT = "services_synced_at2"
    const val ORDERS_SYNCED_AT = "orders_synced_at"
    const val SERVICE_ORDERS_SYNCED_AT = "service_orders_synced_at"
    const val DELETE_ANIM_TIMES = "delete_anim_times"
}