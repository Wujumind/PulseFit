package com.example.pulsefit

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

import java.time.LocalDate

class WorkoutViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

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
            "Mon" to false,
            "Tue" to false,
            "Wed" to true,
            "Thu" to false,
            "Fri" to false,
            "Sat" to false,
            "Sun" to true
        )
    )

    fun getDayIndex(day: String): Int = days.indexOf(day) + 1

    fun getCurrentDayOfWeek(): Int {
        return LocalDate.now().dayOfWeek.value // 1 (Mon) to 7 (Sun)
    }

    fun isToday(day: String): Boolean {
        return getDayIndex(day) == getCurrentDayOfWeek()
    }

    fun isPastDay(day: String): Boolean {
        return getDayIndex(day) < getCurrentDayOfWeek()
    }

    fun startTodayWorkout() {
        val currentDayStr = days.getOrNull(getCurrentDayOfWeek() - 1) ?: return
        toggleCompletion(currentDayStr, completed = true)
    }

    init {
        auth.addAuthStateListener { firebaseAuth ->
            val currentUser = firebaseAuth.currentUser
            if (currentUser != null) {
                loadWorkoutData(currentUser.uid)
            } else {
                clearData()
            }
        }
    }

    fun updateSchedule(day: String, workout: String) {
        schedule += (day to workout)
        if (workout.contains("Rest", ignoreCase = true)) {
            toggleCompletion(day, completed = true)
        }
        saveWorkoutData()
    }

    fun toggleCompletion(day: String, completed: Boolean) {
        completionStatus = completionStatus + (day to completed)
        saveWorkoutData()
        updateGlobalStreak()
    }

    private fun updateGlobalStreak() {
        val userId = auth.currentUser?.uid ?: return
        val currentStreak = completionStatus.values.count { it }
        db.collection("users").document(userId).update("streak", currentStreak)
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
                val remoteSchedule = document.get("schedule") as? Map<String, String>
                if (remoteSchedule != null) schedule = remoteSchedule
                
                @Suppress("UNCHECKED_CAST")
                val remoteCompletion = document.get("completionStatus") as? Map<String, Boolean>
                if (remoteCompletion != null) completionStatus = remoteCompletion
            }
        }
    }

    fun clearData() {
        schedule = mapOf(
            "Mon" to "Chest & Triceps",
            "Tue" to "Back & Biceps",
            "Wed" to "Rest Day",
            "Thu" to "Legs",
            "Fri" to "Shoulders",
            "Sat" to "Full Body / Cardio",
            "Sun" to "Rest Day"
        )
        completionStatus = mapOf(
            "Mon" to false,
            "Tue" to false,
            "Wed" to true,
            "Thu" to false,
            "Fri" to false,
            "Sat" to false,
            "Sun" to true
        )
    }
}
