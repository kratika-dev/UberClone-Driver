package com.example.uberclone.model

data class TripCompletedNotificationRequest(
    val riderToken: String,
    val requestId: String
)