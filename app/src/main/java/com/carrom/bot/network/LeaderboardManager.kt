// app/src/main/java/com/carrom/bot/network/LeaderboardManager.kt

package com.carrom.bot.network

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import timber.log.Timber

data class PlayerProfile(
    val userId: String = "",
    val username: String = "",
    val totalScore: Int = 0,
    val accuracy: Float = 0f,
    val gamesPlayed: Int = 0,
    val rank: Int = 0,
    val trophy: Int = 0,
    val lastUpdated: Long = 0
)

data class Challenge(
    val id: String = "",
    val challenger: String = "",
    val opponent: String = "",
    val status: String = "pending",
    val challengerScore: Int = 0,
    val opponentScore: Int = 0,
    val createdAt: Long = 0
)

class LeaderboardManager {
    
    private val db = FirebaseFirestore.getInstance()
    
    suspend fun uploadPlayerStats(userId: String, stats: Map<String, Any>) {
        try {
            db.collection("players").document(userId)
                .update(stats)
                .await()
            Timber.d("Player stats uploaded")
        } catch (e: Exception) {
            Timber.e(e, "Upload failed")
        }
    }
    
    suspend fun getGlobalLeaderboard(limit: Int = 100): List<PlayerProfile> {
        return try {
            db.collection("players")
                .orderBy("totalScore", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()
                .toObjects(PlayerProfile::class.java)
        } catch (e: Exception) {
            Timber.e(e, "Leaderboard fetch failed")
            emptyList()
        }
    }
    
    suspend fun getFriendsLeaderboard(friendIds: List<String>): List<PlayerProfile> {
        return try {
            db.collection("players")
                .whereIn("userId", friendIds)
                .orderBy("totalScore", Query.Direction.DESCENDING)
                .get()
                .await()
                .toObjects(PlayerProfile::class.java)
        } catch (e: Exception) {
            Timber.e(e, "Friends leaderboard fetch failed")
            emptyList()
        }
    }
    
    suspend fun sendChallenge(challenger: String, opponent: String): Boolean {
        return try {
            val challenge = Challenge(
                challenger = challenger,
                opponent = opponent,
                status = "pending",
                createdAt = System.currentTimeMillis()
            )
            db.collection("challenges").add(challenge).await()
            Timber.d("Challenge sent")
            true
        } catch (e: Exception) {
            Timber.e(e, "Challenge send failed")
            false
        }
    }
    
    suspend fun acceptChallenge(challengeId: String, score: Int): Boolean {
        return try {
            db.collection("challenges").document(challengeId)
                .update("status" to "accepted", "opponentScore" to score)
                .await()
            Timber.d("Challenge accepted")
            true
        } catch (e: Exception) {
            Timber.e(e, "Challenge acceptance failed")
            false
        }
    }
}
