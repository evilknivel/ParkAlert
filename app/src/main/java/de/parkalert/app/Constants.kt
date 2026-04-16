package de.parkalert.app

object Constants {
    // Broadcast actions (not exported, app-internal only)
    const val ACTION_TIMER_UPDATE = "de.parkalert.app.TIMER_UPDATE"

    // Intent/broadcast extras
    const val EXTRA_REMAINING_MILLIS = "remaining_millis"
    const val EXTRA_TIMER_STATE = "timer_state"
    const val EXTRA_DURATION_MINUTES = "duration_minutes"
    const val EXTRA_START_TIME_MILLIS = "start_time_millis"
    const val EXTRA_END_TIME_MILLIS = "end_time_millis"

    // Timer states
    const val TIMER_STATE_RUNNING = 0
    const val TIMER_STATE_WARNING = 1   // <= 10 minutes remaining
    const val TIMER_STATE_ALERT = 2     // time is up
    const val TIMER_STATE_STOPPED = 3   // manually reset

    // Warning threshold: 10 minutes in milliseconds
    const val WARNING_THRESHOLD_MILLIS = 10L * 60L * 1000L

    // Foreground service notification
    const val NOTIFICATION_ID_TIMER = 1001
    const val NOTIFICATION_ID_ALERT = 1002
    const val CHANNEL_ID_TIMER = "parkalert_timer"
    const val CHANNEL_ID_ALERT = "parkalert_alert"
}
