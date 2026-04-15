package de.parktimer.app.automotive

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.*
import androidx.core.content.ContextCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import de.parktimer.app.Constants
import de.parktimer.app.R
import de.parktimer.app.TimerService

/**
 * Car app screen that shows the countdown timer after a duration has been selected.
 *
 * Updates via broadcast from [TimerService]. A "Stop" action lets the user
 * cancel the timer and return to [SelectDurationScreen].
 */
class TimerScreen(
    carContext: CarContext,
    private val durationMinutes: Int
) : Screen(carContext) {

    private var remainingMillis: Long = durationMinutes * 60_000L
    private var timerState: Int = Constants.TIMER_STATE_RUNNING
    private var timerReceiver: BroadcastReceiver? = null

    init {
        // Start the background service when this screen is created
        val serviceIntent = Intent(carContext, TimerService::class.java).apply {
            putExtra(Constants.EXTRA_DURATION_MINUTES, durationMinutes)
        }
        ContextCompat.startForegroundService(carContext, serviceIntent)

        // Register a broadcast receiver and clean it up when the screen is destroyed
        registerTimerReceiver()
        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                unregisterTimerReceiver()
            }
        })
    }

    private fun registerTimerReceiver() {
        timerReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                remainingMillis = intent.getLongExtra(Constants.EXTRA_REMAINING_MILLIS, 0L)
                timerState = intent.getIntExtra(Constants.EXTRA_TIMER_STATE, Constants.TIMER_STATE_RUNNING)
                invalidate() // triggers onGetTemplate()
            }
        }
        val filter = IntentFilter(Constants.ACTION_TIMER_UPDATE)
        ContextCompat.registerReceiver(
            carContext, timerReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    private fun unregisterTimerReceiver() {
        timerReceiver?.let {
            try { carContext.unregisterReceiver(it) } catch (_: Exception) {}
        }
        timerReceiver = null
    }

    override fun onGetTemplate(): Template {
        val timeText = formatTime(remainingMillis)

        val message = when (timerState) {
            Constants.TIMER_STATE_WARNING ->
                carContext.getString(R.string.car_timer_warning, timeText)
            Constants.TIMER_STATE_ALERT ->
                carContext.getString(R.string.car_timer_alert)
            else ->
                carContext.getString(R.string.car_timer_running, timeText)
        }

        val stopAction = Action.Builder()
            .setTitle(carContext.getString(R.string.btn_stop))
            .setOnClickListener {
                carContext.stopService(Intent(carContext, TimerService::class.java))
                screenManager.pop()
            }
            .build()

        return MessageTemplate.Builder(message)
            .setTitle(carContext.getString(R.string.app_name))
            .setHeaderAction(Action.BACK)
            .addAction(stopAction)
            .build()
    }

    private fun formatTime(millis: Long): String {
        val totalSec = millis / 1_000
        val min = totalSec / 60
        val sec = totalSec % 60
        return "%02d:%02d".format(min, sec)
    }
}
