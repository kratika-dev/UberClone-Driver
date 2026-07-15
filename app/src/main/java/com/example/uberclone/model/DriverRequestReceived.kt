package com.example.uberclone.model

class DriverRequestReceived(
    var key: String = "",              // Rider ID
    var pickupLocation: String = "",
    var requestId: String = ""         // Firebase RideRequest push key
)