package com.gamestudiolab.psgaming.ads

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.View
import com.gamestudiolab.psgaming.data.AppPrefs
import com.gamestudiolab.psgaming.data.PrefHelper
import java.util.*

class SmartAds(private val activity: Activity) {
    private val random = Random()
    private var smartAdmobAds: SmartAdmobAds? = null
    private val prefHelper: PrefHelper = AppPrefs(activity)

    private val minDuration: Long by lazy {
        prefHelper.minDuration()
    }

    private val maxDuration: Long by lazy {
        prefHelper.maxDuration()
    }

    private var nextShow = minDuration

    private val mHandler = Handler(Looper.getMainLooper()) {
        generateNextShow()
        forceShow()
        showDelay()

        false
    }

    init {
        if (prefHelper.isAdmobEnabled()) {
            SmartUnityAds.initUnityAds(activity)
            smartAdmobAds = SmartAdmobAds(activity)
        }
    }

    fun onPause() {
        removeMessage()
    }

    fun onResume() {
        showDelay()
    }

    fun onDestroy() {
        removeMessage()
    }

    fun forceShow() {
        if (!prefHelper.interstitialAdsAuto()) return

        if (random.nextInt(100) < 80 && smartAdmobAds?.isReady() == true) {
            smartAdmobAds?.showAdmob()
        } else {
            SmartUnityAds.showUnity(activity)
        }
    }

    fun forceShowBoolean() {
        if (random.nextBoolean()) {
            forceShow()
        }
    }

    private fun generateNextShow() {
        nextShow = minDuration + random.nextInt((maxDuration - minDuration).toInt())
    }

    private fun removeMessage() {
        mHandler.removeMessages(DELAY)
    }

    private fun showDelay() {
        removeMessage()
        val message = Message()
        message.what = DELAY
        mHandler.sendMessageDelayed(message, nextShow)
    }

    fun getAdView(): View? = smartAdmobAds?.getAdView(activity)

    companion object {
        private const val DELAY = 1
    }
}
