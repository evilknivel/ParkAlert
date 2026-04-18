package de.parkalert.app

import android.app.Application
import android.content.Context
import java.util.Locale

class ParkAlertApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Only apply language and AdMob in main process
        // CarAppService runs in a separate process context
        if (isMainProcess()) {
            applyStoredLanguage()
        }

        if (BuildConfig.DEBUG) {
            Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
                android.util.Log.e(
                    "ParkAlert_CRASH",
                    "Uncaught exception in thread ${thread.name}",
                    throwable
                )
            }
        }
    }

    private fun isMainProcess(): Boolean {
        val pid = android.os.Process.myPid()
        val manager = getSystemService(ACTIVITY_SERVICE)
            as android.app.ActivityManager
        return manager.runningAppProcesses?.any {
            it.pid == pid && it.processName == packageName
        } ?: true
    }

    private fun applyStoredLanguage() {
        val prefs = getSharedPreferences("parkalert_prefs", Context.MODE_PRIVATE)
        val lang = prefs.getString("selected_language", "auto") ?: "auto"

        if (lang != "auto") {
            val locale = Locale(lang)
            Locale.setDefault(locale)
            val config = resources.configuration
            config.setLocale(locale)
            createConfigurationContext(config)
            @Suppress("DEPRECATION")
            resources.updateConfiguration(config, resources.displayMetrics)
        }
    }
}
