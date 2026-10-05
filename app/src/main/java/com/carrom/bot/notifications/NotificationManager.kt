// app/src/main/java/com/carrom/bot/notifications/NotificationManager.kt

package com.carrom.bot.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessaging
import timber.log.Timber

class NotificationManager(private val context: Context) {
    
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    
    fun createNotificationChannels() {
        val channels = listOf(
            NotificationChannel(
                "bot_status",
                "Bot Status",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Bot start/stop notifications"
                enableVibration(true)
                enableLights(true)
            },
            NotificationChannel(
                "challenges",
                "Challenges",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Friend challenges and invites"
                enableVibration(true)
                setSound(
                    android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION),
                    null
                )
            },
            NotificationChannel(
                "updates",
                "App Updates",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "App update notifications"
            }
        )
        
        channels.forEach { notificationManager.createNotificationChannel(it) }
    }
    
    fun sendBotStatusNotification(running: Boolean) {
        val notification = NotificationCompat.Builder(context, "bot_status")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Carrom Bot")
            .setContentText(if (running) "Bot is running" else "Bot stopped")
            .setAutoCancel(true)
            .build()
        
        notificationManager.notify(1, notification)
    }
    
    fun sendChallengeNotification(challenger: String) {
        val notification = NotificationCompat.Builder(context, "challenges")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("New Challenge!")
            .setContentText("$challenger challenged you to a match")
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        
        notificationManager.notify(2, notification)
    }
    
    fun subscribeToTopics() {
        FirebaseMessaging.getInstance().apply {
            subscribeToTopic("updates")
            subscribeToTopic("challenges")
            subscribeToTopic("announcements")
        }
        Timber.d("Subscribed to topics")
    }
}
