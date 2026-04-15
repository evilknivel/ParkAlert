package de.parktimer.app.automotive

import android.content.Intent
import androidx.car.app.Screen
import androidx.car.app.Session

/**
 * Manages the screen stack for the car app.
 * The first screen shown is always [SelectDurationScreen].
 */
class ParkTimerSession : Session() {

    override fun onCreateScreen(intent: Intent): Screen =
        SelectDurationScreen(carContext)
}
