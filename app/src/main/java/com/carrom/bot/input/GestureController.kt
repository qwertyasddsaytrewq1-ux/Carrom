// app/src/main/java/com/carrom/bot/input/GestureController.kt

package com.carrom.bot.input

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.GestureDetector
import android.view.MotionEvent
import timber.log.Timber

class GestureController(private val context: Context) : GestureDetector.OnGestureListener {
    
    private val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    private var gestureListener: ((Gesture) -> Unit)? = null
    
    enum class Gesture {
        SWIPE_UP,
        SWIPE_DOWN,
        SWIPE_LEFT,
        SWIPE_RIGHT,
        DOUBLE_TAP,
        LONG_PRESS,
        PINCH_ZOOM
    }
    
    fun hapticFeedback(duration: Long = 50, amplitude: Int = 200) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val effect = VibrationEffect.createOneShot(
                duration,
                amplitude
            )
            vibrator.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(duration)
        }
    }
    
    fun patternHaptic(pattern: LongArray = longArrayOf(0, 200, 100, 200)) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val effect = VibrationEffect.createWaveform(pattern)
            vibrator.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(pattern, -1)
        }
    }
    
    override fun onDown(e: MotionEvent): Boolean = true
    override fun onShowPress(e: MotionEvent) {}
    override fun onSingleTapUp(e: MotionEvent): Boolean {
        hapticFeedback()
        return true
    }
    override fun onScroll(e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float) = false
    override fun onLongPress(e: MotionEvent) {
        patternHaptic()
        gestureListener?.invoke(Gesture.LONG_PRESS)
    }
    override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
        patternHaptic(longArrayOf(0, 100, 50, 100, 50, 200))
        
        return when {
            velocityX > 1000 -> { gestureListener?.invoke(Gesture.SWIPE_RIGHT); true }
            velocityX < -1000 -> { gestureListener?.invoke(Gesture.SWIPE_LEFT); true }
            velocityY > 1000 -> { gestureListener?.invoke(Gesture.SWIPE_DOWN); true }
            velocityY < -1000 -> { gestureListener?.invoke(Gesture.SWIPE_UP); true }
            else -> false
        }
    }
}
