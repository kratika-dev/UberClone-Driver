package com.example.uberclone.model

data class AcceptNotificationRequest(
    val riderToken: String,
    val driverKey: String,
    val requestId: String
)
