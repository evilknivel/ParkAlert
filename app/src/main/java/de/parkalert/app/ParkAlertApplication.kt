package de.parkalert.app

import android.app.Application

class ParkAlertApplication : Application() {
    override fun onCreate() {
        super.onCreate()

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
}
