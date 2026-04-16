package de.parkalert.app

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.FormError
import com.google.android.ump.UserMessagingPlatform

/**
 * Wraps the Google UMP (User Messaging Platform) SDK for GDPR consent management.
 *
 * Usage:
 *  1. Call [gatherConsent] once in MainActivity.onCreate() – shows the consent
 *     dialog on first launch (or when consent expires) and resolves silently on
 *     subsequent launches where valid consent is already stored.
 *  2. Check [canRequestAds] before initialising MobileAds or loading any ad.
 *  3. In Settings, check [isPrivacyOptionsRequired] and call [showPrivacyOptionsForm]
 *     to let the user change their consent at any time.
 */
class ConsentManager private constructor(context: Context) {

    private val consentInformation: ConsentInformation =
        UserMessagingPlatform.getConsentInformation(context)

    /** True once UMP confirms that ads may be requested. */
    val canRequestAds: Boolean
        get() = consentInformation.canRequestAds()

    /**
     * True when UMP requires that a Privacy Options Form be available to the user
     * (typically for EEA / GDPR users). Show a "Manage Privacy" entry in settings.
     */
    val isPrivacyOptionsRequired: Boolean
        get() = consentInformation.privacyOptionsRequirementStatus ==
                ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED

    /**
     * Requests up-to-date consent information from Google, then shows the full
     * consent form if required (first launch / expired consent).
     *
     * In DEBUG builds a 300 ms delay is inserted between reset() and the network
     * request to give the UMP SDK time to flush its reset state.
     */
    fun gatherConsent(
        activity: Activity,
        onComplete: (FormError?) -> Unit
    ) {
        val paramsBuilder = ConsentRequestParameters.Builder()
            .setTagForUnderAgeOfConsent(false)

        // DEBUG only: simulate EEA geography so the consent dialog always appears
        // on test devices. canRequestAds() becomes true after the user taps
        // Accept/Decline, which unblocks MobileAds.initialize() and the banner.
        // Remove this block before publishing a release build.
        if (BuildConfig.DEBUG) {
            val debugSettings = ConsentDebugSettings.Builder(activity)
                .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
                .addTestDeviceHashedId("7B05469EEF60FD8AB5044BCA30D236D7")
                .build()
            paramsBuilder.setConsentDebugSettings(debugSettings)
        }

        val params = paramsBuilder.build()

        fun doRequest() {
            Log.d(TAG, "requestConsentInfoUpdate started")
            consentInformation.requestConsentInfoUpdate(
                activity,
                params,
                {
                    Log.d(TAG, "onSuccess - canRequestAds: $canRequestAds")
                    Log.d(TAG, "loadAndShowConsentFormIfRequired called")
                    UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
                        Log.d(TAG, "form dismissed - error: $formError")
                        onComplete(formError)
                    }
                },
                { requestConsentError ->
                    Log.d(TAG, "onFailure - error: ${requestConsentError.message}")
                    onComplete(requestConsentError)
                }
            )
        }

        if (BuildConfig.DEBUG) {
            // Small delay so the UMP SDK has time to flush the reset() state before
            // the next requestConsentInfoUpdate hits the network.
            Handler(Looper.getMainLooper()).postDelayed(::doRequest, RESET_DELAY_MS)
        } else {
            doRequest()
        }
    }

    /**
     * Shows the privacy options form so the user can review or change their consent.
     * Only call this when [isPrivacyOptionsRequired] is true.
     */
    fun showPrivacyOptionsForm(
        activity: Activity,
        onComplete: (FormError?) -> Unit
    ) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity) { formError ->
            onComplete(formError)
        }
    }

    /**
     * Resets stored consent state so the dialog appears fresh on the next launch.
     * DEBUG builds only — remove the call site in MainActivity before releasing.
     */
    fun resetConsentForTesting() {
        if (BuildConfig.DEBUG) {
            consentInformation.reset()
            Log.d(TAG, "reset() called")
        }
    }

    companion object {
        private const val TAG = "ParkAlert_UMP"
        private const val RESET_DELAY_MS = 300L

        @Volatile
        private var instance: ConsentManager? = null

        fun getInstance(context: Context): ConsentManager =
            instance ?: synchronized(this) {
                instance ?: ConsentManager(context.applicationContext).also { instance = it }
            }
    }
}
