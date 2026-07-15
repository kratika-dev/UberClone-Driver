package com.example.uberclone.model

data class CancellationNotificationRequest(
    val requestId: String,
    val cancelledBy: String
)
