package com.example.pulsefit

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

import java.time.LocalDate

/**
 * ViewModel responsible for managing the user's weekly workout schedule and completion status.
 * Handles calculation of streaks and historical data synchronization via Firestore.
 */
class WorkoutViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // Fixed list of days for consistent ordering across the UI
    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    // --- Observable Workout State ---
    var schedule by mutableStateOf(
        mapOf(
            "Mon" to "Chest & Triceps",
            "Tue" to "Back & Biceps",
            "Wed" to "Rest Day",
            "Thu" to "Legs",
            "Fri" to "Shoulders",
            "Sat" to "Full Body / Cardio",
            "Sun" to "Rest Day",
        )
    )

    var completionStatus by mutableStateOf(
        mapOf(
            "Mon" to false,
            "Tue" to false,
            "Wed" to false,
            "Thu" to false,
            "Fri" to false,
            "Sat" to false,
            "Sun" to false,
        )
    )

    /**
     * Converts a day string (e.g., "Mon") to its ISO index (1-7).
     */
    fun getDayIndex(day: String): Int = days.indexOf(day) + 1

    /**
     * Returns the current day of the week as an integer (1=Mon, 7=Sun).
     */
    fun getCurrentDayOfWeek(): Int {
        return LocalDate.now().dayOfWeek.value // 1 (Mon) to 7 (Sun)
    }

    /**
     * Checks if the given day string represents today.
     */
    fun isToday(day: String): Boolean {
        return getDayIndex(day) == getCurrentDayOfWeek()
    }

    /**
     * Checks if the given day represents a day that has already passed in the current week.
     */
    fun isPastDay(day: String): Boolean {
        return getDayIndex(day) < getCurrentDayOfWeek()
    }

    /**
     * Marks the workout for today as completed and triggers a cloud sync.
     */
    fun startTodayWorkout() {
        val currentDayStr = days.getOrNull(getCurrentDayOfWeek() - 1) ?: return
        toggleCompletion(currentDayStr, completed = true)
    }

    init {
        // Auth listener to load/clear data based on user session
        auth.addAuthStateListener { firebaseAuth ->
            val currentUser = firebaseAuth.currentUser
            if (currentUser != null) {
                loadWorkoutData(currentUser.uid)
            } else {
                clearData()
            }
        }
    }

    /**
     * Updates the plan for a specific day and saves to Firestore.
     */
    fun updateSchedule(day: String, workout: String) {
        schedule += (day to workout)
        saveWorkoutData()
    }

    /**
     * Toggles the completion checkbox for a specific day.
     */
    fun toggleCompletion(day: String, completed: Boolean) {
        completionStatus += (day to completed)
        saveWorkoutData()
        updateGlobalStreak() // Recalculate leaderboard streak
    }

    /**
     * Updates the 'streak' count in the main 'users' collection based on current completions.
     */
    private fun updateGlobalStreak() {
        val userId = auth.currentUser?.uid ?: return
        val currentStreak = completionStatus.values.count { it }
        db.collection("users").document(userId).update("streak", currentStreak)
    }

    /**
     * Serializes and saves workout data to the Firestore 'workouts' collection.
     */
    private fun saveWorkoutData() {
        val userId = auth.currentUser?.uid ?: return
        val data = mapOf(
            "schedule" to schedule,
            "completionStatus" to completionStatus,
        )
        db.collection("workouts").document(userId).set(data)
    }

    /**
     * Loads workout data from Firestore for the specific user.
     */
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

    /**
     * Resets local workout state to defaults (used on logout).
     */
    fun clearData() {
        schedule = mapOf(
            "Mon" to "Chest & Triceps",
            "Tue" to "Back & Biceps",
            "Wed" to "Rest Day",
            "Thu" to "Legs",
            "Fri" to "Shoulders",
            "Sat" to "Full Body / Cardio",
            "Sun" to "Rest Day",
        )
        completionStatus = mapOf(
            "Mon" to false,
            "Tue" to false,
            "Wed" to false,
            "Thu" to false,
            "Fri" to false,
            "Sat" to false,
            "Sun" to false,
        )
    }
}
