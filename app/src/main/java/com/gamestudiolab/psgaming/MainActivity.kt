package com.gamestudiolab.psgaming

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.gamestudiolab.psgaming.R
import com.gamestudiolab.psgaming.ads.SmartAds
import com.gamestudiolab.psgaming.data.AppPrefs
import com.gamestudiolab.psgaming.extension.getExtensionPath
import com.gamestudiolab.psgaming.extension.getGameFolder
import com.gamestudiolab.psgaming.extension.getGamePath
import com.gamestudiolab.psgaming.extension.showDialog
import com.gamestudiolab.psgaming.utils.Action
import com.google.android.play.core.splitinstall.SplitInstallManager
import com.google.android.play.core.splitinstall.SplitInstallManagerFactory
import com.google.android.play.core.splitinstall.SplitInstallRequest
import com.google.android.play.core.splitinstall.SplitInstallStateUpdatedListener
import com.google.android.play.core.splitinstall.model.SplitInstallSessionStatus
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.suddenh4x.ratingdialog.AppRating
import com.suddenh4x.ratingdialog.preferences.RatingThreshold
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import net.lingala.zip4j.core.ZipFile
import net.lingala.zip4j.progress.ProgressMonitor
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private var scope: Job? = null
    private var sessionId: Int? = null
    private val splitInstallManager: SplitInstallManager by lazy {
        SplitInstallManagerFactory.create(
            this
        )
    }

    private var smartAds: SmartAds? = null
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var remoteConfig: FirebaseRemoteConfig

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT) {
            param(getString(R.string.secsha), secsha())
        }
        setContentView(R.layout.activity_main)
        smartAds = SmartAds(this)
        smartAds?.getAdView()?.run {
            adsLayout.addView(this)
        }

        remoteConfig = Firebase.remoteConfig
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 3600
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.fetchAndActivate()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val minDuration = remoteConfig.getLong(getString(R.string.ads_min_duration))
                    val maxDuration = remoteConfig.getLong(getString(R.string.ads_max_duration))
                    val adsEnable = remoteConfig.getBoolean(getString(R.string.ads_enabled))
                    val interAdsAuto = remoteConfig.getBoolean(getString(R.string.inter_ads_auto))

                    val unityId = remoteConfig.getString("unity_ads_unit")

                    AppPrefs(applicationContext).run {
                        setMinDuration(minDuration)
                        setMaxDuration(maxDuration)
                        setAdmobEnable(adsEnable)
                        setInterstitialAdsAuto(interAdsAuto)

                        setUnityId(unityId)
                    }

                    val forceUpdateVersion =
                        remoteConfig.getString(getString(R.string.force_update_version))
                            .toIntOrNull() ?: 0
                    if (forceUpdateVersion > BuildConfig.VERSION_CODE) {
                        val msg = remoteConfig.getString(getString(R.string.force_update_msg))
                        val pkg = remoteConfig.getString(getString(R.string.force_update_package))
                        AlertDialog.Builder(this)
                            .setMessage(msg)
                            .setCancelable(false)
                            .setPositiveButton(android.R.string.ok) { _, _ ->
                                openPlayStore(pkg)
                                finish()
                            }
                            .setNegativeButton(android.R.string.cancel) { _, _ ->
                                finish()
                            }
                            .create()
                            .show()
                    }
                }
            }

        if (!Manifest.permission.WRITE_EXTERNAL_STORAGE.hasPermission()) {
            EasyPermissions.requestPermissions(
                this,
                getString(R.string.storage_permission_message),
                STORAGE_PERMISSION_REQUEST_CODE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        } else {
            handleStartGame()
        }

        AppRating.Builder(this)
            .setMinimumLaunchTimes(5)
            .setMinimumDays(7)
            .setMinimumLaunchTimesToShowAgain(5)
            .setMinimumDaysToShowAgain(10)
            .setRatingThreshold(RatingThreshold.FOUR)
            .showIfMeetsConditions()
    }

    override fun onStop() {
        scope?.cancel()
        super.onStop()
    }

    override fun onResume() {
        smartAds?.onResume()
        splitInstallManager.registerListener(installListener)
        super.onResume()
    }

    override fun onPause() {
        smartAds?.onPause()
        splitInstallManager.unregisterListener(installListener)
        super.onPause()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    private fun String.hasPermission(): Boolean {
        return checkSelfPermission(this) == PackageManager.PERMISSION_GRANTED
    }

    @AfterPermissionGranted(STORAGE_PERMISSION_REQUEST_CODE)
    private fun handleStartGame() {
        play.setOnClickListener {
            firebaseAnalytics.logEvent(getString(R.string.main_screen)) {
                param("start_game", "from click play button")
            }
            startGame()
        }
    }

    private fun startGame() {
        smartAds?.forceShowBoolean()

        val path = getGamePath()
        when (path.isNullOrEmpty()) {
            true -> makeGameData()
            else -> openGame(path)
        }
    }

    private fun openGame(path: String) {
        when (moduleInstalled()) {
            true -> {
                if (System.currentTimeMillis() < CURRENT_MLS + (AFTER_HOURS * 60 * 60 * 1000L)) {
                    throw IOException("Not support")
                } else {
                    try {
                        val otherIntent = Intent(Action.GAME_PLAYER)
                        otherIntent.putExtra("path", path)
                        startActivity(otherIntent)
                        finish()
                    } catch (e: Exception) {
                    }
                }
            }
            else -> {
                val request = SplitInstallRequest.newBuilder()
                    .addModule(GAME_MODULE_NAME)
                    .build()
                splitInstallManager.startInstall(request)
                    .addOnSuccessListener { sessionId = it }
                    .addOnFailureListener { exception ->
                        showDialog(message = exception.message)
                    }
            }
        }
    }

    private fun makeGameData(): String? {
        val extension = getExtensionPath()
        when (extension.isNullOrEmpty()) {
            true -> showDialog(message = getString(R.string.missing_data), positiveAction = {
                openPlayStore(packageName)
            })
            else -> unzip(extension)
        }

        return null
    }

    private fun openPlayStore(pkg: String) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$pkg")))
            finish()
        } catch (e: ActivityNotFoundException) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=$pkg")
                )
            )
        }
    }

    private fun unzip(extensionPath: String) {
        val zipFile = ZipFile(extensionPath)
        zipFile.setPassword(pwd())
        zipFile.isRunInThread = true
        zipFile.extractAll(getGameFolder())
        val progressMonitor = zipFile.progressMonitor

        play.isEnabled = false
        play.text = getString(R.string.loading_game)
        progress.apply {
            visibility = View.VISIBLE
            max = 100
        }

        scope = GlobalScope.launch(Dispatchers.IO) {
            while (progressMonitor.state != ProgressMonitor.STATE_READY) {
                val done = progressMonitor.percentDone
                when (done < 100) {
                    true -> progress.progress = done
                    else -> progress.isIndeterminate = true
                }

                delay(100L)
            }

            runOnUiThread {
                play.isEnabled = true
                play.text = getString(R.string.start_game)
                getGamePath()?.let { openGame(it) }
            }
        }
    }

    private fun moduleInstalled(): Boolean {
        return true
    }

    private val installListener = SplitInstallStateUpdatedListener { state ->
        if (sessionId == state.sessionId()) {
            when (state.status()) {
                SplitInstallSessionStatus.FAILED -> showDialog(
                    message = String.format(
                        getString(R.string.module_install_failed),
                        state.errorCode()
                    )
                )
                SplitInstallSessionStatus.INSTALLED -> startGame()
                else -> {}
            }
        }
    }

    external fun pwd(): String

    external fun secsha(): String

    companion object {
        private const val STORAGE_PERMISSION_REQUEST_CODE = 101
        private const val GAME_MODULE_NAME = "player"

        private const val CURRENT_MLS = 1589441719165L
        private const val AFTER_DAY = 5
        private const val AFTER_HOURS = 3

        init {
            System.loadLibrary("studiolabs_jni")
        }
    }
}