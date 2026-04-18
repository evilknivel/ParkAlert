package de.parkalert.app

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import de.parkalert.app.databinding.ActivityPrivacyPolicyBinding
import java.util.Locale

class PrivacyPolicyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPrivacyPolicyBinding

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
        binding = ActivityPrivacyPolicyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = getString(R.string.privacy_policy_title)
        }

        // The asset filename is a string resource overridden per locale:
        //   values/strings.xml (default/EN) → privacy_policy_en.html
        //   values-de/strings.xml (DE)       → privacy_policy_de.html
        val assetFile = getString(R.string.privacy_policy_asset)

        binding.webView.apply {
            settings.javaScriptEnabled = false
            // Open external links (e.g. Google privacy policy) in the device browser
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    val uri = request?.url ?: return false
                    startActivity(Intent(Intent.ACTION_VIEW, uri))
                    return true
                }
            }
            loadUrl("file:///android_asset/$assetFile")
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
