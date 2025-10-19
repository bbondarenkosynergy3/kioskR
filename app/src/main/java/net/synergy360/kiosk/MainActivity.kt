package net.synergy360.kiosk

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var offlineLayout: LinearLayout
    private lateinit var sleepWakeManager: SleepWakeManager
    private lateinit var cornerTapDetector: CornerTapDetector
    private lateinit var wakeLock: PowerManager.WakeLock
    
    private val webUrl = "https://app.360synergy.net"
    private val handler = Handler(Looper.getMainLooper())

    @SuppressLint("SetJavaScriptEnabled", "ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupFullscreen()
        setupWakeLock()
        
        webView = findViewById(R.id.webView)
        offlineLayout = findViewById(R.id.offlineLayout)

        setupWebView()
        setupKioskMode()
        
        cornerTapDetector = CornerTapDetector(this) { showExitDialog() }
        sleepWakeManager = SleepWakeManager(this, wakeLock, webView)
        
        webView.setOnTouchListener { _, event ->
            cornerTapDetector.onTouch(event)
            sleepWakeManager.resetTimer()
            false
        }

        sleepWakeManager.start()

        loadWebsite()
    }

    private fun setupFullscreen() {
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_FULLSCREEN
            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        )
        
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD)
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
        window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
    }

    private fun setupWakeLock() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.FULL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
            "SynergyKiosk::WakeLock"
        )
        wakeLock.acquire(10 * 60 * 1000L)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            cacheMode = android.webkit.WebSettings.LOAD_DEFAULT
            setSupportZoom(false)
            builtInZoomControls = false
            displayZoomControls = false
        }

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                return false
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                if (!isNetworkAvailable()) {
                    showOfflineScreen()
                    scheduleReconnect()
                }
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                hideOfflineScreen()
            }
        }

        webView.webChromeClient = WebChromeClient()
    }

    private fun setupKioskMode() {
        try {
            val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            if (activityManager.lockTaskModeState == ActivityManager.LOCK_TASK_MODE_NONE) {
                startLockTask()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadWebsite() {
        if (isNetworkAvailable()) {
            webView.loadUrl(webUrl)
            hideOfflineScreen()
        } else {
            showOfflineScreen()
            scheduleReconnect()
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun showOfflineScreen() {
        runOnUiThread {
            offlineLayout.visibility = View.VISIBLE
            webView.visibility = View.GONE
        }
    }

    private fun hideOfflineScreen() {
        runOnUiThread {
            offlineLayout.visibility = View.GONE
            webView.visibility = View.VISIBLE
        }
    }

    private fun scheduleReconnect() {
        handler.postDelayed({
            if (isNetworkAvailable()) {
                loadWebsite()
            } else {
                scheduleReconnect()
            }
        }, 5000)
    }

    private fun showExitDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.exit_kiosk_title)
            .setMessage(R.string.exit_kiosk_message)
            .setPositiveButton(R.string.yes) { _, _ ->
                exitKioskMode()
            }
            .setNegativeButton(R.string.no, null)
            .show()
    }

    private fun exitKioskMode() {
        try {
            stopLockTask()
            sleepWakeManager.stop()
            if (wakeLock.isHeld) {
                wakeLock.release()
            }
            finishAffinity()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onBackPressed() {
    }

    override fun onResume() {
        super.onResume()
        setupFullscreen()
        webView.onResume()
    }

    override fun onPause() {
        super.onPause()
        webView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        sleepWakeManager.stop()
        if (wakeLock.isHeld) {
            wakeLock.release()
        }
        handler.removeCallbacksAndMessages(null)
    }
}
