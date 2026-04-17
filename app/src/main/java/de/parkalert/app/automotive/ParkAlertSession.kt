package de.parkalert.app.automotive

import android.content.Intent
import androidx.car.app.Screen
import androidx.car.app.Session

/**
 * Manages the screen stack for the ParkAlert car app.
 * The first screen shown is always [SelectDurationScreen].
 */
class ParkAlertSession : Session() {

    override fun onCreateScreen(intent: Intent): Screen {
        android.util.Log.d("ParkAlert_AUTO", "onCreateScreen() called")
        return try {
            android.util.Log.d("ParkAlert_AUTO", "Creating SelectDurationScreen...")
            val screen = SelectDurationScreen(carContext)
            android.util.Log.d("ParkAlert_AUTO", "SelectDurationScreen created OK")
            screen
        } catch (e: Exception) {
            android.util.Log.e("ParkAlert_AUTO", "CRASH in onCreateScreen: ${e.javaClass.simpleName}: ${e.message}", e)
            throw e
        }
    }
}
