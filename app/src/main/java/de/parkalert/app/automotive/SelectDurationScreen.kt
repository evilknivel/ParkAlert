package de.parkalert.app.automotive

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.*
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import de.parkalert.app.Constants
import de.parkalert.app.R
import de.parkalert.app.TimerService
import de.parkalert.app.TimerState

/**
 * Car app screen that shows a 5-item grid for parking duration selection.
 *
 * Displayed as the first screen when the user opens ParkAlert via Android Auto.
 * No ads are shown on this screen – Google Car App policy prohibits ads on Auto.
 */
class SelectDurationScreen(carContext: CarContext) : Screen(carContext) {

    private val durations = listOf(
        15 to R.string.minutes_15,
        30 to R.string.minutes_30,
        60 to R.string.minutes_60,
        90 to R.string.minutes_90,
        120 to R.string.minutes_120
    )
    private var timerReceiver: BroadcastReceiver? = null

    // What the current template shows. The service broadcasts every second, but the
    // host throttles template updates, so we only invalidate when this changes.
    private var renderedRunning = TimerState.isRunning
    private var renderedMinutes = TimerState.remainingMinutes

    init {
        timerReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val state = intent.getIntExtra(
                    Constants.EXTRA_TIMER_STATE, Constants.TIMER_STATE_RUNNING
                )
                if (state == Constants.TIMER_STATE_STOPPED) {
                    TimerState.isRunning = false
                }
                if (TimerState.isRunning != renderedRunning ||
                    TimerState.remainingMinutes != renderedMinutes
                ) {
                    invalidate()
                }
            }
        }
        ContextCompat.registerReceiver(
            carContext, timerReceiver,
            IntentFilter(Constants.ACTION_TIMER_UPDATE),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                timerReceiver?.let {
                    try { carContext.unregisterReceiver(it) } catch (_: Exception) {}
                }
                timerReceiver = null
            }
        })
    }

    private fun createParkingIcon(): CarIcon =
        CarIcon.Builder(IconCompat.createWithResource(carContext, R.drawable.ic_parking)).build()

    override fun onGetTemplate(): Template {
        renderedRunning = TimerState.isRunning
        renderedMinutes = TimerState.remainingMinutes

        if (TimerState.isRunning) {
            val stopAction = Action.Builder()
                .setTitle(carContext.getString(R.string.btn_stop))
                .setOnClickListener {
                    carContext.stopService(Intent(carContext, TimerService::class.java))
                    TimerState.isRunning = false
                    invalidate()
                }
                .build()

            return MessageTemplate.Builder(
                carContext.getString(
                    R.string.car_timer_already_running,
                    TimerState.remainingMinutes
                )
            )
                .setTitle(carContext.getString(R.string.app_name))
                .setHeaderAction(Action.APP_ICON)
                .addAction(stopAction)
                .build()
        }

        val icon = createParkingIcon()
        val itemList = ItemList.Builder().apply {
            durations.forEach { (minutes, labelRes) ->
                addItem(
                    GridItem.Builder()
                        .setTitle(carContext.getString(labelRes))
                        .setImage(icon, GridItem.IMAGE_TYPE_ICON)
                        .setOnClickListener {
                            screenManager.push(TimerScreen(carContext, minutes))
                        }
                        .build()
                )
            }
        }.build()

        return GridTemplate.Builder()
            .setTitle(carContext.getString(R.string.app_name))
            .setHeaderAction(Action.APP_ICON)
            .setSingleList(itemList)
            .build()
    }
}
