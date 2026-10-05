// app/src/main/java/com/carrom/bot/cloud/CloudSyncManager.kt

package com.carrom.bot.cloud

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.io.File

class CloudSyncManager {
    
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    
    suspend fun backupAllStats(userId: String, statsFile: File) {
        try {
            val ref = storage.reference.child("backups/$userId/stats.json")
            ref.putFile(statsFile.absolutePath.toUri()).await()
            Timber.d("Backup completed")
        } catch (e: Exception) {
            Timber.e(e, "Backup failed")
        }
    }
    
    suspend fun restoreStats(userId: String): File? {
        return try {
            val ref = storage.reference.child("backups/$userId/stats.json")
            val file = File.createTempFile("stats", ".json")
            ref.getFile(file).await()
            Timber.d("Restore completed")
            file
        } catch (e: Exception) {
            Timber.e(e, "Restore failed")
            null
        }
    }
    
    suspend fun syncSettingsToCloud(userId: String, settings: Map<String, Any>) {
        try {
            db.collection("users").document(userId)
                .collection("settings").document("preferences")
                .set(settings)
                .await()
            Timber.d("Settings synced")
        } catch (e: Exception) {
            Timber.e(e, "Settings sync failed")
        }
    }
    
    suspend fun downloadSettingsFromCloud(userId: String): Map<String, Any>? {
        return try {
            val doc = db.collection("users").document(userId)
                .collection("settings").document("preferences")
                .get()
                .await()
            doc.data
        } catch (e: Exception) {
            Timber.e(e, "Settings download failed")
            null
        }
    }
}
