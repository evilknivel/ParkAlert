package de.parkalert.app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import de.parkalert.app.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = getString(R.string.settings_title)
        }

        val versionName = try {
            packageManager.getPackageInfo(packageName, 0).versionName
        } catch (e: Exception) {
            "–"
        }
        binding.tvAppVersion.text = getString(R.string.settings_app_version, versionName)

        binding.btnConsent.setOnClickListener { reopenConsentDialog() }
        binding.btnPrivacyPolicy.setOnClickListener { openPrivacyPolicy() }
        binding.btnLicenses.setOnClickListener { showLicenses() }
    }

    private fun reopenConsentDialog() {
        val consentManager = ConsentManager.getInstance(this)
        if (consentManager.isPrivacyOptionsRequired) {
            consentManager.showPrivacyOptionsForm(this) { _ -> }
        } else {
            AlertDialog.Builder(this)
                .setTitle(R.string.consent_not_required_title)
                .setMessage(R.string.consent_not_required_message)
                .setPositiveButton(android.R.string.ok, null)
                .show()
        }
    }

    private fun openPrivacyPolicy() {
        startActivity(Intent(this, PrivacyPolicyActivity::class.java))
    }

    private fun showLicenses() {
        AlertDialog.Builder(this)
            .setTitle(R.string.settings_licenses)
            .setMessage(R.string.licenses_text)
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
