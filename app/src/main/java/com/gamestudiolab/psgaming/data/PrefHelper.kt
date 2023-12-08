package com.gamestudiolab.psgaming.data

interface PrefHelper {
    fun isAdmobEnabled(): Boolean

    fun setAdmobEnable(enable: Boolean)

    fun minDuration(): Long

    fun setMinDuration(duration: Long)

    fun maxDuration(): Long

    fun setMaxDuration(duration: Long)

    fun interstitialAdsAuto(): Boolean

    fun setInterstitialAdsAuto(auto: Boolean)

    fun setUnityId(unityId: String)

    fun getUnityId(): String
}