// app/src/main/java/com/carrom/bot/voice/VoiceCommandController.kt

package com.carrom.bot.voice

import android.content.Context
import android.content.Intent
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import timber.log.Timber
import java.util.*

class VoiceCommandController(private val context: Context) {
    
    private var speechRecognizer: SpeechRecognizer? = null
    private var commandListener: ((VoiceCommand) -> Unit)? = null
    
    enum class VoiceCommand {
        START_BOT,
        STOP_BOT,
        CHANGE_MODE,
        SHOW_STATS,
        CALIBRATE,
        EXPORT_DATA,
        SHOW_GUIDE,
        ADJUST_POWER,
        ADJUST_AIM,
        ACTIVATE_AR,
        UNKNOWN
    }
    
    fun initialize() {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
            Timber.d("Speech recognizer initialized")
        }
    }
    
    fun startListening(listener: (VoiceCommand) -> Unit) {
        commandListener = listener
        
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }
        
        speechRecognizer?.startListening(intent)
        Timber.d("Voice listening started")
    }
    
    fun stopListening() {
        speechRecognizer?.stopListening()
    }
    
    private fun parseVoiceInput(input: String): VoiceCommand {
        val normalized = input.lowercase().trim()
        
        return when {
            normalized.contains("start") -> VoiceCommand.START_BOT
            normalized.contains("stop") -> VoiceCommand.STOP_BOT
            normalized.contains("mode") -> VoiceCommand.CHANGE_MODE
            normalized.contains("stat") -> VoiceCommand.SHOW_STATS
            normalized.contains("calibrate") -> VoiceCommand.CALIBRATE
            normalized.contains("export") -> VoiceCommand.EXPORT_DATA
            normalized.contains("guide") -> VoiceCommand.SHOW_GUIDE
            normalized.contains("power") -> VoiceCommand.ADJUST_POWER
            normalized.contains("aim") -> VoiceCommand.ADJUST_AIM
            normalized.contains("ar") || normalized.contains("augment") -> VoiceCommand.ACTIVATE_AR
            else -> VoiceCommand.UNKNOWN
        }
    }
    
    fun release() {
        speechRecognizer?.destroy()
    }
}
