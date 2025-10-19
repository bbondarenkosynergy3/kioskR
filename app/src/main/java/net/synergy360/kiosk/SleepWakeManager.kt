package net.synergy360.kiosk

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.view.WindowManager
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity

class SleepWakeManager(
    private val activity: AppCompatActivity,
    private val wakeLock: PowerManager.WakeLock,
    private val webView: WebView
) {
    private val handler = Handler(Looper.getMainLooper())
    private val sleepInterval = 120000L // 2 minutes in milliseconds
    
    private var isRunning = false
    private var isSleeping = false

    private val sleepRunnable = object : Runnable {
        override fun run() {
            if (isRunning) {
                if (isSleeping) {
                    wakeUp()
                } else {
                    sleep()
                }
                handler.postDelayed(this, sleepInterval)
            }
        }
    }

    fun start() {
        if (!isRunning) {
            isRunning = true
            handler.postDelayed(sleepRunnable, sleepInterval)
        }
    }

    fun stop() {
        isRunning = false
        handler.removeCallbacks(sleepRunnable)
        if (isSleeping) {
            wakeUp()
        }
    }

    fun resetTimer() {
        if (isSleeping) {
            wakeUp()
        }
        handler.removeCallbacks(sleepRunnable)
        if (isRunning) {
            handler.postDelayed(sleepRunnable, sleepInterval)
        }
    }

    private fun sleep() {
        isSleeping = true
        activity.runOnUiThread {
            webView.onPause()
            webView.pauseTimers()
            
            if (wakeLock.isHeld) {
                wakeLock.release()
            }
            
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    private fun wakeUp() {
        isSleeping = false
        activity.runOnUiThread {
            if (!wakeLock.isHeld) {
                wakeLock.acquire(10 * 60 * 1000L)
            }
            
            activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            activity.window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
            activity.window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
            
            webView.resumeTimers()
            webView.onResume()
            webView.reload()
        }
    }
}
