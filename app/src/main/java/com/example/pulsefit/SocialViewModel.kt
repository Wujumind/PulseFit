package com.example.pulsefit

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue

data class UserProfile(
    val uid: String = "",
    val username: String = "",
    val profilePictureUrl: String? = null,
    val streak: Int = 0,
    val totalWorkouts: Int = 0
)

class SocialViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    var searchResults = mutableStateListOf<UserProfile>()
    var friendsList = mutableStateListOf<UserProfile>()
    var leaderboard = mutableStateListOf<UserProfile>()
    
    var isSearching by mutableStateOf(false)

    init {
        auth.addAuthStateListener { firebaseAuth ->
            if (firebaseAuth.currentUser != null) {
                loadFriends()
                loadLeaderboard()
            } else {
                friendsList.clear()
                searchResults.clear()
            }
        }
    }

    fun searchUsers(query: String) {
        if (query.isBlank()) {
            searchResults.clear()
            return
        }
        isSearching = true
        db.collection("users")
            .whereGreaterThanOrEqualTo("username", query)
            .whereLessThanOrEqualTo("username", query + "\uf8ff")
            .limit(10)
            .get()
            .addOnSuccessListener { documents ->
                searchResults.clear()
                for (doc in documents) {
                    val user = doc.toObject(UserProfile::class.java).copy(uid = doc.id)
                    if (user.uid != auth.currentUser?.uid) {
                        searchResults.add(user)
                    }
                }
                isSearching = false
            }
    }

    fun addFriend(friendUid: String) {
        val currentUid = auth.currentUser?.uid ?: return
        db.collection("users").document(currentUid)
            .update("friends", FieldValue.arrayUnion(friendUid))
            .addOnSuccessListener {
                loadFriends()
            }
    }

    private fun loadFriends() {
        val currentUid = auth.currentUser?.uid ?: return
        db.collection("users").document(currentUid).get().addOnSuccessListener { doc ->
            val friendIds = doc.get("friends") as? List<String> ?: emptyList()
            if (friendIds.isEmpty()) {
                friendsList.clear()
                return@addOnSuccessListener
            }
            
            db.collection("users")
                .whereIn(com.google.firebase.firestore.FieldPath.documentId(), friendIds)
                .get()
                .addOnSuccessListener { documents ->
                    friendsList.clear()
                    for (d in documents) {
                        friendsList.add(d.toObject(UserProfile::class.java).copy(uid = d.id))
                    }
                }
        }
    }

    fun loadLeaderboard() {
        db.collection("users")
            .orderBy("streak", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(20)
            .get()
            .addOnSuccessListener { documents ->
                leaderboard.clear()
                for (doc in documents) {
                    leaderboard.add(doc.toObject(UserProfile::class.java).copy(uid = doc.id))
                }
            }
    }
}
