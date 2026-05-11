package com.example.pulsefit

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class WorkoutViewModel : ViewModel() {
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

    // Tracks if a workout was completed for each day (true = completed, false = missed/pending)
    var completionStatus by mutableStateOf(
        mapOf(
            "Mon" to true,
            "Tue" to false,
            "Wed" to true, // Rest day is automatically "completed"
            "Thu" to false,
            "Fri" to false,
            "Sat" to false,
            "Sun" to true  // Rest day
        )
    )

    fun updateSchedule(day: String, workout: String) {
        schedule = schedule + (day to workout)
        // If it's a rest day, mark it as completed automatically
        if (workout.contains("Rest", ignoreCase = true)) {
            toggleCompletion(day, true)
        }
    }

    fun toggleCompletion(day: String, completed: Boolean) {
        completionStatus = completionStatus + (day to completed)
    }
}
