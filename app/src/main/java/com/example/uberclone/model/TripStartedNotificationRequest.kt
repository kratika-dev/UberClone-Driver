package com.example.uberclone.model

data class TripStartedNotificationRequest(
    val riderToken: String,
    val requestId: String
)