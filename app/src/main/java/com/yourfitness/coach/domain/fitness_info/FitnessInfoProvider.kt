package com.yourfitness.coach.domain.fitness_info

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import com.yourfitness.common.BuildConfig
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

class FitnessInfoProvider : ContentProvider() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface FitnessInfoContentProviderEntryPoint {
        fun fitnessRepository(): ProviderFitnessDataRepository
        fun fitnessBackendRepository(): BackendFitnessDataRepository
    }

    companion object {
        private const val PROVIDER_NAME = "com.yourfitness.fitness"
        private const val infoCode = 12
        private const val backendInfoCode = 22
        private val uriMatcher: UriMatcher = UriMatcher(UriMatcher.NO_MATCH).also {
            it.addURI(
                PROVIDER_NAME,
                "fitnessInfo",
                infoCode
            )

            it.addURI(
                PROVIDER_NAME,
                "fitnessBackendInfo",
                backendInfoCode
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
            infoCode -> getFitnessInfo(uri, selection.orEmpty())
            backendInfoCode -> getFitnessBackendInfo(uri, selection.orEmpty())
            else -> null
        }
    }

    private fun getFitnessInfo(uri: Uri, dateDayStart: String): Cursor {
        val fitness = getFitnessRepositoryObject()
        val start = dateDayStart.toLong()
        val end = start + (24 * 60 * 60 * 1000) - 1
        val cursor = fitness.getNotSyncedDataForDayCursor(start, end)

        cursor.setNotificationUri(context?.contentResolver, uri)
        return cursor
    }

    private fun getFitnessBackendInfo(uri: Uri, dateDayStart: String): Cursor {
        val fitnessBackend = getFitnessBackendRepositoryObject()
        val start = dateDayStart.toLong()
        val end = start + (24 * 60 * 60 * 1000) - 1
        val cursor = fitnessBackend.getRecordForDayCursor(start, end)

        cursor.setNotificationUri(context?.contentResolver, uri)
        return cursor
    }

    private fun getFitnessRepositoryObject(): ProviderFitnessDataRepository {
        val hiltEntryPoint = initHiltEntryPoint()
        return hiltEntryPoint.fitnessRepository()
    }

    private fun getFitnessBackendRepositoryObject(): BackendFitnessDataRepository {
        val hiltEntryPoint = initHiltEntryPoint()
        return hiltEntryPoint.fitnessBackendRepository()
    }

    private fun initHiltEntryPoint(): FitnessInfoContentProviderEntryPoint {
        val appContext = context?.applicationContext ?: throw IllegalStateException()
        return EntryPointAccessors.fromApplication(
            appContext,
            FitnessInfoContentProviderEntryPoint::class.java
        )
    }

    override fun update(
        uri: Uri, values: ContentValues?, selection: String?,
        selectionArgs: Array<String>?
    ): Int = 0
}
