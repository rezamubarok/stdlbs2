package com.gamestudiolab.psgaming

import android.app.Application
import com.gamestudiolab.psgaming.ads.AppOpenManager
import com.google.android.gms.ads.MobileAds
import com.google.android.play.core.splitcompat.SplitCompat

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        SplitCompat.install(this)


        MobileAds.initialize(this)
        AppOpenManager(this)
    }
}