package com.example.pulsefit

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class UserViewModel : ViewModel() {
    var username by mutableStateOf("User123")
    var profilePictureUrl by mutableStateOf<String?>(null)
    var email by mutableStateOf("")

    fun updateUserInfo(name: String, photoUrl: String?, userEmail: String) {
        username = name
        profilePictureUrl = photoUrl
        email = userEmail
    }
}
