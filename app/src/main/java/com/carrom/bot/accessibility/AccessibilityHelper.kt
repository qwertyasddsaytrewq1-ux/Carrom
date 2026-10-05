// app/src/main/java/com/carrom/bot/accessibility/AccessibilityHelper.kt

package com.carrom.bot.accessibility

import android.content.Context
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import androidx.core.view.ViewCompat

class AccessibilityHelper(private val context: Context) {
    
    fun setContentDescription(view: ViewGroup, descriptions: Map<Int, String>) {
        descriptions.forEach { (viewId, description) ->
            view.findViewById<android.view.View>(viewId)?.contentDescription = description
        }
    }
    
    fun announceToAccessibility(view: android.view.View, text: String) {
        ViewCompat.announceForAccessibility(view, text)
    }
    
    fun setHeading(view: android.view.View) {
        ViewCompat.setAccessibilityHeading(view, true)
    }
    
    fun setupTabletLayout(context: Context): Boolean {
        return (context.resources.configuration.screenLayout and 
                android.content.res.Configuration.SCREENLAYOUT_SIZE_MASK) >= 
                android.content.res.Configuration.SCREENLAYOUT_SIZE_LARGE
    }
    
    fun enableScreenReaderSupport() {
        // Add screen reader optimizations
    }
}
