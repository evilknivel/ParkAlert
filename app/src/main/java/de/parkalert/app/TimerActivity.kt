package de.parkalert.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import de.parkalert.app.databinding.ActivityTimerBinding
import java.util.Calendar

class TimerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTimerBinding
    private var timerReceiver: BroadcastReceiver? = null

    private var startTimeMillis: Long = 0L
    private var endTimeMillis: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTimerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnReset.setOnClickListener { stopTimerAndFinish() }
        loadBannerAdIfReady()

        // Restore state if launched from a notification (e.g. after timer expired)
        val launchState = intent.getIntExtra(Constants.EXTRA_TIMER_STATE, -1)
        val launchStart = intent.getLongExtra(Constants.EXTRA_START_TIME_MILLIS, 0L)
        val launchEnd = intent.getLongExtra(Constants.EXTRA_END_TIME_MILLIS, 0L)

        when {
            launchState == Constants.TIMER_STATE_ALERT -> {
                updateDisplay(0L, Constants.TIMER_STATE_ALERT, launchStart, launchEnd)
            }
            else -> {
                val initialMinutes = intent.getIntExtra(Constants.EXTRA_DURATION_MINUTES, 0)
                if (initialMinutes > 0) {
                    updateDisplay(initialMinutes * 60_000L, Constants.TIMER_STATE_RUNNING)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        registerTimerReceiver()
    }

    override fun onPause() {
        super.onPause()
        unregisterTimerReceiver()
    }

    private fun registerTimerReceiver() {
        timerReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val millis = intent.getLongExtra(Constants.EXTRA_REMAINING_MILLIS, 0L)
                val state = intent.getIntExtra(Constants.EXTRA_TIMER_STATE, Constants.TIMER_STATE_RUNNING)
                val startMs = intent.getLongExtra(Constants.EXTRA_START_TIME_MILLIS, 0L)
                val endMs = intent.getLongExtra(Constants.EXTRA_END_TIME_MILLIS, 0L)
                updateDisplay(millis, state, startMs, endMs)
            }
        }
        val filter = IntentFilter(Constants.ACTION_TIMER_UPDATE)
        ContextCompat.registerReceiver(
            this, timerReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    private fun unregisterTimerReceiver() {
        timerReceiver?.let {
            try { unregisterReceiver(it) } catch (_: Exception) {}
        }
        timerReceiver = null
    }

    private fun updateDisplay(
        millis: Long,
        state: Int,
        startMs: Long = startTimeMillis,
        endMs: Long = endTimeMillis
    ) {
        startTimeMillis = startMs
        endTimeMillis = endMs

        if (startMs > 0L) {
            binding.tvStartTime.text = getString(R.string.timer_started_at, formatClock(startMs))
            binding.tvStartTime.visibility = View.VISIBLE
        }

        when (state) {
            Constants.TIMER_STATE_WARNING -> {
                binding.root.setBackgroundColor(Color.parseColor("#FFF9C4"))
                binding.tvStatus.text = getString(R.string.timer_warning)
                binding.tvStatus.setTextColor(Color.parseColor("#E65100"))
                binding.tvTimer.text = formatTime(millis)
                binding.tvTimer.setTextColor(Color.parseColor("#BF360C"))
                binding.tvLabel.text = getString(R.string.remaining)
            }
            Constants.TIMER_STATE_ALERT -> {
                binding.root.setBackgroundColor(Color.parseColor("#FFCDD2"))
                binding.tvStatus.text = getString(R.string.timer_alert)
                binding.tvStatus.setTextColor(Color.parseColor("#B71C1C"))
                binding.tvTimer.setTextColor(Color.parseColor("#B71C1C"))

                val overtime = if (millis <= 0L) -millis else 0L
                binding.tvTimer.text = "+${formatTime(overtime)}"

                binding.tvLabel.text = if (endMs > 0L) {
                    getString(R.string.timer_ended_at, formatClock(endMs))
                } else {
                    getString(R.string.overtime_label)
                }
            }
            Constants.TIMER_STATE_STOPPED -> {
                finish()
            }
            else -> {
                binding.root.setBackgroundColor(Color.parseColor("#E8F5E9"))
                binding.tvStatus.text = getString(R.string.timer_running)
                binding.tvStatus.setTextColor(Color.parseColor("#2E7D32"))
                binding.tvTimer.text = formatTime(millis)
                binding.tvTimer.setTextColor(Color.parseColor("#1B5E20"))
                binding.tvLabel.text = getString(R.string.remaining)
            }
        }
    }

    private fun loadBannerAdIfReady() {
        if (!ConsentManager.getInstance(this).canRequestAds) return

        // DEBUG only: register test device. Idempotent if MainActivity already called
        // it, but required here when this Activity is launched directly from a
        // notification without MainActivity in the back stack. Remove before publishing.
        if (BuildConfig.DEBUG) {
            val testDeviceIds = listOf("7B05469EEF60FD8AB5044BCA30D236D7")
            val configuration = RequestConfiguration.Builder()
                .setTestDeviceIds(testDeviceIds)
                .build()
            MobileAds.setRequestConfiguration(configuration)
        }

        // MobileAds.initialize() is idempotent; safe to call even if already done in MainActivity.
        MobileAds.initialize(this) {
            runOnUiThread {
                binding.adBannerTimer.loadAd(AdRequest.Builder().build())
            }
        }
    }

    private fun stopTimerAndFinish() {
        stopService(Intent(this, TimerService::class.java))
        finish()
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
