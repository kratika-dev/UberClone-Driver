package com.example.uberclone.model

import android.media.Rating

data class DriverInfoModel(
    var firstName: String,
    var lastName: String,
    var phoneNumber: String,
    var avatar: String,

    var rating: Float = 5f,
    var totalRating: Float = 0f,
    var ratingCount: Int = 0,

    var isOnline:Boolean=false,
    var isBusy: Boolean = false
) {
    constructor() : this("", "", "", "", 0f, 0f, 0)
}