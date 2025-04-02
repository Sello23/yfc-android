package com.yourfitness.coach.domain.facility

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import com.yourfitness.coach.PreferencesStorage
import com.yourfitness.coach.data.dao.FacilityDao
import com.yourfitness.common.domain.date.now
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FacilitiesContentProvider : ContentProvider() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface FacilitiesContentProviderEntryPoint {
        fun dao(): FacilityDao
        fun storage(): PreferencesStorage
        fun repository(): FacilityRepository
    }

    companion object {
        private const val PROVIDER_NAME = "com.yourfitness.facilities"
        private const val URL = "content://$PROVIDER_NAME/facilities"
        private const val facilitiesCode = 1
        private const val facilityCode = 2
        private val uriMatcher: UriMatcher = UriMatcher(UriMatcher.NO_MATCH).also {
            it.addURI(
                PROVIDER_NAME,
                "facilities",
                facilitiesCode
            )

            it.addURI(
                PROVIDER_NAME,
                "facility",
                facilityCode
            )
        }
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int = 0

    override fun getType(uri: Uri): String? = null

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null

    override fun onCreate(): Boolean = true

    override fun query(
        uri: Uri, projection: Array<String>?, selection: String?,
        selectionArgs: Array<String>?, sortOrder: String?
    ): Cursor? {
        return when (uriMatcher.match(uri)) {
            facilitiesCode -> getFacilities(uri)
            facilityCode -> getFacility(uri, selection.orEmpty())
            else -> null
        }
    }

    private fun getFacilities(uri: Uri): Cursor? {
        val dao = getDaoObject()
        val cursor = dao.readAllFacilitiesCursor()
        cursor?.setNotificationUri(context?.contentResolver, uri)
        return cursor
    }

    private fun getFacility(uri: Uri, facilityId: String): Cursor? {
        val storage = getStorageObject()
        val dao = getDaoObject()
        val startTime = now()

        if (storage.facilitiesLoaded == -1) {
            val repository = getRepositoryObject()
            CoroutineScope(Dispatchers.IO).launch {
                repository.downloadFacilitiesData()
            }
        }

        while (storage.facilitiesLoaded != 1) {
            Thread.sleep(100)
            if ((now() - startTime) > 10000) break
        }
        val cursor = dao.readFacilityByIdCursor(facilityId)
        cursor?.setNotificationUri(context?.contentResolver, uri)
        return cursor
    }

    private fun getDaoObject(): FacilityDao {
        val hiltEntryPoint = initHiltEntryPoint()
        return hiltEntryPoint.dao()
    }

    private fun getStorageObject(): PreferencesStorage {
        val hiltEntryPoint = initHiltEntryPoint()
        return hiltEntryPoint.storage()
    }

    private fun getRepositoryObject(): FacilityRepository {
        val hiltEntryPoint = initHiltEntryPoint()
        return hiltEntryPoint.repository()
    }

    private fun initHiltEntryPoint(): FacilitiesContentProviderEntryPoint {
        val appContext = context?.applicationContext ?: throw IllegalStateException()
        return EntryPointAccessors.fromApplication(
            appContext,
            FacilitiesContentProviderEntryPoint::class.java
        )
    }

    override fun update(
        uri: Uri, values: ContentValues?, selection: String?,
        selectionArgs: Array<String>?
    ): Int = 0
}
