package com.example.pulsefit

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue

/**
 * Data model representing a simplified user profile for social features.
 */
data class UserProfile(
    val uid: String = "",
    val username: String = "",
    val profilePictureUrl: String? = null,
    val streak: Int = 0,
    val totalWorkouts: Int = 0
)

/**
 * ViewModel responsible for managing social interactions, including searching users,
 * managing friends, and fetching leaderboard data.
 */
class SocialViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // --- Observable Social State ---
    var searchResults = mutableStateListOf<UserProfile>()
    var friendsList = mutableStateListOf<UserProfile>()
    var leaderboard = mutableStateListOf<UserProfile>()
    var friendSchedule = mutableStateMapOf<String, String>()
    var selectedFriendName by mutableStateOf<String?>(null)
    
    var isSearching by mutableStateOf(false)

    init {
        // Listen for authentication changes to load/clear social data
        auth.addAuthStateListener { firebaseAuth ->
            if (firebaseAuth.currentUser != null) {
                loadFriends()
                loadLeaderboard()
            } else {
                friendsList.clear()
                searchResults.clear()
                friendSchedule.clear()
                selectedFriendName = null
            }
        }
    }

    /**
     * Fetches the workout schedule of a specific friend.
     */
    fun loadFriendSchedule(friendUid: String, friendName: String) {
        db.collection("workouts").document(friendUid).get().addOnSuccessListener { doc ->
            @Suppress("UNCHECKED_CAST")
            val schedule = doc.get("schedule") as? Map<String, String> ?: emptyMap()
            friendSchedule.clear()
            friendSchedule.putAll(schedule)
            selectedFriendName = friendName
        }
    }

    /**
     * Searches for users in Firestore whose usernames match the given query.
     */
    fun searchUsers(query: String) {
        if (query.isBlank()) {
            searchResults.clear()
            isSearching = false
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
                    // Exclude the current user from search results
                    if (user.uid != auth.currentUser?.uid) {
                        searchResults.add(user)
                    }
                }
                isSearching = false
            }
            .addOnFailureListener {
                isSearching = false
            }
    }

    /**
     * Adds a user to the current user's friend list in Firestore.
     */
    fun addFriend(friendUid: String) {
        val currentUid = auth.currentUser?.uid ?: return
        db.collection("users").document(currentUid)
            .update("friends", FieldValue.arrayUnion(friendUid))
            .addOnSuccessListener {
                loadFriends() // Refresh the local friends list
            }
    }

    /**
     * Fetches the full profiles of all users in the current user's friend list.
     */
    private fun loadFriends() {
        val currentUid = auth.currentUser?.uid ?: return
        db.collection("users").document(currentUid).get().addOnSuccessListener { doc ->
            @Suppress("UNCHECKED_CAST")
            val friendIds = doc.get("friends") as? List<String> ?: emptyList()
            if (friendIds.isEmpty()) {
                friendsList.clear()
                return@addOnSuccessListener
            }
            
            // Query user details for all friend UIDs
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

    /**
     * Fetches top users ranked by their current workout streak for the leaderboard.
     */
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
