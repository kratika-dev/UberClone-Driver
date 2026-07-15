package com.example.uberclone.model

data class EarningModel(
    var origin: String = "",
    var destination: String = "",
    var fare: Double = 0.0,
    var timestamp: Long = 0
)