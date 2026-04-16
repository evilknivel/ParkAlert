package de.parkalert.app.automotive

import androidx.car.app.CarAppService
import androidx.car.app.Session
import androidx.car.app.validation.HostValidator

/**
 * Entry point for the Android Auto / Automotive OS car app.
 *
 * The service is declared in the manifest with the intent-filter
 * `androidx.car.app.CarAppService` and the category
 * `androidx.car.app.category.PARKING`.
 */
class ParkAlertCarAppService : CarAppService() {

    /**
     * Allow all hosts during development.
     * For production, replace with a SHA-256 allowlist of trusted host certificates.
     */
    override fun createHostValidator(): HostValidator =
        HostValidator.ALLOW_ALL_HOSTS_VALIDATOR

    override fun onCreateSession(): Session = ParkAlertSession()
}
