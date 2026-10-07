package com.example.util

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

object InterstitialAdManager {
    // Official Google sample interstitial test ad unit ID:
    const val SAMPLE_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"

    private var interstitialAd: InterstitialAd? = null
    private var isLoading = false

    // Professional UX frequency capping:
    // 1. Minimum actions between ads (e.g. 2 actions: complete task, save file)
    // 2. Minimum interval between ads (45 seconds) to avoid spamming the employee
    private const val ACTIONS_THRESHOLD = 2
    private const val MIN_COOLDOWN_MILLIS = 45_000L

    private var actionCounter = 0
    private var lastAdShownTimestamp = 0L

    fun preload(context: Context) {
        if (interstitialAd != null || isLoading) return
        isLoading = true

        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context.applicationContext,
            SAMPLE_INTERSTITIAL_AD_UNIT_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isLoading = false
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    interstitialAd = null
                    isLoading = false
                }
            }
        )
    }

    /**
     * Call this when a high-frequency action occurs (e.g. task completed, file saved).
     * Automatically enforces frequency capping and cooldown for a smooth, professional UX.
     */
    fun triggerActionAd(
        activity: Activity?,
        onActionCompleted: () -> Unit = {}
    ) {
        actionCounter++
        val now = System.currentTimeMillis()
        val isCooldownPassed = (now - lastAdShownTimestamp) >= MIN_COOLDOWN_MILLIS
        val isThresholdMet = actionCounter >= ACTIONS_THRESHOLD

        if (activity != null && isThresholdMet && isCooldownPassed && interstitialAd != null) {
            val ad = interstitialAd
            ad?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    lastAdShownTimestamp = System.currentTimeMillis()
                    actionCounter = 0
                    preload(activity)
                    onActionCompleted()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    interstitialAd = null
                    preload(activity)
                    onActionCompleted()
                }

                override fun onAdShowedFullScreenContent() {
                    interstitialAd = null
                }
            }
            ad?.show(activity)
        } else {
            // If ad not shown now, ensure an ad is preloading for next time
            if (activity != null && interstitialAd == null) {
                preload(activity)
            }
            onActionCompleted()
        }
    }
}
