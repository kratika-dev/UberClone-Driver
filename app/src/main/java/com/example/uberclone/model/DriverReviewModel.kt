package com.example.uberclone.model

data class DriverReviewModel(

    var driverId: String = "",
    var riderId: String = "",
    var tripId: String = "",

    var rating: Float = 0f,
    var review: String = "",

    var timestamp: Long = 0
)