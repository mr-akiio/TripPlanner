package com.agreditar.tripplanner

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.auth.User
import com.google.firebase.firestore.ktx.toObject

class FriendRepository(private val db: FirebaseFirestore, private val auth: FirebaseAuth) {
    fun sendFriendRequest(
        userId: String,
        userToAdd: String,
        onComplete: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection("users").document(userId).collection("friends").document(userToAdd)
            .set(hashMapOf("status" to "pending"))
            .addOnSuccessListener {
                db.collection("users").document(userToAdd).collection("friends").document(userId)
                    .set(hashMapOf("status" to "pending"))
                    .addOnSuccessListener {
                        Log.d("FriendRepository", "Friend Request Sent")
                        onComplete()
                    }.addOnFailureListener { e ->
                        Log.w("FriendRepository", "Error sending friend request", e)
                        onFailure(e)
                    }
            }
            .addOnFailureListener { e ->
                Log.w("FriendRepository", "Error sending friend request", e)
                onFailure(e)
            }
    }

    fun acceptFriendRequest(
        userId: String,
        userToAdd: String,
        onComplete: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection("users").document(userId).collection("friends").document(userToAdd)
            .update("status", "accepted")
            .addOnSuccessListener {
                db.collection("users").document(userToAdd).collection("friends").document(userId)
                    .update("status", "accepted")
                    .addOnSuccessListener {
                        Log.d("FriendRepository", "Friend Request Accepted")
                        onComplete()
                    }.addOnFailureListener { e ->
                        Log.w("FriendRepository", "Error accepting friend request", e)
                        onFailure(e)
                    }
            }
            .addOnFailureListener { e ->
                Log.w("FriendRepository", "Error accepting friend request", e)
                onFailure(e)
            }
    }

    fun rejectFriendRequest(
        userId: String,
        userToAdd: String,
        onComplete: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection("users").document(userId).collection("friends").document(userToAdd).delete()
            .addOnSuccessListener {
                db.collection("users").document(userToAdd).collection("friends").document(userId).delete()
                    .addOnSuccessListener {
                        Log.d("FriendRepository", "Friend Request Rejected")
                        onComplete()
                    }.addOnFailureListener { e ->
                        Log.w("FriendRepository", "Error rejecting friend request", e)
                        onFailure(e)
                    }
            }
            .addOnFailureListener { e ->
                Log.w("FriendRepository", "Error rejecting friend request", e)
                onFailure(e)
            }
    }

    fun loadFriendList(
        userId: String,
        friendList: MutableList<User>,
        onComplete: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection("users").document(userId).collection("friends")
            .whereEqualTo("status", "accepted")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val friendId = document.id
                    db.collection("users").document(friendId).get()
                        .addOnSuccessListener { userDocument ->
                            val user = userDocument.toObject<User>()
                            if (user != null) {
                                friendList.add(user)
                            }
                            if (friendList.size == documents.size()) {
                                onComplete()
                            }
                        }
                        .addOnFailureListener { exception ->
                            Log.w("FriendRepository", "Error getting friends: ", exception)
                            onFailure(exception)
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.w("FriendRepository", "Error getting friends", e)
                onFailure(e)
            }
    }

    fun deleteFriend(
        userId: String,
        friendId: String,
        onComplete: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection("users").document(userId).collection("friends").document(friendId).delete()
            .addOnSuccessListener {
                db.collection("users").document(friendId).collection("friends").document(userId).delete()
                    .addOnSuccessListener {
                        Log.d("FriendRepository", "Friend deleted")
                        onComplete()
                    }.addOnFailureListener { e ->
                        Log.w("FriendRepository", "Error deleting friend", e)
                        onFailure(e)
                    }
            }
            .addOnFailureListener { e ->
                Log.w("FriendRepository", "Error deleting friend", e)
                onFailure(e)
            }
    }
}