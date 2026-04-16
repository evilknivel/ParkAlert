package de.parkalert.app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import de.parkalert.app.databinding.ActivitySettingsBinding
import java.util.Locale

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    private val prefs by lazy { getSharedPreferences("parkalert_prefs", MODE_PRIVATE) }

    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences("parkalert_prefs", Context.MODE_PRIVATE)
        val lang = prefs.getString("selected_language", "auto") ?: "auto"
        if (lang == "auto") {
            super.attachBaseContext(newBase)
            return
        }
        val locale = Locale(lang)
        Locale.setDefault(locale)
        val config = newBase.resources.configuration
        config.setLocale(locale)
        val context = newBase.createConfigurationContext(config)
        super.attachBaseContext(context)
    }

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
        binding.btnLanguage.setOnClickListener { showLanguageDialog() }

        updateLanguageButton()
    }

    private fun updateLanguageButton() {
        val lang = prefs.getString("selected_language", "auto") ?: "auto"
        val label = when (lang) {
            "de" -> getString(R.string.language_de)
            "en" -> getString(R.string.language_en)
            else -> getString(R.string.language_auto)
        }
        binding.btnLanguage.text = "${getString(R.string.settings_language)}: $label"
    }

    private fun showLanguageDialog() {
        val options = arrayOf(
            getString(R.string.language_auto),
            getString(R.string.language_de),
            getString(R.string.language_en)
        )
        val values = arrayOf("auto", "de", "en")
        val current = prefs.getString("selected_language", "auto") ?: "auto"
        val checkedItem = values.indexOf(current).coerceAtLeast(0)

        AlertDialog.Builder(this)
            .setTitle(R.string.settings_language)
            .setSingleChoiceItems(options, checkedItem) { dialog, which ->
                // Use commit() not apply() to ensure write completes before process restart
                prefs.edit().putString("selected_language", values[which]).commit()
                dialog.dismiss()
                restartApp()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun restartApp() {
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        intent?.addFlags(
            Intent.FLAG_ACTIVITY_CLEAR_TOP or
            Intent.FLAG_ACTIVITY_NEW_TASK or
            Intent.FLAG_ACTIVITY_CLEAR_TASK
        )
        // Small delay to ensure prefs are written to disk before process dies
        android.os.Handler(mainLooper).postDelayed({
            startActivity(intent)
            Runtime.getRuntime().exit(0)
        }, 300)
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
