package com.example.mqttclient

data class Device(
    val temperature: String?,
    val humidity: String?,
    val light: String?,
    val sound: String?
)