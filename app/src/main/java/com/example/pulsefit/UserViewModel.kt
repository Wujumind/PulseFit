package com.example.pulsefit

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UserViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    var username by mutableStateOf("User123")
    var profilePictureUrl by mutableStateOf<String?>(null)
    var email by mutableStateOf("")
    var height by mutableStateOf("175")
    var weight by mutableStateOf("70")
    var streak by mutableIntStateOf(0)
    var totalWorkouts by mutableIntStateOf(0)

    fun checkUsernameExists(username: String, onResult: (Boolean) -> Unit) {
        db.collection("users")
            .whereEqualTo("username", username)
            .get()
            .addOnSuccessListener { documents ->
                onResult(!documents.isEmpty)
            }
    }

    init {
        auth.addAuthStateListener { firebaseAuth ->
            val currentUser = firebaseAuth.currentUser
            if (currentUser != null) {
                email = currentUser.email ?: ""
                loadUserData(currentUser.uid)
            } else {
                // Reset state on sign out
                resetState()
            }
        }
    }

    private fun resetState() {
        username = "User123"
        profilePictureUrl = null
        email = ""
        height = "175"
        weight = "70"
        streak = 0
        totalWorkouts = 0
    }

    fun updateUserInfo(name: String, photoUrl: String?, userEmail: String) {
        username = name
        profilePictureUrl = photoUrl
        email = userEmail
        saveUserData()
    }

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

    fun signOut() {
        auth.signOut()
        username = "User123"
        profilePictureUrl = null
        email = ""
        height = "175"
        weight = "70"
    }
}
