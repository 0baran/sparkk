package com.example.spark.data.repository

import com.example.spark.domain.model.Party
import com.example.spark.domain.model.Post
import com.example.spark.domain.model.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object SparkRepository {
    private val db = FirebaseFirestore.getInstance()

    suspend fun getFeedPosts(): Result<List<Post>> {
        return try {
            val snapshot = db.collection("posts")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()
            val posts = snapshot.toObjects(Post::class.java)
            Result.success(posts)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getActiveParties(): Result<List<Party>> {
        return try {
            val snapshot = db.collection("parties")
                .whereEqualTo("isLive", true)
                .get()
                .await()
            val parties = snapshot.toObjects(Party::class.java)
            Result.success(parties)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserProfile(userId: String): Result<User?> {
        return try {
            val doc = db.collection("users").document(userId).get().await()
            Result.success(doc.toObject(User::class.java))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getParty(partyId: String): Result<Party?> {
        return try {
            val doc = db.collection("parties").document(partyId).get().await()
            Result.success(doc.toObject(Party::class.java))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPartyParticipants(partyId: String): Result<List<User>> {
        return try {
            val snapshot = db.collection("parties").document(partyId).collection("participants").get().await()
            val participants = snapshot.toObjects(User::class.java)
            Result.success(participants)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getOnlineUsers(): Result<List<User>> {
        return try {
            val snapshot = db.collection("users")
                .whereEqualTo("isOnline", true)
                .get()
                .await()
            val users = snapshot.toObjects(User::class.java)
            Result.success(users)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun findRandomMatch(currentUserId: String): Result<User?> {
        return try {
            val snapshot = db.collection("users")
                .whereEqualTo("isOnline", true)
                .get()
                .await()
            val users = snapshot.toObjects(User::class.java).filter { it.id != currentUserId }
            if (users.isEmpty()) {
                Result.success(null)
            } else {
                Result.success(users.random())
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createUserProfile(userId: String, username: String): Result<Unit> {
        return try {
            val newUser = User(
                id = userId,
                username = username,
                displayName = username,
                bio = "Merhaba! Spark'ta yeniyim 🎉",
                avatarEmoji = "😎",
                isOnline = true
            )
            db.collection("users").document(userId).set(newUser).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createQrSession(userId: String): Result<Unit> {
        return try {
            val sessionData = mapOf(
                "hostId" to userId,
                "matchedWith" to null,
                "timestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp()
            )
            db.collection("qr_sessions").document(userId).set(sessionData).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun joinQrSession(hostId: String, joinerId: String): Result<Unit> {
        return try {
            db.collection("qr_sessions").document(hostId).update("matchedWith", joinerId).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun listenToQrSession(hostId: String, onMatch: (String) -> Unit): com.google.firebase.firestore.ListenerRegistration {
        return db.collection("qr_sessions").document(hostId).addSnapshotListener { snapshot, _ ->
            if (snapshot != null && snapshot.exists()) {
                val matchedWith = snapshot.getString("matchedWith")
                if (matchedWith != null) {
                    onMatch(matchedWith)
                }
            }
        }
    }
}
