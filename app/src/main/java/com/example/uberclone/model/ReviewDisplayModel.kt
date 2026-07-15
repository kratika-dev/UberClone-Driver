package com.example.uberclone.model

data class ReviewDisplayModel(
    var riderName: String = "",
    var riderAvatar: String = "",
    var rating: Float = 0f,
    var review: String = "",
    var timestamp: Long = 0L
)