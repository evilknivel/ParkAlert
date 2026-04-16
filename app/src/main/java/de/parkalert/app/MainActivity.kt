package de.parkalert.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import de.parkalert.app.databinding.ActivityMainBinding
import java.util.concurrent.atomic.AtomicBoolean

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // Prevents double-initialisation if the consent callback fires while
    // the parallel canRequestAds() check already triggered init.
    private val isMobileAdsInitialized = AtomicBoolean(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestNotificationPermission()
        setupDurationButtons()
        setupSettingsButton()
        gatherConsentAndInitAds()
    }

    private fun setupSettingsButton() {
        binding.btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    private fun gatherConsentAndInitAds() {
        if (BuildConfig.DEBUG) {
            // DEBUG only: skip consent flow and load ads directly for testing
            // Remove this block before release build
            initializeMobileAdsIfNeeded()
            return
        }

        // RELEASE: normal UMP consent flow
        val consentManager = ConsentManager.getInstance(this)
        consentManager.gatherConsent(this) { _ ->
            if (consentManager.canRequestAds) {
                initializeMobileAdsIfNeeded()
            }
        }
        if (consentManager.canRequestAds) {
            initializeMobileAdsIfNeeded()
        }
    }

    private fun initializeMobileAdsIfNeeded() {
        if (isMobileAdsInitialized.getAndSet(true)) return

        // DEBUG only: register test device so Google serves "Test Ad" banners.
        // Must be called BEFORE MobileAds.initialize(). Remove before publishing.
        if (BuildConfig.DEBUG) {
            val testDeviceIds = listOf("7B05469EEF60FD8AB5044BCA30D236D7")
            val configuration = RequestConfiguration.Builder()
                .setTestDeviceIds(testDeviceIds)
                .build()
            MobileAds.setRequestConfiguration(configuration)
        }

        MobileAds.initialize(this) {
            runOnUiThread { loadBannerAd() }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.adBannerMain.resume()
    }

    override fun onPause() {
        binding.adBannerMain.pause()
        super.onPause()
    }

    override fun onDestroy() {
        binding.adBannerMain.destroy()
        super.onDestroy()
    }

    private fun loadBannerAd() {
        try {
            binding.adBannerMain.loadAd(AdRequest.Builder().build())
        } catch (e: Exception) {
            android.util.Log.e("ParkAlert_ADS", "Failed to load banner ad", e)
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    REQUEST_NOTIFICATION_PERMISSION
                )
            }
        }
    }

    private fun setupDurationButtons() {
        val buttonMap = listOf(
            binding.btn15min  to 15,
            binding.btn30min  to 30,
            binding.btn60min  to 60,
            binding.btn90min  to 90,
            binding.btn120min to 120
        )
        buttonMap.forEach { (btn, minutes) ->
            btn.setOnClickListener { startParkTimer(minutes) }
        }
    }

    private fun startParkTimer(minutes: Int) {
        try {
            val serviceIntent = Intent(this, TimerService::class.java).apply {
                putExtra(Constants.EXTRA_DURATION_MINUTES, minutes)
            }
            ContextCompat.startForegroundService(this, serviceIntent)

            val activityIntent = Intent(this, TimerActivity::class.java).apply {
                putExtra(Constants.EXTRA_DURATION_MINUTES, minutes)
            }
            startActivity(activityIntent)
        } catch (e: Exception) {
            android.util.Log.e("ParkAlert_CRASH", "Failed to start timer", e)
        }
    }

    companion object {
        private const val REQUEST_NOTIFICATION_PERMISSION = 1001
    }
}
