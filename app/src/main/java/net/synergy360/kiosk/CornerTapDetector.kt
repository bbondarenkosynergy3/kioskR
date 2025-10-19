package net.synergy360.kiosk

import android.content.Context
import android.graphics.Point
import android.os.Build
import android.view.MotionEvent
import android.view.WindowManager

class CornerTapDetector(
    private val context: Context,
    private val onSequenceComplete: () -> Unit
) {
    private val tapSequence = mutableListOf<Corner>()
    private val requiredSequence = listOf(
        Corner.TOP_LEFT,
        Corner.TOP_RIGHT,
        Corner.BOTTOM_LEFT,
        Corner.BOTTOM_RIGHT
    )
    
    private val cornerThreshold = 150
    private val tapTimeout = 10000L
    private var lastTapTime = 0L

    enum class Corner {
        TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT, NONE
    }

    fun onTouch(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val currentTime = System.currentTimeMillis()
            
            if (currentTime - lastTapTime > tapTimeout) {
                tapSequence.clear()
            }
            lastTapTime = currentTime

            val corner = detectCorner(event.x, event.y)
            if (corner != Corner.NONE) {
                tapSequence.add(corner)
                
                if (tapSequence.size > requiredSequence.size) {
                    tapSequence.removeAt(0)
                }

                if (tapSequence == requiredSequence) {
                    tapSequence.clear()
                    onSequenceComplete()
                    return true
                }
            }
        }
        return false
    }

    private fun detectCorner(x: Float, y: Float): Corner {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        
        val (width, height) = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics = windowManager.currentWindowMetrics
            val bounds = windowMetrics.bounds
            Pair(bounds.width().toFloat(), bounds.height().toFloat())
        } else {
            @Suppress("DEPRECATION")
            val display = windowManager.defaultDisplay
            val size = Point()
            @Suppress("DEPRECATION")
            display.getSize(size)
            Pair(size.x.toFloat(), size.y.toFloat())
        }

        return when {
            x < cornerThreshold && y < cornerThreshold -> Corner.TOP_LEFT
            x > width - cornerThreshold && y < cornerThreshold -> Corner.TOP_RIGHT
            x < cornerThreshold && y > height - cornerThreshold -> Corner.BOTTOM_LEFT
            x > width - cornerThreshold && y > height - cornerThreshold -> Corner.BOTTOM_RIGHT
            else -> Corner.NONE
        }
    }
}
