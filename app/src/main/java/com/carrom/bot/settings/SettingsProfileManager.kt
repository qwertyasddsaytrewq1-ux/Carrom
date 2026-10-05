// app/src/main/java/com/carrom/bot/settings/SettingsProfileManager.kt

package com.carrom.bot.settings

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import timber.log.Timber

data class SettingsProfile(
    val name: String,
    val autoAim: Boolean,
    val autoPower: Boolean,
    val powerMultiplier: Float,
    val aimOffset: Float,
    val gameMode: String,
    val delay: Float,
    val theme: String,
    val soundEnabled: Boolean,
    val hapticEnabled: Boolean
)

enum class Theme {
    LIGHT,
    DARK,
    AUTO
}

class SettingsProfileManager(private val context: Context) {
    
    private val prefs = context.getSharedPreferences("profiles", Context.MODE_PRIVATE)
    private val gson = Gson()
    
    fun saveProfile(profile: SettingsProfile) {
        try {
            val json = gson.toJson(profile)
            prefs.edit().putString("profile_${profile.name}", json).apply()
            Timber.d("Profile saved: ${profile.name}")
        } catch (e: Exception) {
            Timber.e(e, "Save profile failed")
        }
    }
    
    fun loadProfile(name: String): SettingsProfile? {
        return try {
            val json = prefs.getString("profile_$name", null) ?: return null
            gson.fromJson(json, SettingsProfile::class.java)
        } catch (e: Exception) {
            Timber.e(e, "Load profile failed")
            null
        }
    }
    
    fun getAllProfiles(): List<SettingsProfile> {
        return try {
            prefs.all.values.mapNotNull { value ->
                if (value is String) {
                    gson.fromJson(value, SettingsProfile::class.java)
                } else null
            }
        } catch (e: Exception) {
            Timber.e(e, "Get profiles failed")
            emptyList()
        }
    }
    
    fun deleteProfile(name: String) {
        prefs.edit().remove("profile_$name").apply()
    }
    
    fun setTheme(theme: Theme) {
        prefs.edit().putString("theme", theme.name).apply()
    }
    
    fun getTheme(): Theme {
        val name = prefs.getString("theme", Theme.AUTO.name) ?: Theme.AUTO.name
        return Theme.valueOf(name)
    }
}
