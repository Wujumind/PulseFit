package com.example.pulsefit

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * ViewModel responsible for managing user-specific data, including profile details and authentication state.
 * Data is synchronized with Firebase Firestore for persistence across devices.
 */
class UserViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // --- Observable User State ---
    var username by mutableStateOf("User123")
    var profilePictureUrl by mutableStateOf<String?>(null)
    var email by mutableStateOf("")
    var height by mutableStateOf("175")
    var weight by mutableStateOf("70")
    var streak by mutableIntStateOf(0)
    var totalWorkouts by mutableIntStateOf(0)

    /**
     * Checks if a specific username is already taken by another user in the database.
     */
    fun checkUsernameExists(username: String, onResult: (Boolean) -> Unit) {
        db.collection("users")
            .whereEqualTo("username", username)
            .get()
            .addOnSuccessListener { documents ->
                onResult(!documents.isEmpty)
            }
    }

    init {
        // Listen for changes in the Firebase authentication state
        auth.addAuthStateListener { firebaseAuth ->
            val currentUser = firebaseAuth.currentUser
            if (currentUser != null) {
                email = currentUser.email ?: ""
                loadUserData(currentUser.uid) // Load profile from Firestore on login
            } else {
                resetState() // Wipe local state on logout
            }
        }
    }

    /**
     * Resets the local user state to default values.
     */
    private fun resetState() {
        username = "User123"
        profilePictureUrl = null
        email = ""
        height = "175"
        weight = "70"
        streak = 0
        totalWorkouts = 0
    }

    /**
     * Updates the local user information and triggers a cloud sync.
     */
    fun updateUserInfo(name: String, photoUrl: String?, userEmail: String) {
        username = name
        profilePictureUrl = photoUrl
        email = userEmail
        saveUserData()
    }

    /**
     * Saves the current user profile data to the Firestore 'users' collection.
     */
    fun saveUserData() {
        val userId = auth.currentUser?.uid ?: return
        val userData = mapOf(
            "username" to username,
            "profilePictureUrl" to profilePictureUrl,
            "email" to email,
            "height" to height,
            "weight" to weight,
            "streak" to streak,
            "totalWorkouts" to totalWorkouts,
        )
        db.collection("users").document(userId).set(userData)
    }

    /**
     * Fetches user profile data from Firestore for the given userId.
     */
    private fun loadUserData(userId: String) {
        db.collection("users").document(userId).get().addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                username = document.getString("username") ?: "User123"
                profilePictureUrl = document.getString("profilePictureUrl")
                height = document.getString("height") ?: "175"
                weight = document.getString("weight") ?: "70"
                streak = document.getLong("streak")?.toInt() ?: 0
                totalWorkouts = document.getLong("totalWorkouts")?.toInt() ?: 0
            }
        }
    }

    /**
     * Signs the user out of Firebase and clears the local session.
     */
    fun signOut() {
        auth.signOut()
        resetState()
    }
}
