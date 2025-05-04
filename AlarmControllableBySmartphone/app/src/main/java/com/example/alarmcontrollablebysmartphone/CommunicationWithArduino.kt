package com.example.alarmcontrollablebysmartphone

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TimePickerState
import java.util.Calendar
import kotlin.math.floor

const val MESSAGE_ALARM_SECONDS = "Alarm"
const val MESSAGE_CURRENT_SECONDS = "Current"
const val MESSAGE_PUSH_ANGLE = "PushAngle"
const val MESSAGE_STATUS = "Status"

data class MyTime(val hour: Int, val minute: Int) {
    fun toSeconds(): Int {
        return 3600 * hour + 60 * minute
    }
}

data class ArduinoTime(val hour: Int, val minute: Int, val second: Int) {
    constructor(): this(0, -1, 0)
    fun getTotalSeconds(): Int {
        return 3600 * hour + 60 + minute + second
    }
    fun display(): String {
        val a = if(hour < 12) {"午前"} else {"午後"}
        val hh = hour % 12
        val mm = minute
        return "$a ${hh}:${mm}"
    }
}

data class ArduinoStatus(val alarmTime: ArduinoTime?, val currentTime: ArduinoTime?, val pushAngle: Int = -1)

fun encodeMessage(tag: String, body: String): String {
    return "$tag,$body;"
}

fun encodeMessage(map: Map<String, String>): String {
    var msg = ""
    for (key in map.keys) {
        msg += encodeMessage(key, map[key]!!)
    }
    return msg
}

fun decodeMessage(msg: String): MutableMap<String, String> {
    val messageMap = mutableMapOf<String, String>()
    msg.replace(" ", "").split(';').forEach {
        val texts = it.split(',')
        if (texts.size == 2) {
            val tag = texts[0]
            val body = texts[1]
            messageMap[tag] = body
        }
    }
    return messageMap
}

fun getArduinoStatus(map: MutableMap<String, String>): ArduinoStatus {
    var alarmTime: ArduinoTime? = null
    var currentTime: ArduinoTime? = null
    var pushAngle: Int = -1
    map.keys.forEach {
        when(it) {
            MESSAGE_ALARM_SECONDS -> {
                val totalSeconds = map[it]!!.toLong()
                val h = (totalSeconds / 3600).toInt()
                val m = ((totalSeconds / 60) % 60).toInt()
                val s = (totalSeconds % 60).toInt()
                alarmTime = ArduinoTime(h, m, s)
            }
            MESSAGE_CURRENT_SECONDS -> {
                val totalSeconds = map[it]!!.toLong()
                val h = (totalSeconds / 3600).toInt()
                val m = ((totalSeconds / 60) % 60).toInt()
                val s = (totalSeconds % 60).toInt()
                currentTime = ArduinoTime(h, m, s)
            }
            MESSAGE_PUSH_ANGLE -> {
                pushAngle = map[it]!!.toInt()
            }
        }
    }
    return ArduinoStatus(alarmTime, currentTime, pushAngle)
}