package com.example.alarmcontrollablebysmartphone

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimePickerState

data class MyTime(val hour: Int, val minute: Int) {
    fun toMinutes(): Int {
        return 60 * hour + minute
    }
}