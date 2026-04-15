package de.parktimer.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import de.parktimer.app.databinding.ActivityTimerBinding

class TimerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTimerBinding
    private var timerReceiver: BroadcastReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTimerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Show initial time so the screen isn't blank before the first broadcast
        val initialMinutes = intent.getIntExtra(Constants.EXTRA_DURATION_MINUTES, 0)
        if (initialMinutes > 0) {
            updateDisplay(initialMinutes * 60_000L, Constants.TIMER_STATE_RUNNING)
        }

        binding.btnReset.setOnClickListener { stopTimerAndFinish() }
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
                updateDisplay(millis, state)
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

    private fun updateDisplay(millis: Long, state: Int) {
        binding.tvTimer.text = formatTime(millis)

        when (state) {
            Constants.TIMER_STATE_WARNING -> {
                binding.root.setBackgroundColor(Color.parseColor("#FFF9C4"))
                binding.tvStatus.text = getString(R.string.timer_warning)
                binding.tvStatus.setTextColor(Color.parseColor("#E65100"))
                binding.tvTimer.setTextColor(Color.parseColor("#BF360C"))
            }
            Constants.TIMER_STATE_ALERT -> {
                binding.root.setBackgroundColor(Color.parseColor("#FFCDD2"))
                binding.tvStatus.text = getString(R.string.timer_alert)
                binding.tvStatus.setTextColor(Color.parseColor("#B71C1C"))
                binding.tvTimer.setTextColor(Color.parseColor("#B71C1C"))
            }
            Constants.TIMER_STATE_STOPPED -> {
                // Service was stopped externally – go back
                finish()
            }
            else -> {
                binding.root.setBackgroundColor(Color.parseColor("#E8F5E9"))
                binding.tvStatus.text = getString(R.string.timer_running)
                binding.tvStatus.setTextColor(Color.parseColor("#2E7D32"))
                binding.tvTimer.setTextColor(Color.parseColor("#1B5E20"))
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
}
