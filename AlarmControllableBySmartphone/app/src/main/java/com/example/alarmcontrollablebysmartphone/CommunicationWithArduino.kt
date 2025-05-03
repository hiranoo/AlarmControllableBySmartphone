package com.example.alarmcontrollablebysmartphone

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimePickerState

data class MyTime(val hour: Int, val minute: Int) {
    fun toMinutes(): Int {
        return 60 * hour + minute
    }
}

fun encodeMessage(tag: String, body: String): String {
    return "$tag,$body;"
}

fun decodeMessage(msg: String): MutableMap<String, String> {
    val messageMap = mutableMapOf<String, String>()
    msg.split(';').forEach {
        if (it.length == 2) {
            val tag = (it.split(','))[0]
            val body = (it.split(','))[1]
            messageMap[tag] = body
        }
    }
    return messageMap
}