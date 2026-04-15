package de.parktimer.app

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
        countDownTimer?.cancel()
        super.onDestroy()
    }

    // -------------------------------------------------------------------------
    // Timer logic
    // -------------------------------------------------------------------------

    private fun launchTimer(durationMillis: Long) {
        countDownTimer?.cancel()
        remainingMillis = durationMillis
        currentState = Constants.TIMER_STATE_RUNNING

        // Start as foreground service immediately
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

                val newState = if (millisUntilFinished <= Constants.WARNING_THRESHOLD_MILLIS) {
                    Constants.TIMER_STATE_WARNING
                } else {
                    Constants.TIMER_STATE_RUNNING
                }
                currentState = newState

                // Update persistent notification
                val nm = getSystemService(NotificationManager::class.java)
                nm.notify(Constants.NOTIFICATION_ID_TIMER, buildTimerNotification(millisUntilFinished, newState))

                // Inform UI
                broadcast(millisUntilFinished, newState)
            }

            override fun onFinish() {
                remainingMillis = 0
                currentState = Constants.TIMER_STATE_ALERT

                broadcast(0L, Constants.TIMER_STATE_ALERT)
                triggerAlert()

                @Suppress("DEPRECATION")
                stopForeground(true)
                stopSelf()
            }
        }.start()
    }

    private fun broadcast(millis: Long, state: Int) {
        sendBroadcast(Intent(Constants.ACTION_TIMER_UPDATE).apply {
            putExtra(Constants.EXTRA_REMAINING_MILLIS, millis)
            putExtra(Constants.EXTRA_TIMER_STATE, state)
            setPackage(packageName) // restrict to our app
        })
    }

    private fun triggerAlert() {
        // High-priority notification with alarm sound
        val pendingIntent = pendingActivityIntent()
        val notification = NotificationCompat.Builder(this, Constants.CHANNEL_ID_ALERT)
            .setContentTitle(getString(R.string.notification_alert_title))
            .setContentText(getString(R.string.notification_alert_text))
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVibrate(longArrayOf(0, 600, 200, 600, 200, 600, 200, 1200))
            .build()

        val nm = getSystemService(NotificationManager::class.java)
        nm.notify(Constants.NOTIFICATION_ID_ALERT, notification)

        // Vibrate
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
            Constants.TIMER_STATE_WARNING -> getString(R.string.notification_warning_title, timeText)
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

    private fun pendingActivityIntent(): PendingIntent {
        val intent = Intent(this, TimerActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createNotificationChannels() {
        val nm = getSystemService(NotificationManager::class.java)

        // Low-importance channel for ongoing timer
        val timerChannel = NotificationChannel(
            Constants.CHANNEL_ID_TIMER,
            "ParkTimer – Laufzeit",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Zeigt die verbleibende Parkzeit an"
            setShowBadge(false)
        }

        // High-importance channel for the alarm at 0 minutes
        val alertSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        val audioAttr = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        val alertChannel = NotificationChannel(
            Constants.CHANNEL_ID_ALERT,
            "ParkTimer – Alarm",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Alarm wenn die Parkzeit abgelaufen ist"
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
