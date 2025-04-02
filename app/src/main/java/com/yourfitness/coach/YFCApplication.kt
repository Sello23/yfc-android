package com.yourfitness.coach

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.stripe.android.PaymentConfiguration
import com.yourfitness.common.BuildConfig.STRIPE_PUBLISHABLE_KEY
import com.yourfitness.common.CommonPreferencesStorage
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class YFCApplication : Application(), Configuration.Provider  {
    @Inject
    lateinit var preferencesStorage: PreferencesStorage
    @Inject
    lateinit var commonStorage: CommonPreferencesStorage
    @Inject
    lateinit var workerFactory: HiltWorkerFactory
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.DEBUG)
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        initStripePaymentConfiguration()
        if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())

        preferencesStorage.facilitiesLoaded = -1
    }

    private fun initStripePaymentConfiguration() {
        PaymentConfiguration.init(
            applicationContext,
            STRIPE_PUBLISHABLE_KEY
        )
    }
}