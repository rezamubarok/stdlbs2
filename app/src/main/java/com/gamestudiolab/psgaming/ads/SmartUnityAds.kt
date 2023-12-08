package com.gamestudiolab.psgaming.ads

import android.app.Activity
import com.gamestudiolab.psgaming.data.AppPrefs
import com.unity3d.ads.IUnityAdsInitializationListener
import com.unity3d.ads.UnityAds

object SmartUnityAds {
    private val mUnityAdsListener = object : IUnityAdsInitializationListener {
        override fun onInitializationComplete() {
        }

        override fun onInitializationFailed(
            p0: UnityAds.UnityAdsInitializationError?,
            placeId: String?
        ) {
        }
    }

    fun initUnityAds(activity: Activity) {
        UnityAds.initialize(activity.applicationContext, AppPrefs(activity).getUnityId(), mUnityAdsListener)
    }

    fun showUnity(activity: Activity) {
        if (UnityAds.isReady()) {
            UnityAds.show(activity)
        }
    }
}