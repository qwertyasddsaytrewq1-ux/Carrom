// app/src/main/java/com/carrom/bot/MainActivity.kt

package com.carrom.bot

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import timber.log.Timber
import com.carrom.bot.database.BotDatabase
import com.carrom.bot.ml.AimPredictionModel
import com.carrom.bot.network.LeaderboardManager
import com.carrom.bot.billing.BillingManager
import com.carrom.bot.cloud.CloudSyncManager
import com.carrom.bot.notifications.NotificationManager
import com.carrom.bot.recording.ReplayRecorder
import com.carrom.bot.input.GestureController
import com.carrom.bot.settings.SettingsProfileManager
import com.carrom.bot.voice.VoiceCommandController
import com.carrom.bot.accessibility.AccessibilityHelper
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    
    private lateinit var botEngine: CarromBotEngineV2
    private lateinit var mlModel: AimPredictionModel
    private lateinit var leaderboardManager: LeaderboardManager
    private lateinit var billingManager: BillingManager
    private lateinit var cloudSyncManager: CloudSyncManager
    private lateinit var notificationManager: NotificationManager
    private lateinit var replayRecorder: ReplayRecorder
    private lateinit var gestureController: GestureController
    private lateinit var settingsProfileManager: SettingsProfileManager
    private lateinit var voiceController: VoiceCommandController
    private lateinit var accessibilityHelper: AccessibilityHelper
    
    private var isRunning = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        Timber.plant(Timber.DebugTree())
        
        requestPermissions()
        initializeAllSystems()
        setupUI()
    }
    
    private fun requestPermissions() {
        val permissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET,
            Manifest.permission.SYSTEM_ALERT_WINDOW,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.VIBRATE,
            Manifest.permission.QUERY_ALL_PACKAGES
        )
        
        val neededPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        
        if (neededPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, neededPermissions.toTypedArray(), 100)
        }
    }
    
    private fun initializeAllSystems() {
        botEngine = CarromBotEngineV2(this)
        mlModel = AimPredictionModel(this)
        leaderboardManager = LeaderboardManager()
        billingManager = BillingManager(this)
        cloudSyncManager = CloudSyncManager()
        notificationManager = NotificationManager(this)
        replayRecorder = ReplayRecorder(this)
        gestureController = GestureController(this)
        settingsProfileManager = SettingsProfileManager(this)
        voiceController = VoiceCommandController(this)
        accessibilityHelper = AccessibilityHelper(this)
        
        notificationManager.createNotificationChannels()
        notificationManager.subscribeToTopics()
        billingManager.initialize()
        voiceController.initialize()
        
        Timber.d("All systems initialized")
    }
    
    private fun setupUI() {
        val btnStart = findViewById<Button>(R.id.btn_start)
        val btnStop = findViewById<Button>(R.id.btn_stop)
        val btnGuide = findViewById<Button>(R.id.btn_guide)
        val btnAnalytics = findViewById<Button>(R.id.btn_analytics)
        val btnLeaderboard = findViewById<Button>(R.id.btn_leaderboard)
        val btnSettings = findViewById<Button>(R.id.btn_settings)
        val btnVoice = findViewById<Button>(R.id.btn_voice)
        val btnAR = findViewById<Button>(R.id.btn_ar)
        val btnRecording = findViewById<Button>(R.id.btn_recording)
        val btnCloud = findViewById<Button>(R.id.btn_cloud)
        
        btnStart.setOnClickListener { startBotWithAllFeatures() }
        btnStop.setOnClickListener { stopBot() }
        btnGuide.setOnClickListener { showGuide() }
        btnAnalytics.setOnClickListener { showAnalytics() }
        btnLeaderboard.setOnClickListener { showLeaderboard() }
        btnSettings.setOnClickListener { showSettings() }
        btnVoice.setOnClickListener { startVoiceControl() }
        btnAR.setOnClickListener { startAR() }
        btnRecording.setOnClickListener { toggleRecording() }
        btnCloud.setOnClickListener { syncToCloud() }
        
        gestureController.hapticFeedback()
    }
    
    private fun startBotWithAllFeatures() {
        Timber.d("Starting bot with all features...")
        
        lifecycleScope.launch {
            isRunning = true
            
            // Start recording
            replayRecorder.startRecording()
            
            // Send notification
            notificationManager.sendBotStatusNotification(true)
            
            // Calibrate first
            botEngine.runCalibration(3)
            
            // Run continuous play
            botEngine.runContinuousAutoPlay(10, 2000)
            
            // Export stats
            botEngine.exportStatistics("json")
            
            // Upload to leaderboard
            val stats = botEngine.getStats()
            leaderboardManager.uploadPlayerStats(
                FirebaseAuth.getInstance().currentUser?.uid ?: "anonymous",
                mapOf(
                    "totalScore" to stats.totalScore,
                    "accuracy" to stats.accuracy,
                    "gamesPlayed" to stats.shotsFired,
                    "lastUpdated" to System.currentTimeMillis()
                )
            )
        }
    }
    
    private fun stopBot() {
        isRunning = false
        botEngine.stop()
        replayRecorder.stopRecording()
        notificationManager.sendBotStatusNotification(false)
    }
    
    private fun showGuide() {
        startActivity(android.content.Intent(this, GuideActivity::class.java))
    }
    
    private fun showAnalytics() {
        startActivity(android.content.Intent(this, AnalyticsActivity::class.java))
    }
    
    private fun showLeaderboard() {
        lifecycleScope.launch {
            val leaders = leaderboardManager.getGlobalLeaderboard(50)
            Timber.d("Top player: ${leaders.firstOrNull()?.username}")
        }
    }
    
    private fun showSettings() {
        val profiles = settingsProfileManager.getAllProfiles()
        Timber.d("Loaded ${profiles.size} profiles")
    }
    
    private fun startVoiceControl() {
        voiceController.startListening { command ->
            Timber.d("Voice command: $command")
            when (command) {
                VoiceCommandController.VoiceCommand.START_BOT -> startBotWithAllFeatures()
                VoiceCommandController.VoiceCommand.STOP_BOT -> stopBot()
                else -> {}
            }
        }
    }
    
    private fun startAR() {
        Timber.d("AR mode would start here")
    }
    
    private fun toggleRecording() {
        if (isRunning) {
            replayRecorder.stopRecording()
            Timber.d("Recording stopped")
        } else {
            replayRecorder.startRecording()
            Timber.d("Recording started")
        }
    }
    
    private fun syncToCloud() {
        lifecycleScope.launch {
            val backupFile = replayRecorder.getRecordedReplays().firstOrNull()
            backupFile?.let {
                cloudSyncManager.backupAllStats(
                    FirebaseAuth.getInstance().currentUser?.uid ?: "anonymous",
                    it
                )
            }
        }
    }
}
