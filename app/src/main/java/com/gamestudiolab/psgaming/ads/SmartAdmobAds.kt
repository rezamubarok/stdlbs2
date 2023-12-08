package com.gamestudiolab.psgaming.ads

import android.app.Activity
import android.view.View
import com.google.android.gms.ads.*
import com.google.android.gms.ads.InterstitialAd
import com.gamestudiolab.psgaming.R

class SmartAdmobAds constructor(activity: Activity) {
    private var interstitialAd: InterstitialAd? = null
    private var adView: AdView? = null

    init {
        initAdmob(activity)
    }

    private val mAdListener = object : AdListener() {
        override fun onAdClosed() {
            super.onAdClosed()
            interstitialAd?.loadAd(AdRequest.Builder().build())
        }
    }

    private val mAdViewListener = object : AdListener() {
        override fun onAdOpened() {
            super.onAdOpened()
            adView?.visibility = View.GONE
        }

        override fun onAdClicked() {
            super.onAdClicked()
            adView?.visibility = View.GONE
        }
    }

    private fun initAdmob(activity: Activity) {
        MobileAds.initialize(activity)

        interstitialAd = InterstitialAd(activity).apply {
            adUnitId = activity.getString(R.string.admob_ads_interstitial)
            adListener = mAdListener
            loadAd(AdRequest.Builder().build())
        }
    }

    fun isReady() = interstitialAd?.isLoaded == true

    fun showAdmob() {
        if (interstitialAd?.isLoaded == true) {
            interstitialAd?.show()
        }
    }

    fun getAdView(activity: Activity, size: AdSize = AdSize.BANNER): AdView? {
        adView = AdView(activity).apply {
            adUnitId = activity.getString(R.string.admob_ads_banner)
            adSize = size
            adListener = mAdViewListener

            loadAd(AdRequest.Builder().build())
        }

        return adView
    }
}