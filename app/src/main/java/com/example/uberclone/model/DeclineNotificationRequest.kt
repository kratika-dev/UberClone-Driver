package com.example.uberclone.model

data class DeclineNotificationRequest(
    val riderToken: String,
    val driverKey: String,
    val requestId: String
)