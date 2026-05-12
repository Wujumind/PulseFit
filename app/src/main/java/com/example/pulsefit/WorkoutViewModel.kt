package com.example.pulsefit

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class WorkoutViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    var schedule by mutableStateOf(
        mapOf(
            "Mon" to "Chest & Triceps",
            "Tue" to "Back & Biceps",
            "Wed" to "Rest Day",
            "Thu" to "Legs",
            "Fri" to "Shoulders",
            "Sat" to "Full Body / Cardio",
            "Sun" to "Rest Day"
        )
    )

    var completionStatus by mutableStateOf(
        mapOf(
            "Mon" to true,
            "Tue" to false,
            "Wed" to true,
            "Thu" to false,
            "Fri" to false,
            "Sat" to false,
            "Sun" to true
        )
    )

    init {
        auth.currentUser?.let { loadWorkoutData(it.uid) }
    }

    fun updateSchedule(day: String, workout: String) {
        schedule = schedule + (day to workout)
        if (workout.contains("Rest", ignoreCase = true)) {
            toggleCompletion(day, true)
        }
        saveWorkoutData()
    }

    fun toggleCompletion(day: String, completed: Boolean) {
        completionStatus = completionStatus + (day to completed)
        saveWorkoutData()
    }

    private fun saveWorkoutData() {
        val userId = auth.currentUser?.uid ?: return
        val data = mapOf(
            "schedule" to schedule,
            "completionStatus" to completionStatus
        )
        db.collection("workouts").document(userId).set(data)
    }

    private fun loadWorkoutData(userId: String) {
        db.collection("workouts").document(userId).get().addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                @Suppress("UNCHECKED_CAST")
                schedule = (document.get("schedule") as? Map<String, String>) ?: schedule
                @Suppress("UNCHECKED_CAST")
                completionStatus = (document.get("completionStatus") as? Map<String, Boolean>) ?: completionStatus
            }
        }
    }
}
