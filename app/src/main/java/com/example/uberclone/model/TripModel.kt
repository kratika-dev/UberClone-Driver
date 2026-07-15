package com.example.uberclone.model

import java.io.Serializable
data class TripModel(
    var tripId: String = "",
    var riderId: String = "",
    var driverId: String = "",

    var origin: String = "",
    var destination: String = "",

    var originLat: Double = 0.0,
    var originLng: Double = 0.0,

    var destinationLat: Double = 0.0,
    var destinationLng: Double = 0.0,

    var distanceInKm: Double = 0.0,
    var durationInMinutes: Int = 0,

    var fare: Double = 0.0,

    var timestamp: Long = 0,

    var status: String = "COMPLETED"
): Serializable