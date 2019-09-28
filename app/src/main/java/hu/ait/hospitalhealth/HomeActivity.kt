package hu.ait.hospitalhealth

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_home.*

class HomeActivity : AppCompatActivity() {

    private val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_dashboard -> {
                startActivity(
                    Intent(
                        this@HomeActivity,
                        ScrollingActivity::class.java
                    )
                )
                return@OnNavigationItemSelectedListener true

            }
            R.id.navigation_notifications -> {
                startActivity(
                    Intent(
                        this@HomeActivity,
                        NearbyPlacesActivity::class.java
                    )
                )
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        navView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)

        loadWeb()
    }

    fun loadWeb(){
        if (etUrl.text.isNotEmpty()) {
            webView.settings.builtInZoomControls = true
            webView.loadUrl(etUrl.text.toString())

            btnGo.setOnClickListener {
                webView.loadUrl(etUrl.text.toString())
            }

            webView.webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                    webView.loadUrl(request!!.url!!.toString())
                    return true
                }
            }

        } else etUrl.error = getString(R.string.error_valid_url)
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) webView.goBack()
        else super.onBackPressed()
    }
}
