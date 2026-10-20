package de.parkalert.app.automotive

import android.content.pm.ApplicationInfo
import androidx.car.app.CarAppService
import androidx.car.app.Session
import androidx.car.app.validation.HostValidator

/**
 * Entry point for the Android Auto / Automotive OS car app.
 *
 * The service is declared in the manifest with the intent-filter
 * `androidx.car.app.CarAppService` and the category
 * `androidx.car.app.category.IOT`.
 */
class ParkAlertCarAppService : CarAppService() {

    /**
     * Debug builds accept any host (needed for the Desktop Head Unit).
     * Release builds only accept the Google-signed Android Auto / Automotive hosts
     * from the Car App Library's allowlist.
     */
    override fun createHostValidator(): HostValidator {
        val isDebuggable = applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
        return if (isDebuggable) {
            HostValidator.ALLOW_ALL_HOSTS_VALIDATOR
        } else {
            HostValidator.Builder(applicationContext)
                .addAllowedHosts(androidx.car.app.R.array.hosts_allowlist_sample)
                .build()
        }
    }

    override fun onCreateSession(): Session = ParkAlertSession()
}
