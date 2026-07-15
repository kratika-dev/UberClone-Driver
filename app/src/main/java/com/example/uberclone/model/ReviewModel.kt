package com.example.uberclone.model

data class ReviewModel(

    var tripId: String = "",

    var riderId: String = "",

    var driverId: String = "",

    var rating: Float = 0f,

    var review: String = "",

    var timestamp: Long = 0

)