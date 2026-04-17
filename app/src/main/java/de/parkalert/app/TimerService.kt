package de.parkalert.app

import android.app.*
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.*
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class TimerService : Service() {

    private var countDownTimer: CountDownTimer? = null
    private var remainingMillis: Long = 0
    private var currentState: Int = Constants.TIMER_STATE_STOPPED

    private var startTimeMillis: Long = 0L
    private var endTimeMillis: Long = 0L
    private var overtimeMillis: Long = 0L
    private val overtimeHandler = Handler(Looper.getMainLooper())
    private var overtimeRunnable: Runnable? = null

    // -------------------------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------------------------

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val minutes = intent?.getIntExtra(Constants.EXTRA_DURATION_MINUTES, 0) ?: 0
        if (minutes > 0) {
            launchTimer(minutes * 60_000L)
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        cancelOvertime()
        countDownTimer?.cancel()
        TimerState.isRunning = false
        sendBroadcast(Intent(Constants.ACTION_TIMER_UPDATE).apply {
            putExtra(Constants.EXTRA_TIMER_STATE, Constants.TIMER_STATE_STOPPED)
            putExtra(Constants.EXTRA_REMAINING_MILLIS, 0L)
            setPackage(packageName)
        })
        super.onDestroy()
    }

    // -------------------------------------------------------------------------
    // Timer logic
    // -------------------------------------------------------------------------

    private fun launchTimer(durationMillis: Long) {
        cancelOvertime()
        countDownTimer?.cancel()
        startTimeMillis = System.currentTimeMillis()
        endTimeMillis = 0L
        overtimeMillis = 0L
        remainingMillis = durationMillis
        currentState = Constants.TIMER_STATE_RUNNING
        TimerState.isRunning = true
        TimerState.durationMinutes = (durationMillis / 60_000L).toInt()
        TimerState.remainingMinutes = TimerState.durationMinutes

        val notification = buildTimerNotification(durationMillis, currentState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                Constants.NOTIFICATION_ID_TIMER,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            startForeground(Constants.NOTIFICATION_ID_TIMER, notification)
        }

        countDownTimer = object : CountDownTimer(durationMillis, 1_000) {

            override fun onTick(millisUntilFinished: Long) {
                remainingMillis = millisUntilFinished
                TimerState.remainingMinutes = (millisUntilFinished / 60_000L).toInt()

                val newState = if (millisUntilFinished <= Constants.WARNING_THRESHOLD_MILLIS) {
                    Constants.TIMER_STATE_WARNING
                } else {
                    Constants.TIMER_STATE_RUNNING
                }
                currentState = newState

                val nm = getSystemService(NotificationManager::class.java)
                nm.notify(Constants.NOTIFICATION_ID_TIMER, buildTimerNotification(millisUntilFinished, newState))

                broadcast(millisUntilFinished, newState)
            }

            override fun onFinish() {
                endTimeMillis = System.currentTimeMillis()
                remainingMillis = 0L
                currentState = Constants.TIMER_STATE_ALERT

                broadcast(0L, Constants.TIMER_STATE_ALERT)
                triggerAlert()
                startOvertimeCounter()
                // Service stays alive — overtime counter keeps it running
            }
        }.start()
    }

    private fun startOvertimeCounter() {
        overtimeMillis = 0L
        val nm = getSystemService(NotificationManager::class.java)

        val runnable = object : Runnable {
            override fun run() {
                overtimeMillis += 1_000L
                nm.notify(Constants.NOTIFICATION_ID_TIMER, buildOvertimeNotification(overtimeMillis))
                broadcast(-overtimeMillis, Constants.TIMER_STATE_ALERT)
                overtimeHandler.postDelayed(this, 1_000L)
            }
        }
        overtimeRunnable = runnable
        nm.notify(Constants.NOTIFICATION_ID_TIMER, buildOvertimeNotification(0L))
        overtimeHandler.postDelayed(runnable, 1_000L)
    }

    private fun cancelOvertime() {
        overtimeRunnable?.let { overtimeHandler.removeCallbacks(it) }
        overtimeRunnable = null
    }

    private fun broadcast(millis: Long, state: Int) {
        sendBroadcast(Intent(Constants.ACTION_TIMER_UPDATE).apply {
            putExtra(Constants.EXTRA_REMAINING_MILLIS, millis)
            putExtra(Constants.EXTRA_TIMER_STATE, state)
            putExtra(Constants.EXTRA_START_TIME_MILLIS, startTimeMillis)
            putExtra(Constants.EXTRA_END_TIME_MILLIS, endTimeMillis)
            setPackage(packageName)
        })
    }

    private fun triggerAlert() {
        val notification = NotificationCompat.Builder(this, Constants.CHANNEL_ID_ALERT)
            .setContentTitle(getString(R.string.notification_alert_title))
            .setContentText(getString(R.string.notification_alert_text))
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentIntent(pendingActivityIntent())
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVibrate(longArrayOf(0, 600, 200, 600, 200, 600, 200, 1200))
            .build()

        val nm = getSystemService(NotificationManager::class.java)
        nm.notify(Constants.NOTIFICATION_ID_ALERT, notification)

        vibrate()
    }

    private fun vibrate() {
        val vibrator: Vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = getSystemService(VibratorManager::class.java)
            vm.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Vibrator::class.java)
        }
        val pattern = VibrationEffect.createWaveform(
            longArrayOf(0, 600, 200, 600, 200, 600, 200, 1200), -1
        )
        vibrator.vibrate(pattern)
    }

    // -------------------------------------------------------------------------
    // Notification helpers
    // -------------------------------------------------------------------------

    private fun buildTimerNotification(millisRemaining: Long, state: Int): Notification {
        val timeText = formatTime(millisRemaining)
        val title = when (state) {
            Constants.TIMER_STATE_WARNING -> getString(R.string.notification_warning_title)
            else                          -> getString(R.string.notification_timer_title, timeText)
        }

        return NotificationCompat.Builder(this, Constants.CHANNEL_ID_TIMER)
            .setContentTitle(title)
            .setContentText(getString(R.string.notification_tap_to_open))
            .setSmallIcon(android.R.drawable.ic_menu_recent_history)
            .setContentIntent(pendingActivityIntent())
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
    }

    private fun buildOvertimeNotification(overtimeMs: Long): Notification {
        val timeText = formatTime(overtimeMs)
        val title = getString(R.string.notification_overtime_title, timeText)

        return NotificationCompat.Builder(this, Constants.CHANNEL_ID_TIMER)
            .setContentTitle(title)
            .setContentText(getString(R.string.notification_tap_to_open))
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentIntent(pendingActivityIntent())
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
    }

    private fun pendingActivityIntent(): PendingIntent {
        val intent = Intent(this, TimerActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(Constants.EXTRA_TIMER_STATE, currentState)
            putExtra(Constants.EXTRA_START_TIME_MILLIS, startTimeMillis)
            putExtra(Constants.EXTRA_END_TIME_MILLIS, endTimeMillis)
        }
        return PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createNotificationChannels() {
        val nm = getSystemService(NotificationManager::class.java)

        val timerChannel = NotificationChannel(
            Constants.CHANNEL_ID_TIMER,
            getString(R.string.notification_channel_timer_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = getString(R.string.notification_channel_timer_desc)
            setShowBadge(false)
        }

        val alertSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        val audioAttr = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        val alertChannel = NotificationChannel(
            Constants.CHANNEL_ID_ALERT,
            getString(R.string.notification_channel_alert_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = getString(R.string.notification_channel_alert_desc)
            setSound(alertSound, audioAttr)
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 600, 200, 600, 200, 600, 200, 1200)
        }

        nm.createNotificationChannel(timerChannel)
        nm.createNotificationChannel(alertChannel)
    }

    // -------------------------------------------------------------------------
    // Utility
    // -------------------------------------------------------------------------

    private fun formatTime(millis: Long): String {
        val totalSec = millis / 1_000
        val min = totalSec / 60
        val sec = totalSec % 60
        return "%02d:%02d".format(min, sec)
    }
}
