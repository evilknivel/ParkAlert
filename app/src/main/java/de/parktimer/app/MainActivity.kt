package de.parktimer.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import de.parktimer.app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestNotificationPermission()
        setupDurationButtons()
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    REQUEST_NOTIFICATION_PERMISSION
                )
            }
        }
    }

    private fun setupDurationButtons() {
        val buttonMap = listOf(
            binding.btn15min  to 15,
            binding.btn30min  to 30,
            binding.btn60min  to 60,
            binding.btn90min  to 90,
            binding.btn120min to 120
        )
        buttonMap.forEach { (btn, minutes) ->
            btn.setOnClickListener { startParkTimer(minutes) }
        }
    }

    private fun startParkTimer(minutes: Int) {
        // Start background foreground service
        val serviceIntent = Intent(this, TimerService::class.java).apply {
            putExtra(Constants.EXTRA_DURATION_MINUTES, minutes)
        }
        ContextCompat.startForegroundService(this, serviceIntent)

        // Open countdown screen
        val activityIntent = Intent(this, TimerActivity::class.java).apply {
            putExtra(Constants.EXTRA_DURATION_MINUTES, minutes)
        }
        startActivity(activityIntent)
    }

    companion object {
        private const val REQUEST_NOTIFICATION_PERMISSION = 1001
    }
}
