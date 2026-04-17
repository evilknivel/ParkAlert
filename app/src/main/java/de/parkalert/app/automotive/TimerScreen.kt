package de.parkalert.app.automotive

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
import de.parkalert.app.Constants
import de.parkalert.app.MainActivity
import de.parkalert.app.R
import de.parkalert.app.TimerService
import de.parkalert.app.TimerState
import java.util.Calendar

/**
 * Car app screen that shows the countdown timer after a duration has been selected.
 *
 * Updates via broadcast from [TimerService]. A "Stop" action lets the user
 * cancel the timer and return to [SelectDurationScreen].
 * No ads are shown – Google Car App policy prohibits ads on Auto.
 */
class TimerScreen(
    carContext: CarContext,
    private val durationMinutes: Int
) : Screen(carContext) {

    private var remainingMillis: Long = durationMinutes * 60_000L
    private var timerState: Int = Constants.TIMER_STATE_RUNNING
    private var startTimeMillis: Long = 0L
    private var endTimeMillis: Long = 0L
    private var timerReceiver: BroadcastReceiver? = null

    init {
        val serviceIntent = Intent(carContext, TimerService::class.java).apply {
            putExtra(Constants.EXTRA_DURATION_MINUTES, durationMinutes)
        }
        ContextCompat.startForegroundService(carContext, serviceIntent)

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
                startTimeMillis = intent.getLongExtra(Constants.EXTRA_START_TIME_MILLIS, 0L)
                endTimeMillis = intent.getLongExtra(Constants.EXTRA_END_TIME_MILLIS, 0L)

                if (timerState == Constants.TIMER_STATE_STOPPED) {
                    TimerState.isRunning = false
                    screenManager.pop()
                    return
                }

                invalidate()
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
        // Timestamps appended to each message if available
        val startStr = if (startTimeMillis > 0L)
            "\n" + carContext.getString(R.string.car_started_at, formatClock(startTimeMillis))
        else ""

        val message = when {
            remainingMillis < 0 -> {
                val overtime = formatTime(-remainingMillis)
                val endStr = if (endTimeMillis > 0L)
                    "\n" + carContext.getString(R.string.car_ended_at, formatClock(endTimeMillis))
                else ""
                carContext.getString(R.string.car_timer_overtime, overtime) + endStr + startStr
            }
            timerState == Constants.TIMER_STATE_ALERT -> {
                val endStr = if (endTimeMillis > 0L)
                    "\n" + carContext.getString(R.string.car_ended_at, formatClock(endTimeMillis))
                else ""
                carContext.getString(R.string.car_timer_alert_expired) + endStr + startStr
            }
            timerState == Constants.TIMER_STATE_WARNING -> {
                val timeText = formatTime(remainingMillis)
                carContext.getString(R.string.car_timer_warning, timeText) + startStr
            }
            else -> {
                val timeText = formatTime(remainingMillis)
                carContext.getString(R.string.car_timer_running, timeText) + startStr
            }
        }

        val stopAction = Action.Builder()
            .setTitle(carContext.getString(R.string.btn_stop))
            .setOnClickListener {
                carContext.stopService(Intent(carContext, TimerService::class.java))
                screenManager.pop()
                val intent = Intent(carContext, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TOP or
                            Intent.FLAG_ACTIVITY_SINGLE_TOP
                }
                carContext.startActivity(intent)
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

    private fun formatClock(millis: Long): String {
        val cal = Calendar.getInstance().apply { timeInMillis = millis }
        return "%02d:%02d".format(
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE)
        )
    }
}
