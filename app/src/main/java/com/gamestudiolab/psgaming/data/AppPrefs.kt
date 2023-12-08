package com.gamestudiolab.psgaming.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.gamestudiolab.psgaming.R

class AppPrefs constructor(private val context: Context) : PrefHelper {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)

    override fun isAdmobEnabled(): Boolean = sharedPreferences.getBoolean(ADMOB_ENABLED, false)

    override fun setAdmobEnable(enable: Boolean) {
        sharedPreferences.edit { putBoolean(ADMOB_ENABLED, enable) }
    }

    override fun minDuration(): Long = sharedPreferences.getLong(MIN_DURATION, MIN_DURATION_DEFAULT)

    override fun setMinDuration(duration: Long) {
        sharedPreferences.edit { putLong(MIN_DURATION, duration) }
    }

    override fun maxDuration() = sharedPreferences.getLong(MAX_DURATION, MAX_DURATION_DEFAULT)

    override fun setMaxDuration(duration: Long) {
        sharedPreferences.edit { putLong(MAX_DURATION, duration) }
    }

    override fun interstitialAdsAuto() = sharedPreferences.getBoolean(INTER_AUTO, INTER_ADS_AUTO_DEFAULT)

    override fun setInterstitialAdsAuto(auto: Boolean) {
        sharedPreferences.edit { putBoolean(INTER_AUTO, auto) }
    }

    override fun setUnityId(unityId: String) {
        sharedPreferences.edit { putString(UNITY_ADS, unityId) }
    }

    override fun getUnityId(): String {
        val unity = sharedPreferences.getString(UNITY_ADS, null)
        return if (unity?.isEmpty() == true) {
            context.getString(R.string.unity_ads_unit)
        } else {
            unity!!
        }
    }

    companion object {
        private const val MIN_DURATION_DEFAULT = 70000L
        private const val MAX_DURATION_DEFAULT = 110000L
        private const val INTER_ADS_AUTO_DEFAULT = false
        private const val MIN_DURATION = "min_duration"
        private const val MAX_DURATION = "max_duration"
        private const val INTER_AUTO = "inter_auto"
        private const val ADMOB_ENABLED = "enabled"
        private const val UNITY_ADS = "unity_ads"
    }
}