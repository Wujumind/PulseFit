package com.example.pulsefit

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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

    init {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            email = currentUser.email ?: ""
            loadUserData(currentUser.uid)
        }
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
            "weight" to weight
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
