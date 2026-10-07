package com.example

import android.app.Application
import com.example.di.AppContainer
import com.example.di.DefaultAppContainer
import com.example.util.NotificationHelper
import com.google.android.gms.ads.MobileAds

class GovWorkApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
        NotificationHelper.createNotificationChannel(this)

        // Initialize Google Mobile Ads SDK asynchronously
        try {
            MobileAds.initialize(this) {
                com.example.util.InterstitialAdManager.preload(this)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
